package com.japik.element;

import com.japik.Japik;
import com.japik.dep.DependencyLord;
import com.japik.dep.ElementImplJarDependency;
import com.japik.dep.ResolveDependencyException;
import com.japik.dep.Tenant;
import com.japik.logger.ILogger;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public abstract class AElementLoader <T extends IElement> {
    @Getter
    protected final ElementType elementTypeLoader;

    protected final Japik server;
    protected final Path corePath;
    protected final DependencyLord dependencyLord;
    protected final ClassLoader baseClassLoader;
    protected final ILogger logger;

    protected final Map<String, T> nameElementMap = Collections.synchronizedMap(new HashMap<>());

    protected final ReentrantLock lock = new ReentrantLock();

    public AElementLoader(ElementType elementTypeLoader,
                          Japik server, Path corePath,
                          DependencyLord dependencyLord, ClassLoader baseClassLoader, ILogger logger) {

        this.elementTypeLoader = elementTypeLoader;

        this.server = server;
        this.corePath = corePath;
        this.dependencyLord = dependencyLord;
        this.baseClassLoader = baseClassLoader;
        this.logger = logger;
    }

    public final T load(String elSubtype, String elName)
            throws
            ElementAlreadyExistsException,
            IOException,
            ResolveDependencyException,
            IllegalElementFormatException {
        return this.load(elSubtype, elName, null);
    }

    public final T load(String elSubtype, String elName, @Nullable String elVersion)
        throws
            ElementAlreadyExistsException,
            IOException,
            ResolveDependencyException,
            IllegalElementFormatException {

        //region check server status
        if (server.getLiveCycle().getStatus().isNotInitialized()){
            throw new IllegalStateException();
        }
        //endregion

        lock.lock();
        try {
            existsThenThrow(elName);

            try {
                final ElementImplJarDependency.BuilderByType implDepBuilder = createDependencyBuilderByType(
                        elSubtype, elName, elVersion
                );
                implDepBuilder.setBaseClassLoader(baseClassLoader);
                implDepBuilder.init();

                if (implDepBuilder.getSharedSubtype() == null) {
                    logger.warn("Shared-Subtype not specified for element " + elementTypeLoader + "-Impl subtype='" + elSubtype + "'");
                }

                final ElementImplJarDependency implDependency = (ElementImplJarDependency) dependencyLord.buildImpl(implDepBuilder);
                implDependency.resolveAll();

                final T element = createElement(
                        implDependency,
                        elName,
                        Objects.requireNonNull(implDepBuilder.getTenant())
                );

                nameElementMap.put(elName, element);
                logger.info("New element loaded: " + element.toString());
                return element;

            } catch (IOException | ResolveDependencyException | IllegalElementFormatException e){
                throw e;

            } catch (IllegalClassFormatException |
                    ClassNotFoundException |
                    NoSuchMethodException |
                    InstantiationException |
                    IllegalAccessException |
                    InvocationTargetException illegalEx) {
                throw new IllegalElementFormatException(illegalEx);

            } catch (Throwable throwable){
                throw new ResolveDependencyException(throwable);
            }

        } finally {
            lock.unlock();
        }
    }

    protected abstract ElementImplJarDependency.BuilderByType createDependencyBuilderByType(
            String elSubtype,
            String elName,
            @Nullable String elVersion) throws Throwable;

    public final T load(URL elUrl, String elName)
            throws
            ElementAlreadyExistsException,
            IOException,
            ResolveDependencyException,
            IllegalElementFormatException {

        //region check server status
        if (server.getLiveCycle().getStatus().isNotInitialized()){
            throw new IllegalStateException();
        }
        //endregion

        lock.lock();
        try {
            existsThenThrow(elName);

            try {
                final ElementImplJarDependency.BuilderByUrl implDepBuilder = createDependencyBuilderByUrl(
                        elUrl, elName
                );
                implDepBuilder.init();

                if (implDepBuilder.getSharedSubtype() == null) {
                    logger.warn("Shared-Subtype not specified for element " + elementTypeLoader + "-Impl subtype='" + implDepBuilder.getElementSubtype() + "'");
                }

                final ElementImplJarDependency implDependency = (ElementImplJarDependency) dependencyLord.buildImpl(implDepBuilder);

                implDependency.resolveAll();

                final T element = createElement(
                        implDependency,
                        elName,
                        Objects.requireNonNull(implDepBuilder.getTenant()));

                nameElementMap.put(elName, element);
                logger.info("New element loaded: " + element.toString());
                return element;

            } catch (IOException | ResolveDependencyException | IllegalElementFormatException e){
                throw e;

            } catch (IllegalClassFormatException |
                    ClassNotFoundException |
                    NoSuchMethodException |
                    InstantiationException |
                    IllegalAccessException |
                    InvocationTargetException illegalEx) {
                throw new IllegalElementFormatException(illegalEx);

            } catch (Throwable throwable){
                throw new ResolveDependencyException(throwable);
            }

        } finally {
            lock.unlock();
        }

    }

    protected abstract ElementImplJarDependency.BuilderByUrl createDependencyBuilderByUrl(
            URL elUrl,
            String elName) throws Throwable;

    protected abstract T createElement(ElementImplJarDependency implDependency,
                                       String elName,
                                       Tenant elTenant) throws Throwable;

    public final void unload(String elName) throws ElementNotFoundException {
        lock.lock();
        try {

            @Nullable final IElement element = nameElementMap.get(elName);
            if (element == null) {
                throw new ElementNotFoundException(elementTypeLoader, elName);
            }

            logger.info("Unloading element: " + element.toString());

            if (element.getLiveCycle().getStatus().isStarted() || element.getLiveCycle().getStatus().isBroken()) {
                try {
                    element.getLiveCycle().stopForce();
                } catch (Throwable throwable) {
                    logger.exception(throwable);
                }
            }

            if (element.getLiveCycle().getStatus().isInitialized() || element.getLiveCycle().getStatus().isBroken()) {
                try {
                    element.getLiveCycle().destroy();
                } catch (Throwable throwable) {
                    logger.exception(throwable);
                }
            }

            nameElementMap.remove(elName);
            //nameClassLoaderMap.remove(elName);

            dependencyLord.release(element.getTenant());

            logger.info("Element name='" + elName + "' unloaded");

        } finally {
            lock.unlock();
        }
    }

    public final void unloadAll() {
        lock.lock();
        try{

            while (!nameElementMap.isEmpty()){
                try {
                    unload(nameElementMap.keySet().iterator().next());
                } catch (ElementNotFoundException ignored) {
                }
            }

        } finally {
            lock.unlock();
        }
    }

    @Nullable
    public final T get(String elName) {
        return nameElementMap.get(elName);
    }

    public final boolean exists(String elName) {
        return nameElementMap.containsKey(elName);
    }

    public final void existsThenThrow(String elName) throws ElementAlreadyExistsException {
        final T existingElement = nameElementMap.get(elName);
        if (existingElement != null) {
            throw new ElementAlreadyExistsException(existingElement);
        }
    }

    public final T getOrThrow(String elName) throws ElementNotFoundException {
        final T element = nameElementMap.get(elName);
        if (element == null) {
            throw new ElementNotFoundException(elementTypeLoader, elName);
        }
        return element;
    }

    public final boolean isLoaded(String elName) {
        return nameElementMap.containsKey(elName);
    }

    public final Set<String> getElementNames(){
        return Collections.unmodifiableSet(nameElementMap.keySet());
    }

    public final Collection<T> getElements(){
        return Collections.unmodifiableCollection(nameElementMap.values());
    }

    public final int getElementsCount(){
        return nameElementMap.size();
    }
}

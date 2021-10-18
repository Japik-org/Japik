package com.pro100kryto.server.utils;

import java.util.ArrayList;
import java.util.List;

public class ResolveDependenciesIncompleteException extends Throwable {
    private final Builder builder;

    private ResolveDependenciesIncompleteException(
            Builder builder) {
        this.builder = builder;
    }

    public List<Throwable> getWarningList() {
        return builder.warningList;
    }

    public List<Throwable> getErrorList() {
        return builder.errorList;
    }

    public boolean hasWarnings(){
        return builder.hasWarnings();
    }

    public boolean hasErrors(){
        return builder.hasErrors();
    }

    public boolean isEmpty(){
        return builder.isEmpty();
    }

    public List<ResolveDependenciesIncompleteException> getCauses() {
        return builder.causes;
    }

    public static final class Builder{
        private final List<ResolveDependenciesIncompleteException> causes = new ArrayList<>();
        private final ArrayList<Throwable> warningList = new ArrayList<>();
        private final ArrayList<Throwable> errorList = new ArrayList<>();

        public void addWarning(Throwable throwable){
            warningList.add(throwable);
        }

        public boolean hasWarnings(){
            if (!warningList.isEmpty()) return true;
            for (ResolveDependenciesIncompleteException cause : causes){
                if (cause.hasWarnings()) return true;
            }
            return false;
        }

        public void addError(Throwable throwable){
            errorList.add(throwable);
        }

        public void addCause(ResolveDependenciesIncompleteException resolveDependenciesIncompleteException){
            causes.add(resolveDependenciesIncompleteException);
        }

        public boolean hasErrors(){
            if (!errorList.isEmpty()) return true;
            for (ResolveDependenciesIncompleteException cause : causes){
                if (cause.hasErrors()) return true;
            }
            return false;
        }

        public boolean isEmpty(){
            return warningList.isEmpty() && errorList.isEmpty() && causes.isEmpty();
        }

        public ResolveDependenciesIncompleteException build(){
            return new ResolveDependenciesIncompleteException(this);
        }
    }
}

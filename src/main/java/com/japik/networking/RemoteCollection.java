package com.japik.networking;

import com.japik.Japik;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractCollection;
import java.util.HashMap;
import java.util.Iterator;

@RequiredArgsConstructor
public final class RemoteCollection extends AbstractCollection<Remote> {
    private final Japik server;
    private final HashMap<String, Remote> remoteMap = new HashMap<>();
    private final Object locker = new Object();

    @Override
    public boolean add(Remote remote) {
        synchronized (locker) {
            if (remoteMap.containsKey(remote.getName())) {
                throw new IllegalArgumentException("Remote '" + remote.getName() + "' already exists.");
            }
            remoteMap.put(remote.getName(), remote);
            return true;
        }
    }

    public void add(Remote.Builder remoteBuilder) {
        synchronized (locker) {
            if (remoteMap.containsKey(remoteBuilder.getRemoteName())) {
                throw new IllegalArgumentException("Remote '" + remoteBuilder.getRemoteName() + "' already exists.");
            }
            remoteMap.put(remoteBuilder.getRemoteName(), remoteBuilder.build(server));
        }
    }

    @Nullable
    public Remote removeByName(String remoteName) {
        synchronized (locker) {
            return remoteMap.remove(remoteName);
        }
    }

    public boolean containsByName(String remoteName) {
        synchronized (locker) {
            return remoteMap.containsKey(remoteName);
        }
    }

    @Nullable
    public Remote getByName(String remoteName){
        synchronized (locker) {
            return remoteMap.get(remoteName);
        }
    }

    @Override
    public @NotNull Iterator<Remote> iterator() {
        return new Iterator<Remote>() {
            private final Iterator<Remote> parentIterator = remoteMap.values().iterator();
            @Override
            public boolean hasNext() {
                synchronized (locker) {
                    return parentIterator.hasNext();
                }
            }

            @Override
            public Remote next() {
                synchronized (locker) {
                    return parentIterator.next();
                }
            }
        };
    }

    @Override
    public int size() {
        synchronized (locker) {
            return remoteMap.size();
        }
    }
}

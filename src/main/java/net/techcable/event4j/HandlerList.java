package net.techcable.event4j;

import lombok.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class HandlerList<E, L> {
    private final EventBus<E, L> eventBus;

    public void fire(E event) {
        for (RegisteredListener<E, L> listener : baked()) {
            listener.fire(event);
        }
    }

    public void register(RegisteredListener<E,L> listener) {
        listenerSet.add(listener);
        bakedListeners = null;
    }

    public void unregister(RegisteredListener listener) {
        listenerSet.remove(listener);
        bakedListeners = null;
    }

    private final SortedSet<RegisteredListener<E,L>> listenerSet = Collections.synchronizedSortedSet(new TreeSet<>());
    private final SortedSet<RegisteredListener> immutableSet = Collections.unmodifiableSortedSet((SortedSet<RegisteredListener>)(Object)listenerSet);
    private volatile RegisteredListener[] bakedListeners = null;

    @SuppressWarnings("unchecked")
    private RegisteredListener<E, L>[] baked() {
        RegisteredListener[] baked = this.bakedListeners;
        if (baked == null) baked = bakeListeners(); // Seperate method to assist inlining
        return baked;
    }

    private RegisteredListener[] bakeListeners() {
        RegisteredListener[] baked;
        synchronized (this) {
            if ((baked = this.bakedListeners) == null) { // In case someone else baked while we were locking
                this.bakedListeners = baked = listenerSet.toArray(new RegisteredListener[listenerSet.size()]);
            }
        }
        return baked;
    }

    public SortedSet<RegisteredListener> getListenerSet() {
        return immutableSet;
    }
}

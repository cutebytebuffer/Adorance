package cute.bytebuffer.adorance.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class EventBus {

    private static final Logger LOGGER = LoggerFactory.getLogger("Adorance/EventBus");
    private static final EventBus INSTANCE = new EventBus();
    private static final Comparator<Listener> BY_PRIORITY = Comparator.comparingInt(listener -> listener.priority().ordinal());

    private final Map<Class<? extends Event>, List<Listener>> listenersByEvent = new ConcurrentHashMap<>();
    private final Map<Object, List<Listener>> listenersByOwner = new ConcurrentHashMap<>();

    private EventBus() {
    }

    public static EventBus get() {
        return INSTANCE;
    }

    public void subscribe(Object owner) {
        if (listenersByOwner.containsKey(owner)) {
            return;
        }
        List<Listener> listeners = new ArrayList<>();
        for (Method method : handlerMethods(owner.getClass())) {
            listeners.add(Listener.bind(owner, method));
        }
        listenersByOwner.put(owner, List.copyOf(listeners));
        listeners.forEach(this::attach);
    }

    public void unsubscribe(Object owner) {
        List<Listener> listeners = listenersByOwner.remove(owner);
        if (listeners != null) {
            listeners.forEach(this::detach);
        }
    }

    public boolean isSubscribed(Object owner) {
        return listenersByOwner.containsKey(owner);
    }

    public <E extends Event> E post(E event) {
        List<Listener> listeners = listenersByEvent.get(event.getClass());
        if (listeners == null) {
            return event;
        }
        Cancellable cancellable = event instanceof Cancellable c ? c : null;
        for (Listener listener : listeners) {
            if (cancellable != null && cancellable.isCancelled() && !listener.receiveCancelled()) {
                continue;
            }
            dispatch(listener, event);
        }
        return event;
    }

    private void dispatch(Listener listener, Event event) {
        try {
            listener.invoke(event);
        } catch (Throwable throwable) {
            LOGGER.error("{} failed handling {}", listener.owner().getClass().getSimpleName(),
                    event.getClass().getSimpleName(), throwable);
        }
    }

    private void attach(Listener listener) {
        listenersByEvent.compute(listener.eventType(), (type, current) -> {
            List<Listener> next = current == null ? new ArrayList<>() : new ArrayList<>(current);
            next.add(listener);
            next.sort(BY_PRIORITY);
            return List.copyOf(next);
        });
    }

    private void detach(Listener listener) {
        listenersByEvent.computeIfPresent(listener.eventType(), (type, current) -> {
            List<Listener> next = new ArrayList<>(current);
            next.remove(listener);
            return next.isEmpty() ? null : List.copyOf(next);
        });
    }

    private static List<Method> handlerMethods(Class<?> type) {
        List<Method> methods = new ArrayList<>();
        for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass()) {
            for (Method method : current.getDeclaredMethods()) {
                if (method.isAnnotationPresent(EventHandler.class)) {
                    methods.add(method);
                }
            }
        }
        return methods;
    }
}

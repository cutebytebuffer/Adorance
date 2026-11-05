package cute.bytebuffer.adorance.event;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

final class Listener {

    private final Object owner;
    private final Class<? extends Event> eventType;
    private final EventPriority priority;
    private final boolean receiveCancelled;
    private final MethodHandle handle;

    private Listener(Object owner, Method method, MethodHandle handle) {
        EventHandler annotation = method.getAnnotation(EventHandler.class);
        this.owner = owner;
        this.eventType = method.getParameterTypes()[0].asSubclass(Event.class);
        this.priority = annotation.priority();
        this.receiveCancelled = annotation.receiveCancelled();
        this.handle = handle;
    }

    static Listener bind(Object owner, Method method) {
        validate(method);
        try {
            method.setAccessible(true);
            MethodHandle handle = MethodHandles.lookup().unreflect(method).bindTo(owner);
            return new Listener(owner, method, handle);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Unable to bind event handler " + method, exception);
        }
    }

    private static void validate(Method method) {
        if (Modifier.isStatic(method.getModifiers())) {
            throw new IllegalArgumentException("Event handler " + method + " must not be static");
        }
        if (method.getReturnType() != void.class) {
            throw new IllegalArgumentException("Event handler " + method + " must return void");
        }
        if (method.getParameterCount() != 1 || !Event.class.isAssignableFrom(method.getParameterTypes()[0])) {
            throw new IllegalArgumentException("Event handler " + method + " must accept exactly one Event");
        }
    }

    void invoke(Event event) throws Throwable {
        handle.invoke(event);
    }

    Object owner() {
        return owner;
    }

    Class<? extends Event> eventType() {
        return eventType;
    }

    EventPriority priority() {
        return priority;
    }

    boolean receiveCancelled() {
        return receiveCancelled;
    }
}

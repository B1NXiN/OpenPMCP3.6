package pmcp.event;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class EventManager {
    public static final EventManager instance = new EventManager();

    private final Map<Class<? extends Event>, ConcurrentLinkedQueue<EventHook>> registry = new ConcurrentHashMap<>();
    private final Map<Method, MethodHandle> methodHandleCache = new ConcurrentHashMap<>();


    public void register(EventInterface eventInterface) {
        MethodHandles.Lookup lookup = MethodHandles.lookup();

        for (Method method : eventInterface.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(EventTarget.class) && method.getParameterCount() == 1 && Event.class.isAssignableFrom(method.getParameterTypes()[0])) {
                Class<? extends Event> eventClass = method.getParameterTypes()[0].asSubclass(Event.class);
                EventTarget eventTarget = method.getAnnotation(EventTarget.class);

                registry.computeIfAbsent(eventClass, k -> new ConcurrentLinkedQueue<>())
                        .add(new EventHook(eventInterface, method, eventTarget));

                method.setAccessible(true);

                try {
                    MethodHandle handle = lookup.unreflect(method);
                    methodHandleCache.put(method, handle);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void unregister(EventInterface listenable) {
        registry.forEach((key, targets) -> targets.removeIf(eventHook -> eventHook.getEventInterface() == listenable));
    }


    public void call(Event event) {
        ConcurrentLinkedQueue<EventHook> targets = registry.get(event.getClass());
        if (targets == null || targets.isEmpty()) {
            return;
        }

        targets.forEach(eventHook -> {
            try {
                if (eventHook.getEventInterface().handleEvents() || eventHook.isIgnoreCondition()) {
                    MethodHandle handle = methodHandleCache.get(eventHook.getMethod());
                    if (handle != null) {
                        handle.invoke(eventHook.getEventInterface(), event);
                    } else {
                        eventHook.getMethod().invoke(eventHook.getEventInterface(), event);
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        });
    }
}
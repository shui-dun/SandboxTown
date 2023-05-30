package com.shuidun.sandbox_town_backend.observer;

import com.shuidun.sandbox_town_backend.bean.Event;
import com.shuidun.sandbox_town_backend.enumeration.EventEnum;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ObserverNotifier {
    private static Map<EventEnum, List<AbstractObserver>> map = new ConcurrentHashMap<>();

    public static void notify(Event event) {
        map.get(event.getType()).forEach(observer -> observer.update(event));
    }

    public static void register(EventEnum eventEnum, AbstractObserver observer) {
        map.get(eventEnum).add(observer);
    }

}

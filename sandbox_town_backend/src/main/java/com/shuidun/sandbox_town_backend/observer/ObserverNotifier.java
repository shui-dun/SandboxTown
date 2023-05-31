package com.shuidun.sandbox_town_backend.observer;

import com.shuidun.sandbox_town_backend.bean.EventMessage;
import com.shuidun.sandbox_town_backend.enumeration.EventEnum;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ObserverNotifier {
    private static Map<EventEnum, List<AbstractObserver>> map = new ConcurrentHashMap<>();

    public static void notify(EventMessage eventMessage) {
        // TO-DO：增加作弊校验

        map.get(eventMessage.getType()).forEach(observer -> observer.update(eventMessage));
    }

    public static void register(EventEnum eventEnum, AbstractObserver observer) {
        if (!map.containsKey(eventEnum)) {
            map.put(eventEnum, new ArrayList<>());
        }
        map.get(eventEnum).add(observer);
    }

}

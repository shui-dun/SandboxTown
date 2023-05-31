package com.shuidun.sandbox_town_backend.observer;

import com.shuidun.sandbox_town_backend.bean.EventMessage;
import com.shuidun.sandbox_town_backend.enumeration.EventEnum;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public abstract class AbstractObserver {
    // 事件类型 -> 处理函数
    protected Map<EventEnum, BiFunction<String, Map<String, Object>, Void>> mp = new HashMap<>();

    // 对于某些观察者，可能还需要记录一些状态

    public void update(EventMessage eventMessage) {
        mp.get(eventMessage.getType()).apply(eventMessage.getInitiator(), eventMessage.getData());
    }
}

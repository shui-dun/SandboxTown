package com.shuidun.sandbox_town_backend.observer;

import com.shuidun.sandbox_town_backend.bean.Event;
import com.shuidun.sandbox_town_backend.enumeration.EventEnum;

import java.util.Map;
import java.util.function.Function;

public abstract class AbstractObserver {
    // 事件类型 -> 处理函数
    protected Map<EventEnum, Function<Map<String, Object>, Void>> mp;

    // 对于某些观察者，可能还需要记录一些状态

    public void update(Event event) {
        mp.get(event.getType()).apply(event.getData());
    }
}

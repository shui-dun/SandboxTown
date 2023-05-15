package com.shuidun.sandbox_town_backend.bean;

import lombok.Data;

import java.util.Map;

@Data
public class EventBean {
    private EventTypeEnum type;

    private Map<String, Object> data;

    public enum EventTypeEnum {

        FOO("foo");

        private final String type;

        EventTypeEnum(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }


}

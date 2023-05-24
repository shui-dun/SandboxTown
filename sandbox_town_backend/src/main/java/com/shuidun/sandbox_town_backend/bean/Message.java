package com.shuidun.sandbox_town_backend.bean;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class Message {
    String userName;

    OperationTypeEnum operation;

    private Map<String, Object> data;

    public enum OperationTypeEnum {

        MOVE("move");

        private final String type;

        OperationTypeEnum(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }
}
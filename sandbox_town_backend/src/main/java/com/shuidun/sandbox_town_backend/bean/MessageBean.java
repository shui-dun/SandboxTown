package com.shuidun.sandbox_town_backend.bean;

import lombok.Data;
import org.springframework.context.annotation.Bean;

import java.util.Map;

@Data
public class MessageBean {
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

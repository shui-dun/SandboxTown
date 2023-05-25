package com.shuidun.sandbox_town_backend.bean;

import com.shuidun.sandbox_town_backend.enumeration.ResponseEventEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WSResponseEvent {
    private ResponseEventEnum type;

    private Map<String, Object> data;
}

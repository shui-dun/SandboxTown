package com.shuidun.sandbox_town_backend.bean;

import com.shuidun.sandbox_town_backend.enumeration.EventEnum;
import com.shuidun.sandbox_town_backend.enumeration.WSResponseEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class WSResponse {
    private WSResponseEnum type;

    private Map<String, Object> data;
}

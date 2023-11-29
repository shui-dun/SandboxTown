package com.shuidun.sandbox_town_backend.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfflineVo {
    @NonNull
    private List<String> ids;
}

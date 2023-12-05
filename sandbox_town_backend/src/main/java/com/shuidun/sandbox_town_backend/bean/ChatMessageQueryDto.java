package com.shuidun.sandbox_town_backend.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ChatMessageQueryDto {
    @NotNull
    private String userId;

    @NotNull
    private Integer messageId;

    @NotNull
    private Integer length;
}

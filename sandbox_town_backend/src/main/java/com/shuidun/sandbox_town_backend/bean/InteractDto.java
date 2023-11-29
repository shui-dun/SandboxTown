package com.shuidun.sandbox_town_backend.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

import javax.validation.constraints.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class InteractDto {
    /** 交互事件的发起者 */
    @NotNull
    @NonNull
    private String source;
    /** 交互事件的目标 */
    @NotNull
    @NonNull
    private String target;
    /** 交互事件的序列号（用于去重） */
    @NotNull
    @NonNull
    private Integer sn;
}

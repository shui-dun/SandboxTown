package com.shuidun.sandbox_town_backend.bean;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@TableName("user")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDo {

    @TableId
    @NonNull
    private String username;

    @NonNull
    private String password;

    @NonNull
    private String salt;

    @Nullable
    private java.util.Date banEndDate;

    @NonNull
    private Integer cheatCount;

    @NonNull
    private java.util.Date createDate;

    @Nullable
    private java.util.Date lastOnline;
}

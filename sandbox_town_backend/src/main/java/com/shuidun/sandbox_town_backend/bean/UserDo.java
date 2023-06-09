package com.shuidun.sandbox_town_backend.bean ;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@TableName("user")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDo {

    @TableId
    private String username;

    private String password;

    private String salt;

    private java.util.Date banEndDate;

    private Integer cheatCount;

    private java.util.Date createDate;

    private java.util.Date lastOnline;
}

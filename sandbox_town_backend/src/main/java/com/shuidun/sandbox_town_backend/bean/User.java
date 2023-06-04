package com.shuidun.sandbox_town_backend.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    private String username;

    private String password;

    private String salt;

    private java.util.Date banEndDate;

    private Integer cheatCount;
}

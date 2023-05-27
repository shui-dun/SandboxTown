package com.shuidun.sandbox_town_backend.service;

import com.shuidun.sandbox_town_backend.bean.User;

import java.util.Set;

public interface UserService {

    public User findUserByName(String username);

    public void signup(User user);

    public int deleteNotAdminUser(String username);

    Set<User> listAll();
}

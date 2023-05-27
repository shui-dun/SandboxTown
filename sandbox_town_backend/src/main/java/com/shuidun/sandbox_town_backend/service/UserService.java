package com.shuidun.sandbox_town_backend.service;

import com.shuidun.sandbox_town_backend.bean.User;
import com.shuidun.sandbox_town_backend.enumeration.StatusCodeEnum;

import java.util.Set;

public interface UserService {

    public User findUserByName(String username);

    public void insertUser(User user);

    public void updateUser(User user);

    // public StatusCodeEnum banUser(String username, int days);

    public int deleteNotAdminUser(String username);

    Set<User> listAll();
}

package com.shuidun.sandbox_town_backend.service.impl;

import com.shuidun.sandbox_town_backend.bean.User;
import com.shuidun.sandbox_town_backend.mapper.RoleMapper;
import com.shuidun.sandbox_town_backend.mapper.UserMapper;
import com.shuidun.sandbox_town_backend.service.RoleService;
import com.shuidun.sandbox_town_backend.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;

    private final RoleMapper roleMapper;

    public UserServiceImpl(UserMapper userMapper, RoleMapper roleMapper) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
    }

    @Override
    public User findUserByName(String username) {
        return userMapper.findUserByName(username);
    }

    @Override
    @Transactional
    public void signup(User user) {
        userMapper.insertUser(user);
        roleMapper.insertUserRole(user.getUsername(), "normal");
    }

    @Override
    @Transactional
    public int deleteNotAdminUser(String username) {
        Set<String> roleSet = roleMapper.getRolesByUserName(username);
        if (roleSet.contains("admin")) {
            throw new RuntimeException("无权删除该用户");
        }
        roleMapper.deleteByUsername(username);
        return userMapper.deleteUser(username);
    }

    @Override
    public Set<User> listAll() {
        return userMapper.listAll();
    }
}

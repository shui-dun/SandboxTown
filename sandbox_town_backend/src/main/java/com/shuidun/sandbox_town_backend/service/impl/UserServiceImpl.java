package com.shuidun.sandbox_town_backend.service.impl;

import com.shuidun.sandbox_town_backend.bean.User;
import com.shuidun.sandbox_town_backend.enumeration.StatusCodeEnum;
import com.shuidun.sandbox_town_backend.exception.BusinessException;
import com.shuidun.sandbox_town_backend.mapper.RoleMapper;
import com.shuidun.sandbox_town_backend.mapper.UserMapper;
import com.shuidun.sandbox_town_backend.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
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
    public void insertUser(User user) {
        userMapper.insertUser(user);
        roleMapper.insertUserRole(user.getUsername(), "normal");
    }

    @Override
    public void updateUser(User user) {
        userMapper.updateUser(user);
    }

    @Override
    @Transactional
    public void banUser(String username, int days) {
        Set<String> roleSet = roleMapper.getRolesByUserName(username);
        if (roleSet.contains("admin")) {
            throw new BusinessException(StatusCodeEnum.NO_PERMISSION);
        }
        User user = userMapper.findUserByName(username);
        if (user == null) {
            throw new BusinessException(StatusCodeEnum.USER_NOT_EXIST);
        }
        // 计算从1970年1月1日00:00:00 GMT开始计算的毫秒数
        user.setBanEndDate(new Timestamp(System.currentTimeMillis() + (long) days * 24 * 60 * 60 * 1000));
        userMapper.updateUser(user);
    }

    @Override
    @Transactional
    public int deleteNotAdminUser(String username) {
        Set<String> roleSet = roleMapper.getRolesByUserName(username);
        if (roleSet.contains("admin")) {
            throw new BusinessException(StatusCodeEnum.NO_PERMISSION);
        }
        roleMapper.deleteByUsername(username);
        return userMapper.deleteUser(username);
    }

    @Override
    public Set<User> listAll() {
        return userMapper.listAll();
    }

    @Override
    public void unbanUser(String username) {
        User user = userMapper.findUserByName(username);
        if (user == null) {
            throw new BusinessException(StatusCodeEnum.USER_NOT_EXIST);
        }
        user.setBanEndDate(null);
        userMapper.updateUser(user);
    }

    @Transactional
    @Override
    public boolean isUserBanned(String username) {
        User user = userMapper.findUserByName(username);
        if (user == null) {
            throw new BusinessException(StatusCodeEnum.USER_NOT_EXIST);
        }
        Timestamp banEndDate = user.getBanEndDate();
        if (banEndDate == null) {
            return false;
        }
        return banEndDate.after(new Timestamp(System.currentTimeMillis()));
    }
}

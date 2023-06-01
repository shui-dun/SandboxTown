package com.shuidun.sandbox_town_backend.service;

import com.shuidun.sandbox_town_backend.bean.Player;
import com.shuidun.sandbox_town_backend.bean.User;
import com.shuidun.sandbox_town_backend.enumeration.StatusCodeEnum;
import com.shuidun.sandbox_town_backend.exception.BusinessException;
import com.shuidun.sandbox_town_backend.mapper.PlayerMapper;
import com.shuidun.sandbox_town_backend.mapper.RoleMapper;
import com.shuidun.sandbox_town_backend.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Set;

@Service
public class UserService {
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;

    private final PlayerMapper playerMapper;

    public UserService(UserMapper userMapper, RoleMapper roleMapper, PlayerMapper playerMapper) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.playerMapper = playerMapper;
    }

    public User findUserByName(String username) {
        return userMapper.findUserByName(username);
    }

    @Transactional
    public void createUser(User user) {
        userMapper.insertUser(user);
        roleMapper.insertUserRole(user.getUsername(), "normal");
        Player player = new Player(user.getUsername(), 10, 0, 1, 100, 100, 10, 10, 5, 0, 0);
        playerMapper.insertPlayer(player);
    }

    public void updateUser(User user) {
        userMapper.updateUser(user);
    }

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

    @Transactional
    public int deleteNotAdminUser(String username) {
        Set<String> roleSet = roleMapper.getRolesByUserName(username);
        if (roleSet.contains("admin")) {
            throw new BusinessException(StatusCodeEnum.NO_PERMISSION);
        }
        roleMapper.deleteByUsername(username);
        return userMapper.deleteUser(username);
    }

    public Set<User> listAll() {
        return userMapper.listAll();
    }

    public void unbanUser(String username) {
        User user = userMapper.findUserByName(username);
        if (user == null) {
            throw new BusinessException(StatusCodeEnum.USER_NOT_EXIST);
        }
        user.setBanEndDate(null);
        userMapper.updateUser(user);
    }

    @Transactional
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

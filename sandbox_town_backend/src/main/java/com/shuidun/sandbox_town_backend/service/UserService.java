package com.shuidun.sandbox_town_backend.service;

import com.shuidun.sandbox_town_backend.bean.Sprite;
import com.shuidun.sandbox_town_backend.bean.User;
import com.shuidun.sandbox_town_backend.enumeration.StatusCodeEnum;
import com.shuidun.sandbox_town_backend.exception.BusinessException;
import com.shuidun.sandbox_town_backend.mapper.SpriteMapper;
import com.shuidun.sandbox_town_backend.mapper.UserMapper;
import com.shuidun.sandbox_town_backend.mapper.UserRoleMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
public class UserService {
    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;

    private final SpriteMapper spriteMapper;

    private final String mapId;

    public UserService(UserMapper userMapper, UserRoleMapper userRoleMapper, SpriteMapper spriteMapper, @Value("${mapId}") String mapId) {
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
        this.spriteMapper = spriteMapper;
        this.mapId = mapId;
    }

    public User findUserByName(String username) {
        return userMapper.selectById(username);
    }

    @Transactional
    public void createUser(User user) {
        userMapper.insert(user);
        userRoleMapper.insertUserRole(user.getUsername(), "normal");
        Sprite sprite = new Sprite(user.getUsername(), "user", null,
                10, 0, 1, 100, 100,
                10, 10, 5, 0, 0, 150, 150, mapId, null);
        spriteMapper.insert(sprite);
    }

    public void updateUser(User user) {
        userMapper.updateById(user);
    }

    @Transactional
    public void banUser(String username, int days) {
        Set<String> roleSet = userRoleMapper.getRolesByUserName(username);
        if (roleSet.contains("admin")) {
            throw new BusinessException(StatusCodeEnum.NO_PERMISSION);
        }
        User user = userMapper.selectById(username);
        if (user == null) {
            throw new BusinessException(StatusCodeEnum.USER_NOT_EXIST);
        }
        // 计算从1970年1月1日00:00:00 GMT开始计算的毫秒数
        user.setBanEndDate(new Timestamp(System.currentTimeMillis() + (long) days * 24 * 60 * 60 * 1000));
        userMapper.updateById(user);
    }

    @Transactional
    public int deleteNotAdminUser(String username) {
        Set<String> roleSet = userRoleMapper.getRolesByUserName(username);
        if (roleSet.contains("admin")) {
            throw new BusinessException(StatusCodeEnum.NO_PERMISSION);
        }
        userRoleMapper.deleteByUsername(username);
        return userMapper.deleteById(username);
    }

    public List<User> listAll() {
        return userMapper.selectList(null);
    }

    public void unbanUser(String username) {
        User user = userMapper.selectById(username);
        if (user == null) {
            throw new BusinessException(StatusCodeEnum.USER_NOT_EXIST);
        }
        user.setBanEndDate(null);
        userMapper.updateById(user);
    }

    @Transactional
    public boolean isUserBanned(String username) {
        User user = userMapper.selectById(username);
        if (user == null) {
            throw new BusinessException(StatusCodeEnum.USER_NOT_EXIST);
        }
        Date banEndDate = user.getBanEndDate();
        if (banEndDate == null) {
            return false;
        }
        return banEndDate.after(new Date(System.currentTimeMillis()));
    }
}

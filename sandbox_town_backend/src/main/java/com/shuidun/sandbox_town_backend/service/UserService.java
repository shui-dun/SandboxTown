package com.shuidun.sandbox_town_backend.service;

import com.shuidun.sandbox_town_backend.bean.Character;
import com.shuidun.sandbox_town_backend.bean.User;
import com.shuidun.sandbox_town_backend.enumeration.StatusCodeEnum;
import com.shuidun.sandbox_town_backend.exception.BusinessException;
import com.shuidun.sandbox_town_backend.mapper.CharacterMapper;
import com.shuidun.sandbox_town_backend.mapper.RoleMapper;
import com.shuidun.sandbox_town_backend.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Set;

@Service
public class UserService {
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;

    private final CharacterMapper characterMapper;

    private final String mapName;

    public UserService(UserMapper userMapper, RoleMapper roleMapper, CharacterMapper characterMapper, @Value("${mapName}") String mapName) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.characterMapper = characterMapper;
        this.mapName = mapName;
    }

    public User findUserByName(String username) {
        return userMapper.findUserByName(username);
    }

    @Transactional
    public void createUser(User user) {
        userMapper.insertUser(user);
        roleMapper.insertUserRole(user.getUsername(), "normal");
        Character character = new Character(user.getUsername(), null, 10, 0, 1, 100, 100, 10, 10, 5, 0, 0, mapName);
        characterMapper.createCharacter(character);
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

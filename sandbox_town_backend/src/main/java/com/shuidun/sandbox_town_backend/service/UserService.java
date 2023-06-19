package com.shuidun.sandbox_town_backend.service;

import cn.dev33.satoken.stp.StpUtil;
import com.shuidun.sandbox_town_backend.bean.Sprite;
import com.shuidun.sandbox_town_backend.bean.User;
import com.shuidun.sandbox_town_backend.enumeration.StatusCodeEnum;
import com.shuidun.sandbox_town_backend.exception.BusinessException;
import com.shuidun.sandbox_town_backend.mapper.SpriteMapper;
import com.shuidun.sandbox_town_backend.mapper.UserMapper;
import com.shuidun.sandbox_town_backend.mapper.UserRoleMapper;
import com.shuidun.sandbox_town_backend.utils.PasswordEncryptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class UserService {
    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;

    private final SpriteMapper spriteMapper;

    @Value("${mapId}")
    private String mapId;

    public UserService(UserMapper userMapper, UserRoleMapper userRoleMapper, SpriteMapper spriteMapper) {
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
        this.spriteMapper = spriteMapper;
    }

    public User findUserByName(String username) {
        return userMapper.selectById(username);
    }


    @Transactional
    public String signup(String usernameSuffix, String password) {
        // 判断用户名是否合法
        if (usernameSuffix == null || usernameSuffix.length() < 3) {
            throw new BusinessException(StatusCodeEnum.USERNAME_TOO_SHORT);
        }
        // 判断密码强度
        if (password == null || password.length() < 6) {
            throw new BusinessException(StatusCodeEnum.PASSWORD_TOO_SHORT);
        }
        // 生成盐和加密后的密码
        String[] saltAndPasswd = PasswordEncryptor.generateSaltedHash(password);
        // 用户名为"user_" + usernameSuffix
        String username = "user_" + usernameSuffix;
        try {
            Date currentDate = new Date(System.currentTimeMillis());
            User user = new User(username, saltAndPasswd[1], saltAndPasswd[0], null, 0,
                    currentDate, null);
            userMapper.insert(user);
            userRoleMapper.insertUserRole(user.getUsername(), "normal");
            Sprite sprite = new Sprite(user.getUsername(), "user", null,
                    10, 0, 1, 100, 100,
                    10, 10, 5, 0, 0, 150, 150, mapId, null);
            spriteMapper.insert(sprite);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(StatusCodeEnum.USER_ALREADY_EXIST);
        }
        return username;
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

    @Transactional
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

    /**
     * 用户登录时，领取奖励
     *
     * @param username 用户名
     * @return 奖励金额
     */
    @Transactional
    public int enterGameToReceiveReward(String username) {
        User user = userMapper.selectById(username);
        // 获取上次登录日期
        Date lastLoginDate = user.getLastOnline();
        // 计算奖励
        int reward = 0;
        // 如果是新用户，获得200元奖励
        if (lastLoginDate == null) {
            reward = 200;
        } else {
            LocalDate lastLoginDay = lastLoginDate.toInstant().atZone(ZoneId.of("Asia/Shanghai")).toLocalDate();
            // 获取当前日期
            LocalDate today = LocalDate.now(ZoneId.of("Asia/Shanghai"));
            // 上次登录日期距离今天的天数
            long days = ChronoUnit.DAYS.between(lastLoginDay, today);
            log.info("today: {}, lastLoginDay: {} days: {}", today, lastLoginDay, days);
            if (days > 30) { // 如果超过30天没有上线，获得200元奖励
                reward = 200;
            } else if (days >= 1) { // 如果1天没有上线，获得40元奖励
                reward = 40;
            }
        }
        // 获得玩家当前金钱
        int money = spriteMapper.getSpriteById(username).getMoney();
        // 更新玩家金钱
        spriteMapper.updateSpriteAttribute(username, "money", money + reward);
        // 更新玩家上次登录时间
        user.setLastOnline(new Date(System.currentTimeMillis()));
        userMapper.updateById(user);
        return reward;
    }

    /**
     * 用户登录
     */
    @Transactional
    public void login(String username, String password) {
        User user = userMapper.selectById(username);
        // 判断用户是否存在
        if (user == null) {
            throw new BusinessException(StatusCodeEnum.USER_NOT_EXIST);
        }
        // 用户是否被封禁
        if (isUserBanned(username)) {
            throw new BusinessException(StatusCodeEnum.USER_BEEN_BANNED);
        }
        // 判断密码是否正确
        String encryptedPasswd = PasswordEncryptor.encryptedPasswd(password, user.getSalt());
        assert encryptedPasswd != null;
        if (!encryptedPasswd.equals(user.getPassword())) {
            throw new BusinessException(StatusCodeEnum.INCORRECT_CREDENTIALS);
        }
    }

    /**
     * 用户修改密码
     */
    @Transactional
    public void changePassword(String oldPassword, String newPassword) {
        // 判断密码强度
        if (newPassword == null || newPassword.length() < 6) {
            throw new BusinessException(StatusCodeEnum.PASSWORD_TOO_SHORT);
        }
        // 获取当前用户
        String username = StpUtil.getLoginIdAsString();
        User user = userMapper.selectById(username);
        // 判断密码是否正确
        String encryptedPasswd = PasswordEncryptor.encryptedPasswd(oldPassword, user.getSalt());
        assert encryptedPasswd != null;
        if (encryptedPasswd.equals(user.getPassword())) {
            // 生成盐和加密后的密码
            String[] saltAndPasswd = PasswordEncryptor.generateSaltedHash(newPassword);
            user.setSalt(saltAndPasswd[0]);
            user.setPassword(saltAndPasswd[1]);
            userMapper.updateById(user);
        } else {
            throw new BusinessException(StatusCodeEnum.INCORRECT_CREDENTIALS);
        }
    }
}

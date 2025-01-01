package com.shuidun.sandbox_town_backend.service;

import cn.dev33.satoken.stp.StpUtil;
import com.shuidun.sandbox_town_backend.bean.SpriteDo;
import com.shuidun.sandbox_town_backend.bean.UserDo;
import com.shuidun.sandbox_town_backend.enumeration.SpriteTypeEnum;
import com.shuidun.sandbox_town_backend.enumeration.StatusCodeEnum;
import com.shuidun.sandbox_town_backend.exception.BusinessException;
import com.shuidun.sandbox_town_backend.mapper.UserMapper;
import com.shuidun.sandbox_town_backend.mapper.UserRoleMapper;
import com.shuidun.sandbox_town_backend.utils.PasswordEncryptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.lang.Nullable;
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

    private final SpriteService spriteService;

    @Value("${mapId}")
    private String mapId;

    public UserService(UserMapper userMapper, UserRoleMapper userRoleMapper, SpriteService spriteService) {
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
        this.spriteService = spriteService;
    }

    @Nullable
    public UserDo findUserByName(String username) {
        return userMapper.selectById(username);
    }


    @Transactional
    public String signup(String usernameSuffix, String password) {
        // 判断用户名是否合法
        if (usernameSuffix.length() < 3) {
            throw new BusinessException(StatusCodeEnum.USERNAME_TOO_SHORT);
        }
        // 判断密码强度
        if (password.length() < 6) {
            throw new BusinessException(StatusCodeEnum.PASSWORD_TOO_SHORT);
        }
        // 生成盐和加密后的密码
        String[] saltAndPasswd = PasswordEncryptor.generateSaltedHash(password);
        // 用户名为"user_" + usernameSuffix
        String username = SpriteTypeEnum.USER.name() + "_" + usernameSuffix;
        try {
            Date currentDate = new Date(System.currentTimeMillis());
            UserDo user = new UserDo(username, saltAndPasswd[1], saltAndPasswd[0], null, 0,
                    currentDate, null);
            userMapper.insert(user);
            userRoleMapper.insert(user.getUsername(), "NORMAL");
            spriteService.generateFixedSprite(SpriteTypeEnum.USER, user.getUsername(), null, 0, 0);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(StatusCodeEnum.USER_ALREADY_EXIST);
        }
        return username;
    }

    public void updateUser(UserDo user) {
        userMapper.updateById(user);
    }

    @Transactional
    public void banUser(String username, int days) {
        Set<String> roleSet = userRoleMapper.selectByUserName(username);
        if (roleSet.contains("ADMIN")) {
            throw new BusinessException(StatusCodeEnum.NO_PERMISSION);
        }
        UserDo user = userMapper.selectById(username);
        if (user == null) {
            throw new BusinessException(StatusCodeEnum.USER_NOT_EXIST);
        }
        // 计算从1970年1月1日00:00:00 GMT开始计算的毫秒数
        user.setBanEndDate(new Timestamp(System.currentTimeMillis() + (long) days * 24 * 60 * 60 * 1000));
        userMapper.updateById(user);
    }

    @Transactional
    public int deleteNotAdminUser(String username) {
        Set<String> roleSet = userRoleMapper.selectByUserName(username);
        if (roleSet.contains("ADMIN")) {
            throw new BusinessException(StatusCodeEnum.NO_PERMISSION);
        }
        userRoleMapper.deleteByUsername(username);
        return userMapper.deleteById(username);
    }

    public List<UserDo> listAll() {
        return userMapper.selectList(null);
    }

    @Transactional
    public void unbanUser(String username) {
        UserDo user = userMapper.selectById(username);
        if (user == null) {
            throw new BusinessException(StatusCodeEnum.USER_NOT_EXIST);
        }
        user.setBanEndDate(null);
        userMapper.updateById(user);
    }

    @Transactional
    public boolean isUserBanned(String username) {
        UserDo user = userMapper.selectById(username);
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
        UserDo user = userMapper.selectById(username);
        if (user == null) {
            throw new BusinessException(StatusCodeEnum.USER_NOT_EXIST);
        }
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
        var sprite = spriteService.selectByIdWithType(username);
        assert sprite != null;
        // 获得玩家当前金钱
        int money = sprite.getMoney();
        // 更新玩家金钱
        SpriteDo spriteDo = spriteService.selectById(username);
        assert spriteDo != null;
        spriteDo.setMoney(money + reward);
        spriteService.normalizeAndUpdateSprite(spriteDo);
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
        UserDo user = userMapper.selectById(username);
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
        if (newPassword.length() < 6) {
            throw new BusinessException(StatusCodeEnum.PASSWORD_TOO_SHORT);
        }
        // 获取当前用户
        String username = StpUtil.getLoginIdAsString();
        UserDo user = userMapper.selectById(username);
        if (user == null) {
            throw new BusinessException(StatusCodeEnum.USER_NOT_EXIST);
        }
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

    /**
     * 根据用户名搜索用户列表
     */
    public List<UserDo> searchUser(String username) {
        return userMapper.searchUser(username);
    }
}

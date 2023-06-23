package com.shuidun.sandbox_town_backend.service;

import com.shuidun.sandbox_town_backend.bean.User;
import com.shuidun.sandbox_town_backend.exception.BusinessException;
import com.shuidun.sandbox_town_backend.mapper.UserRoleMapper;
import com.shuidun.sandbox_town_backend.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * GPT4写的单元测试，感觉单元测试啥都测不出来
 */
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    // @InjectMocks表示自动将Mock对象注入到测试对象中的相应字段中
    @InjectMocks
    private UserService userService;

    // @Mock表示创建模拟对象
    @Mock
    private UserMapper userMapper;

    @Mock
    private UserRoleMapper userRoleMapper;

    private User user;
    private String username;
    private Set<String> roleSet;

    @BeforeEach
    // 在每个测试方法执行前执行一些预处理操作
    public void setUp() {
        username = "testUser";
        user = new User();
        user.setUsername(username);
        roleSet = new HashSet<>();
    }

    @Test
    public void testBanUser() {
        roleSet.add("normal");
        when(userRoleMapper.selectByUserName(username)).thenReturn(roleSet);
        when(userMapper.selectById(username)).thenReturn(user);

        userService.banUser(username, 3);

        verify(userMapper, times(1)).updateById(user);
    }

    @Test
    public void testUnbanUser() {
        when(userMapper.selectById(username)).thenReturn(user);

        userService.unbanUser(username);

        verify(userMapper, times(1)).updateById(user);
    }

    @Test
    public void testBanUserAdmin() {
        roleSet.add("admin");
        when(userRoleMapper.selectByUserName(username)).thenReturn(roleSet);

        assertThrows(BusinessException.class, () -> userService.banUser(username, 3));
    }

    @Test
    public void testBanUserNotFound() {
        when(userRoleMapper.selectByUserName(username)).thenReturn(roleSet);
        when(userMapper.selectById(username)).thenReturn(null);

        assertThrows(BusinessException.class, () -> userService.banUser(username, 3));
    }

    @Test
    public void testUnbanUserNotFound() {
        when(userMapper.selectById(username)).thenReturn(null);

        assertThrows(BusinessException.class, () -> userService.unbanUser(username));
    }
}

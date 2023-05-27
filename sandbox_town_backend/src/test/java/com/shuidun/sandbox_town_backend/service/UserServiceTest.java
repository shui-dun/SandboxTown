package com.shuidun.sandbox_town_backend.service;

import com.shuidun.sandbox_town_backend.bean.User;
import com.shuidun.sandbox_town_backend.exception.BusinessException;
import com.shuidun.sandbox_town_backend.mapper.RoleMapper;
import com.shuidun.sandbox_town_backend.mapper.UserMapper;
import com.shuidun.sandbox_town_backend.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Timestamp;
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
    private UserServiceImpl userService;

    // @Mock表示创建模拟对象
    @Mock
    private UserMapper userMapper;

    @Mock
    private RoleMapper roleMapper;

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
        when(roleMapper.getRolesByUserName(username)).thenReturn(roleSet);
        when(userMapper.findUserByName(username)).thenReturn(user);

        userService.banUser(username, 3);

        verify(userMapper, times(1)).updateUser(user);
    }

    @Test
    public void testUnbanUser() {
        when(userMapper.findUserByName(username)).thenReturn(user);

        userService.unbanUser(username);

        verify(userMapper, times(1)).updateUser(user);
    }

    @Test
    public void testBanUserAdmin() {
        roleSet.add("admin");
        when(roleMapper.getRolesByUserName(username)).thenReturn(roleSet);

        assertThrows(BusinessException.class, () -> userService.banUser(username, 3));
    }

    @Test
    public void testBanUserNotFound() {
        when(roleMapper.getRolesByUserName(username)).thenReturn(roleSet);
        when(userMapper.findUserByName(username)).thenReturn(null);

        assertThrows(BusinessException.class, () -> userService.banUser(username, 3));
    }

    @Test
    public void testUnbanUserNotFound() {
        when(userMapper.findUserByName(username)).thenReturn(null);

        assertThrows(BusinessException.class, () -> userService.unbanUser(username));
    }
}

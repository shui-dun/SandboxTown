package com.shuidun.sandbox_town_backend.config;


import com.shuidun.sandbox_town_backend.bean.User;
import com.shuidun.sandbox_town_backend.service.PermService;
import com.shuidun.sandbox_town_backend.service.RoleService;
import com.shuidun.sandbox_town_backend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.*;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@Slf4j
/** Shiro可以从Realm中获取安全数据（如用户、角色、权限），即认证和授权等操作所需要的安全数据都需要从Realm中获得 */
 public class CustomRealm extends AuthorizingRealm {

    @Autowired
    @Lazy
    private UserService userService;

    @Autowired
    @Lazy
    private RoleService roleService;

    @Autowired
    @Lazy
    private PermService permService;

    /** 授权 */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        //null usernames are invalid
        if (principals == null) {
            throw new AuthorizationException("PrincipalCollection method argument cannot be null.");
        }

        User user = (User) getAvailablePrincipal(principals);
        Set<String> roles = user.getRoles();
        Set<String> perms = user.getPerms();
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        info.setRoles(roles);
        info.setStringPermissions(perms);
        return info;
    }

    /** 身份验证，这个方法名doGetAuthenticationInfo起的有点迷惑性，让人误以为这是鉴权而不是认证 */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        UsernamePasswordToken upToken = (UsernamePasswordToken) authenticationToken;
        String username = upToken.getUsername();

        // Null username is invalid
        if (username == null) {
            throw new AccountException("Null usernames are not allowed by this realm.");
        }

        User userDB = userService.findUserByName(username);

        if (userDB == null) {
            throw new UnknownAccountException("No account found for admin [" + username + "]");
        }

        // 查询用户的角色和权限存到SimpleAuthenticationInfo中，这样在其它地方
        // SecurityUtils.getSubject().getPrincipal()就能拿出用户的所有信息，包括角色和权限

        Set<String> roles = roleService.getRolesByUserName(userDB.getName());
        userDB.getRoles().addAll(roles);
        Set<String> perms = permService.getPermsByRoles(userDB.getRoles());
        userDB.getPerms().addAll(perms);

        SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(userDB, userDB.getPasswd(), getName());
        if (userDB.getSalt() != null) {
            info.setCredentialsSalt(ByteSource.Util.bytes(userDB.getSalt()));
        }

        return info;
    }
}

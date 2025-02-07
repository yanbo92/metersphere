package io.metersphere.security.realm;


import io.metersphere.commons.constants.UserSource;
import io.metersphere.commons.user.SessionUser;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.dto.UserDTO;
import io.metersphere.i18n.Translator;
import io.metersphere.service.BaseUserService;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Set;


/**
 * 自定义Realm 注入service 可能会导致在 service的aop 失效，例如@Transactional,
 * 解决方法：
 * <p>
 * 1. 这里改成注入mapper，这样mapper 中的事务失效<br/>
 * 2. 这里仍然注入service，在配置ShiroConfig 的时候不去set realm, 等到spring 初始化完成之后
 * set realm
 * </p>
 */
public class LocalRealm extends AuthorizingRealm {

    private Logger logger = LoggerFactory.getLogger(LocalRealm.class);
    @Resource
    private BaseUserService baseUserService;

    @Override
    public String getName() {
        return "LOCAL";
    }

    /**
     * 角色认证
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        return null;
    }

    public static AuthorizationInfo getAuthorizationInfo(String userId, BaseUserService baseUserService) {
        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
        Set<String> userPermission = baseUserService.getUserPermission(userId);
        authorizationInfo.setStringPermissions(userPermission);
        return authorizationInfo;
    }

    /**
     * 登录认证
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        UsernamePasswordToken token = (UsernamePasswordToken) authenticationToken;
        String login = (String) SecurityUtils.getSubject().getSession().getAttribute("authenticate");

        String userId = token.getUsername();
        String password = String.valueOf(token.getPassword());

        if (StringUtils.equals(login, UserSource.LOCAL.name())) {
            return loginLocalMode(userId, password);
        }

        UserDTO user = getUserWithOutAuthenticate(userId);
        userId = user.getId();
        SessionUser sessionUser = SessionUser.fromUser(user, SessionUtils.getSessionId());
        SessionUtils.putUser(sessionUser);
        return new SimpleAuthenticationInfo(userId, password, getName());

    }

    @Override
    public boolean isPermitted(PrincipalCollection principals, String permission) {
        return SessionUtils.hasPermission(SessionUtils.getCurrentWorkspaceId(), SessionUtils.getCurrentProjectId(), permission);
    }

    private UserDTO getUserWithOutAuthenticate(String userId) {
        UserDTO user = baseUserService.getUserDTO(userId);
        String msg;
        if (user == null) {
            user = baseUserService.getUserDTOByEmail(userId);
            if (user == null) {
                msg = "The user does not exist: " + userId;
                logger.warn(msg);
                throw new UnknownAccountException(Translator.get("password_is_incorrect"));
            }
        }
        return user;
    }

    private AuthenticationInfo loginLocalMode(String userId, String password) {
        UserDTO user = baseUserService.getLoginUser(userId, Collections.singletonList(UserSource.LOCAL.name()));
        String msg;
        if (user == null) {
            user = baseUserService.getUserDTOByEmail(userId, UserSource.LOCAL.name());
            if (user == null) {
                msg = "The user does not exist: " + userId;
                logger.warn(msg);
                throw new UnknownAccountException(Translator.get("password_is_incorrect"));
            }
            userId = user.getId();
        }
        // 密码验证
        if (!baseUserService.checkUserPassword(userId, password)) {
            throw new IncorrectCredentialsException(Translator.get("password_is_incorrect"));
        }
        SessionUser sessionUser = SessionUser.fromUser(user, SessionUtils.getSessionId());
        SessionUtils.putUser(sessionUser);
        return new SimpleAuthenticationInfo(userId, password, getName());
    }

}

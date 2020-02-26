package com.example.awesomecurriculum.data;

import com.example.awesomecurriculum.data.model.LoggedInUser;

/**
 * 该类从远程数据源请求身份验证和用户信息，并维护一个包含登录状态和用户凭据信息的内存缓存。
 */
public class LoginRepository {

    private static volatile LoginRepository instance;

    private LoginDataSource dataSource;

    /**
     * 如果用户凭据将缓存在本地存储中，建议对其进行加密
     * @see https://developer.android.com/training/articles/keystore
     */
    private LoggedInUser user = null;

    /** 私有构造函数:单例访问 */
    private LoginRepository(LoginDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static LoginRepository getInstance(LoginDataSource dataSource) {
        if (instance == null) {
            instance = new LoginRepository(dataSource);
        }
        return instance;
    }
    /** 判断用户是否已经登录 */
    public boolean isLoggedIn() {
        return user != null;
    }

    /** 用户退出登录 */
    public void logout() {
        user = null;
        dataSource.logout();
    }

    /** 设置已登录的用户信息 */
    private void setLoggedInUser(LoggedInUser user) {
        this.user = user;
    }

    /** 用户登录 */
    public Result<LoggedInUser> login(String username, String password) {
        // TODO：handle login
        Result<LoggedInUser> result = dataSource.login(username, password);
        if (result instanceof Result.Success) {
            setLoggedInUser(((Result.Success<LoggedInUser>) result).getData());
        }
        return result;
    }
}

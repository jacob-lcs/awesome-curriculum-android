package com.example.awesomecurriculum.ui.login;

/**
 * 类向UI公开已验证的用户详细信息。
 */
class LoggedInUserView {
    private String displayName;
    private String avatar;
    private String email;
    private String token;
    //... other data fields that may be accessible to the UI

    LoggedInUserView(String displayName, String avatar, String email, String token) {
        this.displayName = displayName;
        this.avatar = avatar;
        this.email = email;
        this.token = token;
    }

    String getDisplayName() {
        return displayName;
    }

    String getAvatar() {
        return avatar;
    }

    String getEmail() {
        return email;
    }

    String getToken() {
        return token;
    }

}

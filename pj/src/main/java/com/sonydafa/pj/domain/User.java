package com.sonydafa.pj.domain;

import org.springframework.stereotype.Component;

@Component
public class User {
    private String id;
    private String password;
    private String captcha;

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", password='" + password + '\'' +
                ", captcha='" + captcha + '\'' +
                '}';
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setcaptcha(String captcha) {
        this.captcha = captcha;
    }

    public String getId() {
        return id;
    }

    public String getPassword() {
        return password;
    }

    public String getcaptcha() {
        return captcha;
    }
}

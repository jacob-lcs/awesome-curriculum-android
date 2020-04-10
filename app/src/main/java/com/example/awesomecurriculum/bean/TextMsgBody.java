package com.example.awesomecurriculum.bean;


/**
 * @author Jacob
 */
public class TextMsgBody extends MsgBody {
    private String message;
    private String extra;
    private String name;
    private String time;
    private String avatar;

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public TextMsgBody() {
    }

    public TextMsgBody(String message, String time, String name, String avatar) {
        this.message = message;
        this.name = name;
        this.time = time;
        this.avatar = avatar;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }


    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }


    @Override
    public String toString() {
        return "TextMsgBody{" +
                "message='" + message + '\'' +
                ", extra='" + extra + '\'' +
                '}';
    }
}

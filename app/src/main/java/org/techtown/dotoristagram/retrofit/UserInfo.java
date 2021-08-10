package org.techtown.dotoristagram.retrofit;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class UserInfo implements Serializable{

    @SerializedName("userId")
    private String userId;

    @SerializedName("userPassword")
    private String userPassword;

    @SerializedName("userName")
    private String userName;


    @SerializedName("PhoneNum")
    private String PhoneNum;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }


    public UserInfo(String userId, String userPassword, String userName, String phoneNum) {
        this.userId = userId;
        this.userPassword = userPassword;
        this.userName = userName;

        PhoneNum = phoneNum;
    }

    public String getPhoneNum() {
        return PhoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        PhoneNum = phoneNum;
    }




}

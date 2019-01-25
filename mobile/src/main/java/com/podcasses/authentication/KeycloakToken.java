package com.podcasses.authentication;

import com.auth0.android.jwt.JWT;
import com.google.android.gms.common.util.Strings;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Created by aleksandar.kovachev.
 */
public class KeycloakToken {

    public static final String PREFERRED_USERNAME_CLAIMS = "preferred_username";

    @SerializedName("access_token")
    private String accessToken;

    @SerializedName("expires_in")
    private Integer expiresIn;

    @SerializedName("refresh_expires_in")
    private Integer refreshExpiresIn;

    @SerializedName("refresh_token")
    private String refreshToken;

    public static boolean isValidToken(String accessToken) {
        if(Strings.isEmptyOrWhitespace(accessToken)) {
            return false;
        }
        if(new JWT(accessToken).getExpiresAt().before(new Date())) {
            return false;
        }
        return true;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public Integer getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Integer expiresIn) {
        this.expiresIn = expiresIn;
    }

    public Integer getRefreshExpiresIn() {
        return refreshExpiresIn;
    }

    public void setRefreshExpiresIn(Integer refreshExpiresIn) {
        this.refreshExpiresIn = refreshExpiresIn;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}

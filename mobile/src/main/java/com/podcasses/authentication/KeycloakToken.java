package com.podcasses.authentication;

import com.auth0.android.jwt.JWT;
import com.google.android.gms.common.util.Strings;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

import lombok.Data;

/**
 * Created by aleksandar.kovachev.
 */
@Data
public class KeycloakToken {

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

}

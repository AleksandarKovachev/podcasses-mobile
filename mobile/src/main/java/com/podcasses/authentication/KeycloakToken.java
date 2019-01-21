package com.podcasses.authentication;

import com.google.gson.annotations.SerializedName;

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

}

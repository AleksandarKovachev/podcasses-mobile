package com.podcasses.retrofit;

import com.podcasses.authentication.KeycloakToken;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by aleksandar.kovachev.
 */
public interface AuthenticationCallInterface {

    String BASE_URL = "http://192.168.0.102:8080/auth/realms/podcast/protocol/openid-connect/";

    String ACCESS_TOKEN_GRANT_TYPE = "password";

    String CLIENT_ID = "android-app";

    String CLIENT_SECRET = "3d541700-0250-424f-8b19-53c883f841ab";

    @POST("token")
    @FormUrlEncoded
    Call<KeycloakToken> grantNewAccessToken(
            @Field("username") String username,
            @Field("password") String password,
            @Field("grant_type") String grantType
    );

    @POST("logout")
    @FormUrlEncoded
    Call<Void> logout(
            @Field("client_id") String clientId,
            @Field("refresh_token") String refreshToken
    );

}

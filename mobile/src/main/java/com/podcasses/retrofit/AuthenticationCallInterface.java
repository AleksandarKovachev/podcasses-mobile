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

    String BASE_URL = "http://192.168.0.103:8080/auth/realms/podcast/protocol/openid-connect/";

    String ACCESS_TOKEN_GRANT_TYPE = "password";

    String REFRESH_TOKEN_GRANT_TYPE = "refresh_token";

    String CLIENT_ID = "web-app";

    String CLIENT_SECRET = "eafc3ccc-8390-4999-b84d-23cb76b4776e";

    @POST("token")
    @FormUrlEncoded
    Call<KeycloakToken> grantNewAccessToken(
            @Field("username") String username,
            @Field("password") String password,
            @Field("grant_type") String grantType
    );

    @POST("token")
    @FormUrlEncoded
    Call<KeycloakToken> refreshAccessToken(
            @Field("refresh_token") String refreshToken,
            @Field("grant_type") String grantType
    );

    @POST("logout")
    @FormUrlEncoded
    Call<Void> logout(
            @Field("client_id") String clientId,
            @Field("refresh_token") String refreshToken
    );

}

package com.podcasses.retrofit;

import com.podcasses.BuildConfig;
import com.podcasses.authentication.KeycloakToken;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.POST;

/**
 * Created by aleksandar.kovachev.
 */
public interface AuthenticationCallInterface {

    String KEYCLOAK_URL = BuildConfig.KEYCLOAK_URL;

    String ACCESS_TOKEN_GRANT_TYPE = "password";

    String CLIENT_ID = "android-app";

    String CLIENT_SECRET = "28f75581-188f-4d46-a6e2-2f1f841e5f62";

    String TOKEN_TYPE = "Bearer ";

    @POST("token")
    @FormUrlEncoded
    Call<KeycloakToken> grantNewAccessToken(
            @Field("username") String username,
            @Field("password") String password,
            @Field("grant_type") String grantType
    );

    @POST("logout")
    @FormUrlEncoded
    Call<Void> logout(@Header("Authorization") String auth,
                      @Field("client_id") String clientId,
                      @Field("client_secret") String clientSecret,
                      @Field("refresh_token") String refreshToken
    );

}

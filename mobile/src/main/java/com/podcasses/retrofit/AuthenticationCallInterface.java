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

    String CLIENT_ID = BuildConfig.KEYCLOAK_CLIENT_ID;

    String CLIENT_SECRET = BuildConfig.KEYCLOAK_CLIENT_SECRET;

    String FACEBOOK_ISSUER = "facebook";

    String GOOGLE_ISSUER = "google";

    String ACCESS_TOKEN_GRANT_TYPE = "password";

    String REFRESH_TOKEN_GRANT_TYPE = "refresh_token";

    String TOKEN_EXCHANGE_GRANT_TYPE = "urn:ietf:params:oauth:grant-type:token-exchange";

    String TOKEN_TYPE = "Bearer ";

    @POST("token")
    @FormUrlEncoded
    Call<KeycloakToken> accessToken(@Field("username") String username,
                                    @Field("password") String password,
                                    @Field("grant_type") String grantType
    );

    @POST("token")
    @FormUrlEncoded
    Call<KeycloakToken> refreshToken(@Field("client_id") String clientId,
                                     @Field("refresh_token") String refreshToken,
                                     @Field("grant_type") String grantType
    );

    @POST("logout")
    @FormUrlEncoded
    Call<Void> logout(@Header("Authorization") String auth,
                      @Field("client_id") String clientId,
                      @Field("client_secret") String clientSecret,
                      @Field("refresh_token") String refreshToken
    );

    @POST("token")
    @FormUrlEncoded
    Call<KeycloakToken> tokenExchange(@Field("client_id") String clientId,
                                      @Field("client_secret") String clientSecret,
                                      @Field("grant_type") String grantType,
                                      @Field("subject_issuer") String issuer,
                                      @Field("subject_token") String token
    );

}

package com.podcasses.retrofit;

import com.google.gson.JsonObject;
import com.podcasses.constant.ApiUrl;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by aleksandar.kovachev.
 */
public interface ApiCallInterface {

    @GET(ApiUrl.ACCOUNT)
    Observable<JsonObject> account(@Path("username") String username);

}

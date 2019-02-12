package com.podcasses.retrofit;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Created by aleksandar.kovachev.
 */
public interface ApiFileUploadInterface {

    @Multipart
    @POST("/podcast/image")
    Call<ResponseBody> podcastImage(@Header("Authorization") String token, @Part MultipartBody.Part podcastImage);

    @Multipart
    @POST("/podcast/upload")
    Call<ResponseBody> podcastUpload(@Header("Authorization") String token, @Part MultipartBody.Part podcast);

}

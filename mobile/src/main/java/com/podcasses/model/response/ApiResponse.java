package com.podcasses.model.response;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static com.podcasses.model.response.ApiResponse.Status.DATABASE;
import static com.podcasses.model.response.ApiResponse.Status.ERROR;
import static com.podcasses.model.response.ApiResponse.Status.FETCHED;
import static com.podcasses.model.response.ApiResponse.Status.LOADING;
import static com.podcasses.model.response.ApiResponse.Status.SUCCESS;

/**
 * Created by aleksandar.kovachev.
 */
public class ApiResponse {

    public final Status status;

    @Nullable
    public final Object data;

    @Nullable
    public final Throwable error;

    public final String url;

    private ApiResponse(Status status, @Nullable Object data, @Nullable Throwable error, String url) {
        this.status = status;
        this.data = data;
        this.error = error;
        this.url = url;
    }

    public static ApiResponse loading() {
        return new ApiResponse(LOADING, null, null, null);
    }

    public static ApiResponse success(@NonNull Object data, String url) {
        return new ApiResponse(SUCCESS, data, null, url);
    }

    public static ApiResponse error(@NonNull Throwable error, String url) {
        return new ApiResponse(ERROR, null, error, url);
    }

    public static ApiResponse fetched() {
        return  new ApiResponse(FETCHED, null, null, null);
    }

    public static ApiResponse database(Object data) {
        return new ApiResponse(DATABASE, data, null, null);
    }

    public enum Status {

        LOADING,
        SUCCESS,
        ERROR,
        FETCHED,
        DATABASE

    }
}

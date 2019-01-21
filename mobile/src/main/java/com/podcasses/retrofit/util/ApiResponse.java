package com.podcasses.retrofit.util;

import com.google.gson.JsonElement;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static com.podcasses.retrofit.util.Status.ERROR;
import static com.podcasses.retrofit.util.Status.LOADING;
import static com.podcasses.retrofit.util.Status.SUCCESS;

/**
 * Created by aleksandar.kovachev.
 */
public class ApiResponse {

    public final Status status;

    @Nullable
    public final JsonElement data;

    @Nullable
    public final Throwable error;

    private ApiResponse(Status status, @Nullable JsonElement data, @Nullable Throwable error) {
        this.status = status;
        this.data = data;
        this.error = error;
    }

    public static ApiResponse loading() {
        return new ApiResponse(LOADING, null, null);
    }

    public static ApiResponse success(@NonNull JsonElement data) {
        return new ApiResponse(SUCCESS, data, null);
    }

    public static ApiResponse error(@NonNull Throwable error) {
        return new ApiResponse(ERROR, null, error);
    }

}

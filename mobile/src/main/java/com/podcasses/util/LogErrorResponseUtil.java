package com.podcasses.util;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.podcasses.R;
import com.podcasses.model.response.ApiResponse;

import java.io.IOException;
import java.net.ConnectException;

import es.dmoral.toasty.Toasty;
import retrofit2.Response;

/**
 * Created by aleksandar.kovachev.
 */
public class LogErrorResponseUtil {

    public static void logErrorResponse(Response response, Context context) {
        if (response.errorBody() != null) {
            Toasty.error(context, context.getString(R.string.error_response), Toast.LENGTH_SHORT, true).show();
            try {
                Log.e("ErrorResponse", String.format("Bad network request to %1$s with code %2$s and body: %3$s",
                        response.raw().request().url(), response.code(), response.errorBody().string()));
            } catch (IOException e) {
                Log.e("ErrorResponse", String.format("Could not parse error response from url %1$s: ", response.raw().request().url()), e);
            }
        }
    }

    public static void logFailure(Throwable throwable, Context context) {
        Log.e("ErrorResponse", "onFailure() returned: ", throwable);
        Toasty.error(context, context.getString(R.string.error_response), Toast.LENGTH_SHORT, true).show();
    }

    public static void logErrorApiResponse(@NonNull ApiResponse apiResponse, Context context) {
        Log.e("ErrorResponse", String.format("API Error response from url %1$s: ", apiResponse.url), apiResponse.error);
        if (apiResponse.error instanceof ConnectException) {
            Toast.makeText(context, context.getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, context.getString(R.string.could_not_fetch_data), Toast.LENGTH_SHORT).show();
        }
    }

}

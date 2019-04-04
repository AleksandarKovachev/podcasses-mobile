package com.podcasses.util;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.podcasses.R;
import com.podcasses.model.response.ApiResponse;

import java.io.IOException;
import java.net.ConnectException;

import androidx.annotation.NonNull;
import es.dmoral.toasty.Toasty;
import retrofit2.Response;

/**
 * Created by aleksandar.kovachev.
 */
public class LogErrorResponseUtil {

    public static void logErrorResponse(Response response, Context context) {
        if (response.errorBody() != null) {
            try {
                Log.e("ErrorResponse", "onFailure() returned: " + response.errorBody().string());
            } catch (IOException e) {
                Log.e("ErrorResponse", "Could not parse error response: ", e);
            }
        }
        Toasty.error(context, context.getString(R.string.error_response), Toast.LENGTH_SHORT, true).show();
    }

    public static void logFailure(Throwable throwable, Context context) {
        Log.e("ErrorResponse", "onFailure() returned: ", throwable);
        Toasty.error(context, context.getString(R.string.error_response), Toast.LENGTH_SHORT, true).show();
    }

    public static void logErrorApiResponse(@NonNull ApiResponse apiResponse, Context context) {
        Log.e("ErrorResponse", "consumeResponse: ", apiResponse.error);
        if (apiResponse.error instanceof ConnectException) {
            Toast.makeText(context, context.getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, context.getString(R.string.could_not_fetch_data), Toast.LENGTH_SHORT).show();
        }
    }

}

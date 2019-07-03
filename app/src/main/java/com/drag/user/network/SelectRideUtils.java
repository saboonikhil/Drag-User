package com.drag.user.network;

import android.content.Context;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Response;

public class SelectRideUtils {

    public static SelectRideResponse parseError(Response<?> response, Context context) {

        Converter<ResponseBody, SelectRideResponse> converter = APIUtils.getRideErrorService(context);
        SelectRideResponse message;

        try {
            message = converter.convert(response.errorBody());
        } catch (IOException e) {
            return new SelectRideResponse();
        }

        return message;
    }
}

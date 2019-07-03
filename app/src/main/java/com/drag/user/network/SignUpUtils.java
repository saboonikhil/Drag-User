package com.drag.user.network;

import android.content.Context;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Response;

public class SignUpUtils {

    public static SignUpResponse parseError(Response<?> response, Context context) {

        Converter<ResponseBody, SignUpResponse> converter = APIUtils.getAPIErrorService(context);
        SignUpResponse message;

        try {
            message = converter.convert(response.errorBody());
        } catch (IOException e) {
            return new SignUpResponse();
        }

        return message;
    }
}

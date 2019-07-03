package com.drag.user.network;

import android.content.Context;

import java.lang.annotation.Annotation;

import okhttp3.ResponseBody;
import retrofit2.Converter;

public class APIUtils {

    public static EndPointInterface getAPIService(Context context) {
        return RetrofitClientInstance.getRetrofitInstance(context).create(EndPointInterface.class);
    }

    static Converter<ResponseBody, SignUpResponse> getAPIErrorService(Context context) {
        return RetrofitClientInstance.getRetrofitInstance(context).responseBodyConverter(SignUpResponse.class, new Annotation[0]);
    }

    static Converter<ResponseBody, SelectRideResponse> getRideErrorService(Context context) {
        return RetrofitClientInstance.getRetrofitInstance(context).responseBodyConverter(SelectRideResponse.class, new Annotation[0]);
    }
}
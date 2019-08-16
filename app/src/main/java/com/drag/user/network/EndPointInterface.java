package com.drag.user.network;

import com.drag.user.model.Cab;
import com.drag.user.model.Location;
import com.drag.user.model.Notification;
import com.drag.user.model.Paytm;
import com.drag.user.model.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface EndPointInterface {

    @GET("/locations")
    Call<Location[]> initLocation();

    @GET("/api/locations")
    Call<Location[]> authLocation(
            @Query("x_key") String key,
            @Query("token") String token
    );

    @POST("/signIn")
    @FormUrlEncoded
    Call<User> authSignIn(
            @Field("email") String email,
            @Field("password") String password,
            @Field("role") String role
    );

    @POST("/signUp")
    @FormUrlEncoded
    Call<User> createUser(
            @Field("name") String name,
            @Field("email") String email,
            @Field("contact") String contact,
            @Field("password") String password
    );

    @GET("/api/users/{uID}/trips")
    Call<Cab[]> userTrips(
            @Path("uID") String uID,
            @Query("x_key") String key,
            @Query("token") String token
    );

    @PUT("/api/users/{uID}")
    @FormUrlEncoded
    Call<User> userUpdate(
            @Path("uID") String uID,
            @Query("x_key") String key,
            @Query("token") String token,
            @Field("name") String name,
            @Field("email") String email,
            @Field("contact") String contact,
            @Field("alternateContact") String alternateContact
    );

    @PUT("/api/users/{uID}/updatePassword")
    @FormUrlEncoded
    Call<User> updatePassword(
            @Path("uID") String uID,
            @Query("x_key") String key,
            @Query("token") String token,
            @Field("password") String password
    );

    /*@GET("/api/rides")
    Call<List<Cab>> userRideList(
            @Query("x_key") String key,
            @Query("token") String token,
            @Query("city") String city,
            @Query("pickup") String pickup,
            @Query("drop") String drop,
            @Query("seats") String seats,
            @Query("startTime") String startTime
    );*/

    /*@POST("/api/rides")
    @FormUrlEncoded
    Call<User> requestRide(
            @Query("x_key") String key,
            @Query("token") String token,
            @Field("city") String city,
            @Field("pickup") String pickup,
            @Field("drop") String drop,
            @Field("startTime") String startTime,
            @Field("seats") String seats
    );

    @PUT("/api/users/{uID}/joinRide")
    @FormUrlEncoded
    Call<Cab> userJoinRide(
            @Path("uID") String uID,
            @Query("x_key") String key,
            @Query("token") String token,
            @Field("ride") String cID,
            @Field("seats") String seats
    );*/

    @GET("/api/cabs")
    Call<Cab[]> cabFareList(
            @Query("x_key") String key,
            @Query("token") String token,
            @Query("pickup") String pickup,
            @Query("drop") String drop,
            @Query("startTime") String startTime
    );

    @POST("/api/users/{uID}/generateChecksum")
    @FormUrlEncoded
    Call<Paytm> generateChecksum(
            @Path("uID") String uID,
            @Query("x_key") String key,
            @Query("token") String token,
            @Field("cabTypeSelected") String cID
    );

    @POST("/api/users/{uID}/createTrip")
    @FormUrlEncoded
    Call<Cab> createTrip(
            @Path("uID") String uID,
            @Query("x_key") String key,
            @Query("token") String token,
            @Field("cabTypeSelected") String cID,
            @Field("pickup") String pickup,
            @Field("drop") String drop,
            @Field("startTime") String startTime,
            @Field("orderId") String oID
    );

    @GET("/api/notifications")
    Call<List<Notification>> notificationList(
            @Query("x_key") String key,
            @Query("token") String token
    );

    @PUT("/api/users/{uID}/sendFeedback")
    @FormUrlEncoded
    Call<User> sendFeedback(
            @Path("uID") String uID,
            @Query("x_key") String key,
            @Query("token") String token,
            @Field("message") String message
    );
}
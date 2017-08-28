package jdroidcoder.ua.taxi_bishkek_driver.network;

import java.util.List;

import jdroidcoder.ua.taxi_bishkek_driver.model.OrderDto;
import jdroidcoder.ua.taxi_bishkek_driver.model.UserCoordinateDto;
import jdroidcoder.ua.taxi_bishkek_driver.model.UserProfileDto;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by jdroidcoder on 07.04.17.
 */
public interface ApiNetwork {
    @POST("register")
    @FormUrlEncoded
    Call<Boolean> register(@Field("email") String email, @Field("password") String password);

    @POST("login")
    @FormUrlEncoded
    Call<UserProfileDto> login(@Field("email") String email, @Field("password") String password);

    @POST("setDataToProfile")
    @FormUrlEncoded
    Call<UserProfileDto> setDataToProfile(@Field("email") String email,
                                          @Field("firstName") String firstName,
                                          @Field("lastName") String lastName,
                                          @Field("phone") String phone);

    @POST("acceptOrder")
    @FormUrlEncoded
    Call<OrderDto> acceptOrder(
            @Field("id") Long id,
            @Field("pointA") String pointA,
            @Field("pointB") String pointB,
            @Field("userPhone") String userPhone,
            @Field("status") String status,
            @Field("driverPhone") String driverPhone,
            @Field("acceptDate") Long acceptDate);

//    @GET("getOrders")
//    Call<List<OrderDto>> getOrders(@Query("userPhone") String userPhone);

    @GET("getAllOrders/{driverPhone}")
    Call<List<OrderDto>> getOrders(@Path("driverPhone") String driverPhone);

    @GET("getAcceptOrders")
    Call<List<OrderDto>> getAllAcceptOrders(@Query("driverPhone") String driverPhone);

    @GET("deleteOrder")
    Call<Boolean> removeOrder(@Query("id") Long id);

    @POST("getUserCoordinate")
    @FormUrlEncoded
    Call<UserCoordinateDto> getUserCoordinate(@Field("userPhone") String email);

    @POST("removeAcceptedOrder")
    @FormUrlEncoded
    Call<OrderDto> removeAcceptedOrder(@Field("id") Long id, @Field("driverPhone") String driverPhone);

    @GET("editBalance")
    Call<Void> editBalance(@Query("userEmail") String userEmail,
                           @Query("balance") int balance);

    @POST("startCall")
    @FormUrlEncoded
    Call<Void> startCall(@Field("id") Long orderId);

    @POST("getProfile")
    @FormUrlEncoded
    Call<UserProfileDto> getProfile(@Field("email") String email);

    @POST("uploadCheck")
    Call<Void> uploadCheck(@Body RequestBody file);

    @FormUrlEncoded
    @POST("addComplaint")
    Call<Void> addComplaint (@Field("driverPhone") String driverPhone, @Field("userPhone") String userPhone);

    @FormUrlEncoded
    @POST("setCoordinate")
    Call<Void> setCoordinate(@Field("userPhone") String userPhone,
                             @Field("lat") Double lat,
                             @Field("lng") Double lng);
}

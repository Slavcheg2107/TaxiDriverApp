package jdroidcoder.ua.taxi_bishkek_driver.network;

import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import jdroidcoder.ua.taxi_bishkek_driver.R;
import jdroidcoder.ua.taxi_bishkek_driver.activity.LoginActivity;
import jdroidcoder.ua.taxi_bishkek_driver.activity.OrdersActivity;
import jdroidcoder.ua.taxi_bishkek_driver.events.ConnectionErrorEvent;
import jdroidcoder.ua.taxi_bishkek_driver.events.ErrorMessageEvent;
import jdroidcoder.ua.taxi_bishkek_driver.events.MoveNextEvent;
import jdroidcoder.ua.taxi_bishkek_driver.events.ShowMapEvent;
import jdroidcoder.ua.taxi_bishkek_driver.events.SimChangedEvent;
import jdroidcoder.ua.taxi_bishkek_driver.events.TypePhoneEvent;
import jdroidcoder.ua.taxi_bishkek_driver.events.UpdateAdapterEvent;
import jdroidcoder.ua.taxi_bishkek_driver.events.UpdateNotificationEvent;
import jdroidcoder.ua.taxi_bishkek_driver.events.UserCoordinateNullEvent;
import jdroidcoder.ua.taxi_bishkek_driver.model.OrderDto;
import jdroidcoder.ua.taxi_bishkek_driver.model.UserCoordinateDto;
import jdroidcoder.ua.taxi_bishkek_driver.model.UserProfileDto;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by jdroidcoder on 07.04.17.
 */
public class NetworkService {
    private RetrofitConfig retrofitConfig;

    public NetworkService() {
        retrofitConfig = new RetrofitConfig();
    }

    public void register(final String login, final String password) {
        Call<Boolean> call = retrofitConfig.getApiNetwork().register(login, password);
        call.enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.body()) {
                    EventBus.getDefault().post(new TypePhoneEvent());
                    }
                 else {
                    if (!LoginActivity.settings.getString(LoginActivity.APP_PREFERENCES_SIMID, "").equals(LoginActivity.m.getSimSerialNumber())) {
                        if(LoginActivity.count == 1){
                        LoginActivity.prevGEmail = login;}
                        EventBus.getDefault().post(new SimChangedEvent());
                    } else {
                        login(login, password);
                    }
                }
            }


            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                EventBus.getDefault().post(new ConnectionErrorEvent(true));
            }
        });
    }

    public void setDataToProfile(String email, String firstName, String lastName, String phone) {
        Call<UserProfileDto> call = retrofitConfig.getApiNetwork().setDataToProfile(email, firstName, lastName, phone);
        call.enqueue(new Callback<UserProfileDto>() {
            @Override
            public void onResponse(Call<UserProfileDto> call, Response<UserProfileDto> response) {
                UserProfileDto.User.setPhone(response.body().getPhone());
                UserProfileDto.User.setFirstName(response.body().getFirstName());
                UserProfileDto.User.setLastName(response.body().getLastName());
                UserProfileDto.User.setEmail(response.body().getEmail());
                UserProfileDto.User.setBalance(response.body().getBalance());
                EventBus.getDefault().post(new MoveNextEvent());
            }

            @Override
            public void onFailure(Call<UserProfileDto> call, Throwable t) {
                EventBus.getDefault().post(new ErrorMessageEvent(String.valueOf(R.string.new_phone_number)));
            }
        });
    }

    public void login(final String login, final String password) {
        Call<UserProfileDto> call = retrofitConfig.getApiNetwork().login(login, password);
        call.enqueue(new Callback<UserProfileDto>() {
            @Override
            public void onResponse(Call<UserProfileDto> call, Response<UserProfileDto> response) {
                try {
                    UserProfileDto.User.setPhone(response.body().getPhone());
                    UserProfileDto.User.setFirstName(response.body().getFirstName());
                    UserProfileDto.User.setLastName(response.body().getLastName());
                    UserProfileDto.User.setEmail(response.body().getEmail());
                    UserProfileDto.User.setBalance(response.body().getBalance());
                    EventBus.getDefault().post(new MoveNextEvent());
                } catch (Exception e) {
                    Log.e("TAG", "Login failed");
                    EventBus.getDefault().post(new ErrorMessageEvent("Your phone used"));
                }
            }

            @Override
            public void onFailure(Call<UserProfileDto> call, Throwable t) {
                EventBus.getDefault().post(new ConnectionErrorEvent(true));
            }
        });
    }

    public void getOrders() {
        Call<List<OrderDto>> call = retrofitConfig.getApiNetwork().getOrders(UserProfileDto.User.getPhone());
        call.enqueue(new Callback<List<OrderDto>>() {
            @Override
            public void onResponse(Call<List<OrderDto>> call, Response<List<OrderDto>> response) {
                try {
                    if (OrdersActivity.myLocation != null) {
                        for (int i = 0; i < response.body().size(); i++) {
                            if(response.body().get(i).getPointACoordinate() != null){
                            response.body().get(i).setDistance(gps2m(OrdersActivity.myLocation.getLatitude(),
                                    OrdersActivity.myLocation.getLongitude(),
                                    response.body().get(i).getPointACoordinate()[0],
                                    response.body().get(i).getPointACoordinate()[1]));
//                                if(response.body().get(i).getDistance()>20000) response.body().remove(i);
                            }else{
                                response.body().get(i).setDistance(null);
                            }
                        }
                        Collections.sort(response.body(), new Comparator<OrderDto>() {
                            @Override
                            public int compare(OrderDto o1, OrderDto o2) {
                                if (o1.getDistance() == o2.getDistance()) {
                                    return 0;
                                }
                                if (o1.getDistance() == null||o1.getCanceled()) {
                                    return 1;
                                }
                                if (o2.getDistance() == null||o2.getCanceled() ) {
                                    return -1;
                                }
//                                if (o1.getCanceled()){
//                                    return -1;
//                                }
//                                if(o2.getCanceled()){
//                                    return -1;
//                                }
                                return o1.getDistance().compareTo(o2.getDistance());
                            }
                        });
                    }
                    OrderDto.Oreders.setItems(response.body());
                    EventBus.getDefault().post(new UpdateAdapterEvent());
                    EventBus.getDefault().post(new UpdateNotificationEvent());
                    EventBus.getDefault().post(new ConnectionErrorEvent(false));
                } catch (Exception e) {
                    Log.e("SetItems", "set Items fail");
                }
            }

            @Override
            public void onFailure(Call<List<OrderDto>> call, Throwable t) {
                EventBus.getDefault().post(new ConnectionErrorEvent(true));
            }
        });
    }

    private int gps2m(double lat_a, double lng_a, double lat_b, double lng_b) {
        double pk = (180 / 3.14169);

        double a1 = lat_a / pk;
        double a2 = lng_a / pk;
        double b1 = lat_b / pk;
        double b2 = lng_b / pk;

        double t1 = Math.cos(a1) * Math.cos(a2) * Math.cos(b1) * Math.cos(b2);
        double t2 = Math.cos(a1) * Math.sin(a2) * Math.cos(b1) * Math.sin(b2);
        double t3 = Math.sin(a1) * Math.sin(b1);
        double tt = Math.acos(t1 + t2 + t3);

        return (int) (6366000 * tt);
    }

    public void getAllAcceptOrders(String driverPhone) {
        Call<List<OrderDto>> call = retrofitConfig.getApiNetwork().getAllAcceptOrders(driverPhone);
        call.enqueue(new Callback<List<OrderDto>>() {
            @Override
            public void onResponse(Call<List<OrderDto>> call, Response<List<OrderDto>> response) {
                try {
                    for (int i = 0; i < response.body().size(); i++) {
                        response.body().get(i).setDistance(gps2m(OrdersActivity.myLocation.getLatitude(),
                                OrdersActivity.myLocation.getLongitude(),
                                response.body().get(i).getPointACoordinate()[0],
                                response.body().get(i).getPointACoordinate()[1]));
                    }
                } catch (Exception e) {

                }
                OrderDto.AcceptOreders.setItems(response.body());
                EventBus.getDefault().post(new UpdateAdapterEvent());
                EventBus.getDefault().post(new ConnectionErrorEvent(false));
            }

            @Override
            public void onFailure(Call<List<OrderDto>> call, Throwable t) {
                EventBus.getDefault().post(new ConnectionErrorEvent(true));
            }
        });
    }

    public void acceptOrder(Long id, String pointA, String pointB, String userPhone) {
        Call<OrderDto> call = retrofitConfig.getApiNetwork().acceptOrder(id, pointA, pointB,
                userPhone, "accepted", UserProfileDto.User.getPhone(), new Date().getTime());
        call.enqueue(new Callback<OrderDto>() {
            @Override
            public void onResponse(Call<OrderDto> call, Response<OrderDto> response) {
                try {
                    if(response.code()==400){
                        EventBus.getDefault().post(new ErrorMessageEvent("Вы взяли максимальное число заказов"));
                    }
                    else if (response.message().equals("Insufficient balance")){
                        EventBus.getDefault().post(new ErrorMessageEvent("У вас нулевой баланс"));
                    EventBus.getDefault().post(new UpdateNotificationEvent());
                    OrderDto.AcceptOreders.add(response.body());
                    }
                } catch (Exception e) {
                    EventBus.getDefault().post(new ErrorMessageEvent("Order is done"));
                }
            }

            @Override
            public void onFailure(Call<OrderDto> call, Throwable t) {
                EventBus.getDefault().post(new ConnectionErrorEvent(true));
            }
        });
    }

    public void removeOrder(final OrderDto orderDto) {
        Call<Boolean> call = retrofitConfig.getApiNetwork().removeOrder(orderDto.getId());
        call.enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.body()) {
                    OrderDto.Oreders.getOrders().remove(orderDto);
                    EventBus.getDefault().post(new UpdateAdapterEvent());
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                EventBus.getDefault().post(new ConnectionErrorEvent(true));
            }
        });
    }

    public void getUserCoordinate(String userEmail) {
        Call<UserCoordinateDto> call = retrofitConfig.getApiNetwork().getUserCoordinate(userEmail);
        call.enqueue(new Callback<UserCoordinateDto>() {
            @Override
            public void onResponse(Call<UserCoordinateDto> call, Response<UserCoordinateDto> response) {
                try {
                    EventBus.getDefault().post(new ShowMapEvent(response.body().getLat(), response.body().getLng()));
                } catch (Exception e) {
                    EventBus.getDefault().post(new ShowMapEvent());
                }
            }

            @Override
            public void onFailure(Call<UserCoordinateDto> call, Throwable t) {
                EventBus.getDefault().post(new UserCoordinateNullEvent());
            }
        });
    }

    public void editBalance(int balance) {
        Call<Void> call = retrofitConfig.getApiNetwork().editBalance(UserProfileDto.User.getEmail(), balance);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                getProfile(UserProfileDto.User.getEmail());
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                EventBus.getDefault().post(new ConnectionErrorEvent(true));
            }
        });
    }

    public void removeAcceptedOrder(long id, String driverPhone) {
        Call<OrderDto> call = retrofitConfig.getApiNetwork().removeAcceptedOrder(id, driverPhone);
        call.enqueue(new Callback<OrderDto>() {
            @Override
            public void onResponse(Call<OrderDto> call, Response<OrderDto> response) {
                if(response.code()!=400){
                try {
                    OrderDto.Oreders.add(response.body());
                    OrderDto.AcceptOreders.getOrders().remove(response.body());
                    EventBus.getDefault().post(new UpdateNotificationEvent());
                } catch (Exception e) {
                    EventBus.getDefault().post(new ErrorMessageEvent("Error while removing accepted order"));
                }
                }
                else EventBus.getDefault().post(new ErrorMessageEvent("Заказ уже нельзя отменить"));
            }

            @Override
            public void onFailure(Call<OrderDto> call, Throwable t) {
                EventBus.getDefault().post(new ConnectionErrorEvent(true));
            }
        });
    }

    public void startCall(Long orderId){
        Call<Void> call = retrofitConfig.getApiNetwork().startCall(orderId);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Log.e("startCall", String.valueOf(response.code()));
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {

            }
        });
    }
    public void addComplaint(String driverPhone, String userPhone){
        Call<Void> call = retrofitConfig.getApiNetwork().addComplaint(driverPhone, userPhone);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if(response.code() == 400){
                    EventBus.getDefault().post(new ErrorMessageEvent("Вы уже отправляли жалобу на этого клиента"));
                }
                else if(response.errorBody().toString().equals ("Driver can't complaint, before call")){
                    EventBus.getDefault().post(new ErrorMessageEvent("Вы не можете пожаловатся не позвонив клиенту"));
                }
                else if(response.code() == 200) EventBus.getDefault().post(new ErrorMessageEvent("Жалоба принята, вам вернут единицы."));
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                try{
                Log.e("TAG", t.getMessage());
            }catch (Exception e){
            Log.e("AddComplaint", "Add complaint fail");}
            }
        });
    }

    public void getProfile(final String email) {
        Call<UserProfileDto> call = retrofitConfig.getApiNetwork().getProfile(email);
        call.enqueue(new Callback<UserProfileDto>() {
            @Override
            public void onResponse(Call<UserProfileDto> call, Response<UserProfileDto> response) {
                   UserProfileDto.User.setPhone(response.body().getPhone());
                   UserProfileDto.User.setFirstName(response.body().getFirstName());
                   UserProfileDto.User.setLastName(response.body().getLastName());
                   UserProfileDto.User.setEmail(response.body().getEmail());
                   UserProfileDto.User.setBalance(response.body().getBalance());
                   EventBus.getDefault().post(new MoveNextEvent());
            }

            @Override
            public void onFailure(Call<UserProfileDto> call, Throwable t) {
                EventBus.getDefault().post(new ConnectionErrorEvent(true));
            }
        });
    }

    public void uploadCheck(RequestBody requestBody) {
        Call<Void> call = retrofitConfig.getApiNetwork().uploadCheck(requestBody);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                EventBus.getDefault().post(new ErrorMessageEvent("uploaded"));
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                EventBus.getDefault().post(new ConnectionErrorEvent(true));
            }
        });
    }

    public void setCoordinate(Double lat, Double lng) {
        Call<Void> call = retrofitConfig.getApiNetwork().setCoordinate(UserProfileDto.User.getPhone(), lat, lng);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                EventBus.getDefault().post(new ConnectionErrorEvent(false));
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                EventBus.getDefault().post(new ConnectionErrorEvent(true));
            }
        });
    }
}
package jdroidcoder.ua.taxi_bishkek_driver.network;

import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import jdroidcoder.ua.taxi_bishkek_driver.activity.OrdersActivity;
import jdroidcoder.ua.taxi_bishkek_driver.events.ConnectionErrorEvent;
import jdroidcoder.ua.taxi_bishkek_driver.events.ErrorMessageEvent;
import jdroidcoder.ua.taxi_bishkek_driver.events.MoveNextEvent;
import jdroidcoder.ua.taxi_bishkek_driver.events.ShowMapEvent;
import jdroidcoder.ua.taxi_bishkek_driver.events.TypePhoneEvent;
import jdroidcoder.ua.taxi_bishkek_driver.events.UpdateAdapterEvent;
import jdroidcoder.ua.taxi_bishkek_driver.events.UpdateNotificationEvent;
import jdroidcoder.ua.taxi_bishkek_driver.events.UserCoordinateNullEvent;
import jdroidcoder.ua.taxi_bishkek_driver.model.OrderDto;
import jdroidcoder.ua.taxi_bishkek_driver.model.UserCoordinateDto;
import jdroidcoder.ua.taxi_bishkek_driver.model.UserProfileDto;
import jdroidcoder.ua.taxi_bishkek_driver.utils.Settings;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NetworkService {
    private RetrofitConfig retrofitConfig;
    private String password1;
    boolean registered;
    public NetworkService() {
        retrofitConfig = new RetrofitConfig();
    }

    public void register(final String phone, final String password) {
        Call<Boolean> call = retrofitConfig.getApiNetwork().register(phone, password);
        call.enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                password1 = password;
                String error;
                if (!response.isSuccessful()) {
                    try {
                        error = response.errorBody().string();
                        if (error.equals("User with this phone already exists")) {
                            setDataToProfile(phone);
                        } else if (error.equals("User with this IP have more that 4 accounts")) {
                            EventBus.getDefault().post(new ErrorMessageEvent("Вы имеете максимальное число аккаунтов"));
                            EventBus.getDefault().post(new TypePhoneEvent());
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    setDataToProfile(phone);
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                EventBus.getDefault().post(new ConnectionErrorEvent(true));
            }
        });
    }

    public void setDataToProfile(final String phone) {
        Call<UserProfileDto> call = retrofitConfig.getApiNetwork().setDataToProfile(phone);
        call.enqueue(new Callback<UserProfileDto>() {
            @Override
            public void onResponse(Call<UserProfileDto> call, Response<UserProfileDto> response) {
                try {
                    if (response.isSuccessful()) {
                        Settings.currentUser.setPhone(response.body().getPhone());
                        login(phone, password1);
                    }
                } catch (Exception e) {
                    EventBus.getDefault().post(new ErrorMessageEvent(e.getMessage()));
                }
            }

            @Override
            public void onFailure(Call<UserProfileDto> call, Throwable t) {
                EventBus.getDefault().post(new ErrorMessageEvent("Неизвестная ошибка"));
            }
        });
    }

    public void login(final String phone, final String password) {
        Call<Void> call = retrofitConfig.getApiNetwork().login(phone, password);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                try {
                    if (response.isSuccessful()) {
                        Settings.currentUser.setPhone(phone);
                        Settings.currentUser.setPassword(password);
                        EventBus.getDefault().post(new MoveNextEvent());
                    } else {
                        String error = response.errorBody().string();
                        if (error.equals("Login failed")) {
                            EventBus.getDefault().post(new ErrorMessageEvent("Неверный пароль или номер"));
                            EventBus.getDefault().post(new TypePhoneEvent());
                        }

                    }
                } catch (Exception e) {
                    Log.e("TAG", e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("login", t.getMessage());
                EventBus.getDefault().post(new ConnectionErrorEvent(true));
            }
        });
    }

    public void getOrders() {
        Call<List<OrderDto>> call = retrofitConfig.getApiNetwork().getOrders(Settings.currentUser.getPhone());
        call.enqueue(new Callback<List<OrderDto>>() {
            @Override
            public void onResponse(Call<List<OrderDto>> call, Response<List<OrderDto>> response) {
                try {
                    if (OrdersActivity.myLocation != null) {
                        for (int i = 0; i < response.body().size(); i++) {
                            if (response.body().get(i).getPointACoordinate() != null) {
                                response.body().get(i).setDistance(gps2m(OrdersActivity.myLocation.getLatitude(),
                                        OrdersActivity.myLocation.getLongitude(),
                                        response.body().get(i).getPointACoordinate()[0],
                                        response.body().get(i).getPointACoordinate()[1]));
//                                if(response.body().get(i).getDistance()>20000) response.body().remove(i);
                            } else {
                                response.body().get(i).setDistance(null);
                            }
                        }
                        Collections.sort(response.body(), new Comparator<OrderDto>() {
                            @Override
                            public int compare(OrderDto o1, OrderDto o2) {
                                if (o1.getDistance() == o2.getDistance()) {
                                    return 0;
                                }
                                if (o1.getDistance() == null) {
                                    return 1;
                                }
                                if (o2.getDistance() == null) {
                                    return -1;
                                }
                                return o1.getDistance().compareTo(o2.getDistance());
                            }
                        });
                        Collections.sort(response.body(), new Comparator<OrderDto>() {
                            @Override
                            public int compare(OrderDto o, OrderDto o0) {
                                return o.getCanceled().compareTo(o0.getCanceled());
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
                userPhone, "accepted", Settings.currentUser.getPhone(), new Date().getTime());
        call.enqueue(new Callback<OrderDto>() {
            @Override
            public void onResponse(Call<OrderDto> call, Response<OrderDto> response) {
                if (!response.isSuccessful()) {
                    if (response.code() == 400 && response.errorBody() != null) {
                        EventBus.getDefault().post(new ErrorMessageEvent("Пополните баланс"));
                    }
                    if (response.code() == 400 && response.errorBody() == null) {
                        EventBus.getDefault().post(new ErrorMessageEvent("Вы взяли слишком много заказов"));
                    }
                } else {
                    OrderDto.AcceptOreders.add(response.body());
                    EventBus.getDefault().post(new UpdateNotificationEvent());
                }
                EventBus.getDefault().post(new UpdateNotificationEvent());
            }


            @Override
            public void onFailure(Call<OrderDto> call, Throwable t) {
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

//    public void editBalance(int balance) {
//        Call<Void> call = retrofitConfig.getApiNetwork().editBalance(Settings.currentUser.getEmail(), balance);
//        call.enqueue(new Callback<Void>() {
//            @Override
//            public void onResponse(Call<Void> call, Response<Void> response) {
//                getProfile(Settings.currentUser.getEmail());
//            }
//
//            @Override
//            public void onFailure(Call<Void> call, Throwable t) {
//                EventBus.getDefault().post(new ConnectionErrorEvent(true));
//            }
//        });
//    }

    public void removeAcceptedOrder(long id, String driverPhone) {
        Call<OrderDto> call = retrofitConfig.getApiNetwork().removeAcceptedOrder(id, driverPhone);
        call.enqueue(new Callback<OrderDto>() {
            @Override
            public void onResponse(Call<OrderDto> call, Response<OrderDto> response) {
                if (response.isSuccessful()) {
                    try {
                        OrderDto.Oreders.add(response.body());
                        OrderDto.AcceptOreders.getOrders().remove(response.body());
                        EventBus.getDefault().post(new UpdateNotificationEvent());
                    } catch (Exception e) {
                        EventBus.getDefault().post(new ErrorMessageEvent("Error while removing accepted order"));
                    }
                } else
                    EventBus.getDefault().post(new ErrorMessageEvent("Жалоба принята, вам вернут единицы"));
            }

            @Override
            public void onFailure(Call<OrderDto> call, Throwable t) {
                EventBus.getDefault().post(new ConnectionErrorEvent(true));
            }
        });
    }

    public void startCall(Long orderId) {
        Call<Void> call = retrofitConfig.getApiNetwork().startCall(orderId);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
//                try {
////                if(response.isSuccessful()){
//////                    EventBus.getDefault().post(new CallEvent());
////                }
////                else {
////                    String error  = response.errorBody().string();
////                    if(error.equals("Insufficient balance")){
////                        EventBus.getDefault().post(new ErrorMessageEvent("Недостаточно баланса"));
////                    }
////                }
//
//                Log.e("startCall", String.valueOf(response.code()));
//            }catch (IOException e) {
//                    e.printStackTrace();
//                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {

            }
        });
    }

    public void addComplaint(String driverPhone, String userPhone) {
        Call<Void> call = retrofitConfig.getApiNetwork().addComplaint(driverPhone, userPhone);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                try {
                    if (!response.isSuccessful()) {
                        String error = response.errorBody().string();
                        switch (error) {
                            case "Driver already complained this user":
                                EventBus.getDefault().post(new ErrorMessageEvent("Вы уже отправляли жалобу на этого клиента"));
                                break;
                            case "Driver can't complaint, before call":
                                EventBus.getDefault().post(new ErrorMessageEvent("Вы не можете пожаловатся не позвонив клиенту"));
                                break;
                            case "Order not found":
                                EventBus.getDefault().post(new ErrorMessageEvent("Этого заказа уже нет"));
                                break;

                        }
                    } else if (response.isSuccessful()) {
                        EventBus.getDefault().post(new ErrorMessageEvent("Ваша жалоба принята"));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                try {
                    Log.e("TAG", t.getMessage());
                } catch (Exception e) {
                    Log.e("AddComplaint", "Add complaint fail");
                }
            }
        });
    }

    public void getProfile(final String userPhone) {
        Call<UserProfileDto> call = retrofitConfig.getApiNetwork().getProfile(userPhone);
        call.enqueue(new Callback<UserProfileDto>() {
            @Override
            public void onResponse(Call<UserProfileDto> call, Response<UserProfileDto> response) {
                if (response.isSuccessful()) {
                    Settings.currentUser.setPhone(response.body().getPhone());
                    Settings.currentUser.setFirstName(response.body().getFirstName());
                    Settings.currentUser.setLastName(response.body().getLastName());
                    Settings.currentUser.setEmail(response.body().getEmail());
                    Settings.currentUser.setBalance(response.body().getBalance());
//                    if(Settings.currentUser.getEmail()==null) {
//                        EventBus.getDefault().post(new MoveNextEvent());
//                    }
                }
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
        Call<Void> call = retrofitConfig.getApiNetwork().setCoordinate(Settings.currentUser.getPhone(), lat, lng);
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
package jdroidcoder.ua.taxi_bishkek.network;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import jdroidcoder.ua.taxi_bishkek.events.ErrorMessageEvent;
import jdroidcoder.ua.taxi_bishkek.events.MoveNextEvent;
import jdroidcoder.ua.taxi_bishkek.events.ShowMapEvent;
import jdroidcoder.ua.taxi_bishkek.events.TypePhoneEvent;
import jdroidcoder.ua.taxi_bishkek.events.UpdateAdapterEvent;
import jdroidcoder.ua.taxi_bishkek.model.OrderDto;
import jdroidcoder.ua.taxi_bishkek.model.UserCoordinateDto;
import jdroidcoder.ua.taxi_bishkek.model.UserProfileDto;
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
                } else {
                    login(login, password);
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                EventBus.getDefault().post(new ErrorMessageEvent(t.getMessage()));
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
                EventBus.getDefault().post(new MoveNextEvent());
            }

            @Override
            public void onFailure(Call<UserProfileDto> call, Throwable t) {
                EventBus.getDefault().post(new ErrorMessageEvent(t.getMessage()));
            }
        });
    }

    public void login(final String login, final String password) {
        Call<UserProfileDto> call = retrofitConfig.getApiNetwork().login(login, password);
        call.enqueue(new Callback<UserProfileDto>() {
            @Override
            public void onResponse(Call<UserProfileDto> call, Response<UserProfileDto> response) {
                UserProfileDto.User.setPhone(response.body().getPhone());
                UserProfileDto.User.setFirstName(response.body().getFirstName());
                UserProfileDto.User.setLastName(response.body().getLastName());
                EventBus.getDefault().post(new MoveNextEvent());
            }

            @Override
            public void onFailure(Call<UserProfileDto> call, Throwable t) {
                EventBus.getDefault().post(new ErrorMessageEvent(t.getMessage()));
            }
        });
    }

    public void getOrders() {
        Call<List<OrderDto>> call = retrofitConfig.getApiNetwork().getOrders();
        call.enqueue(new Callback<List<OrderDto>>() {
            @Override
            public void onResponse(Call<List<OrderDto>> call, Response<List<OrderDto>> response) {
                OrderDto.Oreders.setItems(response.body());
            }

            @Override
            public void onFailure(Call<List<OrderDto>> call, Throwable t) {
                EventBus.getDefault().post(new ErrorMessageEvent(t.getMessage()));
            }
        });
    }

    public void getAllAcceptOrders(String driverPhone) {
        Call<List<OrderDto>> call = retrofitConfig.getApiNetwork().getAllAcceptOrders(driverPhone);
        call.enqueue(new Callback<List<OrderDto>>() {
            @Override
            public void onResponse(Call<List<OrderDto>> call, Response<List<OrderDto>> response) {
                OrderDto.AcceptOreders.setItems(response.body());
            }

            @Override
            public void onFailure(Call<List<OrderDto>> call, Throwable t) {
                EventBus.getDefault().post(new ErrorMessageEvent(t.getMessage()));
            }
        });
    }

    public void acceptOrder(Long id, String pointA, String pointB, String userPhone) {
        Call<OrderDto> call = retrofitConfig.getApiNetwork().acceptOrder(id, pointA, pointB,
                userPhone, "accepted", UserProfileDto.User.getPhone());
        call.enqueue(new Callback<OrderDto>() {
            @Override
            public void onResponse(Call<OrderDto> call, Response<OrderDto> response) {
                OrderDto.AcceptOreders.add(response.body());
            }

            @Override
            public void onFailure(Call<OrderDto> call, Throwable t) {
                EventBus.getDefault().post(new ErrorMessageEvent(t.getMessage()));
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
                EventBus.getDefault().post(new ErrorMessageEvent(t.getMessage()));
            }
        });
    }

    public void getUserCoordinate(String userEmail) {
        Call<UserCoordinateDto> call = retrofitConfig.getApiNetwork().getUserCoordinate(userEmail);
        call.enqueue(new Callback<UserCoordinateDto>() {
            @Override
            public void onResponse(Call<UserCoordinateDto> call, Response<UserCoordinateDto> response) {
                EventBus.getDefault().post(new ShowMapEvent(response.body().getLat(),response.body().getLng()));
            }

            @Override
            public void onFailure(Call<UserCoordinateDto> call, Throwable t) {
                EventBus.getDefault().post(new ErrorMessageEvent(t.getMessage()));
            }
        });
    }
}

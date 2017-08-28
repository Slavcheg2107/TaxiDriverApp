package jdroidcoder.ua.taxi_bishkek_driver.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import jdroidcoder.ua.taxi_bishkek_driver.R;
import jdroidcoder.ua.taxi_bishkek_driver.activity.MapsActivity;
import jdroidcoder.ua.taxi_bishkek_driver.activity.PayActivity;
import jdroidcoder.ua.taxi_bishkek_driver.adapters.OrderAdapter;
import jdroidcoder.ua.taxi_bishkek_driver.events.ChangeListViewEvent;
import jdroidcoder.ua.taxi_bishkek_driver.events.ConnectionErrorEvent;
import jdroidcoder.ua.taxi_bishkek_driver.events.ErrorMessageEvent;
import jdroidcoder.ua.taxi_bishkek_driver.events.ShowMapEvent;
import jdroidcoder.ua.taxi_bishkek_driver.events.UpdateAdapterEvent;
import jdroidcoder.ua.taxi_bishkek_driver.model.OrderDto;
import jdroidcoder.ua.taxi_bishkek_driver.model.UserProfileDto;
import jdroidcoder.ua.taxi_bishkek_driver.network.NetworkService;

/**
 * Created by jdroidcoder on 11.04.17.
 */
public class OrderFragment extends Fragment implements AdapterView.OnItemClickListener {
    @BindView(R.id.orderListView)
    ListView orderListView;
    private OrderAdapter orderAdapter;
    private Unbinder unbinder;
    private boolean isOrders = false;
    private NetworkService networkService;
    public static boolean isShowMap = false;
    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    private View view;
    private Snackbar snackbarForUpdate;

    private Snackbar snackbarForConnection;
    private boolean isShowSnackbar = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.order_list_view, container, false);
        unbinder = ButterKnife.bind(this, view);
        orderAdapter = new OrderAdapter(getActivity());
        orderListView.setAdapter(orderAdapter);
        orderListView.setOnItemClickListener(this);
        EventBus.getDefault().register(this);
        networkService = new NetworkService();
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                networkService.getProfile(UserProfileDto.User.getEmail());
                networkService.getOrders();
                networkService.getAllAcceptOrders(UserProfileDto.User.getPhone());
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        snackbarForUpdate = Snackbar.make(view, "Для обновления списка потяните вниз", Snackbar.LENGTH_INDEFINITE);

        snackbarForConnection = Snackbar.make(view, "Подключите интернет", Snackbar.LENGTH_INDEFINITE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onDestroyView() {
        for(int i = 0; i<OrderDto.AcceptOreders.getOrders().size(); i++){
            new NetworkService().removeAcceptedOrder(OrderDto.AcceptOreders.getOrders().get(i).getId(), UserProfileDto.User.getPhone());
        }
        unbinder.unbind();
        EventBus.getDefault().unregister(this);
        super.onDestroyView();
    }

    @Subscribe
    public void onChangeListViewEvent(ChangeListViewEvent changeListViewEvent) {
        isOrders = changeListViewEvent.isOrders();
        orderAdapter.setAccept(changeListViewEvent.isOrders());
        orderAdapter.orderDtos = changeListViewEvent.isOrders() ? OrderDto.AcceptOreders.getOrders() :
                OrderDto.Oreders.getOrders();
        orderAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        OrderDto orderDto;
        if (!isOrders) {
            if (OrderDto.AcceptOreders.getOrders().size() < 4) {
                try {
                    if (checkBalance()) {
                        EventBus.getDefault().post(new ErrorMessageEvent("Пополните баланс"));
                        Intent i = new Intent(getContext(), PayActivity.class);
                        startActivity(i);
                        return;
                    }
                    orderDto = OrderDto.Oreders.getOrders().get(position);
                    if (orderDto.getUserPhone().equals(UserProfileDto.User.getPhone())) {
                        EventBus.getDefault().post(new ErrorMessageEvent("Вы пытаетесь взять заказ у самого себя"));
                        return;
                    }
                    networkService.acceptOrder(orderDto.getId(), orderDto.getPointA(), orderDto.getPointB(),
                            orderDto.getUserPhone());
                    OrderDto.Oreders.getOrders().remove(position);
                    getActivity().invalidateOptionsMenu();
                    orderAdapter.notifyDataSetChanged();
                } catch (Exception e) {
                    EventBus.getDefault().post(new ErrorMessageEvent("On item click error"));
                }
            } else {
                EventBus.getDefault().post(new ErrorMessageEvent("Сначала удалите какой-нибуть заказ"));
            }
        } else {
            try {
                orderDto = OrderDto.AcceptOreders.getOrders().get(position);
                networkService.getUserCoordinate(orderDto.getUserPhone());
            } catch (Exception e) {
                EventBus.getDefault().post(new ErrorMessageEvent(e.getMessage()));
            }
        }
    }

    private boolean checkBalance() {
        return UserProfileDto.User.getBalance() == 0;
    }

    @Subscribe
    public void onUpdateAdapterEvent(UpdateAdapterEvent updateAdapterEvent) {
        if (!isOrders) {
            orderAdapter.orderDtos = OrderDto.Oreders.getOrders();
        } else {
            orderAdapter.orderDtos = OrderDto.AcceptOreders.getOrders();
        }
        if (orderAdapter.orderDtos.isEmpty()) {
            if (!isShowSnackbar) {
                snackbarForUpdate.show();
                isShowSnackbar = true;
            }
        } else {
            isShowSnackbar = false;
            snackbarForUpdate.dismiss();
        }
        orderAdapter.notifyDataSetChanged();
        swipeRefreshLayout.setRefreshing(false);
        getActivity().invalidateOptionsMenu();
    }

    @Subscribe
    public void onShowMapEvent(ShowMapEvent showMapEvent) {
        if (!isShowMap) {
            startActivity(new Intent(getActivity(), MapsActivity.class)
                    .putExtra("userCoordinate", showMapEvent));
            isShowMap = true;
        }
    }

    @Subscribe
    public void onConnectionErrorEvent(ConnectionErrorEvent connectionErrorEvent) {
        if(connectionErrorEvent.isShow()){
            snackbarForConnection.show();
        }else {
            snackbarForConnection.dismiss();
        }
    }
}

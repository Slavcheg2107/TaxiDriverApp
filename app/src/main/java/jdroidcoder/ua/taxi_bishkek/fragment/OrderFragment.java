package jdroidcoder.ua.taxi_bishkek.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import jdroidcoder.ua.taxi_bishkek.R;
import jdroidcoder.ua.taxi_bishkek.activity.MapsActivity;
import jdroidcoder.ua.taxi_bishkek.adapters.OrderAdapter;
import jdroidcoder.ua.taxi_bishkek.events.ChangeListViewEvent;
import jdroidcoder.ua.taxi_bishkek.events.ErrorMessageEvent;
import jdroidcoder.ua.taxi_bishkek.events.ShowMapEvent;
import jdroidcoder.ua.taxi_bishkek.events.UpdateAdapterEvent;
import jdroidcoder.ua.taxi_bishkek.model.OrderDto;
import jdroidcoder.ua.taxi_bishkek.network.NetworkService;

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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.order_list_view, container, false);
        unbinder = ButterKnife.bind(this, view);
        orderAdapter = new OrderAdapter(getActivity());
        orderListView.setAdapter(orderAdapter);
        orderListView.setOnItemClickListener(this);
        EventBus.getDefault().register(this);
        networkService = new NetworkService();
        return view;
    }

    @Override
    public void onDestroyView() {
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
                orderDto = OrderDto.Oreders.getOrders().get(position);
                networkService.acceptOrder(orderDto.getId(), orderDto.getPointA(), orderDto.getPointB(),
                        orderDto.getUserPhone());
                OrderDto.Oreders.getOrders().remove(position);
                orderAdapter.notifyDataSetChanged();
            } else {
                EventBus.getDefault().post(new ErrorMessageEvent("U are have full orders"));
            }
        } else {
            orderDto = OrderDto.AcceptOreders.getOrders().get(position);
            networkService.getUserCoordinate(orderDto.getUserPhone());
        }
    }

    @Subscribe
    public void onUpdateAdapterEvent(UpdateAdapterEvent updateAdapterEvent) {
        if (!isOrders) {
            orderAdapter.orderDtos = OrderDto.Oreders.getOrders();
        }else {
            orderAdapter.orderDtos = OrderDto.AcceptOreders.getOrders();
        }
        orderAdapter.notifyDataSetChanged();
    }


    @Subscribe
    public void onShowMapEvent(ShowMapEvent showMapEvent) {
        if (!isShowMap) {
            startActivity(new Intent(getActivity(), MapsActivity.class)
                    .putExtra("userCoordinate", showMapEvent));
            isShowMap = true;
        }
    }
}

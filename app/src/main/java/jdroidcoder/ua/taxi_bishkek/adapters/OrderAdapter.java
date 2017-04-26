package jdroidcoder.ua.taxi_bishkek.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jdroidcoder.ua.taxi_bishkek.R;
import jdroidcoder.ua.taxi_bishkek.activity.OrdersActivity;
import jdroidcoder.ua.taxi_bishkek.events.UpdateAdapterEvent;
import jdroidcoder.ua.taxi_bishkek.model.OrderDto;
import jdroidcoder.ua.taxi_bishkek.model.UserProfileDto;
import jdroidcoder.ua.taxi_bishkek.network.NetworkService;

/**
 * Created by jdroidcoder on 10.04.17.
 */
public class OrderAdapter extends BaseAdapter {
    private Context context;
    private boolean isAccept = false;
    public List<OrderDto> orderDtos = new ArrayList<>();

    public OrderAdapter(Context context) {
        this.context = context;
        this.orderDtos = OrderDto.Oreders.getOrders();
    }

    @Override
    public int getCount() {
        return orderDtos.size();
    }

    @Override
    public Object getItem(int position) {
        return orderDtos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(context).inflate(R.layout.order_list_style, parent, false);
        final OrderDto orderDto = orderDtos.get(position);
        try {
            ((TextView) convertView.findViewById(R.id.addressTV)).setText(orderDto.getPoints());
            ((TextView) convertView.findViewById(R.id.whenTV)).setText(orderDto.getTime());
            if (OrdersActivity.myLocation == null) {
                (convertView.findViewById(R.id.distanceTV)).setVisibility(View.GONE);
            }
            if (String.valueOf(orderDto.getDistance()).equals("null")) {
                (convertView.findViewById(R.id.distanceTV)).setVisibility(View.GONE);
            } else {
                ((TextView) convertView.findViewById(R.id.distanceTV)).setText(String.valueOf(orderDto.getDistance()) + "m");
            }
            if (isAccept) {
                (convertView.findViewById(R.id.distanceTV)).setVisibility(View.GONE);
                convertView.findViewById(R.id.call).setVisibility(View.VISIBLE);
                convertView.findViewById(R.id.close).setVisibility(View.VISIBLE);
            }
            convertView.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (new Date().getTime() - orderDto.getAcceptDate().getTime() >= 600000) {
                        new NetworkService().removeOrder(orderDto);
                    } else {
                        new NetworkService().removeAcceptedOrder(orderDto.getId());
                        OrderDto.AcceptOreders.getOrders().remove(orderDto);
                        UserProfileDto.User.setBalance(UserProfileDto.User.getBalance() + 5);
                    }
                    EventBus.getDefault().post(new UpdateAdapterEvent());
                }
            });
            convertView.findViewById(R.id.call).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse("tel:" + Uri.encode(orderDto.getUserPhone().trim())));
                    callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(callIntent);
                }
            });
        } catch (Exception e) {

        }
        return convertView;
    }

    public void setAccept(boolean accept) {
        isAccept = accept;
    }
}

package jdroidcoder.ua.taxi_bishkek.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import jdroidcoder.ua.taxi_bishkek.R;
import jdroidcoder.ua.taxi_bishkek.model.OrderDto;
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
        ((TextView) convertView.findViewById(R.id.addressTV)).setText(orderDto.getPoints());
        ((TextView) convertView.findViewById(R.id.whenTV)).setText(orderDto.getTime());
        if (isAccept) {
            convertView.findViewById(R.id.call).setVisibility(View.VISIBLE);
//            convertView.findViewById(R.id.close).setVisibility(View.VISIBLE);
        }
        convertView.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new NetworkService().removeOrder(orderDto);
            }
        });
        convertView.findViewById(R.id.call).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:"+Uri.encode(orderDto.getUserPhone().trim())));
                callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(callIntent);
            }
        });
        return convertView;
    }

    public void setAccept(boolean accept) {
        isAccept = accept;
    }
}

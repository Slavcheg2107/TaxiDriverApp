package jdroidcoder.ua.taxi_bishkek.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudipsp.android.Order;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.OnClick;
import jdroidcoder.ua.taxi_bishkek.R;
import jdroidcoder.ua.taxi_bishkek.activity.OrdersActivity;
import jdroidcoder.ua.taxi_bishkek.events.ErrorMessageEvent;
import jdroidcoder.ua.taxi_bishkek.events.UpdateAdapterEvent;
import jdroidcoder.ua.taxi_bishkek.events.UserCoordinateNullEvent;
import jdroidcoder.ua.taxi_bishkek.model.OrderDto;
import jdroidcoder.ua.taxi_bishkek.model.UserProfileDto;
import jdroidcoder.ua.taxi_bishkek.network.ApiNetwork;
import jdroidcoder.ua.taxi_bishkek.network.NetworkService;

/**
 * Created by jdroidcoder on 10.04.17.
 */
public class OrderAdapter extends BaseAdapter {
    private Context context;
    private boolean isAccept = false;
    public List<OrderDto> orderDtos = new ArrayList<>();
    private boolean isCalled = false;
    NetworkService networkService = new NetworkService();
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(context).inflate(R.layout.order_list_style, parent, false);
        final OrderDto orderDto = orderDtos.get(position);
        if(!orderDtos.isEmpty()){
        try {
             final ImageView close = (ImageView) convertView.findViewById(R.id.close);
            ((TextView) convertView.findViewById(R.id.addressTV)).setText(orderDto.getPoints());
            ((TextView) convertView.findViewById(R.id.whenTV)).setText(orderDto.getTime());
            if (OrdersActivity.myLocation == null) {
                (convertView.findViewById(R.id.distanceTV)).setVisibility(View.GONE);
                Log.v("TAG", "distance == null");
            }
            if (String.valueOf(orderDto.getDistance()).equals("null")) {
                Log.v("TAG", "distance equal null");
                (convertView.findViewById(R.id.distanceTV)).setVisibility(View.GONE);
            } else {
                Log.v("TAG", "distance not null");
                ((TextView) convertView.findViewById(R.id.distanceTV)).setText(String.valueOf(orderDto.getDistance()) + "m");
            }
            if (isAccept) {
                ImageView report = (ImageView) convertView.findViewById(R.id.fake);
                Log.e("ORDERADAPTER", String.valueOf(new Date().getTime() - orderDto.getAcceptDate().getTime()));

                if(new Date().getTime() - orderDto.getAcceptDate().getTime() >= 20*1000){
                    report.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setNegativeButton("Нет",null);
                            builder.setPositiveButton("Да", null);
                            builder.setTitle("Клиент вас обманул?");
                            final AlertDialog alertDialog = builder.create();
                            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                @Override
                                public void onShow(DialogInterface dialog) {
                                    Button yes = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                                    yes.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            new NetworkService().removeOrder(orderDto);
                                            alertDialog.dismiss();
                                        }
                                    });
                                    Button no = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                                    no.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            alertDialog.dismiss();
                                        }
                                    });
                                }
                            });
                            alertDialog.show();
                        }
                    });
                }else{
                    final View finalConvertView1 = convertView;
                    report.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Toast.makeText(finalConvertView1.getContext(), "Еще не время жаловаться", Toast.LENGTH_LONG).show();
                        }
                    });
                }
                (convertView.findViewById(R.id.distanceTV)).setVisibility(View.GONE);
                convertView.findViewById(R.id.call).setVisibility(View.VISIBLE);
                convertView.findViewById(R.id.close).setVisibility(View.VISIBLE);
                convertView.findViewById(R.id.showMap).setVisibility(View.VISIBLE);
                convertView.findViewById(R.id.fake).setVisibility(View.VISIBLE);
            }
            convertView.findViewById(R.id.showMap).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                            new NetworkService().getUserCoordinate(orderDto.getUserPhone());

                    } catch (Exception e) {
                        EventBus.getDefault().post(new ErrorMessageEvent(e.getMessage()));
                    }
                }
            });
//            if(new Date().getTime() - orderDto.getAcceptDate().getTime() < 5000
//                    || new Date().getTime() - orderDto.getAcceptDate().getTime() >10000){
//                close.setVisibility(View.VISIBLE);
//
//            }else close.setVisibility(View.GONE);


            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isCalled){
                        if (new Date().getTime() - orderDto.getAcceptDate().getTime() >= 300000) {
                            new NetworkService().removeOrder(orderDto);
                        }}  if(new Date().getTime() - orderDto.getAcceptDate().getTime() <= 5000){
                        networkService.editBalance(+5);
                        UserProfileDto.User.setBalance(UserProfileDto.User.getBalance() + 5);
                        new NetworkService().removeAcceptedOrder(orderDto.getId());
                        close.setEnabled(false);
                        }
                    else {
                        new NetworkService().removeAcceptedOrder(orderDto.getId());
                        OrderDto.AcceptOreders.getOrders().remove(orderDto);
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

                    isCalled = true;
                }
            });

        } catch (Exception e) {
            e.getMessage();
        }}
        return convertView;
    }

    public void setAccept(boolean accept) {
        isAccept = accept;
    }
}

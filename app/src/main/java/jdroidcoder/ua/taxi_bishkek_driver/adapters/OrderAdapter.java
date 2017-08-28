package jdroidcoder.ua.taxi_bishkek_driver.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
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

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jdroidcoder.ua.taxi_bishkek_driver.R;
import jdroidcoder.ua.taxi_bishkek_driver.activity.OrdersActivity;
import jdroidcoder.ua.taxi_bishkek_driver.events.ErrorMessageEvent;
import jdroidcoder.ua.taxi_bishkek_driver.events.UpdateAdapterEvent;
import jdroidcoder.ua.taxi_bishkek_driver.model.OrderDto;
import jdroidcoder.ua.taxi_bishkek_driver.model.UserProfileDto;
import jdroidcoder.ua.taxi_bishkek_driver.network.NetworkService;

/**
 * Created by jdroidcoder on 10.04.17.
 */
public class OrderAdapter extends BaseAdapter {
    private Context context;
    private boolean isAccept = false;
    public List<OrderDto> orderDtos = new ArrayList<>();
    private boolean isCalled = false;
    boolean isClicked = true;

    private NetworkService networkService = new NetworkService();
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
        boolean isClicked = true;
        final OrderDto orderDto = orderDtos.get(position);
        if(!orderDtos.isEmpty()){
        try {
             final ImageView close = (ImageView) convertView.findViewById(R.id.close);
            ((TextView) convertView.findViewById(R.id.fromET)).setText(orderDto.getPointA());
            ((TextView) convertView.findViewById(R.id.toET)).setText(orderDto.getPointB());
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
                                            if(isCalled) {
                                                networkService.addComplaint(UserProfileDto.User.getPhone(), orderDto.getUserPhone());
                                                networkService.removeAcceptedOrder(orderDto.getId(), UserProfileDto.User.getPhone());
                                            }
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

                (convertView.findViewById(R.id.distanceTV)).setVisibility(View.GONE);
                convertView.findViewById(R.id.nullView).setVisibility(View.VISIBLE);
                convertView.findViewById(R.id.call).setVisibility(View.VISIBLE);
                convertView.findViewById(R.id.close).setVisibility(View.VISIBLE);
                convertView.findViewById(R.id.showMap).setVisibility(View.VISIBLE);
                convertView.findViewById(R.id.fake).setVisibility(View.VISIBLE);
            }
            convertView.findViewById(R.id.showMap).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                            networkService.getUserCoordinate(orderDto.getUserPhone());

                    } catch (Exception e) {
                        EventBus.getDefault().post(new ErrorMessageEvent(e.getMessage()));
                    }
                }
            });


            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                        networkService.removeAcceptedOrder(orderDto.getId(), UserProfileDto.User.getPhone());
                        OrderDto.AcceptOreders.getOrders().remove(orderDto);
                    EventBus.getDefault().post(new UpdateAdapterEvent());
                }
            });
            convertView.findViewById(R.id.call).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    networkService.startCall(orderDto.getId());
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse("tel:" + Uri.encode(orderDto.getUserPhone().trim())));
                    callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(callIntent);
                    isCalled = true;
                }
           });

            if(orderDto.getCanceled()){
                convertView.findViewById(R.id.item_background).setBackgroundColor(Color.BLACK);
                convertView.findViewById(R.id.distanceTV).setBackgroundColor(Color.BLACK);
                convertView.findViewById(R.id.whenTV).setBackgroundColor(Color.BLACK);
                ((TextView) convertView.findViewById(R.id.fromET)).setTextColor(Color.GRAY);
                ((TextView) convertView.findViewById(R.id.toET)).setTextColor(Color.GRAY);
            }
        } catch (Exception e) {
            Log.e("ADAPTER", e.getMessage());
        }
        }
        return convertView;
    }
    public void setAccept(boolean accept) {
        isAccept = accept;
    }
}

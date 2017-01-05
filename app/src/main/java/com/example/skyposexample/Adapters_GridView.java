package com.example.skyposexample;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

import java.util.ArrayList;

/**
 * Created by HanKyul on 2017-01-02.
 */

//FrontActivity GridAdapter
class FrontActivityGridAdapter extends BaseAdapter {

    Activity activity;
    Context context;
    int layout;
    ArrayList<String> list;
    LayoutInflater inflater;
    String userPos;

    public FrontActivityGridAdapter(Context context, int layout, ArrayList<String> list, String userPos, Activity activity){
        this.activity = activity;
        this.context = context;
        this.layout = layout;
        this.list = list;
        this.userPos = userPos;
        inflater = (LayoutInflater) context.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView==null)
            convertView = inflater.inflate(layout, null);
        Button btn = (Button)convertView.findViewById(R.id.food_btn);
        btn.setText(list.get(position));

        btn.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, FrontOrder.class);
                intent.putExtra("userPos", userPos);
                intent.putExtra("table_no", list.get(position));
                activity.startActivity(intent);
            }
        });

        return convertView;
    }
}

//FrontOrderTab GridAdapter
class FrontOrderTabGridAdapter extends BaseAdapter {

    Activity activity;
    Context context;
    int layout;
    String table_no;
    ArrayList<String> list;
    ArrayList<FrontOrderMenuList> callMenus;
    LayoutInflater inflater;

    public FrontOrderTabGridAdapter(Context context, int layout, ArrayList<String> list, ArrayList<FrontOrderMenuList> callMenus, String table_no, Activity activity){
        this.activity = activity;
        this.callMenus = callMenus;
        this.table_no = table_no;
        this.context = context;
        this.layout = layout;
        this.list = list;
        inflater = (LayoutInflater) context.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView==null)
            convertView = inflater.inflate(layout, null);
        Button btn = (Button)convertView.findViewById(R.id.food_btn);
        btn.setText(list.get(position));

        btn.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, FrontOrderTabMenu.class);
                intent.putExtra("table_no", table_no);
                intent.putExtra("food_name", callMenus.get(position).food_name);
                intent.putExtra("food_price", callMenus.get(position).food_price);
                activity.startActivity(intent);
            }
        });

        return convertView;
    }
}

//KitchenActivity GridAdapter
class KitchenActivityGridAdapter extends BaseAdapter {

    Activity activity;
    Context context;
    int layout;
    ArrayList<String> list;
    LayoutInflater inflater;

    public KitchenActivityGridAdapter(Context context, int layout, ArrayList<String> list, Activity activity){
        this.activity = activity;
        this.context = context;
        this.layout = layout;
        this.list = list;
        inflater = (LayoutInflater) context.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView==null)
            convertView = inflater.inflate(layout, null);
        Button btn = (Button)convertView.findViewById(R.id.food_btn);
        btn.setText(list.get(position));

        /*
        btn.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, FrontOrder.class);
                intent.putExtra("table_no", grid.get(position));
                activity.startActivity(intent);
            }
        });
        */

        return convertView;
    }
}
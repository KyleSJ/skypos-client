package com.example.skyposexample;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by HanKyul on 2017-01-02.
 */

//FrontOrder
class FrontOrderAdapter extends BaseAdapter {
    Context context;
    int layout;
    ArrayList<FrontOrderList> alt;
    LayoutInflater inf;

    public FrontOrderAdapter(Context context, int layout, ArrayList<FrontOrderList> alt) {
        this.context = context;
        this.layout = layout;
        this.alt = alt;
        this.inf = (LayoutInflater) context.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() { // 총 데이터의 개수
        return alt.size();
    }

    @Override
    public Object getItem(int position) {
        return alt.get(position);
    }

    @Override
    public long getItemId(int position) { // 해당 행의 유니크한 id
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewholder;

        if(convertView == null)
        {
            convertView = inf.inflate(layout, parent, false);

            viewholder = new ViewHolder();
            viewholder.orderNum = (TextView)convertView.findViewById(R.id.hk_orderNum);
            viewholder.orderGoodsNum = (TextView)convertView.findViewById(R.id.hk_orderGoodsNum);
            viewholder.order_name = (TextView)convertView.findViewById(R.id.hk_name);
            viewholder.order_price = (TextView)convertView.findViewById(R.id.hk_price);
            viewholder.order_number = (TextView)convertView.findViewById(R.id.hk_number);

            convertView.setTag(viewholder);
        }
        else
        {
            viewholder = (ViewHolder)convertView.getTag();
        }

        viewholder.orderNum.setText(alt.get(position).orderNum);
        viewholder.orderGoodsNum.setText(alt.get(position).orderGoodsNum);
        viewholder.order_name.setText(alt.get(position).food_name);
        viewholder.order_price.setText(alt.get(position).price);
        viewholder.order_number.setText(alt.get(position).number);

        viewholder.orderNum.setVisibility(View.GONE);
        viewholder.orderGoodsNum.setVisibility(View.GONE);

        return convertView;
    }

    private class ViewHolder {
        TextView orderNum;
        TextView orderGoodsNum;
        TextView order_name;
        TextView order_price;
        TextView order_number;
    }
}


//KitchenActivity
class KitchenActivityAdapter extends BaseAdapter {
    Context context;
    int layout;
    ArrayList<KitchenActivityOrderList> list;
    LayoutInflater inf;

    public KitchenActivityAdapter(Context context, int layout, ArrayList<KitchenActivityOrderList> list) {
        this.context = context;
        this.layout = layout;
        this.list = list;
        this.inf = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return list.size();
    }

    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) { // 해당 행의 유니크한 id
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewholder;

        if (convertView == null) {
            convertView = inf.inflate(layout, parent, false);

            viewholder = new ViewHolder();
            viewholder.tv1 = (TextView) convertView.findViewById(R.id.txtTableNo);
            viewholder.tv2 = (TextView) convertView.findViewById(R.id.txtOrder);

            convertView.setTag(viewholder);
        } else {
            viewholder = (ViewHolder) convertView.getTag();
        }

        viewholder.tv1.setText(list.get(position).table_no);
        viewholder.tv2.setText(list.get(position).order);

        return convertView;
    }

    private class ViewHolder {
        TextView tv1;
        TextView tv2;
    }
}

//ServerActivity Adapter
class ServerActivityAdapter extends BaseAdapter {
    Context context;
    int layout;
    ArrayList<ServerActivityOrderList> list;
    LayoutInflater inf;

    public ServerActivityAdapter(Context context, int layout, ArrayList<ServerActivityOrderList> list) {
        this.context = context;
        this.layout = layout;
        this.list = list;
        this.inf = (LayoutInflater) context.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() { // 총 데이터의 개수
        return list.size();
    }

    @Override
    public Object getItem(int position) { // 해당 행의 데이터
        return list.get(position);
    }

    @Override
    public long getItemId(int position) { // 해당 행의 유니크한 id
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewholder;

        if(convertView == null)
        {
            convertView = inf.inflate(layout, parent, false);

            viewholder = new ViewHolder();
            viewholder.tv1 = (TextView)convertView.findViewById(R.id.textView1);
            viewholder.tv2 = (TextView)convertView.findViewById(R.id.textView2);

            convertView.setTag(viewholder);
        }
        else
        {
            viewholder = (ViewHolder)convertView.getTag();
        }

        viewholder.tv1.setText(list.get(position).table_no);
        viewholder.tv2.setText(list.get(position).total);

        return convertView;
    }

    private class ViewHolder {
        TextView tv1;
        TextView tv2;
    }
}

//ServerActivity2 Adapter
class ServerActivity2Adapter extends BaseAdapter {
    Context context;
    int layout;
    ArrayList<ServerActivity2_list> list;
    LayoutInflater inf;

    public ServerActivity2Adapter(Context context, int layout, ArrayList<ServerActivity2_list> list) {
        this.context = context;
        this.layout = layout;
        this.list = list;
        this.inf = (LayoutInflater) context.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() { // 총 데이터의 개수
        return list.size();
    }

    @Override
    public Object getItem(int position) { // 해당 행의 데이터
        return list.get(position);
    }

    @Override
    public long getItemId(int position) { // 해당 행의 유니크한 id
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewholder;

        if(convertView == null)
        {
            convertView = inf.inflate(layout, parent, false);

            viewholder = new ViewHolder();
            viewholder.tv1 = (TextView)convertView.findViewById(R.id.textView1);
            viewholder.tv2 = (TextView)convertView.findViewById(R.id.textView2);
            viewholder.tv3 = (TextView)convertView.findViewById(R.id.textView3);
            viewholder.tv4 = (TextView)convertView.findViewById(R.id.textView4);
            viewholder.tv5 = (TextView)convertView.findViewById(R.id.textView5);

            convertView.setTag(viewholder);
        }
        else
        {
            viewholder = (ViewHolder)convertView.getTag();
        }

        viewholder.tv1.setText(list.get(position).orderNum);
        viewholder.tv2.setText(list.get(position).orderGoodsNum);
        viewholder.tv3.setText(list.get(position).food_name);
        viewholder.tv4.setText(list.get(position).price);
        viewholder.tv5.setText(list.get(position).number);

        viewholder.tv1.setVisibility(View.GONE);
        viewholder.tv2.setVisibility(View.GONE);

        return convertView;
    }

    private class ViewHolder {
        TextView tv1;
        TextView tv2;
        TextView tv3;
        TextView tv4;
        TextView tv5;
    }
}

//ServerActivity3 Adapter
class ServerActivity3Adapter extends BaseAdapter {
    Context context;
    int layout;
    ArrayList<SelectInfo> list;
    LayoutInflater inf;

    public ServerActivity3Adapter(Context context, int layout, ArrayList<SelectInfo> list) {
        this.context = context;
        this.layout = layout;
        this.list = list;
        this.inf = (LayoutInflater) context.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() { // 총 데이터의 개수
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) { // 해당 행의 유니크한 id
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewholder;

        if(convertView == null)
        {
            convertView = inf.inflate(layout, parent, false);

            viewholder = new ViewHolder();
            viewholder.tv3 = (TextView)convertView.findViewById(R.id.orderName);
            viewholder.tv4 = (TextView)convertView.findViewById(R.id.orderPrice);
            viewholder.tv5 = (TextView)convertView.findViewById(R.id.orderNumber);

            convertView.setTag(viewholder);
        }
        else
        {
            viewholder = (ViewHolder)convertView.getTag();
        }

        viewholder.tv3.setText(list.get(position).food_name);
        viewholder.tv4.setText(list.get(position).price);
        viewholder.tv5.setText(list.get(position).number);

        return convertView;
    }

    private class ViewHolder {
        TextView tv3;
        TextView tv4;
        TextView tv5;
    }
}

//ServerActivity3Tab Adapter
class ServerActivity3TabAdapter extends BaseAdapter {
    Context context;
    int layout;
    ArrayList<TabFoodInfo> list;
    LayoutInflater inf;

    public ServerActivity3TabAdapter(Context context, int layout, ArrayList<TabFoodInfo> list) {
        this.context = context;
        this.layout = layout;
        this.list = list;
        this.inf = (LayoutInflater) context.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() { // 총 데이터의 개수
        return list.size();
    }

    @Override
    public Object getItem(int position) { // 해당 행의 데이터
        return list.get(position);
    }

    @Override
    public long getItemId(int position) { // 해당 행의 유니크한 id
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewholder;

        if(convertView == null)
        {
            convertView = inf.inflate(layout, parent, false);
            viewholder = new ViewHolder();

            viewholder.tv1 = (TextView)convertView.findViewById(R.id.textview6);
            viewholder.tv2 = (TextView)convertView.findViewById(R.id.textview7);

            convertView.setTag(viewholder);
        }
        else
        {
            viewholder = (ViewHolder)convertView.getTag();
        }

        viewholder.tv1.setText(list.get(position).food_name);
        viewholder.tv2.setText(list.get(position).num);

        return convertView;

    }
    static class ViewHolder {
        TextView tv1;
        TextView tv2;
    }
}

package com.example.skyposexample;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import static com.example.skyposexample.R.id.price;

/*
 * Created by HanKyul on 2016-08-18.
 */
public class ServerActivity3TabMenu extends Activity {
    Button OK;
    Button cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.menu_detail);

        final TextView tv_food_name = (TextView)findViewById(R.id.name);
        final TextView tv_food_price = (TextView)findViewById(price);
        final EditText et_food_qntt = (EditText)findViewById(R.id.etLine);

        Intent intent = getIntent();
        tv_food_name.setText(intent.getStringExtra("food_name"));
        tv_food_price.setText(intent.getStringExtra("food_price"));

        cancel = (Button)findViewById(R.id.cancelBtn);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        OK = (Button)findViewById(R.id.OkBtn);
        OK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String food = tv_food_name.getText().toString();
                String price = tv_food_price.getText().toString();
                String quantity = et_food_qntt.getText().toString();
                SharedPreferences serverActivity3FoodInfo = getSharedPreferences("serverActivity3FoodInfo", MODE_PRIVATE);
                SharedPreferences.Editor editor = serverActivity3FoodInfo.edit();
                editor.putString("food_name", food);
                editor.putString("food_price", price);
                editor.putString("food_quantity", quantity);
                editor.commit(); //완료한다.
                finish();
            }
        });
    }
}
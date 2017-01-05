package com.example.skyposexample;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

import static com.example.skyposexample.R.id.price;

/*
 * Created by HanKyul on 2016-08-18.
 */

public class FrontOrderTabMenu extends Activity {

    private MySQLiteOpenHelper helper;
    String dbName = "skyPos.db";
    int dbVersion = 1; // 데이터베이스 버전
    private SQLiteDatabase db;
    String tag = "SQLite"; // Log 에 사용할 tag

    Button OK;
    Button cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.menu2_detail);

        helper = new MySQLiteOpenHelper(
                this,  // 현재 화면의 제어권자
                dbName,// db 이름
                null,  // 커서팩토리-null : 표준커서가 사용됨
                dbVersion);       // 버전

        try {
//         // 데이터베이스 객체를 얻어오는 다른 간단한 방법
//         db = openOrCreateDatabase(dbName,  // 데이터베이스파일 이름
//                          Context.MODE_PRIVATE, // 파일 모드
//                          null);    // 커서 팩토리
//
//         String sql = "create table mytable(id integer primary key autoincrement, name text);";
//        db.execSQL(sql);

            db = helper.getWritableDatabase(); // 읽고 쓸수 있는 DB
            //db = helper.getReadableDatabase(); // 읽기 전용 DB select문
        } catch (SQLiteException e) {
            e.printStackTrace();
            Log.e(tag, "데이터베이스를 얻어올 수 없음");
            finish(); // 액티비티 종료
        }

        //테이블 번호 받기
        TextView tv_tableNum = (TextView)findViewById(R.id.table_no_send);

        final TextView tv_name = (TextView)findViewById(R.id.name);
        final TextView tv_price = (TextView)findViewById(price);
        final EditText et_qntt = (EditText)findViewById(R.id.etLine);

        Intent intent = getIntent();
        //table_no
        tv_tableNum.setText(intent.getStringExtra("table_no"));
        tv_tableNum.setVisibility(View.GONE);
        tv_name.setText(intent.getStringExtra("food_name"));
        tv_price.setText(intent.getStringExtra("food_price"));

        final String table_no = intent.getStringExtra("table_no");

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
                //테이블 번호 받아오기
                Log.d("table_no", table_no);

                //음식 정보 받아오기
                String food_name = tv_name.getText().toString();
                String food_price = tv_price.getText().toString();
                String food_Qntt = et_qntt.getText().toString();

                if(food_Qntt == "")
                    finish();

                //orderTime 받아오기
                long time = System.currentTimeMillis();
                Log.d("orderTime", Long.toString(time));

                //orderAmnt 받아오기
                int orderAmnt = 0;
                orderAmnt = Integer.parseInt(food_price) * Integer.parseInt(food_Qntt);
                Log.d("orderAmnt", String.valueOf(orderAmnt));

                //개시일자 받아오기
                String openDay = "";
                Cursor c = db.rawQuery("select openDay from open", null);
                while (c.moveToNext()) {
                    openDay = c.getString(0);
                }
                c.close();

                //데이터베이스에 정보 저장
                String orderNum = "";
                Cursor c2 = db.rawQuery("select orderNum from ordermenu order by orderNum asc", null);
                while (c2.moveToNext()) {
                    orderNum = c2.getString(0);
                }
                c2.close();
                if (orderNum == "")
                    orderNum = "0";
                int ordermenu_orderNum = Integer.parseInt(orderNum) + 1;
                orderNum = Integer.toString(ordermenu_orderNum);

                final String insertOrderMenu = "insert into ordermenu (orderNum, openDay, tableNum, orderTime, orderAmnt, orderComplete, payComplete) values (" + orderNum + ",'" + openDay + "'," + table_no + "," + Long.toString(time) + "," + String.valueOf(orderAmnt) + ",0,0)";
                db.execSQL("insert into ordermenu (orderNum, openDay, tableNum, orderTime, orderAmnt, orderComplete, payComplete) values (" + orderNum + ",'" + openDay + "'," + table_no + "," + Long.toString(time) + "," + String.valueOf(orderAmnt) + ",0,0)");

                //goodsNum 가져오기
                String goodsNum = "";
                Cursor c3 = db.rawQuery("select goodsNum from goods where goodsName = '" + food_name + "'", null);
                while (c3.moveToNext()) {
                    goodsNum = c3.getString(0);
                }
                c3.close();

                //orderGoodsNum 가져오기
                String orderGoodsNum = "";
                Cursor c4 = db.rawQuery("select orderGoodsNum from order_goods order by orderGoodsNum asc", null);
                while (c4.moveToNext()) {
                    orderGoodsNum = c4.getString(0);
                }
                c4.close();
                if (orderGoodsNum == "")
                    orderGoodsNum = "0";
                int order_goods_orderGoodsNum = Integer.parseInt(orderGoodsNum) + 1;
                orderGoodsNum = Integer.toString(order_goods_orderGoodsNum);

                final String insertOrderGoods = "insert into order_goods (orderGoodsNum, goodsNum, orderNum, goodsQntt, openDay) values (" + orderGoodsNum + "," + goodsNum + "," + orderNum + "," + food_Qntt + ",'" + openDay + "')";
                db.execSQL("insert into order_goods (orderGoodsNum, goodsNum, orderNum, goodsQntt, openDay) values (" + orderGoodsNum + "," + goodsNum + "," + orderNum + "," + food_Qntt + ",'" + openDay + "')");

                //네트워크 작업이므로 Thread 생성
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        try {
                            int PORT = 10002; //서버에서 설정한 PORT 번호
                            String ip="192.168.0.45"; //서버 단말기의 IP주소..
                            //본 예제는 Genymotion 에뮬레이터 2대로 테스한 예제입니다.
                            //Genymotion을 실행하면 각 에뮬레이터의 IP를 확인할 수 있습니다.

                            Socket socket;     //클라이언트의 소켓
                            DataInputStream is;
                            DataOutputStream os;

                            //서버와 연결하는 소켓 생성..
                            socket = new Socket(InetAddress.getByName(ip), PORT);

                            //여기까지 왔다는 것을 예외가 발생하지 않았다는 것이므로 소켓 연결 성공..
                            //서버와 메세지를 주고받을 통로 구축
                            is = new DataInputStream(socket.getInputStream());
                            os = new DataOutputStream(socket.getOutputStream());

                            Gson gson = new Gson();
                            ArrayList<SendCommunication> communications = new ArrayList<SendCommunication>();
                            communications.add(new SendCommunication(insertOrderMenu, insertOrderGoods));

                            String json = gson.toJson(communications);

                            os.writeUTF(json);  //서버로 메세지 보내기.UTF 방식으로(한글 전송가능...)
                            os.flush();        //다음 메세지 전송을 위해 연결통로의 버퍼를 지워주는 메소드..
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }//run method..
                }).start(); //Thread 실행..

                //네트워크 작업이므로 Thread 생성
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        try {
                            int PORT = 10002; //서버에서 설정한 PORT 번호
                            String ip="192.168.0.46"; //서버 단말기의 IP주소..
                            //본 예제는 Genymotion 에뮬레이터 2대로 테스한 예제입니다.
                            //Genymotion을 실행하면 각 에뮬레이터의 IP를 확인할 수 있습니다.

                            Socket socket;     //클라이언트의 소켓
                            DataInputStream is;
                            DataOutputStream os;

                            //서버와 연결하는 소켓 생성..
                            socket = new Socket(InetAddress.getByName(ip), PORT);

                            //여기까지 왔다는 것을 예외가 발생하지 않았다는 것이므로 소켓 연결 성공..
                            //서버와 메세지를 주고받을 통로 구축
                            is = new DataInputStream(socket.getInputStream());
                            os = new DataOutputStream(socket.getOutputStream());

                            Gson gson = new Gson();
                            ArrayList<SendCommunication> communications = new ArrayList<SendCommunication>();
                            communications.add(new SendCommunication(insertOrderMenu, insertOrderGoods));

                            String json = gson.toJson(communications);

                            os.writeUTF(json);  //서버로 메세지 보내기.UTF 방식으로(한글 전송가능...)
                            os.flush();        //다음 메세지 전송을 위해 연결통로의 버퍼를 지워주는 메소드..
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }//run method..
                }).start(); //Thread 실행..

                SharedPreferences test2 = getSharedPreferences("test2", MODE_PRIVATE);
                SharedPreferences.Editor editor2 = test2.edit();
                editor2.putString("orderNum", orderNum);
                editor2.putString("orderGoodsNum", orderGoodsNum);
                editor2.putString("food_name", food_name);
                editor2.putString("food_price", food_price);
                editor2.putString("food_quantity", food_Qntt);
                editor2.commit(); //완료한다.
                //SystemClock.sleep(3000);
                finish();
            }
        });
    }
}
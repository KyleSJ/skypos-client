package com.example.skyposexample;

/**
 * Created by 성쟈 on 2016-09-04.
 */

import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static com.example.skyposexample.R.id.table_no;

public class FrontOrder extends TabActivity {

    private MySQLiteOpenHelper helper;
    String dbName = "skyPos.db";
    int dbVersion = 1; // 데이터베이스 버전
    private SQLiteDatabase db;
    String tag = "SQLite"; // Log 에 사용할 tag

    ArrayList<FrontOrderList> frontOrderLists = new ArrayList<FrontOrderList>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.front_table_order);

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

        //테이블 번호 받아오기
        TextView tv_tableNum = (TextView)findViewById(table_no);

        Intent intent2 = getIntent();
        tv_tableNum.setText(intent2.getStringExtra("table_no"));
        final String table_no = intent2.getStringExtra("table_no");

        //userPos
        final String userPos = intent2.getStringExtra("userPos");

        //현재시간 받아오기
        TextView tv_currentTime = (TextView)findViewById(R.id.time);
        tv_currentTime.setText(getDataString());

        //총액 받아오기
        int total=0;
        String totalCalcuration = "select g.goodsPrice, k.goodsQntt from goods g, ordermenu o, order_goods k where o.orderNum = k.orderNum and o.openDay = k.openDay and g.goodsNum = k.goodsNum and o.tableNum = " + table_no;
        Cursor c = db.rawQuery(totalCalcuration, null);
        while(c.moveToNext()){
            int goodsPrice = Integer.parseInt(c.getString(0));
            int goodsQntt = Integer.parseInt(c.getString(1));
            total += goodsPrice * goodsQntt;
        }
        c.close();
        TextView tv_total = (TextView)findViewById(R.id.table_amount);
        tv_total.setText(String.valueOf(total));

        // 탭 선언 부분
        Resources res = getResources(); //리소스 객체 생성
        TabHost tabHost = getTabHost(); //탭을 붙이기위한 탭호스객체선언
        TabHost.TabSpec spec; //탭호스트에 붙일 각각의 탭스펙을 선언 ; 각 탭의 메뉴와 컨텐츠를 위한 객체
        Intent intent; //각탭에서 사용할 인텐트 선언

        //인텐트 생성
        intent = new Intent().setClass(this, FrontOrder_Food.class);
        //각 탭의 메뉴와 컨텐츠를 위한 객체 생성
        spec = tabHost.newTabSpec("food").setIndicator("음식").setContent(intent.putExtra("table_no", table_no));
        tabHost.addTab(spec);

        //인텐트 생성
        intent = new Intent().setClass(this, FrontOrder_Drink.class);
        //각 탭의 메뉴와 컨텐츠를 위한 객체 생성
        spec = tabHost.newTabSpec("drink").setIndicator("음료").setContent(intent.putExtra("table_no", table_no));
        tabHost.addTab(spec);

        //인텐트 생성
        intent = new Intent().setClass(this, FrontOrder_ETC.class);
        //각 탭의 메뉴와 컨텐츠를 위한 객체 생성
        spec = tabHost.newTabSpec("etc").setIndicator("기타").setContent(intent.putExtra("table_no", table_no));
        tabHost.addTab(spec);

        tabHost.setCurrentTab(0); //처음 시작할때 나타날 탭을 설정

        //주문 목록
        String orderList = "select g.goodsName, g.goodsPrice, k.goodsQntt, k.orderGoodsNum, o.orderNum from goods g, ordermenu o, order_goods k where o.orderNum = k.orderNum and o.openDay = k.openDay and g.goodsNum = k.goodsNum and o.payComplete = 0 and o.tableNum = " + table_no;
        Cursor c1 = db.rawQuery(orderList, null);
        while(c1.moveToNext()){
            String goodsName = c1.getString(0);
            String goodsPrice = c1.getString(1);
            String goodsQntt = c1.getString(2);
            String orderNum = c1.getString(3);
            String orderGoodsNum = c1.getString(4);
            frontOrderLists.add(new FrontOrderList(orderNum, orderGoodsNum, goodsName, goodsPrice, goodsQntt)); // 리스트뷰에 목록 추가
        }

        // adapter
        final FrontOrderAdapter frontOrderAdapter = new FrontOrderAdapter(
                getApplicationContext(),
                R.layout.front_order_list,
                frontOrderLists);

        // adapter - listview 연결
        ListView order = (ListView) findViewById(R.id.list);
        order.setAdapter(frontOrderAdapter);

        // 리스트뷰 롱클릭 이벤트(삭제)
        order.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            int selectedPos = -1;
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                selectedPos = position;
                AlertDialog.Builder alert = new AlertDialog.Builder(FrontOrder.this);
                alert.setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String orderNum = frontOrderLists.get(selectedPos).orderNum;
                        String orderGoodsNum = frontOrderLists.get(selectedPos).orderGoodsNum;
                        final String delete_order_goods = "delete from order_goods where orderGoodsNum = " + orderGoodsNum;
                        final String delete_ordermenu = "delete from ordermenu where orderNum = " + orderNum;
                        db.execSQL(delete_order_goods);
                        db.execSQL(delete_ordermenu);
                        frontOrderLists.remove(selectedPos);
                        //리스트뷰 새로고침
                        frontOrderAdapter.notifyDataSetChanged();
                        //총액 다시 받아오기
                        int total=0;
                        String totalCalcuration = "select g.goodsPrice, k.goodsQntt from goods g, ordermenu o, order_goods k where o.orderNum = k.orderNum and o.openDay = k.openDay and g.goodsNum = k.goodsNum and o.payComplete = 0 and o.tableNum = " + table_no;
                        Cursor c = db.rawQuery(totalCalcuration, null);
                        while(c.moveToNext()){
                            int goodsPrice = Integer.parseInt(c.getString(0));
                            int goodsQntt = Integer.parseInt(c.getString(1));
                            total += goodsPrice * goodsQntt;
                        }
                        c.close();
                        TextView tv_total = (TextView)findViewById(R.id.table_amount);
                        tv_total.setText(String.valueOf(total));

                        //네트워크 작업이므로 Thread 생성
                        new Thread(new Runnable() {

                            @Override
                            public void run() {
                                // TODO Auto-generated method stub
                                try {
                                    int PORT = 10001; //서버에서 설정한 PORT 번호
                                    String ip="192.168.0.45"; //서버 단말기의 IP주소..

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
                                    communications.add(new SendCommunication(delete_order_goods, delete_ordermenu));

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
                                    int PORT = 10001; //서버에서 설정한 PORT 번호
                                    String ip="192.168.0.46"; //서버 단말기의 IP주소..

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
                                    communications.add(new SendCommunication(delete_order_goods, delete_ordermenu));

                                    String json = gson.toJson(communications);

                                    os.writeUTF(json);  //서버로 메세지 보내기.UTF 방식으로(한글 전송가능...)
                                    os.flush();        //다음 메세지 전송을 위해 연결통로의 버퍼를 지워주는 메소드..
                                } catch (IOException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                            }//run method..
                        }).start(); //Thread 실행..

                        dialog.dismiss();
                    }
                });
                alert.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alert.setMessage("삭제하시겠습니까?") ;
                alert.show();
                return false;
            }
        });

        //결제 버튼 클릭
        Button payment = (Button)findViewById(R.id.payment);

        final String finalQntt = String.valueOf(total);

        payment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FrontOrder.this, Payment.class);
                intent.putExtra("Qntt", finalQntt);
                intent.putExtra("table_no", table_no);
                intent.putExtra("userPos", userPos);
                startActivity(intent);
                finish();
            }
        });
    }

    public String getDataString() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA);
        String dtr_date = df.format(new Date());

        return dtr_date;
    }

    @Override
    protected void onResume() {
        TextView tv = (TextView)findViewById(table_no);

        Intent intent2 = getIntent();
        tv.setText(intent2.getStringExtra("table_no"));
        String table_no = intent2.getStringExtra("table_no");

        SharedPreferences test2 = getSharedPreferences("test2", MODE_PRIVATE);
        SharedPreferences.Editor editor2 = test2.edit();
        String orderNum = test2.getString("orderNum", null);
        String orderGoodsNum = test2.getString("orderGoodsNum", null);
        String food_name = test2.getString("food_name", null);
        String food_price = test2.getString("food_price", null);
        String food_quantity = test2.getString("food_quantity", null);

        if (orderNum != null && orderGoodsNum != null && food_name != null && food_price != null && food_quantity != null) {
            //ArrayList에 값 저장
            frontOrderLists.add(new FrontOrderList(orderNum, orderGoodsNum, food_name, food_price, food_quantity));
            //String 파일 초기화
            editor2.clear();
            editor2.commit(); //완료한다.
        }
        //총액 받아오기
        int total=0;
        String totalCalcuration = "select g.goodsPrice, k.goodsQntt from goods g, ordermenu o, order_goods k where o.orderNum = k.orderNum and o.openDay = k.openDay and g.goodsNum = k.goodsNum and o.payComplete = 0 and o.tableNum = " + table_no;
        Cursor c = db.rawQuery(totalCalcuration, null);
        while(c.moveToNext()){
            int goodsPrice = Integer.parseInt(c.getString(0));
            int goodsQntt = Integer.parseInt(c.getString(1));
            total += goodsPrice * goodsQntt;
        }
        c.close();
        TextView tv_total = (TextView)findViewById(R.id.table_amount);
        tv_total.setText(String.valueOf(total));
        super.onResume();
    }
}

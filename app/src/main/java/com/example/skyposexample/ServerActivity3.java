package com.example.skyposexample;

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

import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

public class ServerActivity3 extends TabActivity {

    private MySQLiteOpenHelper helper;
    String dbName = "skyPos.db";
    int dbVersion = 1; // 데이터베이스 버전
    private SQLiteDatabase db;
    String tag = "SQLite"; // Log 에 사용할 tag

    //주문 내역 리스트뷰
    ArrayList<SelectInfo> selectInfos = new ArrayList<SelectInfo>();
    Button cancelBtn; //취소버튼
    Button orderBtn; //주문버튼

    Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab_page);

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

        Resources res = getResources(); //리소스 객체 생성
        TabHost tabHost = getTabHost(); //탭을 붙이기위한 탭호스객체선언
        TabHost.TabSpec spec; //탭호스트에 붙일 각각의 탭스펙을 선언 ; 각 탭의 메뉴와 컨텐츠를 위한 객체
        Intent intent; //각탭에서 사용할 인텐트 선언

        //인텐트 생성
        intent = new Intent().setClass(this, ServerActivity3_Food.class);
        //각 탭의 메뉴와 컨텐츠를 위한 객체 생성
        spec = tabHost.newTabSpec("food").setIndicator("음식").setContent(intent);
        tabHost.addTab(spec);

        //인텐트 생성
        intent = new Intent().setClass(this, ServerActivity3_Drink.class);
        //각 탭의 메뉴와 컨텐츠를 위한 객체 생성
        spec = tabHost.newTabSpec("drink").setIndicator("음료").setContent(intent);
        tabHost.addTab(spec);

        //인텐트 생성
        intent = new Intent().setClass(this, ServerActivity3_ETC.class);
        //각 탭의 메뉴와 컨텐츠를 위한 객체 생성
        spec = tabHost.newTabSpec("etc").setIndicator("기타").setContent(intent);
        tabHost.addTab(spec);

        tabHost.setCurrentTab(0); //처음 시작할때 나타날 탭을 설정


        final ServerActivity3Adapter serverActivity3Adapter = new ServerActivity3Adapter(
                getApplicationContext(),
                R.layout.order_list,
                selectInfos);

        // adapter - listview 연결
        ListView order = (ListView) findViewById(R.id.OrderList);
        order.setAdapter(serverActivity3Adapter);

        // 리스트뷰 롱클릭 이벤트(삭제)
        order.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            int selectedPos = -1;

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                selectedPos = position;
                AlertDialog.Builder alert = new AlertDialog.Builder(ServerActivity3.this);
                alert.setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectInfos.remove(selectedPos);
                        serverActivity3Adapter.notifyDataSetChanged();

                        dialog.dismiss();
                    }
                });
                alert.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alert.setMessage("삭제하시겠습니까?");
                alert.show();
                return false;
            }
        });

        // 취소 버튼
        cancelBtn = (Button) findViewById(R.id.cancelBtn1);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ServerActivity3.this, ServerActivity2.class);
                Intent intent2 = getIntent();
                String table_no = intent2.getStringExtra("table_no2").toString();
                intent.putExtra("table_no", table_no);
                startActivity(intent);
                finish();
            }
        });

        // 주문 버튼
        // 여기서 Client 간의 통신이 이뤄져야함...ㅠ
        orderBtn = (Button) findViewById(R.id.orderBtn1);
        orderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //테이블 번호 받아오기
                Intent intent2 = getIntent();
                String table_no = intent2.getStringExtra("table_no2").toString();
                Log.d("table_no", table_no);

                //음식 정보 받아오기
                String s = gson.toJson(selectInfos);
                SelectInfo[] foodInfo = gson.fromJson(s, SelectInfo[].class);

                //orderTime 받아오기
                long time = System.currentTimeMillis();
                Log.d("orderTime", Long.toString(time));

                //개시일자 받아오기
                String openDay = "";
                Cursor c = db.rawQuery("select openDay from open", null);
                while (c.moveToNext()) {
                    openDay = c.getString(0);
                    Log.d("openDay", openDay);
                }
                c.close();

                //데이터베이스에 정보 저장
                for (int i = 0; i < selectInfos.size(); i++) {

                    //주문번호 가져오기
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

                    //상품 정보
                    String food_name = foodInfo[i].food_name;
                    String food_price = foodInfo[i].price;
                    String food_quantity = foodInfo[i].number;

                    //총액 가져오기
                    int orderAmnt = 0;
                    orderAmnt += Integer.parseInt(food_price) * Integer.parseInt(food_quantity);

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

                    final String insertOrderGoods = "insert into order_goods (orderGoodsNum, goodsNum, orderNum, goodsQntt, openDay) values (" + orderGoodsNum + "," + goodsNum + "," + orderNum + "," + food_quantity + ",'" + openDay + "')";
                    db.execSQL("insert into order_goods (orderGoodsNum, goodsNum, orderNum, goodsQntt, openDay) values (" + orderGoodsNum + "," + goodsNum + "," + orderNum + "," + food_quantity + ",'" + openDay + "')");

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
                    }).start(); //Thread 실행...
                }
                Intent intent = new Intent(ServerActivity3.this, ServerActivity2.class);
                intent.putExtra("table_no", table_no);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        SharedPreferences serverActivity3FoodInfo = getSharedPreferences("serverActivity3FoodInfo", MODE_PRIVATE);
        SharedPreferences.Editor editor = serverActivity3FoodInfo.edit();
        String food_name = serverActivity3FoodInfo.getString("food_name", null);
        String food_price = serverActivity3FoodInfo.getString("food_price", null);
        String food_quantity = serverActivity3FoodInfo.getString("food_quantity", null);

        if (food_name != null && food_price != null && food_quantity != null) {
            //ArrayList에 값 저장
            selectInfos.add(new SelectInfo(food_name, food_price, food_quantity));
            //String 파일 초기화
            editor.clear();
            editor.commit(); //완료한다.
        }
        super.onResume();
    }
}
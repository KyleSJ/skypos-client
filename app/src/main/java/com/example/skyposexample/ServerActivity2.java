package com.example.skyposexample;

/**
 * Created by HanKyul on 2016-11-19.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

import static com.example.skyposexample.R.id.table_no;

public class ServerActivity2 extends Activity {

    private MySQLiteOpenHelper helper;
    String dbName = "skyPos.db";
    int dbVersion = 1; // 데이터베이스 버전
    private SQLiteDatabase db;
    String tag = "SQLite"; // Log 에 사용할 tag

    //주문내역 리스트뷰
    ArrayList<ServerActivity2_list> orderInfo = new ArrayList<ServerActivity2_list>();
    Button btn_Order; // 주문 버튼
    Button btn_Ok; // 확인 버튼

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.server2_main);

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

        //테이블 번호 받아오는 텍스트 뷰
        TextView tv_table_no = (TextView)findViewById(table_no);

        //테이블 번호 데이터 받아오는 intent
        Intent intent = getIntent();
        tv_table_no.setText(intent.getStringExtra("table_no"));
        final String table_no = intent.getStringExtra("table_no");

        // 리스트뷰 항목들 + 총액 계산
        int total = 0;
        String sql = "select g.goodsName, g.goodsPrice, k.goodsQntt, k.orderGoodsNum, o.orderNum from goods g, ordermenu o, order_goods k where o.orderNum = k.orderNum and o.openDay = k.openDay and g.goodsNum = k.goodsNum and o.payComplete = 0 and o.tableNum = " + table_no;
        Cursor c = db.rawQuery(sql, null);
        while(c.moveToNext()){
        String goodsName = c.getString(0);
        String goodsPrice = c.getString(1);
        String goodsQntt = c.getString(2);
        String orderGoodsNum = c.getString(3);
        String orderNum = c.getString(4);
        int goodsPrice_calcu = Integer.parseInt(c.getString(1));
        int goodsQntt_calcu = Integer.parseInt(c.getString(2));
        orderInfo.add(new ServerActivity2_list(orderNum, orderGoodsNum, goodsName, goodsPrice, goodsQntt)); // 리스트뷰에 목록 추가
        total += goodsPrice_calcu*goodsQntt_calcu;
    }

        TextView tv_total = (TextView)findViewById(R.id.total);
        tv_total.setText(String.valueOf(total));

        // adapter
        final ServerActivity2Adapter serverActivity2Adapter = new ServerActivity2Adapter(
                getApplicationContext(),
                R.layout.server2_list,
                orderInfo);

        // adapterView - ListView 연결
        ListView lv = (ListView) findViewById(R.id.listView2);
        lv.setAdapter(serverActivity2Adapter);

        // 리스트뷰 롱클릭 이벤트(삭제)
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            int selectedPos = -1;
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                selectedPos = position;
                AlertDialog.Builder alert = new AlertDialog.Builder(ServerActivity2.this);
                alert.setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String orderNum = orderInfo.get(selectedPos).orderNum;
                        String orderGoodsNum = orderInfo.get(selectedPos).orderGoodsNum;
                        final String sql = "delete from order_goods where orderGoodsNum = " + orderGoodsNum;
                        final String sql2 = "delete from ordermenu where orderNum = " + orderNum;
                        db.execSQL(sql);
                        db.execSQL(sql2);
                        orderInfo.remove(selectedPos);

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
                                    communications.add(new SendCommunication(sql, sql2));

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
                                int PORT = 10001; //서버에서 설정한 PORT 번호
                                String ip="192.168.0.46"; //서버 단말기의 IP주소..

                                Socket socket;     //클라이언트의 소켓
                                DataInputStream is;
                                DataOutputStream os;

                                // TODO Auto-generated method stub
                                try {
                                    //서버와 연결하는 소켓 생성..
                                    socket = new Socket(InetAddress.getByName(ip), PORT);

                                    //여기까지 왔다는 것을 예외가 발생하지 않았다는 것이므로 소켓 연결 성공..
                                    //서버와 메세지를 주고받을 통로 구축
                                    is = new DataInputStream(socket.getInputStream());
                                    os = new DataOutputStream(socket.getOutputStream());

                                    Gson gson = new Gson();
                                    ArrayList<SendCommunication> communications = new ArrayList<SendCommunication>();
                                    communications.add(new SendCommunication(sql, sql2));

                                    String json = gson.toJson(communications);

                                    os.writeUTF(json);  //서버로 메세지 보내기.UTF 방식으로(한글 전송가능...)
                                    os.flush();        //다음 메세지 전송을 위해 연결통로의 버퍼를 지워주는 메소드..
                                } catch (IOException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                            }//run method..
                        }).start(); //Thread 실행..

                        //리스트뷰 갱신
                        serverActivity2Adapter.notifyDataSetChanged();
                        //총액 계산
                        int total = 0;
                        String totalCalcuration = "select g.goodsPrice, k.goodsQntt from goods g, ordermenu o, order_goods k where o.orderNum = k.orderNum and o.openDay = k.openDay and g.goodsNum = k.goodsNum and o.payComplete = 0 and o.tableNum = " + table_no;
                        Cursor c = db.rawQuery(totalCalcuration, null);
                        while(c.moveToNext()){
                            int goodsPrice_calcu = Integer.parseInt(c.getString(0));
                            int goodsQntt_calcu = Integer.parseInt(c.getString(1));
                            total += goodsPrice_calcu*goodsQntt_calcu;
                        }
                        TextView tv_total = (TextView)findViewById(R.id.total);
                        tv_total.setText(String.valueOf(total));

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

        //주문버튼
        btn_Order = (Button)findViewById(R.id.callBtn);
        btn_Order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ServerActivity2.this, ServerActivity3.class);
                intent.putExtra("table_no2", table_no);
                startActivity(intent);
                finish();
            }
        });

        //확인버튼
        btn_Ok = (Button)findViewById(R.id.checkBtn);
        btn_Ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ServerActivity2.this, ServerActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}

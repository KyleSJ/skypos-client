package com.example.skyposexample;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

public class KitchenActivity extends Activity {

    private MySQLiteOpenHelper helper;
    String dbName = "skyPos.db";
    int dbVersion = 1; // 데이터베이스 버전
    private SQLiteDatabase db;
    String tag = "SQLite"; // Log 에 사용할 tag

    ArrayList<String> table_grid = new ArrayList<String>();
    ArrayList<KitchenActivityOrderList> kitchenActivityOrderLists = new ArrayList<KitchenActivityOrderList>();

    Runnable runnable;
    Thread thread;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kitchen_main);

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

        Cursor c = db.rawQuery("select tableNum from seattable;", null);
        while (c.moveToNext()) {
            String tableNumber = c.getString(0);
            table_grid.add(tableNumber); // 리스트뷰에 목록 추가
        }
        c.close();

        KitchenActivityGridAdapter kitchenActivityGridAdapter = new KitchenActivityGridAdapter(
                getApplicationContext(),
                R.layout.grid_item,
                table_grid,
                this);

        GridView gridView = (GridView) findViewById(R.id.gridView2);
        gridView.setAdapter(kitchenActivityGridAdapter);

        final KitchenActivityAdapter kitchenActivityAdapter = new KitchenActivityAdapter(
                getApplicationContext(),
                R.layout.kitchen_order_list,
                kitchenActivityOrderLists
        );

        final ListView lv = (ListView) findViewById(R.id.listview);
        lv.setAdapter(kitchenActivityAdapter);

        Cursor c1 = db.rawQuery("select tableNum, orderNum from ordermenu where orderComplete = 0;", null);
        while (c1.moveToNext()) {
            String tableNum = c1.getString(0);
            String orderNum = c1.getString(1);
            kitchenActivityOrderLists.add(new KitchenActivityOrderList(tableNum, orderNum));
        }

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder alert = new AlertDialog.Builder(KitchenActivity.this);
                String goodsName = "";
                String goodsQntt = "";
                Cursor c = db.rawQuery("select g.goodsName, k.goodsQntt from goods g, order_goods k where k.goodsNum = g.goodsNum", null);
                while (c.moveToNext()) {
                    goodsName = c.getString(0);
                    goodsQntt = c.getString(1);
                }
                alert.setMessage("table_no : " + kitchenActivityOrderLists.get(position).table_no + "\norder_no : " + kitchenActivityOrderLists.get(position).order + "\nmenu_name :" + goodsName + "\nmenu_Qntt :" + goodsQntt);

                //"주문확인"클릭시 내용
                alert.setNegativeButton("주문확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.dismiss();
                    }
                });

                //상품 완성 시 해당 주문 킵
                alert.setNeutralButton("완성", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String orderNum = kitchenActivityOrderLists.get(position).order;
                        final String update_sql = "update ordermenu set orderComplete = 1 where orderNum = " + orderNum;
                        db.execSQL(update_sql);
                        kitchenActivityOrderLists.remove(position);
                        //네트워크 작업이므로 Thread 생성
                        new Thread(new Runnable() {

                            @Override
                            public void run() {
                                // TODO Auto-generated method stub
                                try {
                                    int PORT = 10003; //서버에서 설정한 PORT 번호
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

                                    os.writeUTF(update_sql);  //서버로 메세지 보내기.UTF 방식으로(한글 전송가능...)
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
                                    int PORT = 10003; //서버에서 설정한 PORT 번호
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

                                    os.writeUTF(update_sql);  //서버로 메세지 보내기.UTF 방식으로(한글 전송가능...)
                                    os.flush();        //다음 메세지 전송을 위해 연결통로의 버퍼를 지워주는 메소드..
                                } catch (IOException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                            }//run method..
                        }).start(); //Thread 실행..
                        kitchenActivityAdapter.notifyDataSetChanged();
                        dialog.dismiss();
                    }
                });

                //주문취소 시 해당 주문 삭제
                alert.setPositiveButton("주문취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String orderNum = kitchenActivityOrderLists.get(position).order;
                        final String delete_sql = "delete from ordermenu where orderNum = " + orderNum;
                        final String delete_sql2 = "delete from order_goods where orderNum = " + orderNum;
                        db.execSQL(delete_sql);
                        db.execSQL(delete_sql2);
                        kitchenActivityOrderLists.remove(position);
                        //네트워크 작업이므로 Thread 생성
                        new Thread(new Runnable() {

                            @Override
                            public void run() {
                                // TODO Auto-generated method stub
                                try {
                                    int PORT = 10002; //서버에서 설정한 PORT 번호
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
                                    communications.add(new SendCommunication(delete_sql, delete_sql2));

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
                                    communications.add(new SendCommunication(delete_sql, delete_sql2));

                                    String json = gson.toJson(communications);

                                    os.writeUTF(json);  //서버로 메세지 보내기.UTF 방식으로(한글 전송가능...)
                                    os.flush();        //다음 메세지 전송을 위해 연결통로의 버퍼를 지워주는 메소드..
                                } catch (IOException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                            }//run method..
                        }).start(); //Thread 실행..
                        kitchenActivityAdapter.notifyDataSetChanged();
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = alert.show();

                //주문내역 글자크기조절
                TextView msgView = (TextView) dialog.findViewById(android.R.id.message);
                msgView.setTextSize(20);
            }
        });

        runnable = new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            kitchenActivityOrderLists.clear();
                            Cursor c1 = db.rawQuery("select tableNum, orderNum from ordermenu where orderComplete = 0;", null);
                            while (c1.moveToNext()) {
                                String tableNum = c1.getString(0);
                                String orderNum = c1.getString(1);
                                kitchenActivityOrderLists.add(new KitchenActivityOrderList(tableNum, orderNum));
                            }
                            kitchenActivityAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        };
        thread = new Thread(runnable);
        thread.start();
    }
}

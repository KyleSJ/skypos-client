package com.example.skyposexample;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by HanKyul on 2016-12-26.
 */

public class Payment extends Activity {

    private MySQLiteOpenHelper helper;
    String dbName = "skyPos.db";
    int dbVersion = 1; // 데이터베이스 버전
    private SQLiteDatabase db;
    String tag = "SQLite"; // Log 에 사용할 tag

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment);

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
        } catch (SQLiteException e) {
            e.printStackTrace();
            Log.e(tag, "데이터베이스를 얻어올 수 없음");
            finish(); // 액티비티 종료
        }

        Intent intent = getIntent();

        final String table_no = intent.getStringExtra("table_no");
        final String userPos = intent.getStringExtra("userPos");

        //결제시간
        final long payTime = System.currentTimeMillis();

        //결제 버튼 클릭
        Button cash = (Button)findViewById(R.id.cash);
        Button card = (Button)findViewById(R.id.card);
        cash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean check = true;
                while(check){
                    //orderNum, openDay 가져오기
                    String orderNum = "";
                    String openDay = "";
                    String select_sql = "select orderNum, openDay from ordermenu where payComplete = 0 and tableNum =" + table_no;
                    Cursor c = db.rawQuery(select_sql, null);
                    while (c.moveToNext()) {
                        orderNum = c.getString(0);
                        openDay = c.getString(1);

                        //cmplxPayNum 가져오기
                        String cmplxPayNum = "";
                        Cursor c2 = db.rawQuery("select cmplxPayNum from cmplx_pay order by cmplxPayNum asc", null);
                        while (c2.moveToNext()) {
                            cmplxPayNum = c2.getString(0);
                        }
                        c2.close();
                        if (cmplxPayNum == "")
                            cmplxPayNum = "0";
                        int cmplx_pay_cmplxPayNum = Integer.parseInt(cmplxPayNum) + 1;
                        cmplxPayNum = Integer.toString(cmplx_pay_cmplxPayNum);

                        int total = 0;
                        String totalCalcuration = "select g.goodsPrice, k.goodsQntt from goods g, ordermenu o, order_goods k where o.orderNum = k.orderNum and o.openDay = k.openDay and g.goodsNum = k.goodsNum and o.payComplete = 0 and o.tableNum = " + table_no;
                        Cursor c4 = db.rawQuery(totalCalcuration, null);
                        while(c4.moveToNext()){
                            String goodsPrice = c4.getString(0);
                            String goodsQntt = c4.getString(1);
                            total += Integer.parseInt(goodsPrice) * Integer.parseInt(goodsQntt);
                        }

                        //insert cmplx_pay
                        final String insert_cmplx_pay = "insert into cmplx_pay (cmplxPayNum, payTime, totalPayAmnt, orderNum, openDay) values(" + cmplxPayNum + ",'" + payTime + "'," + total + "," + orderNum + ",'" + openDay + "')";
                        db.execSQL(insert_cmplx_pay);

                        //payNum 가져오기
                        String payNum = "";
                        Cursor c3 = db.rawQuery("select payNum from pay order by payNum asc", null);
                        while (c3.moveToNext()) {
                            payNum = c3.getString(0);
                        }
                        c3.close();
                        if (payNum == "")
                            payNum = "0";
                        int pay_payNum = Integer.parseInt(payNum) + 1;
                        payNum = Integer.toString(pay_payNum);

                        //insert pay
                        final String insert_pay = "insert into pay (payNum, cmplxPayNum, payWay, payAmnt) values(" + payNum + "," + cmplxPayNum + ", 'cash', " + total + ")";
                        db.execSQL(insert_pay);
                        //네트워크 작업이므로 Thread 생성
                        new Thread(new Runnable() {

                            @Override
                            public void run() {
                                // TODO Auto-generated method stub
                                try {
                                    int PORT = 10005; //서버에서 설정한 PORT 번호
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
                                    communications.add(new SendCommunication(insert_cmplx_pay, insert_pay));

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
                                int PORT = 10005; //서버에서 설정한 PORT 번호
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
                                    communications.add(new SendCommunication(insert_cmplx_pay, insert_pay));

                                    String json = gson.toJson(communications);

                                    os.writeUTF(json);  //서버로 메세지 보내기.UTF 방식으로(한글 전송가능...)
                                    os.flush();        //다음 메세지 전송을 위해 연결통로의 버퍼를 지워주는 메소드..
                                } catch (IOException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                            }//run method..
                        }).start(); //Thread 실행..
                    }
                    c.close();
                    //update ordermenu
                    final String update_sql = "update ordermenu set payComplete = 1 where tableNum = " + table_no;
                    db.execSQL(update_sql);
                    //네트워크 작업이므로 Thread 생성
                    new Thread(new Runnable() {

                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            try {
                                int PORT = 10007; //서버에서 설정한 PORT 번호
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
                                int PORT = 10007; //서버에서 설정한 PORT 번호
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
                    //while 탈출
                    check = false;
                }
                Intent intent = new Intent(Payment.this, FrontOrder.class);
                intent.putExtra("table_no", table_no);
                intent.putExtra("userPos", userPos);
                startActivity(intent);
                finish();
            }
        });
        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean check = true;
                while(check){
                    //orderNum, openDay 가져오기
                    String orderNum = "";
                    String openDay = "";
                    String select_sql = "select orderNum, openDay from ordermenu where payComplete = 0 and tableNum =" + table_no;
                    Cursor c = db.rawQuery(select_sql, null);
                    while (c.moveToNext()) {
                        orderNum = c.getString(0);
                        openDay = c.getString(1);
                        //cmplxPayNum 가져오기
                        String cmplxPayNum = "";
                        Cursor c2 = db.rawQuery("select cmplxPayNum from cmplx_pay order by cmplxPayNum asc", null);
                        while (c2.moveToNext()) {
                            cmplxPayNum = c2.getString(0);
                        }
                        c2.close();
                        if (cmplxPayNum == "")
                            cmplxPayNum = "0";
                        int cmplx_pay_cmplxPayNum = Integer.parseInt(cmplxPayNum) + 1;
                        cmplxPayNum = Integer.toString(cmplx_pay_cmplxPayNum);

                        int total = 0;
                        String totalCalcuration = "select g.goodsPrice, k.goodsQntt from goods g, ordermenu o, order_goods k where o.orderNum = k.orderNum and o.openDay = k.openDay and g.goodsNum = k.goodsNum and o.payComplete = 0 and o.tableNum = " + table_no;
                        Cursor c4 = db.rawQuery(totalCalcuration, null);
                        while(c4.moveToNext()){
                            String goodsPrice = c4.getString(0);
                            String goodsQntt = c4.getString(1);
                            total += Integer.parseInt(goodsPrice) * Integer.parseInt(goodsQntt);
                        }

                        //insert cmplx_pay
                        final String insert_cmplx_pay = "insert into cmplx_pay (cmplxPayNum, payTime, totalPayAmnt, orderNum, openDay) values(" + cmplxPayNum + ",'" + payTime + "'," + total + "," + orderNum + ",'" + openDay + "')";
                        db.execSQL(insert_cmplx_pay);

                        //payNum 가져오기
                        String payNum = "";
                        Cursor c3 = db.rawQuery("select payNum from pay order by payNum asc", null);
                        while (c3.moveToNext()) {
                            payNum = c3.getString(0);
                        }
                        c3.close();
                        if (payNum == "")
                            payNum = "0";
                        int pay_payNum = Integer.parseInt(payNum) + 1;
                        payNum = Integer.toString(pay_payNum);

                        //insert pay
                        final String insert_pay = "insert into pay (payNum, cmplxPayNum, payWay, payAmnt) values(" + payNum + "," + cmplxPayNum + ", 'card', " + total +")";
                        db.execSQL(insert_pay);
                        //네트워크 작업이므로 Thread 생성
                        new Thread(new Runnable() {

                            @Override
                            public void run() {
                                // TODO Auto-generated method stub
                                try {
                                    int PORT = 10006; //서버에서 설정한 PORT 번호
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
                                    communications.add(new SendCommunication(insert_cmplx_pay, insert_pay));

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
                                int PORT = 10006; //서버에서 설정한 PORT 번호
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
                                    communications.add(new SendCommunication(insert_cmplx_pay, insert_pay));

                                    String json = gson.toJson(communications);

                                    os.writeUTF(json);  //서버로 메세지 보내기.UTF 방식으로(한글 전송가능...)
                                    os.flush();        //다음 메세지 전송을 위해 연결통로의 버퍼를 지워주는 메소드..
                                } catch (IOException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                            }//run method..
                        }).start(); //Thread 실행..
                    }
                    c.close();
                    //update ordermenu
                    final String update_sql = "update ordermenu set payComplete = 1 where tableNum = " + table_no;
                    db.execSQL(update_sql);
                    //네트워크 작업이므로 Thread 생성
                    new Thread(new Runnable() {

                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            try {
                                int PORT = 10007; //서버에서 설정한 PORT 번호
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
                                int PORT = 10007; //서버에서 설정한 PORT 번호
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
                    //while 탈출
                    check = false;
                }
                Intent intent = new Intent(Payment.this, FrontOrder.class);
                intent.putExtra("table_no", table_no);
                intent.putExtra("userPos", userPos);
                startActivity(intent);
                finish();
            }
        });
    }
}
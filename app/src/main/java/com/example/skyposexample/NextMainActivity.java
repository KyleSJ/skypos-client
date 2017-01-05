package com.example.skyposexample;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by HanKyul on 2016-11-25.
 */

public class NextMainActivity extends Activity {

    private MySQLiteOpenHelper helper;
    String dbName = "skyPos.db";
    int dbVersion = 1; // 데이터베이스 버전
    private SQLiteDatabase db;
    String tag = "SQLite"; // Log 에 사용할 tag

    final static int PORT=10001;
    final static int PORT2=10002;
    final static int PORT3=10003;
    final static int PORT4=10004;
    final static int PORT5=10005;
    final static int PORT6=10006;
    final static int PORT7=10007;
    ServerSocket serversocket, serversocket2, serversocket3, serversocket4, serversocket5, serversocket6, serversocket7;
    Socket socket, socket2, socket3, socket4, socket5, socket6, socket7;
    DataInputStream is, is2, is3, is4, is5, is6, is7;
    DataOutputStream os, os2, os3, os4, os5, os6, os7;

    String msg="123";

    Runnable runnable1, runnable2, runnable3, runnable4, runnable5, runnable6, runnable7;
    Thread thread1, thread2, thread3, thread4, thread5, thread6, thread7;

    String userPos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next_main);

        Intent intent = getIntent();
        userPos = intent.getStringExtra("userPos");

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

        // 클라이언트 간 쓰레드를 이용한 소켓 통신
        runnable1 = new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                        serversocket = new ServerSocket(PORT);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        Log.d("run()", "ServerSocket");
                    }

                    try {
                        socket = serversocket.accept();

                        is= new DataInputStream(socket.getInputStream()); //클라이언트로 부터 메세지를 받기 위한 통로
                        os= new DataOutputStream(socket.getOutputStream()); //클라이언트로 메세지를 보내기 위한 통로
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        Log.d("run()", "socket");
                    }

                    try {
                        Thread.sleep(100);
                        msg=is.readUTF();
                        Gson gson = new Gson();
                        ReceiveCommunication[] receiveCommunications = gson.fromJson(msg, ReceiveCommunication[].class);
                        for(int i=0; i<receiveCommunications.length; i++){
                            db.execSQL(receiveCommunications[i].getInsertOrderMenu());
                            db.execSQL(receiveCommunications[i].getInsertOrderGoods());
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            Log.d("msg", msg);
                        }
                    });
                }
            }
        };

        runnable2 = new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                        serversocket2 = new ServerSocket(PORT2);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        Log.d("run()", "ServerSocket");
                    }

                    try {
                        socket2 = serversocket2.accept();

                        is2= new DataInputStream(socket2.getInputStream()); //클라이언트로 부터 메세지를 받기 위한 통로
                        os2= new DataOutputStream(socket2.getOutputStream()); //클라이언트로 메세지를 보내기 위한 통로
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        Log.d("run()", "socket");
                    }

                    try {
                        Thread.sleep(100);
                        msg=is2.readUTF();
                        Gson gson = new Gson();
                        ReceiveCommunication[] receiveCommunications = gson.fromJson(msg, ReceiveCommunication[].class);
                        for(int i=0; i<receiveCommunications.length; i++){
                            db.execSQL(receiveCommunications[i].getInsertOrderMenu());
                            db.execSQL(receiveCommunications[i].getInsertOrderGoods());
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            Log.d("msg", msg);
                        }
                    });
                }
            }
        };

        // 클라이언트 간 쓰레드를 이용한 소켓 통신
        runnable3 = new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                        serversocket3 = new ServerSocket(PORT3);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        Log.d("run()", "ServerSocket");
                    }

                    try {
                        socket3 = serversocket3.accept();

                        is3= new DataInputStream(socket3.getInputStream()); //클라이언트로 부터 메세지를 받기 위한 통로
                        os3= new DataOutputStream(socket3.getOutputStream()); //클라이언트로 메세지를 보내기 위한 통로
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        Log.d("run()", "socket");
                    }

                    try {
                        Thread.sleep(100);
                        msg=is3.readUTF();
                        db.execSQL(msg);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            Log.d("msg", msg);
                        }
                    });
                }
            }
        };

        runnable4 = new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                        serversocket4 = new ServerSocket(PORT4);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        Log.d("run()", "ServerSocket");
                    }

                    try {
                        socket4 = serversocket4.accept();

                        is4= new DataInputStream(socket4.getInputStream()); //클라이언트로 부터 메세지를 받기 위한 통로
                        os4= new DataOutputStream(socket4.getOutputStream()); //클라이언트로 메세지를 보내기 위한 통로
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        Log.d("run()", "socket");
                    }

                    try {
                        Thread.sleep(100);
                        msg=is4.readUTF();
                        Gson gson = new Gson();
                        ReceiveCommunication[] receiveCommunications = gson.fromJson(msg, ReceiveCommunication[].class);
                        for(int i=0; i<receiveCommunications.length; i++){
                            db.execSQL(receiveCommunications[i].getInsertOrderMenu());
                            db.execSQL(receiveCommunications[i].getInsertOrderGoods());
                        }
                        delete();
                        Intent intentHome = new Intent(NextMainActivity.this, MainActivity.class);
                        intentHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intentHome.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intentHome);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            Log.d("msg", msg);
                        }
                    });
                }
            }
        };

        runnable5 = new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                        serversocket5 = new ServerSocket(PORT5);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        Log.d("run()", "ServerSocket");
                    }

                    try {
                        socket5 = serversocket5.accept();

                        is5= new DataInputStream(socket5.getInputStream()); //클라이언트로 부터 메세지를 받기 위한 통로
                        os5= new DataOutputStream(socket5.getOutputStream()); //클라이언트로 메세지를 보내기 위한 통로
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        Log.d("run()", "socket");
                    }

                    try {
                        Thread.sleep(100);
                        msg=is5.readUTF();
                        Gson gson = new Gson();
                        ReceiveCommunication[] receiveCommunications = gson.fromJson(msg, ReceiveCommunication[].class);
                        for(int i=0; i<receiveCommunications.length; i++){
                            db.execSQL(receiveCommunications[i].getInsertOrderMenu());
                            db.execSQL(receiveCommunications[i].getInsertOrderGoods());
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            Log.d("msg", msg);
                        }
                    });
                }
            }
        };

        runnable6 = new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                        serversocket6 = new ServerSocket(PORT6);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        Log.d("run()", "ServerSocket");
                    }

                    try {
                        socket6 = serversocket6.accept();

                        is6= new DataInputStream(socket6.getInputStream()); //클라이언트로 부터 메세지를 받기 위한 통로
                        os6= new DataOutputStream(socket6.getOutputStream()); //클라이언트로 메세지를 보내기 위한 통로
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        Log.d("run()", "socket");
                    }

                    try {
                        Thread.sleep(100);
                        msg=is6.readUTF();
                        Gson gson = new Gson();
                        ReceiveCommunication[] receiveCommunications = gson.fromJson(msg, ReceiveCommunication[].class);
                        for(int i=0; i<receiveCommunications.length; i++){
                            db.execSQL(receiveCommunications[i].getInsertOrderMenu());
                            db.execSQL(receiveCommunications[i].getInsertOrderGoods());
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            Log.d("msg", msg);
                        }
                    });
                }
            }
        };

        // 클라이언트 간 쓰레드를 이용한 소켓 통신
        runnable7 = new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                        serversocket7 = new ServerSocket(PORT7);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        Log.d("run()", "ServerSocket");
                    }

                    try {
                        socket7 = serversocket7.accept();

                        is7= new DataInputStream(socket7.getInputStream()); //클라이언트로 부터 메세지를 받기 위한 통로
                        os7= new DataOutputStream(socket7.getOutputStream()); //클라이언트로 메세지를 보내기 위한 통로
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        Log.d("run()", "socket");
                    }

                    try {
                        Thread.sleep(1000);
                        msg=is7.readUTF();
                        Thread.sleep(500);
                        db.execSQL(msg);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            Log.d("msg", msg);
                        }
                    });
                }
            }
        };

        thread1 = new Thread(runnable1);
        thread2 = new Thread(runnable2);
        thread3 = new Thread(runnable3);
        thread4 = new Thread(runnable4);
        thread5 = new Thread(runnable5);
        thread6 = new Thread(runnable6);
        thread7 = new Thread(runnable7);
        thread1.start();
        thread2.start();
        thread3.start();
        thread4.start();
        thread5.start();
        thread6.start();
        thread7.start();

        Button front = (Button)findViewById(R.id.front);
        Button kitchen = (Button)findViewById(R.id.kitchen);
        Button server = (Button) findViewById(R.id.server);

        front.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NextMainActivity.this, FrontActivity.class);
                intent.putExtra("userPos", userPos);
                startActivity(intent);
            }
        });
        kitchen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NextMainActivity.this, KitchenActivity.class);
                startActivity(intent);
            }
        });
        server.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NextMainActivity.this, ServerActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder alert = new AlertDialog.Builder(NextMainActivity.this);
        alert.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
                dialog.dismiss();
            }
        });
        alert.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.setMessage("로그인 창으로 나가겠습니까?") ;
        alert.show();
    }

    void delete() {
        //어플 실행할 때마다 DataBase 내의 데이터 초기화
        db.execSQL("delete from pay");
        db.execSQL("delete from cmplx_pay");
        db.execSQL("delete from order_goods");
        db.execSQL("delete from ordermenu");
        db.execSQL("delete from seattable");
        db.execSQL("delete from calcu");
        db.execSQL("delete from print");
        db.execSQL("delete from open");
        db.execSQL("delete from goods");
        db.execSQL("delete from emp");
        db.execSQL("delete from biz_clnt");
        db.execSQL("delete from table_cat");
        db.execSQL("delete from goods_cat");
        db.execSQL("delete from ext_dev");
        db.execSQL("delete from card_compa");
        db.execSQL("delete from calcu_chng_rec");
        db.execSQL("delete from van");
    }
}

package com.example.skyposexample;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

public class ServerActivity extends Activity {

    private MySQLiteOpenHelper helper;
    String dbName = "skyPos.db";
    int dbVersion = 1; // 데이터베이스 버전
    private SQLiteDatabase db;
    String tag = "SQLite"; // Log 에 사용할 tag

    //테이블번호, 총액 나열할 리스트
    ArrayList<ServerActivityOrderList> tableInfo = new ArrayList<ServerActivityOrderList>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.server_main);

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
        while(c.moveToNext()){
            String tableNumber = c.getString(0);
            //테이블별 총액 계산
            int orderAmnt = 0;
            String sql = "select orderAmnt from ordermenu where payComplete = 0 and tableNum = " + tableNumber;
            Cursor c1 = db.rawQuery(sql, null);
            while(c1.moveToNext()){
                int Amnt = Integer.parseInt(c1.getString(0));
                orderAmnt += Amnt;
            }
            tableInfo.add(new ServerActivityOrderList(tableNumber, String.valueOf(orderAmnt))); // 리스트뷰에 목록 추가
        }
        c.close();

        ServerActivityAdapter serverActivityAdapter = new ServerActivityAdapter(
                getApplicationContext(),
                R.layout.server_list,
                tableInfo);

        // adapterView - ListView 연결
        final ListView lv = (ListView) findViewById(R.id.listView1);
        lv.setAdapter(serverActivityAdapter);

        // 리스트뷰 항목 클릭 이벤트
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ServerActivity.this, ServerActivity2.class);
                intent.putExtra("table_no", tableInfo.get(position).table_no);
                startActivity(intent);
                finish();
            }
        });
    }
}// end of class
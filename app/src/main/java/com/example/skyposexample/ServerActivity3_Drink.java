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

public class ServerActivity3_Drink extends Activity {

    private MySQLiteOpenHelper helper;
    String dbName = "skyPos.db";
    int dbVersion = 1; // 데이터베이스 버전
    private SQLiteDatabase db;
    String tag = "SQLite"; // Log 에 사용할 tag

    //음식 이름, 가격 리스트뷰
    ArrayList<TabFoodInfo> tabFoodInfos = new ArrayList<TabFoodInfo>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.food);

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

        Cursor c = db.rawQuery("select * from goods where goodsCatNum = 2;", null);
        while(c.moveToNext()){
            String menuName = c.getString(3);
            String menuPrice = c.getString(4);
            tabFoodInfos.add(new TabFoodInfo(menuName,menuPrice)); // 리스트뷰에 목록 추가
        }
        c.close();

        ServerActivity3TabAdapter serverActivity3TabAdapter = new ServerActivity3TabAdapter(
                getApplicationContext(),
                R.layout.custom_item,
                tabFoodInfos);

        // adapterView - ListView 연결
        ListView lv3 = (ListView) findViewById(R.id.listview3);
        lv3.setAdapter(serverActivity3TabAdapter);

        // 리스트뷰 클릭 이벤트(팝업)
        lv3.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ServerActivity3_Drink.this, ServerActivity3TabMenu.class);
                intent.putExtra("food_name", tabFoodInfos.get(position).food_name);
                intent.putExtra("food_price", tabFoodInfos.get(position).num);
                startActivity(intent);
            }
        });
    }
}
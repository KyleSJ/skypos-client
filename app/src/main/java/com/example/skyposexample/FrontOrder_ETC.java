package com.example.skyposexample;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by HanKyul on 2016-08-25.
 */
public class FrontOrder_ETC extends Activity {

    private MySQLiteOpenHelper helper;
    String dbName = "skyPos.db";
    int dbVersion = 1; // 데이터베이스 버전
    private SQLiteDatabase db;
    String tag = "SQLite"; // Log 에 사용할 tag

    ArrayList<String> grid = new ArrayList<String>();
    ArrayList<FrontOrderMenuList> frontOrderMenuLists = new ArrayList<FrontOrderMenuList>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.etc2);

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

        TextView tv = (TextView)findViewById(R.id.textView);

        Intent intent2 = getIntent();
        tv.setText(intent2.getStringExtra("table_no"));
        String table_no = intent2.getStringExtra("table_no");
        tv.setVisibility(View.GONE);

        Cursor c = db.rawQuery("select * from goods where goodsCatNum = 3;", null);
        while(c.moveToNext()){
            String menuName = c.getString(3);
            String menuPrice = c.getString(4);
            grid.add(menuName + "\n\n" + menuPrice); // 그리드뷰에 목록 추가
            frontOrderMenuLists.add(new FrontOrderMenuList(menuName, menuPrice));
        }
        c.close();

        FrontOrderTabGridAdapter frontOrderTabGridAdapter = new FrontOrderTabGridAdapter(
                getApplicationContext(),
                R.layout.grid_item,
                grid,
                frontOrderMenuLists,
                table_no,
                this);

        GridView gridView = (GridView) findViewById(R.id.gridView1);
        gridView.setAdapter(frontOrderTabGridAdapter);
    }
}

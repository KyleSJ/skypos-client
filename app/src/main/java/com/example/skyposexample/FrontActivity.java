package com.example.skyposexample;

import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ListView;

import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by 성쟈 on 2016-08-18.
 */
public class FrontActivity extends ActionBarActivity {

    private MySQLiteOpenHelper helper;
    String dbName = "skyPos.db";
    int dbVersion = 1; // 데이터베이스 버전
    private SQLiteDatabase db;
    String tag = "SQLite"; // Log 에 사용할 tag

    private String[] menuList = { "통계" , "내역" , "설정" , "종료" };
    private ActionBarDrawerToggle DrawerToggle;
    private ListView listView;
    private FrameLayout flContainer;
    private DrawerLayout drawerLayout;
    private CharSequence Title = "메뉴";

    ArrayList<String> table_grid = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.front_main);

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

        Intent intent = getIntent();
        final String userPos = intent.getStringExtra("userPos");
        Log.d("userPos", userPos);

        Title = getTitle();
        listView = (ListView) findViewById(R.id.left_drawer);
        flContainer = (FrameLayout) findViewById(R.id.content_frame);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.navigation_menu, menuList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new DrawerItemClickListener());

        //정산 버튼
        Button calcuration = (Button)findViewById(R.id.calcuration);
        calcuration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //정산 변경 이력
                //정산 변경 번호 받아오기
                String insertCalcuChngNum = "";
                Cursor c20 = db.rawQuery("select calcuChngNum from calcu_chng_rec order by calcuChngNum asc", null);
                while (c20.moveToNext()) {
                    insertCalcuChngNum = c20.getString(0);
                }
                c20.close();
                if(insertCalcuChngNum == "")
                    insertCalcuChngNum = "0";
                int calcu_chng_rec_calcuChngNum = Integer.parseInt(insertCalcuChngNum) + 1;
                insertCalcuChngNum = Integer.toString(calcu_chng_rec_calcuChngNum);

                //moneySales 구하기
                int insertMoneySales = 0;
                String moneySales_sql = "select o.orderAmnt from ordermenu o, cmplx_pay c, pay p where o.orderNum = c.orderNum and o.openDay = c.openDay and c.cmplxPayNum = p.cmplxPayNum";
                Cursor c21 = db.rawQuery(moneySales_sql, null);
                while (c21.moveToNext()) {
                    insertMoneySales += Integer.parseInt(c21.getString(0));
                }
                //cardSales 구하기
                int insertCardSales = 0;
                String cardSales_sql = "select o.orderAmnt from ordermenu o, cmplx_pay c, pay p where o.orderNum = c.orderNum and o.openDay = c.openDay and c.cmplxPayNum = p.cmplxPayNum and p.payWay = 'card'";
                Cursor c22 = db.rawQuery(cardSales_sql, null);
                while (c22.moveToNext()) {
                    insertCardSales += Integer.parseInt(c22.getString(0));
                }

                //insert calcu_chng_rec
                final String insertCalcuChngRec = "insert into calcu_chng_rec(calcuChngNum, moneySales, cardSales) values(" + insertCalcuChngNum + "," + insertMoneySales + "," + insertCardSales + ")";
                db.execSQL(insertCalcuChngRec);

                //정산일자(=개시일자) 가져오기
                String insertCalcuDay = "";
                Cursor c23 = db.rawQuery("select openDay from open", null);
                while (c23.moveToNext()) {
                    insertCalcuDay = c23.getString(0);
                }
                c23.close();

                //insert calcu
                final String insertCalcu = "insert into calcu(calcuDay, calcuChngNum, openDay) values('" + insertCalcuDay + "'," + insertCalcuChngNum + ",'" + insertCalcuDay + "')";
                db.execSQL(insertCalcu);

                //네트워크 작업이므로 Thread 생성
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        try {
                            int PORT = 10004; //서버에서 설정한 PORT 번호
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
                            communications.add(new SendCommunication(insertCalcuChngRec, insertCalcu));

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
                        int PORT = 10004; //서버에서 설정한 PORT 번호
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
                            communications.add(new SendCommunication(insertCalcuChngRec, insertCalcu));

                            String json = gson.toJson(communications);

                            os.writeUTF(json);  //서버로 메세지 보내기.UTF 방식으로(한글 전송가능...)
                            os.flush();        //다음 메세지 전송을 위해 연결통로의 버퍼를 지워주는 메소드..
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }//run method..
                }).start(); //Thread 실행..

                Gson gson = new Gson();
                ArrayList<vanVO> van = new ArrayList<>();
                ArrayList<calcu_chng_recVO> calcu_chng_rec = new ArrayList<>();
                ArrayList<card_compaVO> card_compa = new ArrayList<>();
                ArrayList<ext_devVO> ext_dev = new ArrayList<>();
                ArrayList<goodsCatVO> goods_cat = new ArrayList<>();
                ArrayList<table_catVO> table_cat = new ArrayList<>();
                ArrayList<biz_clntVO> biz_clnt = new ArrayList<>();
                ArrayList<empVO> emp = new ArrayList<>();
                ArrayList<goodsVO> goods = new ArrayList<>();
                ArrayList<openVO> open = new ArrayList<>();
                ArrayList<printVO> print = new ArrayList<>();
                ArrayList<calcuVO> calcu = new ArrayList<>();
                ArrayList<seattableVO> seattable = new ArrayList<>();
                ArrayList<ordermenuVO> ordermenu = new ArrayList<>();
                ArrayList<order_goodsVO> order_goods = new ArrayList<>();
                ArrayList<cmplx_payVO> cmplx_pay = new ArrayList<>();
                ArrayList<payVO> pay = new ArrayList<>();

                Cursor c1 = db.rawQuery("select vanNum, vanIP, vanName from van;",null);
                while(c1.moveToNext()){
                    String vanNum = c1.getString(0);
                    String vanIP = c1.getString(1);
                    String vanName = c1.getString(2);
                    van.add(new vanVO(vanNum, vanIP, vanName));
                }
                Cursor c2 = db.rawQuery("select calcuChngNum, calcuChngDay, calcuChngTime, bakDay, bakTime, moneySales, cardSales from calcu_chng_rec;",null);
                while(c2.moveToNext()){
                    String calcuChngNum = c2.getString(0);
                    String calcuChngDay = c2.getString(1);
                    //String calcuChngTime = c2.getString(2);
                    //TimeStamp 변환을 위한 변수명 변경
                    String ClonecalcuChngTime = c2.getString(2);
                    String bakDay = c2.getString(3);
                    //String bakTime = c2.getString(4);
                    //TimeStamp 변환을 위한 변수명 변경
                    String ClonebakTime = c2.getString(4);
                    String moneySales = c2.getString(5);
                    String cardSales = c2.getString(6);
                    calcu_chng_rec.add(new calcu_chng_recVO(calcuChngNum, calcuChngDay, ClonecalcuChngTime, bakDay, ClonebakTime, moneySales, cardSales));
                }
                Cursor c3 = db.rawQuery("select cardCompaNum, cardCompaName, cardCompaPhoneNum from card_compa;",null);
                while(c3.moveToNext()){
                    String cardCompaNum = c3.getString(0);
                    String cardCompaName = c3.getString(1);
                    String cardCompaPhoneNum = c3.getString(2);
                    card_compa.add(new card_compaVO(cardCompaNum, cardCompaName, cardCompaPhoneNum));
                }
                Cursor c4 = db.rawQuery("select devName, devType, prtcl from ext_dev;",null);
                while(c4.moveToNext()){
                    String devName = c4.getString(0);
                    String devType = c4.getString(1);
                    String prtcl = c4.getString(2);
                    ext_dev.add(new ext_devVO(devName, devType, prtcl));
                }
                Cursor c5 = db.rawQuery("select goodsCatNum, goodsCatName, goodsCatLoc from goods_cat;",null);
                while(c5.moveToNext()){
                    String goodsCatNum = c5.getString(0);
                    String goodsCatName = c5.getString(1);
                    String goodsCatLoc = c5.getString(2);
                    goods_cat.add(new goodsCatVO(goodsCatNum, goodsCatName, goodsCatLoc));
                }
                Cursor c6 = db.rawQuery("select tableCatNum, tableCatName, tableCatLoc from table_cat;",null);
                while(c6.moveToNext()){
                    String tableCatNum = c6.getString(0);
                    String tableCatName = c6.getString(1);
                    String tableCatLoc = c6.getString(2);
                    table_cat.add(new table_catVO(tableCatNum, tableCatName, tableCatLoc));
                }
                Cursor c7 = db.rawQuery("select posNum, regNum, vanNum, repreName, compaName, phoneNum, addr from biz_clnt;", null);
                while(c7.moveToNext()){
                    String posNum = c7.getString(0);
                    String regNum = c7.getString(1);
                    String vanNum = c7.getString(2);
                    String repreName = c7.getString(3);
                    String compaName = c7.getString(4);
                    String phoneNum = c7.getString(5);
                    String addr = c7.getString(6);
                    biz_clnt.add(new biz_clntVO(posNum, regNum, vanNum, repreName, compaName, phoneNum, addr));
                }
                Cursor c8 = db.rawQuery("select empID, posNum, empName, pwd from emp;",null);
                while(c8.moveToNext()){
                    String empID = c8.getString(0);
                    String posNum = c8.getString(1);
                    String empName = c8.getString(2);
                    String pwd = c8.getString(3);
                    emp.add(new empVO(empID, posNum, empName, pwd));
                }
                Cursor c9 = db.rawQuery("select goodsNum, goodsCatNum, goodsColor, goodsName, goodsPrice, goodsSeq from goods;",null);
                while(c9.moveToNext()){
                    String goodsNum = c9.getString(0);
                    String goodsCatNum = c9.getString(1);
                    String goodsColor = c9.getString(2);
                    String goodsName = c9.getString(3);
                    String goodsPrice = c9.getString(4);
                    String goodsSeq = c9.getString(5);
                    goods.add(new goodsVO(goodsNum, goodsCatNum, goodsColor, goodsName, goodsPrice, goodsSeq));
                }
                Cursor c10 = db.rawQuery("select openDay, empID, posNum, cashAmnt from open;",null);
                while(c10.moveToNext()){
                    String openDay = c10.getString(0);
                    String empID = c10.getString(1);
                    String posNum = c10.getString(2);
                    String cashAmnt = c10.getString(3);
                    open.add(new openVO(openDay, empID, posNum, cashAmnt));
                }
                Cursor c11 = db.rawQuery("select printNum, devName, printName, printCntt from print;",null);
                while(c11.moveToNext()){
                    String printNum = c11.getString(0);
                    String devName = c11.getString(1);
                    String printName = c11.getString(2);
                    String printCntt = c11.getString(3);
                    print.add(new printVO(printNum, devName, printName, printCntt));
                }
                Cursor c12 = db.rawQuery("select calcuDay, calcuChngNum, printNum, openDay from calcu;",null);
                while(c12.moveToNext()){
                    String calcuDay = c12.getString(0);
                    String calcuChngNum = c12.getString(1);
                    String printNum = c12.getString(2);
                    String openDay = c12.getString(3);
                    calcu.add(new calcuVO(calcuDay, calcuChngNum, printNum, openDay));
                }
                Cursor c13 = db.rawQuery("select tableNum, tableLoc, tableColor, tableCatNum, tableName from seattable;",null);
                while(c13.moveToNext()){
                    String tableNum = c13.getString(0);
                    String tableLoc = c13.getString(1);
                    String tableColor = c13.getString(2);
                    String tableCatNum = c13.getString(3);
                    String tableName = c13.getString(4);
                    seattable.add(new seattableVO(tableNum, tableLoc, tableColor, tableCatNum, tableName));
                }
                Cursor c14 = db.rawQuery("select orderNum, openDay, printNum, tableNum, orderTime, orderAmnt from ordermenu;",null);
                while(c14.moveToNext()){
                    String orderNum = c14.getString(0);
                    String openDay = c14.getString(1);
                    String printNum = c14.getString(2);
                    String tableNum = c14.getString(3);
                    //String orderTime = c14.getString(4);
                    String CloneorderTime = c14.getString(4);
                    String orderAmnt = c14.getString(5);
                    ordermenu.add(new ordermenuVO(orderNum, openDay, printNum, tableNum, CloneorderTime, orderAmnt));
                }
                Cursor c15 = db.rawQuery("select orderGoodsNum, goodsNum, orderNum, goodsQntt, openDay from order_goods;",null);
                while(c15.moveToNext()){
                    String orderGoodsNum = c15.getString(0);
                    String goodsNum = c15.getString(1);
                    String orderNum = c15.getString(2);
                    String goodsQntt = c15.getString(3);
                    String openDay = c15.getString(4);
                    order_goods.add(new order_goodsVO(orderGoodsNum, goodsNum, orderNum, goodsQntt, openDay));
                }
                Cursor c16 = db.rawQuery("select cmplxPayNum, printNum, payTime, orderNum, totalPayAmnt, openDay from cmplx_pay;",null);
                while(c16.moveToNext()){
                    String cmplxPayNum = c16.getString(0);
                    String printNum = c16.getString(1);
                    //String payTime = c16.getString(2);
                    String ClonepayTime = c16.getString(2);
                    String orderNum = c16.getString(3);
                    String totalPayAmnt = c16.getString(4);
                    String openDay = c16.getString(5);
                    cmplx_pay.add(new cmplx_payVO(cmplxPayNum, printNum, ClonepayTime, totalPayAmnt, orderNum, openDay));
                }
                Cursor c17 = db.rawQuery("select payNum, cmplxPayNum, cardCompaNum, payWay, cardNum, payAmnt from pay;",null);
                while(c17.moveToNext()){
                    String payNum = c17.getString(0);
                    String cmplxPayNum = c17.getString(1);
                    String cardCompaNum = c17.getString(2);
                    String payWay = c17.getString(3);
                    String cardNum = c17.getString(4);
                    String payAmnt = c17.getString(5);
                    pay.add(new payVO(payNum, cmplxPayNum, cardCompaNum, payWay, cardNum, payAmnt));
                }
                String json1 = gson.toJson(van);
                String json2 = gson.toJson(calcu_chng_rec);
                String json3 = gson.toJson(card_compa);
                String json4 = gson.toJson(ext_dev);
                String json5 = gson.toJson(goods_cat);
                String json6 = gson.toJson(table_cat);
                String json7 = gson.toJson(biz_clnt);
                String json8 = gson.toJson(emp);
                String json9 = gson.toJson(goods);
                String json10 = gson.toJson(open);
                String json11 = gson.toJson(print);
                String json12 = gson.toJson(calcu);
                String json13 = gson.toJson(seattable);
                String json14 = gson.toJson(ordermenu);
                String json15 = gson.toJson(order_goods);
                String json16 = gson.toJson(cmplx_pay);
                String json17 = gson.toJson(pay);
                FrontActivity.NetworkTask networkTask = new FrontActivity.NetworkTask();
                Map<String,String> params = new HashMap<String,String>();
                params.put("posNum", userPos);
                params.put("van", json1);
                params.put("calcu_chng_rec", json2);
                params.put("card_compa", json3);
                params.put("ext_dev", json4);
                params.put("goods_cat", json5);
                params.put("table_cat", json6);
                params.put("biz_clnt", json7);
                params.put("emp", json8);
                params.put("goods", json9);
                params.put("open", json10);
                params.put("print", json11);
                params.put("calcu", json12);
                params.put("seattable", json13);
                params.put("ordermenu", json14);
                params.put("order_goods", json15);
                params.put("cmplx_pay", json16);
                params.put("pay", json17);
                networkTask.execute(params);

                Intent intentHome = new Intent(FrontActivity.this, MainActivity.class);
                intentHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intentHome.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intentHome);
            }
        });

        //네비게이션바
        DrawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.drawable.ic_drawer,
                R.string.open_drawer,
                R.string.close_drawer
        ) {

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };

        drawerLayout.setDrawerListener(DrawerToggle);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //테이블 클릭시 주문페이지로 이동
        Cursor c = db.rawQuery("select * from seattable;", null);
        while (c.moveToNext()) {
            String tableNumber = c.getString(0);
            table_grid.add(tableNumber); // 리스트뷰에 목록 추가
        }
        c.close();

            FrontActivityGridAdapter frontActivityGridAdapter = new FrontActivityGridAdapter(
                getApplicationContext(),
                R.layout.grid_item,
                table_grid,
                userPos,
                this);

        GridView gridView = (GridView)findViewById(R.id.gridView2);
        gridView.setAdapter(frontActivityGridAdapter);
    }

    //네비게이션바 동기화
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        DrawerToggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (DrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        DrawerToggle.onConfigurationChanged(newConfig);
    }

    //네비게이션바 각 메뉴 클릭시
    private class DrawerItemClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> adapter, View view, int position,
                                long id) {
            switch (position) {
                case 0:
                    Intent intent = new Intent(FrontActivity.this,emptyPage.class);
                    startActivity(intent);
                    break;
                case 1:
                    Intent intent1 = new Intent(FrontActivity.this,emptyPage.class);
                    startActivity(intent1);
                    break;
                case 2:
                    Intent intent2 = new Intent(FrontActivity.this,emptyPage.class);
                    startActivity(intent2);
                    break;
                case 3: //어플 종료
                    Intent intentHome = new Intent(FrontActivity.this, MainActivity.class);
                    intentHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intentHome.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intentHome);
                    break;
            }
            drawerLayout.closeDrawer(listView);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the main; this adds items to the action bar if it is present.
        return true;
    }

    //networkTask를 이용해서 정보를 주고받아
    public class NetworkTask extends AsyncTask<Map<String,String>, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Map<String, String>... maps) {
            HttpClient.Builder http = new HttpClient.Builder("POST", "http://192.168.0.26:8080/android/receive");

            http.addAllParameters(maps[0]);

            HttpClient post = http.create();
            post.request();

            int statusCode = post.getHttpStatusCode();

            String body = post.getBody();

            return body;
        }
    }
}

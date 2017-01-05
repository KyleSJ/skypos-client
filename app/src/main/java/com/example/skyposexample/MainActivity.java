package com.example.skyposexample;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.example.skyposexample.R.id.posNum;

public class MainActivity extends Activity {

    private MySQLiteOpenHelper helper;
    String dbName = "skyPos.db";
    int dbVersion = 1; // 데이터베이스 버전
    private SQLiteDatabase db;
    String tag = "SQLite"; // Log 에 사용할 tag

    String userPos;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        Button btn = (Button) findViewById(R.id.btnLogin);
        //버튼 클릭시 로그인정보 전달
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //기기의 IP 가져와서 sIP에 저장
                WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                int ipAddress = wifiInfo.getIpAddress();
                String sIp = String.format("%d.%d.%d.%d",
                        (ipAddress & 0xff),
                        (ipAddress >> 8 & 0xff),
                        (ipAddress >> 16 & 0xff),
                        (ipAddress >> 24 & 0xff));

                //ID, PW, posNum, IP 서버에 보냄
                EditText ID=(EditText)findViewById(R.id.txtID);
                EditText PW=(EditText)findViewById(R.id.txtPW);
                EditText POS=(EditText)findViewById(posNum);
                NetworkTask networkTask = new NetworkTask();
                Map<String,String> params = new HashMap<String,String>();
                userID = ID.getText().toString();
                String userPW = PW.getText().toString();
                userPos = POS.getText().toString();
                params.put("IP", sIp);
                params.put("empId", userID);
                params.put("pwd", userPW);
                params.put("posNum", userPos);
                networkTask.execute(params);

                /*
                Intent intent = new Intent(MainActivity.this, NextMainActivity.class);
                intent.putExtra("userPos", "20");
                startActivity(intent);
                */
            }
        });
    }
    //networkTask를 이용해서 정보를 주고받아
    public class NetworkTask extends AsyncTask<Map<String,String>, Integer, String> {

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Map<String, String>... maps) {
            HttpClient.Builder http = new HttpClient.Builder("POST", "http://192.168.0.26:8080/android/login");
            HttpClient.Builder http2 = new HttpClient.Builder("POST", "http://192.168.0.26:8080/android/send");

            http.addAllParameters(maps[0]);
            http2.addAllParameters(maps[0]);

            HttpClient post = http.create();
            HttpClient post2 = http2.create();
            post.request();
            post2.request();

            int statusCode = post.getHttpStatusCode();
            int statusCode2 = post2.getHttpStatusCode();

            String body = post.getBody();
            String body2 = post2.getBody();

            if(body == "") {
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                startActivity(intent);
            } else {
                if(body2 == "") {
                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(MainActivity.this, NextMainActivity.class);
                    intent.putExtra("userPos", userPos);
                    startActivity(intent);
                }
            }
            return body2;
        }

        @Override
        protected void onPostExecute(String s) {
            Log.d("onPostExecute2", s);

            Gson gson = new Gson();
            ServerReceiveData[][] data = gson.fromJson(s, ServerReceiveData[][].class);

            if(s == "")
                finish();
            else {
                delete();
                for(int i=0; i < data[0].length; i++) {
                    String van = "insert into van values(" + data[0][i].getVanNum() + "," + data[0][i].getVanIP() + ",'" + data[0][i].getVanName() + "');";
                    db.execSQL(van);
                }
                /*
                Collections.sort(Arrays.asList(data[1]), new CalcuChngRec_AscCompare());
                for(int i=0; i < data[1].length; i++) {
                    String calcu_chng_rec = "insert into calcu_chng_rec values(" + data[1][i].getCalcuChngNum() + ",'" + data[1][i].getCalcuChngDay() + "', '" + data[1][i].getCalcuChngTime() + "', '" + data[1][i].getBakDay() + "','" + data[1][i].getBakTime() +"', " + data[1][i].getMoneySales() +"," + data[1][i].getCardSales() + ");";
                    db.execSQL(calcu_chng_rec);
                }*/
                for(int i=0; i < data[1].length; i++) {
                    String card_compa = "insert into card_compa values(" + data[1][i].getCardCompaNum() + ",'" + data[1][i].getCardCompaName() + "','" + data[1][i].getCardCompaPhoneNum() + "');";
                    db.execSQL(card_compa);
                }
                for(int i=0; i < data[2].length; i++) {
                    String ext_dev = "insert into ext_dev values('" + data[2][i].getDevName() + "','" + data[2][i].getDevType() + "','" + data[2][i].getPrtcl() + "');";
                    db.execSQL(ext_dev);
                }
                for(int i=0; i < data[3].length; i++) {
                    String goods_cat = "insert into goods_cat values(" + data[3][i].getGoodsCatNum() + ",'" + data[3][i].getGoodsCatName() + "'," + data[3][i].getGoodsCatLoc() +");";
                    db.execSQL(goods_cat);
                }
                for(int i=0; i < data[4].length; i++) {
                    String table_cat = "insert into table_cat values(" + data[4][i].getTableCatNum() + ",'" + data[4][i].getTableCatName() + "'," + data[4][i].getTableCatLoc() + ");";
                    db.execSQL(table_cat);
                }
                for(int i=0; i < data[5].length; i++) {
                    String biz_clnt = "insert into biz_clnt values(" + data[5][i].getPosNum() + "," + data[5][i].getRegNum() + "," + data[5][i].getVanNum() + ",'" + data[5][i].getRepreName() + "','" + data[5][i].getCompaName() + "','" + data[5][i].getPhoneNum() + "','" + data[5][i].getAddr() + "','" + data[5][i].getIP() + "');";
                    db.execSQL(biz_clnt);
                }
                for(int i=0; i < data[6].length; i++) {
                    String emp = "insert into emp values('" + data[6][i].getEmpId() + "'," + data[6][i].getPosNum() + ",'" + data[6][i].getEmpName() + "','" + data[6][i].getPwd() + "');";
                    db.execSQL(emp);
                }
                Collections.sort(Arrays.asList(data[7]), new Goods_AscCompare());
                for(int i=0; i < data[7].length; i++) {
                    String goods = "insert into goods values(" + data[7][i].getGoodsNum() + "," + data[7][i].getGoodsCatNum() + "," + data[7][i].getGoodsColor() + ",'" + data[7][i].getGoodsName() + "'," + data[7][i].getGoodsPrice() + "," + data[7][i].getGoodsSeq() + ");";
                    db.execSQL(goods);
                }
                /*
                for(int i=0; i < data[8].length; i++) {
                    String open = "insert into open values('" + data[8][i].getOpenDay() + "','" + data[8][i].getEmpId() + "'," + data[8][i].getPosNum() + "," + data[8][i].getCashAmnt() + ");";
                    db.execSQL(open);
                }*/

                String openDay = getDataString();
                String insertOpen = "insert into open values('" + openDay + "','" + userID + "','" + userPos + "',0)";
                db.execSQL(insertOpen);

                Collections.sort(Arrays.asList(data[8]), new Print_AscCompare());
                for(int i=0; i < data[8].length; i++) {
                    String print = "insert into print values(" + data[8][i].getPrintNum() + ",'" + data[8][i].getDevName() + "','" + data[8][i].getPrintName() + "','" + data[8][i].getPrintCntt() + "');";
                    db.execSQL(print);
                }
                /*
                for(int i=0; i < data[11].length; i++) {
                    String calcu = "insert into calcu values('" + data[11][i].getCalcuDay() + "'," + data[11][i].getCalcuChngNum() + "," + data[11][i].getPrintNum() + ",'" + data[11][i].getOpenDay() + "');";
                    db.execSQL(calcu);
                }*/
                Collections.sort(Arrays.asList(data[9]), new SeatTable_AscCompare());
                for(int i=0; i < data[9].length; i++) {
                    String seattable = "insert into seattable values(" + data[9][i].getTableNum() + "," + data[9][i].getTableLoc() + "," + data[9][i].getTableColor() + "," + data[9][i].getTableCatNum() + ",'" + data[9][i].getTableName() + "');";
                    db.execSQL(seattable);
                }
                /*
                Collections.sort(Arrays.asList(data[13]), new OrderMenu_AscCompare());
                for(int i=0; i < data[13].length; i++) {
                    String ordermenu = "insert into ordermenu values(" + data[13][i].getOrderNum() + ",'" + data[13][i].getOpenDay() + "'," + data[13][i].getPrintNum() + "," + data[13][i].getTableNum() + ",'" + data[13][i].getOrderTime() + "'," + data[13][i].getOrderAmnt() + "," + data[13][i].getOrderComplete() + "," + data[13][i].getPayComplete() + ");";
                    db.execSQL(ordermenu);
                }*/
                /*
                Collections.sort(Arrays.asList(data[14]), new OrderGoods_AscCompare());
                for(int i=0; i < data[14].length; i++) {
                    String order_goods = "insert into order_goods values(" + data[14][i].getOrderGoodsNum() + "," + data[14][i].getGoodsNum() + "," + data[14][i].getOrderNum() + "," + data[14][i].getGoodsQntt() + ",'" + data[14][i].getOpenDay() + "');";
                    db.execSQL(order_goods);
                }*/
                /*
                for(int i=0; i < data[15].length; i++) {
                    String cmplx_pay = "insert into cmplx_pay values(" + data[15][i].getCmplxPayNum() + "," + data[15][i].getPrintNum() + "," + data[15][i].getOrderNum() + ",'" + data[15][i].getPayTime() + "','" + data[15][i].getOpenDay() + "'," + data[15][i].getTotalPayAmnt() + ");";
                    db.execSQL(cmplx_pay);
                }*/
                /*
                for(int i=0; i < data[16].length; i++) {
                    String pay = "insert into pay values(" + data[16][i].getPayNum() + "," + data[16][i].getCmplxPayNum() + "," + data[16][i].getCardCompaNum() + ",'" + data[16][i].getPayWay() + "'," + data[16][i].getCardNum() + "," + data[16][i].getPayAmnt() +");";
                    db.execSQL(pay);
                }*/
            }
        }
        private class CalcuChngRec_AscCompare implements Comparator<ServerReceiveData> {
            @Override
            public int compare(ServerReceiveData o1, ServerReceiveData o2) {
                return o1.getCalcuChngNum().compareTo(o2.getCalcuChngNum());
            }
        }
        private class Goods_AscCompare implements Comparator<ServerReceiveData> {
            @Override
            public int compare(ServerReceiveData o1, ServerReceiveData o2) {
                return o1.getGoodsNum().compareTo(o2.getGoodsNum());
            }
        }
        private class Print_AscCompare implements Comparator<ServerReceiveData> {
            @Override
            public int compare(ServerReceiveData o1, ServerReceiveData o2) {
                return o1.getPrintNum().compareTo(o2.getPrintNum());
            }
        }
        private class SeatTable_AscCompare implements Comparator<ServerReceiveData> {
            @Override
            public int compare(ServerReceiveData o1, ServerReceiveData o2) {
                return o1.getTableNum().compareTo(o2.getTableNum());
            }
        }
        private class OrderMenu_AscCompare implements Comparator<ServerReceiveData> {
            @Override
            public int compare(ServerReceiveData o1, ServerReceiveData o2) {
                return o1.getOrderNum().compareTo(o2.getOrderNum());
            }
        }
        private class OrderGoods_AscCompare implements Comparator<ServerReceiveData> {
            @Override
            public int compare(ServerReceiveData o1, ServerReceiveData o2) {
                return o1.getOrderGoodsNum().compareTo(o2.getOrderGoodsNum());
            }
        }
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
    public String getDataString() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
        String dtr_date = df.format(new Date());

        return dtr_date;
    }
}
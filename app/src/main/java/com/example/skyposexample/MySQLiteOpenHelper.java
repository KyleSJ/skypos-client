package com.example.skyposexample;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by HanKyul on 2016-09-22.
 */
public class MySQLiteOpenHelper extends SQLiteOpenHelper {
    // 안드로이드에서 SQLite 데이터 베이스를 쉽게 사용할 수 있도록 도와주는 클래스
    public MySQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 최초에 데이터베이스가 없을경우, 데이터베이스 생성을 위해 호출됨
        // 테이블 생성하는 코드를 작성한다
        String sql1 = "create table van(vanNum integer not null, vanIP integer default null, vanName text default null, primary key(vanNum));";
        String sql2 = "create table calcu_chng_rec(calcuChngNum integer not null, calcuChngDay text default null, calcuChngTime text default null, bakDay text default null, bakTime text default null, moneySales integer default null, cardSales integer default null, primary key(calcuChngNum));";
        String sql3 = "create table card_compa(cardCompaNum integer not null, cardCompaName text default null, cardCompaPhoneNum text default null, primary key(cardCompaNum));";
        String sql4 = "create table cmmn_code(codeNum integer not null, codeName text default null, primary key(codeNum));";
        String sql5 = "create table ext_dev(devName text not null, devType text default null, prtcl text default null, primary key(devName));";
        String sql6 = "create table goods_cat(goodsCatNum integer not null, goodsCatName text default null, goodsCatLoc integer default null, primary key(goodsCatNum));";
        String sql7 = "create table table_cat(tableCatNum integer not null, tableCatName text default null, tableCatLoc integer default null, primary key(tableCatNum));";
        String sql8 = "create table biz_clnt(posNum integer not null, regNum integer default null, vanNum integer default null, repreName text default null, compaName text default null, phoneNum text default null, addr text default null, ip text defalut null, primary key(posNum), foreign key(vanNum) references van(vanNum));";
        String sql9 = "create table detal_code(detalCode text not null, codeNum integer not null, codeDesc text default null, primary key(detalCode, codeNum), foreign key(codeNum) references cmmn_code(codeNUM));";
        String sql10 = "create table emp(empID text not null, posNum integer not null, empName text default null, pwd text default null, primary key(empID, posNum), foreign key(posNum) references biz_clnt(posNum));";
        String sql11 = "create table goods(goodsNum integer not null, goodsCatNum integer default null, goodsColor integer default null, goodsName text default null, goodsPrice integer default null, goodsSeq integer default null, primary key(goodsNum), foreign key(goodsCatNum) references goods_cat(goodsCatNum));";
        String sql12 = "create table open(openDay text not null, empID text default null, posNum integer not null, cashAmnt integer default null, primary key(openDay, posNum), foreign key(empID) references emp(empID), foreign key(posNum) references emp(posNum));";
        String sql13 = "create table print(printNum integer not null, devName text default null, printName text default null, printCntt text default null, primary key(printNum), foreign key(devName) references ext_dev(devName));";
        String sql14 = "create table calcu(calcuDay text not null, calcuChngNum integer not null, printNum integer default null, openDay text default null, primary key(calcuDay, calcuChngNum), foreign key(calcuChngNum) references calcu_chng_rec(calcuChngNum), foreign key(openDay) references open(openDay), foreign key(printNum) references print(printNum));";
        String sql15 = "create table seattable(tableNum integer not null, tableLoc integer default null, tableColor integer default null, tableCatNum integer default null, tableName text default null, primary key(tableNum), foreign key(tableCatNum) references table_cat(tableCatNum));";
        String sql16 = "create table ordermenu(orderNum integer not null, openDay text not null, printNum integer default null, tableNum integer default null, orderTime text default null, orderAmnt integer default null, orderComplete integer default null, payComplete integer default null, primary key(orderNum, openDay), foreign key(openDay) references open(openDay), foreign key(printNum) references print(printNum), foreign key(tableNum) references seattable(tableNum));";
        String sql17 = "create table order_goods(orderGoodsNum integer not null, goodsNum integer default null, orderNum integer default null, goodsQntt integer default null, openDay text default null, primary key(orderGoodsNum), foreign key(goodsNum) references goods(goodsNum), foreign key(openDay) references open(openDay), foreign key(orderNum) references ordermenu(orderNum));";
        String sql18 = "create table cmplx_pay(cmplxPayNum integer not null, printNum integer default null, orderNum integer default null, payTime text default null, openDay text default null, totalPayAmnt integer default null, primary key(cmplxPayNum), foreign key(orderNum) references ordermenu(orderNum), foreign key(openDay) references ordermenu(openDay), foreign key(printNum) references print(printNum));";
        String sql19 = "create table pay(payNum integer not null, cmplxPayNum integer default null, cardCompaNum integer default null, payWay text default null, cardNum integer default null, payAmnt integer default null, primary key(payNum), foreign key(cardCompaNum) references card_compa(cardCompaNum), foreign key(cmplxPayNum) references cmplx_pay(cmplxPayNum));";
        db.execSQL(sql1);
        db.execSQL(sql2);
        db.execSQL(sql3);
        db.execSQL(sql4);
        db.execSQL(sql5);
        db.execSQL(sql6);
        db.execSQL(sql7);
        db.execSQL(sql8);
        db.execSQL(sql9);
        db.execSQL(sql10);
        db.execSQL(sql11);
        db.execSQL(sql12);
        db.execSQL(sql13);
        db.execSQL(sql14);
        db.execSQL(sql15);
        db.execSQL(sql16);
        db.execSQL(sql17);
        db.execSQL(sql18);
        db.execSQL(sql19);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 데이터베이스의 버전이 바뀌었을 때 호출되는 콜백 메서드
        // 버전 바뀌었을 때 기존데이터베이스를 어떻게 변경할 것인지 작성한다
        // 각 버전의 변경 내용들을 버전마다 작성해야함
        String sql1 = "drop table pay;"; // 테이블 드랍
        String sql2 = "drop table cmplx_pay;";
        String sql3 = "drop table order_goods;";
        String sql4 = "drop table ordermenu;";
        String sql5 = "drop table seattable;";
        String sql6 = "drop table calcu;";
        String sql7 = "drop table print;";
        String sql8 = "drop table open;";
        String sql9 = "drop table goods;";
        String sql10 = "drop table emp;";
        String sql11 = "drop table detal_code;";
        String sql12 = "drop table biz_clnt;";
        String sql13 = "drop table table_cat;";
        String sql14 = "drop table goods_cat;";
        String sql15 = "drop table ext_dev;";
        String sql16 = "drop table cmmn_code;";
        String sql17 = "drop table card_compa;";
        String sql18 = "drop table calcu_chng_rec;";
        String sql19 = "drop table van;";
        db.execSQL(sql1);
        db.execSQL(sql2);
        db.execSQL(sql3);
        db.execSQL(sql4);
        db.execSQL(sql5);
        db.execSQL(sql6);
        db.execSQL(sql7);
        db.execSQL(sql8);
        db.execSQL(sql9);
        db.execSQL(sql10);
        db.execSQL(sql11);
        db.execSQL(sql12);
        db.execSQL(sql13);
        db.execSQL(sql14);
        db.execSQL(sql15);
        db.execSQL(sql16);
        db.execSQL(sql17);
        db.execSQL(sql18);
        db.execSQL(sql19);
        onCreate(db); // 다시 테이블 생성
    }
}
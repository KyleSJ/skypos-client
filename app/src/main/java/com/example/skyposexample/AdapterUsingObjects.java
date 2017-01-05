package com.example.skyposexample;

/**
 * Created by HanKyul on 2017-01-02.
 */

//FrontOrderTabMenuList
//FrontOrderTabGridAdapter - list
//프론트 메뉴판
class FrontOrderMenuList {
    String food_name = "";
    String food_price = "";
    public FrontOrderMenuList(String food, String pri) {
        this.food_name = food;
        this.food_price = pri;
    }
}

//FrontOrderList
//FrontOrderAdapter - list
//프론트 주문 리스트
class FrontOrderList {
    String orderNum = "";
    String orderGoodsNum = "";
    String food_name = "";
    String price = "";
    String number = "";
    public FrontOrderList(String orderNum, String orderGoodsNum, String food, String pri, String num) {
        this.orderNum = orderNum;
        this.orderGoodsNum = orderGoodsNum;
        this.food_name = food;
        this.price = pri;
        this.number = num;
    }
}

//KitchenActivityOrderList
//KitchenActivityAdapter - list
//주방 확인용 주문 목록
class KitchenActivityOrderList {
    String table_no = "";
    String order = "";

    public KitchenActivityOrderList(String table, String order) {
        this.table_no = table;
        this.order = order;
    }
}

//ServerActivity2 list
//ServerActivity2Adapter - list
//테이블별 주문 목록
class ServerActivity2_list {
    String orderNum = "";
    String orderGoodsNum = "";
    String food_name = "";
    String price = "";
    String number = "";
    public ServerActivity2_list(String orderNum, String orderGoodsNum, String food, String pri, String num) {
        this.orderNum = orderNum;
        this.orderGoodsNum = orderGoodsNum;
        this.food_name = food;
        this.price = pri;
        this.number = num;
    }
}

//ServerActivity3 list
//ServerActivity3Adapter - list
//탭 페이지 선택 상품 주문목록으로 이동
class SelectInfo {
    String food_name = "";
    String price = "";
    String number = "";
    public SelectInfo(String food, String pri, String num) {
        this.food_name = food;
        this.price = pri;
        this.number = num;
    }
}

//ServerActivity3TabMenuList
//ServerActivity3TabAdapter - list
//서버 메뉴판
class TabFoodInfo {
    String food_name = "";
    String num = "";
    public TabFoodInfo(String food_name, String num) {
        this.food_name = food_name;
        this.num = num;
    }
}

//ServerActivityOrderList
//ServerActivityAdater - list
//서버 테이블별 총액
class ServerActivityOrderList {
    String table_no = "";
    String total = "";
    public ServerActivityOrderList(String table, String tot) {
        this.table_no = table;
        this.total = tot;
    }
}
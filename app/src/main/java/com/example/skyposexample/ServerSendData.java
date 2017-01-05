package com.example.skyposexample;

/**
 * Created by HanKyul on 2017-01-02.
 */

//biz_clnt
class biz_clntVO {
    private String posNum;
    private String regNum;
    private String vanNum;
    private String repreName;
    private String compaName;
    private String phoneNum;
    private String addr;
    public biz_clntVO(String posNum, String regNum, String vanNum, String repreName, String compaName, String phoneNum, String addr) {
        this.posNum = posNum;
        this.regNum = regNum;
        this.vanNum = vanNum;
        this.repreName = repreName;
        this.compaName = compaName;
        this.phoneNum = phoneNum;
        this.addr = addr;
    }
}

//calcu_chng_rec
class calcu_chng_recVO {
    private String calcuChngNum;
    private String calcuChngDay;
    private String ClonecalcuChngTime;
    private String bakDay;
    private String ClonebakTime;
    private String moneySales;
    private String cardSales;
    public calcu_chng_recVO(String calcuChngNum, String calcuChngDay, String ClonecalcuChngTime, String bakDay, String ClonebakTime, String moneySales, String cardSales) {
        this.calcuChngNum = calcuChngNum;
        this.calcuChngDay = calcuChngDay;
        this.ClonecalcuChngTime = ClonecalcuChngTime;
        this.bakDay = bakDay;
        this.ClonebakTime = ClonebakTime;
        this.moneySales = moneySales;
        this.cardSales = cardSales;
    }
}

//calcu
class calcuVO {
    private String calcuDay;
    private String calcuChngNum;
    private String printNum;
    private String openDay;
    public calcuVO(String calcuDay, String calcuChngNum, String printNum, String openDay){
        this.calcuDay = calcuDay;
        this.calcuChngNum = calcuChngNum;
        this.printNum = printNum;
        this.openDay = openDay;
    }
}

//card_compa
class card_compaVO {
    private String cardCompaNum;
    private String cardCompaName;
    private String cardCompaPhoneNum;
    public card_compaVO(String cardCompaNum, String cardCompaName, String cardCompaPhoneNum) {
        this.cardCompaNum = cardCompaNum;
        this.cardCompaName = cardCompaName;
        this.cardCompaPhoneNum = cardCompaPhoneNum;
    }
}

//cmplx_pay
class cmplx_payVO {
    private String cmplxPayNum;
    private String printNum;
    private String openDay;
    private String ClonepayTime;
    private String orderNum;
    private String totalPayAmnt;
    public cmplx_payVO(String cmplxPayNum, String printNum, String ClonepayTime, String totalPayAmnt, String orderNum, String openDay) {
        this.cmplxPayNum = cmplxPayNum;
        this.printNum = printNum;
        this.ClonepayTime = ClonepayTime;
        this.totalPayAmnt = totalPayAmnt;
        this.orderNum = orderNum;
        this.openDay = openDay;
    }
}

//emp
class empVO {
    private String empId;
    private String posNum;
    private String empName;
    private String pwd;
    public empVO(String empId, String posNum, String empName, String pwd) {
        this.empId = empId;
        this.posNum = posNum;
        this.empName = empName;
        this.pwd = pwd;
    }
}

//ext_dev
class ext_devVO {
    private String devName;
    private String devType;
    private String prtcl;
    public ext_devVO(String devName, String devType, String prtcl) {
        this.devName = devName;
        this.devType = devType;
        this.prtcl = prtcl;
    }
}

//goodsCat
class goodsCatVO {
    private String goodsCatNum;
    private String goodsCatName;
    private String goodsCatLoc;
    public goodsCatVO(String goodsCatNum, String goodsCatName, String goodsCatLoc) {
        this.goodsCatNum = goodsCatNum;
        this.goodsCatName = goodsCatName;
        this.goodsCatLoc = goodsCatLoc;
    }
}

//goods
class goodsVO {
    private String goodsNum;
    private String goodsCatNum;
    private String goodsColor;
    private String goodsName;
    private String goodsPrice;
    private String goodsSeq;
    public goodsVO(String goodsNum, String goodsCatNum, String goodsColor, String goodsName, String goodsPrice, String goodsSeq) {
        this.goodsNum = goodsNum;
        this.goodsCatNum = goodsCatNum;
        this.goodsColor = goodsColor;
        this.goodsName = goodsName;
        this.goodsPrice = goodsPrice;
        this.goodsSeq = goodsSeq;
    }
}

//open
class openVO {
    private String openDay;
    private String empId;
    private String posNum;
    private String cashAmnt;

    public openVO(String openDay, String empId, String posNum, String cashAmnt) {
        this.openDay = openDay;
        this.empId = empId;
        this.posNum = posNum;
        this.cashAmnt = cashAmnt;
    }
}

//order_goods
class order_goodsVO {
    private String orderGoodsNum;
    private String goodsNum;
    private String orderNum;
    private String goodsQntt;
    private String openDay;

    public order_goodsVO(String orderGoodsNum, String goodsNum, String orderNum, String goodsQntt, String openDay) {
        this.orderGoodsNum = orderGoodsNum;
        this.goodsNum = goodsNum;
        this.orderNum = orderNum;
        this.goodsQntt = goodsQntt;
        this.openDay = openDay;
    }
}

//ordermenu
class ordermenuVO {
    private String orderNum;
    private String openDay;
    private String printNum;
    private String tableNum;
    private String CloneorderTime;
    private String orderAmnt;

    public ordermenuVO(String orderNum, String openDay, String printNum, String tableNum, String CloneorderTime, String orderAmnt) {
        this.orderNum = orderNum;
        this.openDay = openDay;
        this.printNum = printNum;
        this.tableNum = tableNum;
        this.CloneorderTime = CloneorderTime;
        this.orderAmnt = orderAmnt;
    }
}

//pay
class payVO {
    private String payNum;
    private String cmplxPayNum;
    private String cardCompaNum;
    private String payWay;
    private String cardNum;
    private String payAmnt;

    public payVO(String payNum, String cmplxPayNum, String cardCompaNum, String payWay, String cardNum, String payAmnt) {
        this.payNum = payNum;
        this.cmplxPayNum = cmplxPayNum;
        this.cardCompaNum = cardCompaNum;
        this.payWay = payWay;
        this.cardNum = cardNum;
        this.payAmnt = payAmnt;
    }
}

//print
class printVO {
    private String printNum;
    private String devName;
    private String printName;
    private String printCntt;

    public printVO(String printNum, String devName, String printName, String printCntt) {
        this.printNum = printNum;
        this.devName = devName;
        this.printName = printName;
        this.printCntt = printCntt;
    }
}

//seattable
class seattableVO {
    private String tableNum;
    private String tableLoc;
    private String tableColor;
    private String tableCatNum;
    private String tableName;

    public seattableVO(String tableNum, String tableLoc, String tableColor, String tableCatNum, String tableName) {
        this.tableNum = tableNum;
        this.tableLoc = tableLoc;
        this.tableColor = tableColor;
        this.tableCatNum = tableCatNum;
        this.tableName = tableName;
    }
}

//table_cat
class table_catVO {
    private String tableCatNum;
    private String tableCatName;
    private String tableCatLoc;

    public table_catVO(String tableCatNum, String tableCatName, String tableCatLoc) {
        this.tableCatNum = tableCatNum;
        this.tableCatName = tableCatName;
        this.tableCatLoc = tableCatLoc;
    }
}

//van
class vanVO {
    private String vanNum;
    private String vanIP;
    private String vanName;

    public vanVO(String vanNum, String vanIP, String vanName) {
        this.vanNum = vanNum;
        this.vanIP = vanIP;
        this.vanName = vanName;
    }
}
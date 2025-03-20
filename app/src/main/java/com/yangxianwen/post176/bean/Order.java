package com.yangxianwen.post176.bean;

public class Order {

    private String cBillCode;
    private String cFoodCode;
    private String cStudCode;
    private int iNumber;
    private double nSum;
    private double nPrice;
    private int iState;
    private String cDinnerTable;
    private String dCreate;

    public String getCBillCode() {
        return cBillCode;
    }

    public void setCBillCode(String cBillCode) {
        this.cBillCode = cBillCode;
    }

    public String getCFoodCode() {
        return cFoodCode;
    }

    public void setCFoodCode(String cFoodCode) {
        this.cFoodCode = cFoodCode;
    }

    public String getCStudCode() {
        return cStudCode;
    }

    public void setCStudCode(String cStudCode) {
        this.cStudCode = cStudCode;
    }

    public int getINumber() {
        return iNumber;
    }

    public void setINumber(int iNumber) {
        this.iNumber = iNumber;
    }

    public double getNSum() {
        return nSum;
    }

    public void setNSum(double nSum) {
        this.nSum = nSum;
    }

    public double getNPrice() {
        return nPrice;
    }

    public void setNPrice(double nPrice) {
        this.nPrice = nPrice;
    }

    public int getIState() {
        return iState;
    }

    public void setIState(int iState) {
        this.iState = iState;
    }

    public String getCDinnerTable() {
        return cDinnerTable;
    }

    public void setCDinnerTable(String cDinnerTable) {
        this.cDinnerTable = cDinnerTable;
    }

    public String getDCreate() {
        return dCreate;
    }

    public void setDCreate(String dCreate) {
        this.dCreate = dCreate;
    }
}

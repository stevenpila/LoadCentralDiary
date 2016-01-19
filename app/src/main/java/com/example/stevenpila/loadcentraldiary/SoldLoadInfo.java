package com.example.stevenpila.loadcentraldiary;

/**
 * Created by Steven on 11/14/2015.
 */
public class SoldLoadInfo {
    public int mId;
    public String mProduct;
    public double mAmount;
    public String mNumber;
    public String mNumberName;
    public String mDatetime;
    public double mBalance;
    public String mDescription;
    public boolean mIsPaid;

    public ProductInfo mProductInfo;

    public SoldLoadInfo(int id, String product, double amount, String number, String dateTime, double balance, String description, boolean isPaid) {
        mId = id;
        mProduct = product;
        mAmount = amount;
        mNumber = number;
        mDatetime = dateTime;
        mBalance = balance;
        mDescription = description;
        mIsPaid = isPaid;
    }
    public SoldLoadInfo(int id, String product, double amount, String number, String dateTime, double balance, String description, boolean isPaid, ProductInfo productInfo) {
        mId = id;
        mProduct = product;
        mAmount = amount;
        mNumber = number;
        mDatetime = dateTime;
        mBalance = balance;
        mDescription = description;
        mIsPaid = isPaid;

        mProductInfo = productInfo;
    }
    public SoldLoadInfo(int id, String product, double amount, String number, String numberName, String dateTime, double balance, String description, boolean isPaid, ProductInfo productInfo) {
        mId = id;
        mProduct = product;
        mAmount = amount;
        mNumber = number;
        mNumberName = numberName;
        mDatetime = dateTime;
        mBalance = balance;
        mDescription = description;
        mIsPaid = isPaid;

        mProductInfo = productInfo;
    }
}
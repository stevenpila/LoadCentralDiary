package com.example.stevenpila.loadcentraldiary;

/**
 * Created by stevenjefferson.pila on 12/10/2015.
 */
public class DepositInfo {
    public int mId;
    public double mAmount;
    public String mDatetime;

    public DepositInfo(int id, double amount, String dateTime) {
        mId = id;
        mAmount = amount;
        mDatetime = dateTime;
    }
}

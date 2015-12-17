package com.example.stevenpila.loadcentraldiary;

/**
 * Created by stevenjefferson.pila on 12/10/2015.
 */
public class HistoryRecordInfo {
    public SoldLoadInfo mSoldLoadInfo = null;
    public DepositInfo mDepositInfo = null;
    public int mId;
    public String mTableName;
    public double mBalance;

    public HistoryRecordInfo(int id, String tableName, double balance, SoldLoadInfo soldLoadInfo) {
        mId = id;
        mTableName = tableName;
        mBalance = balance;
        mSoldLoadInfo = soldLoadInfo;
    }

    public HistoryRecordInfo(int id, String tableName, double balance, DepositInfo depositInfo) {
        mId = id;
        mTableName = tableName;
        mBalance = balance;
        mDepositInfo = depositInfo;
    }
}
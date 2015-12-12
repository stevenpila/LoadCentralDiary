package com.example.stevenpila.loadcentraldiary;

/**
 * Created by Steven on 11/14/2015.
 */
public class SoldLoadInfo {
    public int m_id;
    public String m_product;
    public String m_number;
    public String m_dateTime;
    public double m_balance;
    public String m_description;
    public boolean m_isPaid;
    public boolean m_isValidated;

    public SoldLoadInfo(int id, String product, String number, String dateTime, double balance, String description, boolean isPaid, boolean isValid) {
        m_id = id;
        m_product = product;
        m_number = number;
        m_dateTime = dateTime;
        m_balance = balance;
        m_description = description;
        m_isPaid = isPaid;
        m_isValidated = isValid;
    }
}
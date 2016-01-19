package com.example.stevenpila.loadcentraldiary;

/**
 * Created by Steven Pila on 11/17/2015.
 */
public class ProductCodeInfo {
    public int mId; // id from product_code table
    public String mProduct; // code from product_code table
    public String mProductDescription;  // description from product_code table
    public double mDiscount;    // discount from product_code table

    public String mProductView; // temporary variable for matched strings in edittext

    public ProductCodeInfo() {
        mId = -1;
        mProduct = "";
        mDiscount = 0.0;
        mProductDescription = "";
        mProductView = "";
    }
    public ProductCodeInfo(String product, String product_description, String discount) {
        mId = -1;
        mProduct = product;
        mDiscount = getDiscountFromString(discount);
        mProductDescription = product_description;
        mProductView = product;
    }
    public ProductCodeInfo(String product, String product_description, double discount) {
        mId = -1;
        mProduct = product;
        mDiscount = discount;
        mProductDescription = product_description;
        mProductView = product;
    }

    private double getDiscountFromString(String strDiscount) {
        double discount = -1;

        strDiscount = strDiscount.substring(0, strDiscount.indexOf('%'));
        if(!strDiscount.isEmpty()) {
            discount = Double.parseDouble(strDiscount);
        }

        return discount;
    }
}

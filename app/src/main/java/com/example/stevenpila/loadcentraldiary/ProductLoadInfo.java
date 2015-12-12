package com.example.stevenpila.loadcentraldiary;

/**
 * Created by Steven Pila on 11/17/2015.
 */
public class ProductLoadInfo {
    public String m_product;
    public String m_product_description;
    public double m_discount;

    public String m_product_view;

    public ProductLoadInfo() {
        m_product = "";
        m_product_view = "";
        m_product_description = "";
        m_discount = 0.0;
    }
    public ProductLoadInfo(String product, String product_description, String discount) {
        m_product = product;
        m_product_view = product;
        m_product_description = product_description;
        m_discount = getDiscountFromString(discount);
    }
    public ProductLoadInfo(String product, String product_description, double discount) {
        m_product = product;
        m_product_view = product;
        m_product_description = product_description;
        m_discount = discount;
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

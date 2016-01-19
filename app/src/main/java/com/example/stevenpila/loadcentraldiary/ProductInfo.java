package com.example.stevenpila.loadcentraldiary;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Steven Pila on 11/19/2015.
 */
public class ProductInfo {
    public ProductCodeInfo mProductCodeInfo;

    public String mProductName;
    public String mProductCategoryName;

    public ProductInfo() {
        mProductCodeInfo = new ProductCodeInfo();
        mProductName = "";
        mProductCategoryName = "";
    }
    public ProductInfo(ProductCodeInfo productCodeInfo, String productName, String productCategoryName) {
        mProductCodeInfo = productCodeInfo;
        mProductName = productName;
        mProductCategoryName = productCategoryName;
    }
}

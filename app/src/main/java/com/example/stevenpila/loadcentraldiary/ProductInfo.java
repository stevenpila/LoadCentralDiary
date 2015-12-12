package com.example.stevenpila.loadcentraldiary;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Steven Pila on 11/19/2015.
 */
public class ProductInfo {
    public ArrayList<String> m_product_category_list;
    public ArrayList<ProductLoadInfo> m_product_load_info_list;
    public HashMap<String, ArrayList<String>> m_product_category_name_list;
    public boolean m_is_loaded_successful;

    public ProductInfo() {
        m_product_category_list = new ArrayList<String>();
        m_product_load_info_list = new ArrayList<ProductLoadInfo>();
        m_product_category_name_list = new HashMap<String, ArrayList<String>>();
        m_is_loaded_successful = false;
    }
}

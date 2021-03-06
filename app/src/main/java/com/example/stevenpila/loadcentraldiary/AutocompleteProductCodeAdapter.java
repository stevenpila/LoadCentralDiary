package com.example.stevenpila.loadcentraldiary;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import android.widget.Filter;

/**
 * Created by Steven Pila on 11/20/2015.
 */
public class AutocompleteProductCodeAdapter extends ArrayAdapter<ProductCodeInfo> {
    private final Context m_context;
    private final int m_view_resource_id;
    private final ArrayList<ProductCodeInfo> m_full_product_code_list;
    private ArrayList<ProductCodeInfo> m_current_product_code_list;

    public AutocompleteProductCodeAdapter(Context context, int viewResourceId, ArrayList<ProductCodeInfo> product_code_list) {
        super(context, viewResourceId, product_code_list);

        m_context = context;
        m_view_resource_id = viewResourceId;
        m_full_product_code_list = product_code_list;
        m_current_product_code_list = new ArrayList<>(m_full_product_code_list);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        try {
            if(convertView == null) {
                LayoutInflater layoutInflater = ((SellLoadActivity) m_context).getLayoutInflater();
                convertView = layoutInflater.inflate(m_view_resource_id, parent, false);
            }

            ProductCodeInfo productCodeInfo = m_current_product_code_list.get(position);
            TextView textViewCodeItem = (TextView) convertView.findViewById(R.id.autocompleteTextViewCode);
            TextView textViewDescItem = (TextView) convertView.findViewById(R.id.autocompleteTextViewDesc);

            textViewCodeItem.setText(Html.fromHtml(productCodeInfo.mProductView));
            textViewDescItem.setText(productCodeInfo.mProductDescription);
        } catch (NullPointerException e) {
            MyUtility.logMessage(e.getMessage());
        }

        return convertView;
    }

    @Override
    public int getCount() {
        return m_current_product_code_list.size();
    }

    @Override
    public ProductCodeInfo getItem(int position) {
        return m_current_product_code_list.get(position);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if(constraint != null) {
                    ArrayList<ProductCodeInfo> results = getResults(constraint.toString().toLowerCase()); // get a list of results based from constraint...
                    filterResults.count = results.size();
                    filterResults.values = results;
                }

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if(results != null && results.count > 0) {
                    m_current_product_code_list = (ArrayList<ProductCodeInfo>) results.values;
                    notifyDataSetChanged();
                }
                else
                    notifyDataSetInvalidated();
            }
        };
    }

    private ArrayList<ProductCodeInfo> getResults(String constraint) {
        ArrayList<ProductCodeInfo> results = new ArrayList<>();

        for(ProductCodeInfo item : m_full_product_code_list) {
            if(item.mProduct.toLowerCase().contains(constraint)) {
                String product = item.mProduct.toLowerCase();
                if(product.contains("<amount>")) {
                    product = product.replace("<amount>", "[amount]");
                }
                ProductCodeInfo productCodeInfo = new ProductCodeInfo(item.mProduct, item.mProductDescription, item.mDiscount);
                if(product.contains(constraint)) {
                    String startPart = product.substring(0, product.indexOf(constraint)).trim();
                    String endPart = product.substring(product.indexOf(constraint) + constraint.length()).trim();
                    String newStr = startPart + "<font color=\"#333333\"><b>" + constraint + "</b></font>" + endPart;

                    productCodeInfo.mProductView = newStr.toUpperCase();
                }

                results.add(productCodeInfo);
            }
        }

        return results;
    }
}

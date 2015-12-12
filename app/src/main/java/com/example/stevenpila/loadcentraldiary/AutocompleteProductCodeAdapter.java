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
public class AutocompleteProductCodeAdapter extends ArrayAdapter<ProductLoadInfo> {
    Context m_context;
    private int m_view_resource_id;
    private ArrayList<ProductLoadInfo> m_full_product_code_list;
    private ArrayList<ProductLoadInfo> m_current_product_code_list;

    public AutocompleteProductCodeAdapter(Context context, int viewResourceId, ArrayList<ProductLoadInfo> product_code_list) {
        super(context, viewResourceId, product_code_list);

        m_context = context;
        m_view_resource_id = viewResourceId;
        m_full_product_code_list = product_code_list;
        m_current_product_code_list = new ArrayList<ProductLoadInfo>(m_full_product_code_list);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        try {
            if(convertView == null) {
                LayoutInflater layoutInflater = ((SellLoadActivity) m_context).getLayoutInflater();
                convertView = layoutInflater.inflate(m_view_resource_id, parent, false);
            }

            ProductLoadInfo productLoadInfo = m_current_product_code_list.get(position);
            TextView textViewCodeItem = (TextView) convertView.findViewById(R.id.autocompleteTextViewCode);
            TextView textViewDescItem = (TextView) convertView.findViewById(R.id.autocompleteTextViewDesc);

            textViewCodeItem.setText(Html.fromHtml(productLoadInfo.m_product_view));
            textViewDescItem.setText(productLoadInfo.m_product_description);
        } catch (NullPointerException e) {

        } catch (Exception e) {

        }

        return convertView;
    }

    @Override
    public int getCount() {
        return m_current_product_code_list.size();
    }

    @Override
    public ProductLoadInfo getItem(int position) {
        return m_current_product_code_list.get(position);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if(constraint != null) {
                    ArrayList<ProductLoadInfo> results = getResults(constraint.toString().toLowerCase()); // get a list of results based from constraint...
                    filterResults.count = results.size();
                    filterResults.values = results;
                }

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if(results != null && results.count > 0) {
                    m_current_product_code_list = (ArrayList<ProductLoadInfo>) results.values;
                    notifyDataSetChanged();
                }
                else
                    notifyDataSetInvalidated();
            }
        };
    }

    private ArrayList<ProductLoadInfo> getResults(String constraint) {
        ArrayList<ProductLoadInfo> results = new ArrayList<ProductLoadInfo>();

        for(ProductLoadInfo item : m_full_product_code_list) {
            if(item.m_product.toLowerCase().contains(constraint)) {
                String product = item.m_product.toLowerCase();
                ProductLoadInfo productLoadInfo = new ProductLoadInfo(item.m_product, item.m_product_description, item.m_discount);
                if(product.indexOf(constraint) != -1) {
                    String startPart = product.substring(0, product.indexOf(constraint)).trim();
                    String endPart = product.substring(product.indexOf(constraint) + constraint.length()).trim();
                    String newStr = startPart + "<font color=\"#333333\"><b>" + constraint + "</b></font>" + endPart;
                    productLoadInfo.m_product_view = newStr.toUpperCase();
                }

                results.add(productLoadInfo);
            }
        }

        return results;
    }
}

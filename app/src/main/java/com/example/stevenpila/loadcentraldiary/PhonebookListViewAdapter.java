package com.example.stevenpila.loadcentraldiary;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Steven on 11/30/2015.
 */
public class PhonebookListViewAdapter extends ArrayAdapter<PhonebookInfo> {
    private final ArrayList<PhonebookInfo> m_phonebookInfoFullList;
    private ArrayList<PhonebookInfo> m_phonebookInfoCurrenList;

    public PhonebookListViewAdapter(Context context, ArrayList<PhonebookInfo> phonebookInfos) {
        super(context, 0, phonebookInfos);

        m_phonebookInfoFullList = phonebookInfos;
        m_phonebookInfoCurrenList = new ArrayList<>(m_phonebookInfoFullList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        PhonebookInfo phonebookInfo = m_phonebookInfoCurrenList.get(position);

        if(convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_view_item_phonebook, parent, false);

        TextView text1 = (TextView) convertView.findViewById(R.id.phonebookNameTextView);
        TextView text2 = (TextView) convertView.findViewById(R.id.phonebookNumberTextView);

        text1.setText(phonebookInfo.m_name);
        text2.setText(phonebookInfo.m_number);

        return convertView;
    }

    @Override
    public int getCount() {
        return m_phonebookInfoCurrenList.size();
    }

    @Override
    public PhonebookInfo getItem(int position) {
        return m_phonebookInfoCurrenList.get(position);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if(constraint != null) {
                    ArrayList<PhonebookInfo> results = getResults(constraint.toString().toLowerCase()); // get a list of results based from constraint...
                    filterResults.count = results.size();
                    filterResults.values = results;
                }

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if(results != null && results.count > 0) {
                    m_phonebookInfoCurrenList = (ArrayList<PhonebookInfo>) results.values;
                    notifyDataSetChanged();
                }
                else
                    notifyDataSetInvalidated();
            }
        };
    }

    private ArrayList<PhonebookInfo> getResults(String constraint) {
        ArrayList<PhonebookInfo> results = new ArrayList<>();

        for(PhonebookInfo item : m_phonebookInfoFullList) {
            if(item.m_name.toLowerCase().contains(constraint) || item.m_number.toLowerCase().contains(constraint)) {
                results.add(item);
            }
        }

        return results;
    }
}

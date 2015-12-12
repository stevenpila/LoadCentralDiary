package com.example.stevenpila.loadcentraldiary;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Steven on 11/14/2015.
 */
public class ListViewAdapter extends ArrayAdapter<SoldLoadInfo> {
    public ListViewAdapter(Context context, ArrayList<SoldLoadInfo> soldLoadInfos) {
        super(context, 0, soldLoadInfos);

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SoldLoadInfo soldLoadInfo = getItem(position);

        if(convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_view_item_home, parent, false);

        TextView product = (TextView) convertView.findViewById(R.id.productTextView);
        TextView number = (TextView) convertView.findViewById(R.id.numberTextView);
        TextView dateTime = (TextView) convertView.findViewById(R.id.datetimeTextView);
        TextView balance = (TextView) convertView.findViewById(R.id.balanceTextView);
        TextView description = (TextView) convertView.findViewById(R.id.descriptionTextView);
        // TODO - add for description

        product.setText(soldLoadInfo.m_product);
        number.setText(soldLoadInfo.m_number);
        dateTime.setText(MyUtility.getDate(soldLoadInfo.m_dateTime));
        balance.setText(MyUtility.PESO_SIGN + String.format("%.2f", soldLoadInfo.m_balance));
        description.setText(soldLoadInfo.m_description);

        ImageView paidImage = (ImageView) convertView.findViewById(R.id.homeListViewItemPaidImage);
        ImageView validImage = (ImageView) convertView.findViewById(R.id.homeListViewItemValidImage);   // TODO - check if load sold is confirmed from loadcentral
        ImageView infoImage = (ImageView) convertView.findViewById(R.id.homeListViewItemInfoImage);

        if(soldLoadInfo.m_isPaid) {
//            convertView.setBackgroundColor(getContext().getResources().getColor(R.color.colorRed));
            paidImage.setBackgroundColor(getContext().getResources().getColor(R.color.colorGreen));
        }
        else {
//            convertView.setBackgroundColor(getContext().getResources().getColor(R.color.colorDefault));
            paidImage.setBackgroundColor(getContext().getResources().getColor(R.color.colorRed));
        }
        if(soldLoadInfo.m_isValidated)
            validImage.setBackgroundColor(getContext().getResources().getColor(R.color.colorGreen));
        else
            validImage.setBackgroundColor(getContext().getResources().getColor(R.color.colorRed));

        if(!soldLoadInfo.m_description.isEmpty())
            infoImage.setVisibility(View.VISIBLE);
        else
            infoImage.setVisibility(View.INVISIBLE);


        return convertView;
    }
}

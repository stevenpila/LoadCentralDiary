package com.example.stevenpila.loadcentraldiary;

import android.content.Context;
import android.text.Html;
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
public class ListViewAdapter extends ArrayAdapter<HistoryRecordInfo> {
    public ListViewAdapter(Context context, ArrayList<HistoryRecordInfo> historyRecordInfos) {
        super(context, 0, historyRecordInfos);

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        HistoryRecordInfo historyRecordInfo = getItem(position);

        if(historyRecordInfo.mTableName.equals(DatabaseHandler.TABLE_SELL_LOAD)) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_view_item_home, parent, false);
        }
        else if(historyRecordInfo.mTableName.equals(DatabaseHandler.TABLE_DEPOSIT)) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_view_item_home_2, parent, false);
        }

        if(historyRecordInfo.mTableName.equals(DatabaseHandler.TABLE_SELL_LOAD)) {
            TextView product = (TextView) convertView.findViewById(R.id.homeListViewItemProductTextView);
            TextView number = (TextView) convertView.findViewById(R.id.homeListViewItemNumberTextView);
            TextView dateTime = (TextView) convertView.findViewById(R.id.homeListViewItemDatetimeTextView);
            TextView balance = (TextView) convertView.findViewById(R.id.homeListViewItemBalanceTextView);
            TextView description = (TextView) convertView.findViewById(R.id.descriptionTextView);
            // TODO - add for description

            product.setText(historyRecordInfo.mSoldLoadInfo.m_product);
            number.setText(historyRecordInfo.mSoldLoadInfo.m_number);
            dateTime.setText(MyUtility.getDate(historyRecordInfo.mSoldLoadInfo.m_dateTime));
            balance.setText(MyUtility.PESO_SIGN + MyUtility.setDecimalPlaces(2, MyUtility.roundOff(historyRecordInfo.mSoldLoadInfo.m_balance, 2)));
            description.setText(historyRecordInfo.mSoldLoadInfo.m_description);

            ImageView paidImage = (ImageView) convertView.findViewById(R.id.homeListViewItemPaidImage);
            ImageView validImage = (ImageView) convertView.findViewById(R.id.homeListViewItemValidImage);   // TODO - check if load sold is confirmed from loadcentral
            ImageView infoImage = (ImageView) convertView.findViewById(R.id.homeListViewItemInfoImage);

            if(historyRecordInfo.mSoldLoadInfo.m_isPaid) {
//            convertView.setBackgroundColor(getContext().getResources().getColor(R.color.colorRed));
                paidImage.setBackgroundColor(getContext().getResources().getColor(R.color.colorGreen));
            }
            else {
//            convertView.setBackgroundColor(getContext().getResources().getColor(R.color.colorDefault));
                paidImage.setBackgroundColor(getContext().getResources().getColor(R.color.colorRed));
            }

            if(historyRecordInfo.mSoldLoadInfo.m_isValidated)
                validImage.setBackgroundColor(getContext().getResources().getColor(R.color.colorGreen));
            else
                validImage.setBackgroundColor(getContext().getResources().getColor(R.color.colorRed));

            if(!historyRecordInfo.mSoldLoadInfo.m_description.isEmpty())
                infoImage.setVisibility(View.VISIBLE);
            else
                infoImage.setVisibility(View.INVISIBLE);
        }
        else if(historyRecordInfo.mTableName.equals(DatabaseHandler.TABLE_DEPOSIT)) {
            TextView amountAndDatetime = (TextView) convertView.findViewById(R.id.listViewItemHome2AmountDatetimeTV);
            TextView balance = (TextView) convertView.findViewById(R.id.listViewItemHome2BalanceTV);

            amountAndDatetime.setText(Html.fromHtml("Deposited <strong>" + MyUtility.setDecimalPlaces(2, MyUtility.roundOff(historyRecordInfo.mDepositInfo.mAmount, 2)) + "</strong> on <strong>" + MyUtility.getDate(historyRecordInfo.mDepositInfo.mDatetime) + "</strong>"));
            balance.setText(MyUtility.PESO_SIGN + MyUtility.setDecimalPlaces(2, MyUtility.roundOff(historyRecordInfo.mBalance, 2)));
        }

        return convertView;
    }
}

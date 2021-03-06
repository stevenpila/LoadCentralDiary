package com.example.stevenpila.loadcentraldiary;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Steven on 11/14/2015.
 */
public class TransactionRecordListViewAdapter extends ArrayAdapter<TransactionRecordInfo> {

    private ArrayList<TransactionRecordInfo> mTransactionRecordInfos;
    private ArrayList<TransactionRecordInfo> mCurrentTransactionRecordInfosBackup;
    private ArrayList<TransactionRecordInfo> mCurrentTransactionRecordInfos;
    private int mCurrentShowPaidUnpaid = 0;
    private int mCurrentDateRange = 0;
    private MyUtility.Pair<String, String> mDateFromto = null;

    private int stripeIndicator = 0;

    public TransactionRecordListViewAdapter(Context context, ArrayList<TransactionRecordInfo> transactionRecordInfos) {
        super(context, 0, transactionRecordInfos);

        if(transactionRecordInfos.isEmpty()) {
            MyUtility.logMessage("Transaction record history is empty.");
            transactionRecordInfos.add(new TransactionRecordInfo(true));
        }

        mTransactionRecordInfos = transactionRecordInfos;
        mCurrentTransactionRecordInfos = new ArrayList<>(transactionRecordInfos);
        mCurrentTransactionRecordInfosBackup = new ArrayList<>(transactionRecordInfos);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TransactionRecordInfo transactionRecordInfo = mCurrentTransactionRecordInfos.get(position);

        if(transactionRecordInfo.mIsSearchEmpty) {  // search result is empty
            convertView =  LayoutInflater.from(getContext()).inflate(R.layout.list_view_item_not_available, parent, false);
            ((TextView) convertView.findViewById(R.id.listViewItemNotAvailable)).setText("No Transaction(s) Available");

            return convertView;
        }

        convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_view_item_home_sell_load, parent, false);

        if(stripeIndicator++ % 2 == 0)
            convertView.setBackgroundColor(getContext().getResources().getColor(R.color.colorDefault));
        else
            convertView.setBackgroundColor(getContext().getResources().getColor(R.color.transparentColor));

        if(stripeIndicator == getCount())
            stripeIndicator = 0;

        TextView productTV = (TextView) convertView.findViewById(R.id.listViewItemHomeSellLoadProductTV);
        TextView numberTV = (TextView) convertView.findViewById(R.id.listViewItemHomeSellLoadNumberTV);
        TextView dateTimeTV = (TextView) convertView.findViewById(R.id.listViewItemHomeSellLoadDatetimeTV);
        TextView balanceTV = (TextView) convertView.findViewById(R.id.listViewItemHomeSellLoadBalanceTV);
        TextView amountTV = (TextView) convertView.findViewById(R.id.listViewItemHomeSellLoadAmountTV);

        ImageView paidIV = (ImageView) convertView.findViewById(R.id.listViewItemHomeSellLoadPaidIV);
        ImageView infoIV = (ImageView) convertView.findViewById(R.id.listViewItemHomeSellLoadInfoIV);

        if(transactionRecordInfo.mTableName.equals(DatabaseHandler.TABLE_SELL_LOAD)) { // sell load record
            productTV.setText(transactionRecordInfo.mSoldLoadInfo.mProduct);   // set product code
            numberTV.setText(transactionRecordInfo.mSoldLoadInfo.mNumber);     // set number of client
            dateTimeTV.setText(MyUtility.getDate(transactionRecordInfo.mSoldLoadInfo.mDatetime));  // set datetime of transaction
            balanceTV.setText(MyUtility.PESO_SIGN + MyUtility.setDecimalPlaces(MyUtility.roundOff(transactionRecordInfo.mSoldLoadInfo.mBalance, 2)));   // set current balance
            amountTV.setText("-" + MyUtility.PESO_SIGN + MyUtility.setDecimalPlaces(transactionRecordInfo.mSoldLoadInfo.mAmount));
            amountTV.setTextColor(getContext().getResources().getColor(android.R.color.holo_red_dark)); // set text color

            if(transactionRecordInfo.mSoldLoadInfo.mIsPaid)    // if sell load record is already paid
                paidIV.setBackgroundColor(getContext().getResources().getColor(R.color.colorGreen));
            else
                paidIV.setBackgroundColor(getContext().getResources().getColor(R.color.colorRed));

            if(!transactionRecordInfo.mSoldLoadInfo.mDescription.isEmpty())  // if description is not empty
                infoIV.setBackgroundColor(getContext().getResources().getColor(R.color.colorGreen));
            else
                infoIV.setBackgroundColor(getContext().getResources().getColor(R.color.colorRed));
        }
        else if(transactionRecordInfo.mTableName.equals(DatabaseHandler.TABLE_DEPOSIT)) {   // deposit record
            productTV.setText(HomeActivity.DEPOSIT);    // set product to DEPOSIT
            numberTV.setHint("");   // remove hint since it will display Number
            dateTimeTV.setText(MyUtility.getDate(transactionRecordInfo.mDepositInfo.mDatetime));  // set datetime of deposit
            balanceTV.setText(MyUtility.PESO_SIGN + MyUtility.setDecimalPlaces(MyUtility.roundOff(transactionRecordInfo.mBalance, 2)));   // set current balance
            amountTV.setText(MyUtility.PESO_SIGN + MyUtility.setDecimalPlaces(transactionRecordInfo.mDepositInfo.mAmount));  // set amount deposited
            amountTV.setTextColor(getContext().getResources().getColor(android.R.color.holo_green_dark)); // set text color

            paidIV.setVisibility(View.INVISIBLE);   // hide image view since not necessary
            infoIV.setVisibility(View.INVISIBLE);   // hide image view since not necessary
        }

        return convertView;
    }

    @Override
    public int getCount() {
        return mCurrentTransactionRecordInfos.size();
    }

    @Override
    public TransactionRecordInfo getItem(int position) {
        return mCurrentTransactionRecordInfos.get(position);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if(constraint != null && !(mTransactionRecordInfos.get(0).mIsSearchEmpty)) {
                    ArrayList<TransactionRecordInfo> results = getArrayListBySubString(constraint.toString().toLowerCase());
                    filterResults.count = results.size();
                    filterResults.values = results;
                }

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if(results != null && results.count > 0) {
                    mCurrentTransactionRecordInfos = (ArrayList<TransactionRecordInfo>) results.values;
                    notifyDataSetChanged();
                }
                else
                    notifyDataSetInvalidated();
            }
        };
    }

    public ArrayList<TransactionRecordInfo> getArrayListBySubString(String constraint) {
        ArrayList<TransactionRecordInfo> results = new ArrayList<>();

        for(TransactionRecordInfo item: mCurrentTransactionRecordInfosBackup) {
            if(item.mTableName.equals(DatabaseHandler.TABLE_SELL_LOAD)) {
                if (item.mSoldLoadInfo.mProduct.toLowerCase().contains(constraint) || item.mSoldLoadInfo.mNumber.toLowerCase().contains(constraint)) {
                    results.add(item);
                }
            }
            else if(item.mTableName.equals(DatabaseHandler.TABLE_DEPOSIT) && (constraint.isEmpty() || constraint.equals(HomeActivity.DEPOSIT.toLowerCase())))
                    results.add(item);
        }

        if(results.isEmpty())
            results.add(new TransactionRecordInfo(true));

        return results;
    }

    public void setArrayListByPaidStatus(int position) {
        if((mTransactionRecordInfos.get(0)).mIsSearchEmpty)
            return;

        mCurrentShowPaidUnpaid = position;

        setArrayListByDateRange(mCurrentDateRange, mDateFromto);
    }

    public void setArrayListByDateRange(int position) {
        setArrayListByDateRange(position, null);
    }
    public void setArrayListByDateRange(int position, MyUtility.Pair<String, String> dateFromto) {
        if((mTransactionRecordInfos.get(0)).mIsSearchEmpty)
            return;

        ArrayList<TransactionRecordInfo> results = new ArrayList<>();
        int lastNumMonths = 0, lastNumYear = 0;

        for(TransactionRecordInfo item: mTransactionRecordInfos) {
            boolean isAddItem = false;

            switch (position) {
                case 0: // This Month
                    int year = MyUtility.getCalendar().get(Calendar.YEAR);
                    int month = MyUtility.getCalendar().get(Calendar.MONTH);

                    if((item.mTableName.equals(DatabaseHandler.TABLE_SELL_LOAD) && (MyUtility.getDateFromString(item.mSoldLoadInfo.mDatetime).get(Calendar.YEAR) == year && MyUtility.getDateFromString(item.mSoldLoadInfo.mDatetime).get(Calendar.MONTH) == month)) ||
                            (item.mTableName.equals(DatabaseHandler.TABLE_DEPOSIT) && (MyUtility.getDateFromString(item.mDepositInfo.mDatetime).get(Calendar.YEAR) == year && MyUtility.getDateFromString(item.mDepositInfo.mDatetime).get(Calendar.MONTH) == month)))
                        isAddItem = true;

                    mDateFromto = null;
                    break;
                case 1: // Last Week
                    Calendar lastWeek = MyUtility.getCalendar();
                    lastWeek.add(Calendar.DAY_OF_YEAR, -7);

                    if((item.mTableName.equals(DatabaseHandler.TABLE_SELL_LOAD) &&
                            (MyUtility.getDateFromString(item.mSoldLoadInfo.mDatetime).getTimeInMillis() >= lastWeek.getTimeInMillis())
                            ) ||
                            (item.mTableName.equals(DatabaseHandler.TABLE_DEPOSIT) &&
                                    (MyUtility.getDateFromString(item.mDepositInfo.mDatetime).getTimeInMillis() >= lastWeek.getTimeInMillis())
                            ))
                        isAddItem = true;

                    mDateFromto = null;
                    break;
                case 2: // Last Month
                case 3: // Last 3 Months
                case 4: // Last 6 Months
                case 5: // Last Year
                    switch (position) {
                        case 2: lastNumMonths = -1; break; // Last Month
                        case 3: lastNumMonths = -3; break; // Last 3 Months
                        case 4: lastNumMonths = -6; break; // Last 6 Months
                        case 5: lastNumYear = -1; break;
                    }

                    Calendar lastMonthCal = MyUtility.getCalendar();
                    if(lastNumMonths != 0 && lastNumYear == 0)
                        lastMonthCal.add(Calendar.MONTH, lastNumMonths);
                    else if(lastNumMonths == 0 && lastNumYear != 0)
                        lastMonthCal.add(Calendar.YEAR, lastNumYear);

                    int lastYear = lastMonthCal.get(Calendar.YEAR);
                    int lastMonth = lastMonthCal.get(Calendar.MONTH);

                    if((item.mTableName.equals(DatabaseHandler.TABLE_SELL_LOAD) &&
                            (MyUtility.getDateFromString(item.mSoldLoadInfo.mDatetime).get(Calendar.YEAR) > lastYear ||
                                    (MyUtility.getDateFromString(item.mSoldLoadInfo.mDatetime).get(Calendar.YEAR) == lastYear && MyUtility.getDateFromString(item.mSoldLoadInfo.mDatetime).get(Calendar.MONTH) >= lastMonth)
                            )) ||
                            (item.mTableName.equals(DatabaseHandler.TABLE_DEPOSIT) &&
                                    (MyUtility.getDateFromString(item.mDepositInfo.mDatetime).get(Calendar.YEAR) > lastYear ||
                                            (MyUtility.getDateFromString(item.mDepositInfo.mDatetime).get(Calendar.YEAR) == lastYear && MyUtility.getDateFromString(item.mDepositInfo.mDatetime).get(Calendar.MONTH) >= lastMonth)
                                    )
                            ))
                        isAddItem = true;

                    mDateFromto = null;
                    break;
                case 6: // Custom Range
                    // TODO - use datepickerdialog here...
                    long dateFrom = MyUtility.getDateFromString(dateFromto.m_first).getTimeInMillis();
                    long dateTo = MyUtility.getDateFromString(dateFromto.m_second).getTimeInMillis();

                    if((item.mTableName.equals(DatabaseHandler.TABLE_SELL_LOAD) &&
                            (MyUtility.getDateFromString(item.mSoldLoadInfo.mDatetime).getTimeInMillis() >= dateFrom && MyUtility.getDateFromString(item.mSoldLoadInfo.mDatetime).getTimeInMillis() <= dateTo)
                    ) ||
                            (item.mTableName.equals(DatabaseHandler.TABLE_DEPOSIT) &&
                                    (MyUtility.getDateFromString(item.mDepositInfo.mDatetime).getTimeInMillis() >= dateFrom && MyUtility.getDateFromString(item.mDepositInfo.mDatetime).getTimeInMillis() <= dateTo)
                            ))
                        isAddItem = true;
                    break;
                case 7: // All
                    isAddItem = true;

                    mDateFromto = null;
                    break;
            }

            if(isAddItem) {
                switch (mCurrentShowPaidUnpaid) {
                    case 0: results.add(item);  // All
                        break;
                    case 1: // Paid
                        if(item.mTableName.equals(DatabaseHandler.TABLE_SELL_LOAD) && item.mSoldLoadInfo.mIsPaid)
                            results.add(item);
                        break;
                    case 2: // Unpaid
                        if(item.mTableName.equals(DatabaseHandler.TABLE_SELL_LOAD) && !item.mSoldLoadInfo.mIsPaid)
                            results.add(item);
                        break;
                }
            }
        }

        if(results.isEmpty())
            results.add(new TransactionRecordInfo(true));

        if(!results.isEmpty()) {
            mCurrentDateRange = position;
            if(dateFromto != null)
                mDateFromto = dateFromto;
            mCurrentTransactionRecordInfosBackup = results;
            mCurrentTransactionRecordInfos = mCurrentTransactionRecordInfosBackup;
            notifyDataSetChanged();
        }
        else
            notifyDataSetInvalidated();
    }
}
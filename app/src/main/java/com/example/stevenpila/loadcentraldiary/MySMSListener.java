package com.example.stevenpila.loadcentraldiary;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import java.util.ArrayList;

/**
 * Created by Steven on 12/6/2015.
 */
public class MySMSListener extends BroadcastReceiver {
    static public final String SMS_LISTENER_ACTION = "android.provider.Telephony.SMS_RECEIVED";

    private final String REGEX_GET_CLIENT_NUMBER = "([0-9]+)(\\+)*$";
    private final String REGEX_GET_CLIENT_PRODUCT = "(?<=You sold )[A-z0-9]+";
    private final String REGEX_GET_CURRENT_BALANCE = "[0-9\\.]+$";

    private final String NUMBER_PLUS63 = "63";
    private final char NUMBER_ZERO = '0';

    private final String PDUS = "pdus";

    @Override
    public void onReceive(Context arg0, Intent arg1) {
        MyUtility.showToast(arg0, "MySMSListener::onReceive - Started.", MyUtility.ToastLength.LONG);
        if(arg1.getAction().equalsIgnoreCase(SMS_LISTENER_ACTION)) {
            Bundle extras = arg1.getExtras();
            ArrayList<String> accessNumbers = getAccessNumbers(arg0);

            if(extras != null) {
                Object[] smsExtras = (Object[]) extras.get(PDUS);

                if(smsExtras != null) {
                    for(Object object: smsExtras){
                        SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) object);

                        String msgBody = smsMessage.getMessageBody();
                        String msgSrc = getAlternateNumber(smsMessage.getOriginatingAddress());

                        if(accessNumbers.contains(msgSrc)) {
                            processMessage(arg0, msgBody, msgSrc);
                            arg0.unregisterReceiver(this);
                        }
                        else
                            MyUtility.logMessage(arg0, "Ignored SMS: " + msgSrc + " - " + msgBody);
                    }
                }
            }
        }
        MyUtility.showToast(arg0, "MySMSListener::onReceive - Finished.", MyUtility.ToastLength.LONG);
    }

    private void processMessage(Context context, String message, String number) {
        String balance = MyUtility.getStringFromRegex(message, REGEX_GET_CURRENT_BALANCE);
        if(message.contains(".") && !balance.isEmpty()) {
            String tempMsg = message.substring(0, message.indexOf(MyUtility.DOT));    // get message with product and number
            double dBalance = (balance.isEmpty()) ? -1 : Double.parseDouble(balance);

            String clientNumber = MyUtility.getStringFromRegex(tempMsg, REGEX_GET_CLIENT_NUMBER); // get client number
            String clientProduct = MyUtility.getStringFromRegex(tempMsg, REGEX_GET_CLIENT_PRODUCT); // get client product

            if(!clientNumber.isEmpty() && !clientProduct.isEmpty() && dBalance >= 0) {
                MyUtility.showToast(context, "LoadCentralDiary: Received SMS from " + number, MyUtility.ToastLength.LONG);
                DatabaseHandler databaseHandler = new DatabaseHandler(context);
                long id = databaseHandler.getSellLoadID(clientNumber, clientProduct, dBalance);

                if(id > -1) {
                    if(databaseHandler.setValidSellLoad((int) id)) {
                        if(HomeActivity.getInstance() != null) {
                            HomeActivity.getInstance().setValidSellLoad((int) id);
                            MyUtility.showToast(context, "Successfully validated. (Product: " + clientProduct + ", Number: " + clientNumber + ", Balance: " + dBalance + ").", MyUtility.ToastLength.LONG);
                        }
                    }
                    else
                        MyUtility.showToast(context, "Failed to validate. (Product: " + clientProduct + ", Number: " + clientNumber + ", Balance: " + dBalance + ").", MyUtility.ToastLength.LONG);
                }
                else
                    MyUtility.showToast(context, "Record does not exists. (Product: " + clientProduct + ", Number: " + clientNumber + ", Balance: " + dBalance + ").", MyUtility.ToastLength.LONG);
            }
            else
                MyUtility.showToast(context, "Number, Product and Balance values are required.", MyUtility.ToastLength.LONG);
        }
        else
            MyUtility.showToast(context, "Invalid Message: " + message, MyUtility.ToastLength.LONG);
    }

    private ArrayList<String> getAccessNumbers(Context context) {
        ArrayList<String> accessNumbers = new ArrayList<>();
        String[] fullAccessNumbers = context.getResources().getStringArray(R.array.access_numbers);

        for (String accessNumber: fullAccessNumbers) {
            accessNumbers.add(accessNumber.split(MyUtility.COMMA)[1]);
        }

        return accessNumbers;
    }

    private String getAlternateNumber(String number) {
        String alternateNumber = number;

        if(number.substring(0, 3).equals(NUMBER_PLUS63)) {
            alternateNumber = NUMBER_ZERO + number.substring(3);
        }

        return alternateNumber;
    }
}

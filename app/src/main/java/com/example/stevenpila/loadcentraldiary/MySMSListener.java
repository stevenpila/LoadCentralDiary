package com.example.stevenpila.loadcentraldiary;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Steven on 12/6/2015.
 */
public class MySMSListener extends BroadcastReceiver {
    static public final String SMS_LISTENER_ACTION = "android.provider.Telephony.SMS_RECEIVED";
    private final String REGEX_GET_CLIENT_NUMBER = "([0-9]+)(\\+)*$";
    private final String REGEX_GET_CLIENT_PRODUCT = "(?<=You sold )[A-z0-9]+";

    @Override
    public void onReceive(Context arg0, Intent arg1) {
        if(arg1.getAction().equalsIgnoreCase(SMS_LISTENER_ACTION)) {
            Bundle extras = arg1.getExtras();
            ArrayList<String> accessNumbers = getAccessNumbers(arg0);

            String messageStr = "Private Message: ";
            if(extras != null) {
                Object[] smsExtras = (Object[]) extras.get("pdus");

                for(int i = 0; i < smsExtras.length; ++i) {
                    SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) smsExtras[i]);

                    String msgBody = smsMessage.getMessageBody().toString();
                    String msgSrc = smsMessage.getOriginatingAddress();
                    String newMsgSrc = getAlternateNumber(msgSrc);

                    MyUtility.showToast(arg0, msgSrc + " to " + newMsgSrc, MyUtility.ToastLength.SHORT);
                    if(accessNumbers.contains(newMsgSrc))
                        processMessage(arg0, msgBody);
                    else {
                        messageStr += "IGNORED: SMS from " + msgSrc + " : " + msgBody;
                        MyUtility.showToast(arg0, messageStr, MyUtility.ToastLength.LONG);
                    }

//                    abortBroadcast();
                }
            }
        }
    }

    private void processMessage(Context context, String message) {
        String balance = MyUtility.getStringFromRegex(message, "[0-9\\.]+$");
        if(message.indexOf(".") > -1 && !balance.isEmpty()) {
            String tempMsg = message.substring(0, message.indexOf("."));    // get message with product and number // TODO - get wallet balance
            double dBalance = (balance.isEmpty()) ? 0.00 : Double.parseDouble(balance);

            String clientNumber = MyUtility.getStringFromRegex(tempMsg, REGEX_GET_CLIENT_NUMBER); // get client number
            String clientProduct = MyUtility.getStringFromRegex(tempMsg, REGEX_GET_CLIENT_PRODUCT);

            if(!clientNumber.isEmpty() && !clientProduct.isEmpty()) {
                DatabaseHandler databaseHandler = new DatabaseHandler(context);
                long id = databaseHandler.getSellLoadID(clientNumber, clientProduct, dBalance);

                if(id > -1) {
                    if(databaseHandler.setValidSellLoad((int) id)) {
                        if(HomeActivity.getInstance() != null) {
                            HomeActivity.getInstance().setValidSellLoad((int) id);
                            MyUtility.showToast(context, "Successfully validated. " + clientNumber + ":" + clientProduct, MyUtility.ToastLength.LONG);
                        }
                    }
                    else
                        MyUtility.showToast(context, "Failed to validate. " + clientNumber + ":" + clientProduct, MyUtility.ToastLength.LONG);
                }
                else
                    MyUtility.showToast(context, "Record does not exists. (" + clientNumber + ", " + clientProduct + ", " + dBalance + ")", MyUtility.ToastLength.LONG);
            }
            else
                MyUtility.showToast(context, "Either Number or Product cannot be empty.", MyUtility.ToastLength.LONG);
        }
        else
            MyUtility.showToast(context, "Invalid Message: " + message, MyUtility.ToastLength.LONG);
    }

    private ArrayList<String> getAccessNumbers(Context context) {
        ArrayList<String> accessNumbers = new ArrayList<>();
        String[] fullAccessNumbers = context.getResources().getStringArray(R.array.access_numbers);

        for (String accessNumber: fullAccessNumbers) {
            accessNumbers.add(accessNumber.split(",")[1]);
        }

        return accessNumbers;
    }

    private String getAlternateNumber(String number) {
        String alternateNumber = number;

        if(number.substring(0, 3).equals("+63")) {
            alternateNumber = "0" + number.substring(3);
        }

        return alternateNumber;
    }
}

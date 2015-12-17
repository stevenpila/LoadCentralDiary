package com.example.stevenpila.loadcentraldiary;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Steven on 11/10/2015.
 */
public class MyUtility {
    static public char PESO_SIGN = 0x20B1;
    static public int TABLE_EMPTY = -5;
    public enum DirectoryType {
        ROOT,
        FILES,
        CACHE,
        DATABASE
    }
    public enum ToastLength {
        SHORT,
        LONG
    }
    static public String DOT = ".";
    static public String COMMA = ",";

    static public String getCurrentDateTime() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        return simpleDateFormat.format(c.getTime());
    }

    static public String getCurrentDate() {
        Calendar c = Calendar.getInstance();
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd"); // let's support date only for now

        return simpleDateFormat.format(c.getTime());
    }

    static public Pair<String, Integer> getProductAndAmountFromString(String product) {
        Pair<String, Integer> productAndAmount = new Pair<>("", 0);

        if(!product.isEmpty()) {
            Pattern p = Pattern.compile("\\d+");
            Matcher m = p.matcher(product);

            if(m.find()) {
                String amountStr = m.group();

                if(!amountStr.trim().isEmpty()) {
                    productAndAmount.m_second = Integer.parseInt(amountStr.trim());
                    productAndAmount.m_first = product.substring(0, product.indexOf(amountStr));
                }
            }
        }

        return productAndAmount;
    }

    static public String getDate(String dateTime) {
        SimpleDateFormat oldFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        SimpleDateFormat newFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

        Date newDate;
        try {
            newDate = oldFormat.parse(dateTime);
            return newFormat.format(newDate);
        }
        catch (ParseException ex) {
            ex.printStackTrace();
        }

        return "";
    }

    static public String getTime(String dateTime) {
        SimpleDateFormat oldFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        SimpleDateFormat newFormat = new SimpleDateFormat("HH:mm a", Locale.getDefault());

        Date newDate;
        try {
            newDate = oldFormat.parse(dateTime);
            return newFormat.format(newDate);
        }
        catch (ParseException ex) {
            ex.printStackTrace();
        }

        return "";
    }

    static public boolean copyAssetToData(Context context, String sourceFile, String destinationFile, DirectoryType type) {
        boolean bRet = true;
        InputStream inputStream = null;
        OutputStream outputStream = null;
        final int MAX_BUFFER_SIZE = 1024;

        try {
            inputStream = context.getAssets().open(sourceFile);
            String filePath = "";

            if(type == DirectoryType.ROOT)
                filePath = context.getFilesDir().getParent() + File.separator + destinationFile;
            else if(type == DirectoryType.FILES)
                filePath = context.getFilesDir().getAbsolutePath() + File.separator + destinationFile;
            else if(type == DirectoryType.CACHE)
                filePath = context.getCacheDir().getAbsolutePath() + File.separator + destinationFile;
            else if(type == DirectoryType.DATABASE)
                filePath = context.getFilesDir().getParent() + File.separator + "databases" + File.separator + destinationFile;

            if(!isFileExists(filePath)) {
                outputStream = new FileOutputStream(filePath);

                byte[] bufferBytes = new byte[MAX_BUFFER_SIZE];
                int readBytes;
                while((readBytes = inputStream.read(bufferBytes)) != -1) {
                    outputStream.write(bufferBytes, 0, readBytes);
                }
            }
            else
                bRet = false;
        } catch (IOException e) {
            bRet = false;
        } finally {
            if(inputStream != null)
                try {
                    inputStream.close();
                } catch (IOException e) {
                    bRet = false;
                }
            if(outputStream != null)
                try {
                    outputStream.close();
                } catch (IOException e) {
                    bRet = false;
                }
        }

        return bRet;
    }

    static public boolean isFileExists(String filePath) {
        boolean bFound;
        File file;

        try {
            file = new File(filePath);
            bFound = file.exists();
        } catch (Exception e) {
            bFound = false;
        }

        return bFound;
    }

    static public double roundOff(double value, int decimalPlaces) {
        if(decimalPlaces < 0) throw new IllegalArgumentException();

        BigDecimal bigDecimal = new BigDecimal(value);
        bigDecimal = bigDecimal.setScale(decimalPlaces, RoundingMode.HALF_UP);

        return bigDecimal.doubleValue();
    }

    static public class Pair<T, U> {
        T m_first;
        U m_second;

        public Pair(Pair<T, U> rhs) {
            m_first = rhs.m_first;
            m_second = rhs.m_second;
        }
        public Pair(T first, U second) {
            m_first = first;
            m_second = second;
        }
    }

    // format is: PRODUCTCODE<space>PIN<space>NUMBER
    static public String createSellLoadFormat(final String productStr, final String pinStr, final String numberStr) {
        return productStr + " " + pinStr + " " + numberStr;
    }

    static public void showToast(Context context, final String message, final ToastLength length) {
        Toast.makeText(context, message, (length == ToastLength.SHORT) ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG).show();
        final int indexOfCallerOfThisMethod = 3;

        final String className = Thread.currentThread().getStackTrace()[indexOfCallerOfThisMethod].getClassName();
        final int packageNameLen = context.getPackageName().length() + 1; // + 1 for .

        final String newClassName = className.substring(packageNameLen);
        final String newMethodName = Thread.currentThread().getStackTrace()[3].getMethodName();

        String logMessage = getCurrentDateTime() + " - " + newClassName + "::" + newMethodName + " - " + message;

        Log.v(context.getPackageName() + ".log", logMessage);
    }

    static public void sendSMS(Context context, String number, String message) {
        String SENT_SMS_ACTION = "SMS_SENT";
        String DELIVERED_SMS_ACTION = "SMS_DELIVERED";

        final ProgressDialog progress = new ProgressDialog(context);
//        progress.setTitle("Sending...");
        progress.setMessage("Sending to LoadCentral...");
        progress.show();

        // create the sentIntent parameter
        Intent sentIntent = new Intent (SENT_SMS_ACTION);
        PendingIntent sentPI = PendingIntent.getBroadcast(context, 0, sentIntent, 0);

        // create the delivery Intent parameter
        Intent deliveryIntent = new Intent (DELIVERED_SMS_ACTION);
        PendingIntent deliverPI = PendingIntent.getBroadcast(context, 0, deliveryIntent, 0);

        // Register the Broadcast Receivers
        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context _context, Intent _intent) {
                progress.dismiss();
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        MyUtility.showToast(_context, "Successfully sent message.", MyUtility.ToastLength.LONG);
                        IntentFilter intentFilter = new IntentFilter(MySMSListener.SMS_LISTENER_ACTION);
                        _context.registerReceiver(new MySMSListener(), intentFilter);
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        MyUtility.showToast(_context, "Failed to send message. Generic failure.", MyUtility.ToastLength.LONG);
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        MyUtility.showToast(_context, "Failed to send message. Radio off.", MyUtility.ToastLength.LONG);
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        MyUtility.showToast(_context, "Failed to send message. Null PDU.", MyUtility.ToastLength.LONG);
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        MyUtility.showToast(_context, "Failed to send message. No service.", MyUtility.ToastLength.LONG);
                        break;
                    default:
                        MyUtility.showToast(_context, "Unknown result code: " + getResultCode(), MyUtility.ToastLength.LONG);
                        break;
                }
            }
        }, new IntentFilter(SENT_SMS_ACTION));
        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context _context, Intent _intent) {
                MyUtility.showToast(_context, "Successfully delivered message. Result code: " + getResultCode(), MyUtility.ToastLength.LONG);
            }
        }, new IntentFilter(DELIVERED_SMS_ACTION));

        // Send the message
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(number, null, message, sentPI, deliverPI);
        MyUtility.showToast(context, "Message: " + message + ", Number: " + number, MyUtility.ToastLength.LONG);
    }

    static public void setTextViewValue(TextView textView, double value) {
        textView.setText(setDecimalPlaces(2, value));
    }

    static public String setDecimalPlaces(int decimalPlaces, double value) {
        return String.format("%." + decimalPlaces + "f", value);
    }

    static public String getStringFromRegex(String string, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(string);

        return (matcher.find()) ? matcher.group().trim() : "";
    }

    static public void showNotification(Context context, String title, String content) {
        Intent intent = new Intent(context, HomeActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), intent, 0);

        Notification notification = new Notification.Builder(context)
                .setContentTitle(title)
                .setStyle(new Notification.BigTextStyle().bigText(content))
//                .setContentText(content)
                .setSmallIcon(R.drawable.my_loadcentral_logo)
                .setAutoCancel(true)
                .build();

        NotificationManager notificationManager  = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);
    }

    static public String concatTwoStringWithDelimiter(String leftStr, String delimiter, String rightStr) {
        return leftStr + delimiter + rightStr;
    }
}

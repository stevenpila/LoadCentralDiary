/**
 * Created by Steven on 11/7/2015.
 */
package com.example.stevenpila.loadcentraldiary;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class DatabaseHandler extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "load_central_diary.db";

    // product category
    private final String TABLE_PRODUCT_CATEGORY = "product_category";
    private final String PRODUCT_CATEGORY_COLUMN_ID = "id";
    private final String PRODUCT_CATEGORY_COLUMN_CATEGORY = "category";

    // product name
    private final String TABLE_PRODUCT_NAME = "product_name";
    private final String PRODUCT_NAME_COLUMN_ID = "id";
    private final String PRODUCT_NAME_COLUMN_PRODUCT_CATEGORY_ID = "product_category_id";
    private final String PRODUCT_NAME_COLUMN_NAME = "name";

    // product code
    private final String TABLE_PRODUCT_CODE = "product_code";
    private final String PRODUCT_CODE_COLUMN_ID = "id";
    private final String PRODUCT_CODE_COLUMN_PRODUCT_NAME_ID = "product_name_id";
    private final String PRODUCT_CODE_COLUMN_CODE = "code";
    private final String PRODUCT_CODE_COLUMN_DESCRIPTION = "description";
    private final String PRODUCT_CODE_COLUMN_DISCOUNT = "discount";

    // deposit
    static public final String TABLE_DEPOSIT = "deposit";
    private final String DEPOSIT_COLUMN_ID = "id";
    private final String DEPOSIT_COLUMN_AMOUNT = "amount";
    private final String DEPOSIT_COLUMN_DATETIME = "datetime";

    // sell load
    static public final String TABLE_SELL_LOAD = "sell_load";
    private final String SELL_LOAD_COLUMN_ID = "id";
    private final String SELL_LOAD_COLUMN_PRODUCT_CODE_ID = "product_code_id";
    private final String SELL_LOAD_COLUMN_PRODUCT = "product"; // TODO - to be removed...
    private final String SELL_LOAD_COLUMN_AMOUNT = "amount";
    private final String SELL_LOAD_COLUMN_NUMBER = "number";
    private final String SELL_LOAD_COLUMN_BALANCE = "balance";
    private final String SELL_LOAD_COLUMN_DESCRIPTION = "description";
    private final String SELL_LOAD_COLUMN_DATETIME_SOLD = "datetime_sold";
    private final String SELL_LOAD_COLUMN_STATUS = "status";
    private final String SELL_LOAD_COLUMN_DATETIME_PAID = "datetime_paid";

    // balance
    private final String TABLE_BALANCE = "balance";
    private final String BALANCE_COLUMN_ID = "id";
    private final String BALANCE_COLUMN_ROW_ID = "row_id";
    private final String BALANCE_COLUMN_BALANCE = "balance";
    private final String BALANCE_COLUMN_TABLE_NAME = "table_name";
    private final String BALANCE_COLUMN_LATEST_BALANCE = "latest_balance";
//    private final String BALANCE_COLUMN_PREVIOUS_BALANCE = "previous_balance";  // TODO - to be used later on (E.g., update/delete deposit/sell load)
//    private final String BALANCE_COLUMN_COUNT_BALANCE = "count_balance";    // TODO - to be used later on (E.g., update/delete deposit/sell load)

    // phonebook
    private final String TABLE_PHONEBOOK = "phonebook";
    private final String PHONEBOOK_COLUMN_ID = "id";
    private final String PHONEBOOK_COLUMN_NAME = "name";
    private final String PHONEBOOK_COLUMN_NUMBER = "number";

    // triggers TODO - do something with these triggers...
//    private final String TRIGGER_BALANCE_DEPOSIT_INSERT = TABLE_DEPOSIT + "_insert";
//    private final String TRIGGER_BALANCE_SELL_LOAD_INSERT = TABLE_SELL_LOAD + "_insert";
//    private final String TRIGGER_BALANCE_DEPOSIT_UPDATE = TABLE_DEPOSIT + "_update";
//    private final String TRIGGER_BALANCE_SELL_LOAD_UPDATE = TABLE_SELL_LOAD + "_update";
//    private final String TRIGGER_BALANCE_DEPOSIT_DELETE = TABLE_DEPOSIT + "_delete";
//    private final String TRIGGER_BALANCE_SELL_LOAD_DELETE = TABLE_SELL_LOAD + "_delete";

    // member variables
    private String m_error_message = "";
    private final Context m_context;

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        m_context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createProductCategoryTable(db);     // creates product category table
        createProductNameTable(db);         // creates product name table
        createProductCodeTable(db);         // creates product code table
        createDepositTable(db);             // creates deposit table
        createSellLoadTable(db);            // creates sell load table
        createBalanceTable(db);             // creates balance table
        createPhonebookTable(db);           // creates phonebook table

        PDFFileParser pdfFileParser = new PDFFileParser();
        if(pdfFileParser.loadProductLoadList(m_context)) {
            if(!loadProductInfo(db, pdfFileParser))
                MyUtility.showToast(m_context, m_error_message, MyUtility.ToastLength.LONG);
        }
        else {
            m_error_message += pdfFileParser.getErrorMessage();
            MyUtility.showToast(m_context, m_error_message, MyUtility.ToastLength.LONG);
        }

        // triggers
//        String CREATE_TRIGGER_BALANCE_DEPOSIT_INSERT = "CREATE TRIGGER IF NOT EXISTS " +
//                TRIGGER_BALANCE_DEPOSIT_INSERT + " AFTER INSERT ON " + TABLE_DEPOSIT +
//                "BEGIN" +
//                    "UPDATE " + TABLE_BALANCE + " SET balance = balance + NEW.amount WHERE id = 1;" +
//                "END";
//        String CREATE_TRIGGER_BALANCE_SELL_LOAD_INSERT = "CREATE TRIGGER IF NOT EXISTS " +
//                TRIGGER_BALANCE_SELL_LOAD_INSERT + " AFTER INSERT ON " + TABLE_SELL_LOAD +
//                "BEGIN" +
//                    "UPDATE " + TABLE_BALANCE + " SET balance = NEW.balance WHERE id = 1;" +
//                "END";

//        db.execSQL(CREATE_BALANCE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    // @onCreate creates necessary tables
    private void createProductCategoryTable(SQLiteDatabase db) {
        String CREATE_PRODUCT_CATEGORY_TABLE = "CREATE TABLE IF NOT EXISTS " +
                TABLE_PRODUCT_CATEGORY + "(" +
                PRODUCT_CATEGORY_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                PRODUCT_CATEGORY_COLUMN_CATEGORY + " TEXT NOT NULL, UNIQUE (" +
                PRODUCT_CATEGORY_COLUMN_CATEGORY + ") ON CONFLICT IGNORE" + ")";

        db.execSQL(CREATE_PRODUCT_CATEGORY_TABLE);
    }
    private void createProductNameTable(SQLiteDatabase db) {
        String CREATE_PRODUCT_NAME_TABLE = "CREATE TABLE IF NOT EXISTS " +
                TABLE_PRODUCT_NAME + "(" +
                PRODUCT_NAME_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                PRODUCT_NAME_COLUMN_PRODUCT_CATEGORY_ID + " INTEGER NOT NULL, " +
                PRODUCT_NAME_COLUMN_NAME + " TEXT NOT NULL, UNIQUE(" +
                PRODUCT_NAME_COLUMN_PRODUCT_CATEGORY_ID + ", " +
                PRODUCT_NAME_COLUMN_NAME + ") ON CONFLICT IGNORE" + ")";

        db.execSQL(CREATE_PRODUCT_NAME_TABLE);
    }
    private void createProductCodeTable(SQLiteDatabase db) {
        String CREATE_PRODUCT_CODE_TABLE = "CREATE TABLE IF NOT EXISTS " +
                TABLE_PRODUCT_CODE + "(" +
                PRODUCT_CODE_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                PRODUCT_CODE_COLUMN_PRODUCT_NAME_ID + " INTEGER NOT NULL, " +
                PRODUCT_CODE_COLUMN_CODE + " TEXT NOT NULL, " +
                PRODUCT_CODE_COLUMN_DESCRIPTION + " TEXT DEFAULT '', " +
                PRODUCT_CODE_COLUMN_DISCOUNT + " DOUBLE NOT NULL, UNIQUE(" +
                PRODUCT_CODE_COLUMN_PRODUCT_NAME_ID + ", " +
                PRODUCT_CODE_COLUMN_CODE + ", " +
                PRODUCT_CODE_COLUMN_DISCOUNT + ") ON CONFLICT IGNORE" + ")";

        db.execSQL(CREATE_PRODUCT_CODE_TABLE);
    }
    private void createDepositTable(SQLiteDatabase db) {
        String CREATE_DEPOSIT_TABLE = "CREATE TABLE IF NOT EXISTS " +
                TABLE_DEPOSIT + "(" +
                DEPOSIT_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DEPOSIT_COLUMN_AMOUNT + " DOUBLE NOT NULL, " +
                DEPOSIT_COLUMN_DATETIME + " DATETIME NOT NULL" + ")";

        db.execSQL(CREATE_DEPOSIT_TABLE);
    }
    private void createSellLoadTable(SQLiteDatabase db) {
        String CREATE_SELL_LOAD_TABLE = "CREATE TABLE IF NOT EXISTS " +
                TABLE_SELL_LOAD + "(" +
                SELL_LOAD_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                SELL_LOAD_COLUMN_PRODUCT_CODE_ID + " INTEGER NOT NULL, " +
                SELL_LOAD_COLUMN_PRODUCT + " TEXT NOT NULL, " +
                SELL_LOAD_COLUMN_AMOUNT + " DOUBLE NOT NULL, " +
                SELL_LOAD_COLUMN_NUMBER + " TEXT NOT NULL, " +
                SELL_LOAD_COLUMN_BALANCE + " DOUBLE NOT NULL, " +
                SELL_LOAD_COLUMN_DESCRIPTION + " TEXT DEFAULT '', " +
                SELL_LOAD_COLUMN_DATETIME_SOLD + " DATETIME NOT NULL, " +
                SELL_LOAD_COLUMN_STATUS + " BOOLEAN NOT NULL, " +
                SELL_LOAD_COLUMN_DATETIME_PAID + " DATETIME " + ")";

        db.execSQL(CREATE_SELL_LOAD_TABLE);
    }
    private void createBalanceTable(SQLiteDatabase db) {
        String CREATE_BALANCE_TABLE = "CREATE TABLE IF NOT EXISTS " +
                TABLE_BALANCE + "(" +
                BALANCE_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                BALANCE_COLUMN_ROW_ID + " INTEGER, " +
                BALANCE_COLUMN_BALANCE + " DOUBLE, " +
                BALANCE_COLUMN_TABLE_NAME + " TEXT" + ")";

        db.execSQL(CREATE_BALANCE_TABLE);
    }
    private void createPhonebookTable(SQLiteDatabase db) {
        String CREATE_PHONEBOOK_TABLE = "CREATE TABLE IF NOT EXISTS " +
                TABLE_PHONEBOOK + "(" +
                PHONEBOOK_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                PHONEBOOK_COLUMN_NAME + " TEXT NOT NULL, " +
                PHONEBOOK_COLUMN_NUMBER + " TEXT NOT NULL, UNIQUE(" +
                PHONEBOOK_COLUMN_NAME + ", " +
                PHONEBOOK_COLUMN_NUMBER + ") ON CONFLICT IGNORE" + ")";

        db.execSQL(CREATE_PHONEBOOK_TABLE);
    }
    // end of creates necessary tables

    // TODO - to be used in the near future (E.g., user wants to update product list manually by uploading a PDF file)
//    public boolean loadProductInfo(PDFFileParser pdfFileParser) {
//        SQLiteDatabase db = this.getWritableDatabase();
//
//        boolean bRet = loadProductInfo(db, pdfFileParser);
//
//        db.close();
//
//        return bRet;
//    }
    public boolean loadProductInfo(SQLiteDatabase db, PDFFileParser pdfFileParser) {
        boolean bRet = true;

        if(!pdfFileParser.isProductInfoListEmpty()) {
            db.beginTransaction();

            try {
                for (Map.Entry<String, LinkedHashMap<String, ArrayList<ProductCodeInfo>>> product_name_list : pdfFileParser.getProductInfoList().entrySet()) {
                    long category_id = addProductCategory(db, product_name_list.getKey());
                    if(category_id  < 0) {
                        bRet = false;
                        break;
                    }

                    for(Map.Entry<String, ArrayList<ProductCodeInfo>> product_code : product_name_list.getValue().entrySet()) {
                        long name_id = addProductName(db, category_id, product_code.getKey());
                        if(name_id  < 0) {
                            bRet = false;
                            break;
                        }

                        addProductCodes(db, name_id, product_code.getValue());
                    }
                }

                if(bRet)
                    db.setTransactionSuccessful();
            } catch (SQLiteException e) {
                m_error_message += "DatabaseHandler::loadProductInfo - SQLiteException: " + e.getMessage() + "\n";
                bRet = false;
            } catch (SQLException e) {
                m_error_message += "DatabaseHandler::loadProductInfo - SQLException: " + e.getMessage() + "\n";
                bRet = false;
            } catch (Exception e) {
                m_error_message += "DatabaseHandler::loadProductInfo - Exception: " + e.getMessage() + "\n";
                bRet = false;
            } finally {
                db.endTransaction();
            }
        }

        return bRet;
    }
    private long addProductCategory(SQLiteDatabase db, String product_category) {
        ContentValues values = new ContentValues();
        values.put(PRODUCT_CATEGORY_COLUMN_CATEGORY, product_category);

        return db.insert(TABLE_PRODUCT_CATEGORY, null, values);
    }
    private long addProductName(SQLiteDatabase db, long product_category_id, String product_name) {
        ContentValues values = new ContentValues();
        values.put(PRODUCT_NAME_COLUMN_PRODUCT_CATEGORY_ID, product_category_id);
        values.put(PRODUCT_NAME_COLUMN_NAME, product_name);

        return db.insert(TABLE_PRODUCT_NAME, null, values);
    }
    private void addProductCodes(SQLiteDatabase db, long product_name_id, ArrayList<ProductCodeInfo> productCodeInfos) {
        String insertQuery = "INSERT INTO " +
                TABLE_PRODUCT_CODE + " (" +
                PRODUCT_CODE_COLUMN_PRODUCT_NAME_ID + ", " +
                PRODUCT_CODE_COLUMN_CODE + ", " +
                PRODUCT_CODE_COLUMN_DESCRIPTION + ", " +
                PRODUCT_CODE_COLUMN_DISCOUNT + ") " +
                "VALUES (?,?,?,?)";

        SQLiteStatement sqLiteStatement = db.compileStatement(insertQuery);

        for(ProductCodeInfo productCodeInfo : productCodeInfos) {
            sqLiteStatement.bindLong(1, product_name_id);    // PRODUCT_CODE_COLUMN_PRODUCT_NAME_ID
            sqLiteStatement.bindString(2, productCodeInfo.mProduct);   // PRODUCT_CODE_COLUMN_CODE
            sqLiteStatement.bindString(3, productCodeInfo.mProductDescription);   // PRODUCT_CODE_COLUMN_DESCRIPTION
            sqLiteStatement.bindDouble(4, productCodeInfo.mDiscount);  // PRODUCT_CODE_COLUMN_DISCOUNT

            sqLiteStatement.execute();
            sqLiteStatement.clearBindings();
        }
    }

    public MyUtility.Pair<Integer, Double> getDiscountIfProductCodeExist(String product_code) {
        MyUtility.Pair<Integer, Double> pairIdDiscount = new MyUtility.Pair<>(-1, -1.0);

        String selectQuery = "SELECT " +
                PRODUCT_CODE_COLUMN_ID + ", " +
                PRODUCT_CODE_COLUMN_DISCOUNT + " FROM " +
                TABLE_PRODUCT_CODE + " WHERE " +
                PRODUCT_CODE_COLUMN_CODE + "='" + product_code + "' LIMIT 1";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if(cursor.moveToFirst()) {
            cursor.moveToFirst();
            pairIdDiscount.m_first = cursor.getInt(cursor.getColumnIndex(PRODUCT_CODE_COLUMN_ID));
            pairIdDiscount.m_second = cursor.getDouble(cursor.getColumnIndex(PRODUCT_CODE_COLUMN_DISCOUNT));
            cursor.close();
        }

        cursor.close();
        db.close();

        return pairIdDiscount;
    }

    public ArrayList<ProductCodeInfo> getProductCodeList() {
        ArrayList<ProductCodeInfo> product_code_list = new ArrayList<>();
        String selectQuery = "SELECT " +
                PRODUCT_CODE_COLUMN_CODE + ", " +
                PRODUCT_CODE_COLUMN_DESCRIPTION + ", " +
                PRODUCT_CODE_COLUMN_DISCOUNT + " FROM " +
                TABLE_PRODUCT_CODE;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if(cursor.moveToFirst()){
            do {
                String productCode = cursor.getString(cursor.getColumnIndex(PRODUCT_CODE_COLUMN_CODE));
                String productDesc = cursor.getString(cursor.getColumnIndex(PRODUCT_CODE_COLUMN_DESCRIPTION));
                double productDisc = cursor.getDouble(cursor.getColumnIndex(PRODUCT_CODE_COLUMN_DISCOUNT));

                ProductCodeInfo productCodeInfo = new ProductCodeInfo(productCode, productDesc, productDisc);
                product_code_list.add(productCodeInfo);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return product_code_list;
    }
    public ArrayList<MyUtility.Pair<String, MyUtility.Pair<Double, MyUtility.Pair<Integer, Integer>>>> getProductCodeWithAmount() {
        ArrayList<MyUtility.Pair<String, MyUtility.Pair<Double, MyUtility.Pair<Integer, Integer>>>> productCodes = new ArrayList<>();
        String selectQuery = "SELECT " +
                PRODUCT_CODE_COLUMN_CODE + ", " +
                PRODUCT_CODE_COLUMN_DISCOUNT + ", " +
                PRODUCT_CODE_COLUMN_DESCRIPTION + " FROM " +
                TABLE_PRODUCT_CODE + " WHERE " +
                PRODUCT_CODE_COLUMN_CODE + " LIKE '%amount%'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if(cursor.moveToFirst()) {
            do {
                String productCode = cursor.getString(cursor.getColumnIndex(PRODUCT_CODE_COLUMN_CODE));
                double discount = cursor.getDouble(cursor.getColumnIndex(PRODUCT_CODE_COLUMN_DISCOUNT));
                String description = cursor.getString(cursor.getColumnIndex(PRODUCT_CODE_COLUMN_DESCRIPTION));
                MyUtility.Pair<Integer, Integer> minAndMaxValue = getMinAndMaxValue(description);

                if(minAndMaxValue.m_first > -1 && minAndMaxValue.m_second > -1)
                    productCodes.add(new MyUtility.Pair(productCode, new MyUtility.Pair(discount, minAndMaxValue)));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return productCodes;
    }
    private MyUtility.Pair<Integer, Integer> getMinAndMaxValue(String description) {
        MyUtility.Pair<Integer, Integer> minAndMax = new MyUtility.Pair<>(-1, -1);

        String minVal = MyUtility.getStringFromRegex(description, "^([0-9]+)");
        String maxVal = MyUtility.getStringFromRegex(description, "([0-9]+)$");

        if(!minVal.isEmpty() && !maxVal.isEmpty()) {
            minAndMax.m_first = Integer.parseInt(minVal);
            minAndMax.m_second = Integer.parseInt(maxVal);
        }

        return minAndMax;
    }

    public long addDeposit(double amount, String dateTime) {
        ContentValues values = new ContentValues();
        values.put(DEPOSIT_COLUMN_AMOUNT, amount);
        values.put(DEPOSIT_COLUMN_DATETIME, dateTime);

        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        long newId = db.insert(TABLE_DEPOSIT, null, values);
        if(newId > -1) {
            double latestBalance = getLatestBalance(db);
            latestBalance = (latestBalance == MyUtility.TABLE_EMPTY) ? 0 : latestBalance;

            if(latestBalance < 0)
                newId = -1;
            else
                if(addBalance(db, newId, latestBalance + amount, TABLE_DEPOSIT) < 0)
                    newId = -1;
                else
                    db.setTransactionSuccessful();
        }

        db.endTransaction();
        db.close();

        return newId;
    }

    public long addSellLoad(int productCodeID, String dateTime, String number, String product, double amount, double balance, String description, boolean isPaid) {
        ContentValues values = new ContentValues();
        values.put(SELL_LOAD_COLUMN_PRODUCT_CODE_ID, productCodeID);
        values.put(SELL_LOAD_COLUMN_PRODUCT, product);
        values.put(SELL_LOAD_COLUMN_AMOUNT, amount);
        values.put(SELL_LOAD_COLUMN_NUMBER, number);
        values.put(SELL_LOAD_COLUMN_BALANCE, balance);
        values.put(SELL_LOAD_COLUMN_DESCRIPTION, description);
        values.put(SELL_LOAD_COLUMN_DATETIME_SOLD, dateTime);
        values.put(SELL_LOAD_COLUMN_STATUS, isPaid);

        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        long newId = db.insert(TABLE_SELL_LOAD, null, values);
        if(newId > -1)
            if(addBalance(db, newId, balance, TABLE_SELL_LOAD) < 0)
                newId = -1;
            else
                db.setTransactionSuccessful();

        db.endTransaction();
        db.close();

        return newId;
    }
    public boolean setSellLoadPaid(int id) {
        boolean bRet;
        ContentValues values = new ContentValues();
        values.put(SELL_LOAD_COLUMN_STATUS, true);
        values.put(SELL_LOAD_COLUMN_DATETIME_PAID, MyUtility.getCurrentDate());
        SQLiteDatabase db = this.getWritableDatabase();

        bRet = db.update(TABLE_SELL_LOAD, values, SELL_LOAD_COLUMN_ID + "=" + id, null) > 0;

        db.close();

        return bRet;
    }
//    public boolean getSellLoadList(TransactionRecordListViewAdapter listViewAdapter) {
//        boolean bRet = true;
//
//        String query = "SELECT * FROM " +
//                TABLE_SELL_LOAD + " ORDER BY " +
//                SELL_LOAD_COLUMN_DATETIME_SOLD + " DESC, " +
//                SELL_LOAD_COLUMN_ID + " DESC";
//        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor cursor = db.rawQuery(query, null);
//
//        if(cursor.moveToFirst()) {
//            do {
//                int id = cursor.getInt(cursor.getColumnIndex(SELL_LOAD_COLUMN_ID));
//                String product = cursor.getString(cursor.getColumnIndex(SELL_LOAD_COLUMN_PRODUCT));
//                String number = cursor.getString(cursor.getColumnIndex(SELL_LOAD_COLUMN_NUMBER));
//                String dateTime = cursor.getString(cursor.getColumnIndex(SELL_LOAD_COLUMN_DATETIME_SOLD));
//                double balance = cursor.getDouble(cursor.getColumnIndex(SELL_LOAD_COLUMN_BALANCE));
//                String description = (cursor.isNull(cursor.getColumnIndex(SELL_LOAD_COLUMN_DESCRIPTION))) ? "" : cursor.getString(cursor.getColumnIndex(SELL_LOAD_COLUMN_DESCRIPTION));
//                boolean isPaid = cursor.getInt(cursor.getColumnIndex(SELL_LOAD_COLUMN_STATUS)) != 0;
//
//                SoldLoadInfo soldLoadInfo = new SoldLoadInfo(id, product, number, dateTime, balance, description, isPaid);
//                listViewAdapter.add(soldLoadInfo);
//            } while (cursor.moveToNext());
//        }
//        else
//            bRet = false;
//
//        cursor.close();
//        db.close();
//
//        return bRet;
//    }
//    public long getSellLoadID(String number, String product, double balance) {
//        String selectQuery = "SELECT " +
//                SELL_LOAD_COLUMN_ID + " FROM " +
//                TABLE_SELL_LOAD + " WHERE " +
//                SELL_LOAD_COLUMN_NUMBER + "='" + number + "' AND " +
//                SELL_LOAD_COLUMN_PRODUCT + "='" + product + "' AND " +
//                SELL_LOAD_COLUMN_BALANCE + "=" + balance + " ORDER BY " +
//                SELL_LOAD_COLUMN_ID + " DESC LIMIT 1";
//        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor cursor = db.rawQuery(selectQuery, null);
//        long id = -1;
//
//        if(cursor.moveToFirst()) {
//            cursor.moveToFirst();
//            id = cursor.getLong(cursor.getColumnIndex(SELL_LOAD_COLUMN_ID));
//        }
//
//        cursor.close();
//        db.close();
//
//        return id;
//    }

    private long addBalance(SQLiteDatabase db, long rowId, double balance, String tableName) {
        ContentValues values = new ContentValues();
        values.put(BALANCE_COLUMN_ROW_ID, rowId);
        values.put(BALANCE_COLUMN_BALANCE, balance);
        values.put(BALANCE_COLUMN_TABLE_NAME, tableName);

        return db.insert(TABLE_BALANCE, null, values);
    }
    public double getLatestBalance() {
        SQLiteDatabase db = this.getReadableDatabase();
        double balance = getLatestBalance(db);

        db.close();

        return balance;
    }
    public double getLatestBalance(SQLiteDatabase db) {
        if(isTableEmpty(db, TABLE_BALANCE))
            return (double) MyUtility.TABLE_EMPTY;

        String query = "SELECT " +
                BALANCE_COLUMN_BALANCE + " AS " +
                BALANCE_COLUMN_LATEST_BALANCE + " FROM " +
                TABLE_BALANCE + " ORDER BY " +
                BALANCE_COLUMN_ID + " DESC LIMIT 1";
        Cursor cursor = db.rawQuery(query, null);
        double balance = -1;

        if(cursor.moveToFirst()) {
            cursor.moveToFirst();
            balance = Double.parseDouble(cursor.getString(cursor.getColumnIndex(BALANCE_COLUMN_LATEST_BALANCE)));
        }

        cursor.close();

        return balance;
    }
    // TODO - what to do with this function?
//    private MyUtility.Pair<Double, Double> getPreviousAndLatestBalance(int id, String tableName) {
//        SQLiteDatabase db = this.getReadableDatabase();
//        MyUtility.Pair<Double, Double> previousAndLatestBalance = getPreviousAndLatestBalance(db, id, tableName);
//
//        db.close();
//
//        return previousAndLatestBalance;
//    }
    // TODO - what to do with this function?
//    private MyUtility.Pair<Double, Double> getPreviousAndLatestBalance(SQLiteDatabase db, int sellLoadId, String tableName) {
//        double latestBalance = getLatestBalance(db);
//        latestBalance = (latestBalance == MyUtility.TABLE_EMPTY) ? 0 : latestBalance;
//        MyUtility.Pair<Double, Double> previousLatestBalance = new MyUtility.Pair<>(-1.0, -1.0);
//
//        if(latestBalance < 0)
//            return previousLatestBalance;
//
//        String selectQuery = "SELECT COUNT(*) AS " + BALANCE_COLUMN_COUNT_BALANCE + ", " +
//                BALANCE_COLUMN_BALANCE + " AS " + BALANCE_COLUMN_PREVIOUS_BALANCE +  " FROM " +
//                TABLE_BALANCE + " WHERE " +
//                BALANCE_COLUMN_ID + "<" + sellLoadId + " ORDER BY " +
//                BALANCE_COLUMN_ID + " DESC LIMIT 1";
//        Cursor cursor = db.rawQuery(selectQuery, null);
//
//        if(cursor.moveToFirst()) {
//            if(cursor.getInt(cursor.getColumnIndex(BALANCE_COLUMN_COUNT_BALANCE)) > 0)  // there are contents in the table
//                previousLatestBalance.m_first = cursor.getDouble(cursor.getColumnIndex(BALANCE_COLUMN_PREVIOUS_BALANCE));
//            else
//                previousLatestBalance.m_first = (double) 0;
//
//            previousLatestBalance.m_second = latestBalance;
//        }
//
//        cursor.close();
//
//        return previousLatestBalance;
//    }

    // TODO - add for info from product_code table...
    public ArrayList<TransactionRecordInfo> getTransactionRecordList() {
        ArrayList<TransactionRecordInfo> transactionRecordInfos = new ArrayList<>();
        final String TABLE_ID = "table_id";
        final String DATETIME_DEPOSITED = "datetime_deposited";
        final String PRODUCT_DESCRIPTION = "product_description";
        final String COMPARED_DATETIME = "compared_datetime";
        final String PHONEBOOK_NAME = "phonebook_name";
        final String SELL_LOAD_AMOUNT = "sell_load_amount";
        final String DEPOSIT_AMOUNT = "deposit_amount";

        String selectQuery = "SELECT " + TABLE_BALANCE + "." + BALANCE_COLUMN_ROW_ID + " AS " + TABLE_ID + ", " +
                // balance
                TABLE_BALANCE + "." + BALANCE_COLUMN_ID + ", " +
                TABLE_BALANCE + "." + BALANCE_COLUMN_BALANCE + ", " +
                TABLE_BALANCE + "." + BALANCE_COLUMN_TABLE_NAME + ", " +
                // product_category
                TABLE_PRODUCT_CATEGORY + "." + PRODUCT_CATEGORY_COLUMN_CATEGORY + ", " +
                // product_name
                TABLE_PRODUCT_NAME + "." + PRODUCT_NAME_COLUMN_NAME + ", " +
                // product_code
                TABLE_PRODUCT_CODE + "." + PRODUCT_CODE_COLUMN_CODE + ", " +
                TABLE_PRODUCT_CODE + "." + PRODUCT_CODE_COLUMN_DESCRIPTION + " AS " + PRODUCT_DESCRIPTION + ", " +
                TABLE_PRODUCT_CODE + "." + PRODUCT_CODE_COLUMN_DISCOUNT + ", " +
                // sell_load
                TABLE_SELL_LOAD + "." + SELL_LOAD_COLUMN_PRODUCT + ", " +    // TODO - to be removed later... or not (e.g. GMXAMAX20 => GMXAMAX<AMOUNT>)
                TABLE_SELL_LOAD + "." + SELL_LOAD_COLUMN_AMOUNT + " AS " + SELL_LOAD_AMOUNT + ", " +
                TABLE_SELL_LOAD + "." + SELL_LOAD_COLUMN_NUMBER + ", " +
                TABLE_SELL_LOAD + "." + SELL_LOAD_COLUMN_DATETIME_SOLD + ", " +
                TABLE_SELL_LOAD + "." + SELL_LOAD_COLUMN_DESCRIPTION + ", " +
                TABLE_SELL_LOAD + "." + SELL_LOAD_COLUMN_STATUS + ", " +
                TABLE_SELL_LOAD + "." + SELL_LOAD_COLUMN_DATETIME_PAID + ", " +
                // deposit
                TABLE_DEPOSIT + "." + DEPOSIT_COLUMN_AMOUNT + " AS " + DEPOSIT_AMOUNT + ", " +
                TABLE_DEPOSIT + "." + DEPOSIT_COLUMN_DATETIME + " AS " + DATETIME_DEPOSITED + ", " +
                // phonebook
                TABLE_PHONEBOOK + "." + PHONEBOOK_COLUMN_NAME + " AS " + PHONEBOOK_NAME + ", " +
                // date compared to sort records...
                "CASE WHEN " +
                    TABLE_SELL_LOAD + "." + SELL_LOAD_COLUMN_DATETIME_SOLD + " IS NOT NULL" +
                " THEN " + TABLE_SELL_LOAD + "." + SELL_LOAD_COLUMN_DATETIME_SOLD +
                " ELSE " + TABLE_DEPOSIT + "." + DEPOSIT_COLUMN_DATETIME +
                " END AS " + COMPARED_DATETIME + " " +

                "FROM " + TABLE_BALANCE + " " +
                "LEFT JOIN " + TABLE_SELL_LOAD + " ON " + TABLE_ID + "=" + TABLE_SELL_LOAD + "." + SELL_LOAD_COLUMN_ID +
                    " AND " + TABLE_BALANCE + "." + BALANCE_COLUMN_TABLE_NAME + "='" + TABLE_SELL_LOAD + "' " +
                "LEFT JOIN " + TABLE_DEPOSIT + " ON " + TABLE_ID + "=" + TABLE_DEPOSIT + "." + DEPOSIT_COLUMN_ID +
                    " AND " + TABLE_BALANCE + "." + BALANCE_COLUMN_TABLE_NAME + "='" + TABLE_DEPOSIT + "' " +
                "LEFT JOIN " + TABLE_PRODUCT_CODE + " ON " + TABLE_SELL_LOAD + "." + SELL_LOAD_COLUMN_PRODUCT_CODE_ID + "=" + TABLE_PRODUCT_CODE + "." + PRODUCT_CODE_COLUMN_ID +
                    " AND " + TABLE_BALANCE + "." + BALANCE_COLUMN_TABLE_NAME + "='" + TABLE_SELL_LOAD + "' " +
                "LEFT JOIN " + TABLE_PRODUCT_NAME + " ON " + TABLE_PRODUCT_CODE + "." + PRODUCT_CODE_COLUMN_PRODUCT_NAME_ID + "=" + TABLE_PRODUCT_NAME + "." + PRODUCT_NAME_COLUMN_ID +
                    " AND " + TABLE_BALANCE + "." + BALANCE_COLUMN_TABLE_NAME + "='" + TABLE_SELL_LOAD + "' " +
                "LEFT JOIN " + TABLE_PRODUCT_CATEGORY + " ON " + TABLE_PRODUCT_NAME + "." + PRODUCT_NAME_COLUMN_PRODUCT_CATEGORY_ID + "=" + TABLE_PRODUCT_CATEGORY + "." + PRODUCT_CATEGORY_COLUMN_ID +
                    " AND " + TABLE_BALANCE + "." + BALANCE_COLUMN_TABLE_NAME + "='" + TABLE_SELL_LOAD + "' " +
                "LEFT JOIN " + TABLE_PHONEBOOK + " ON " + TABLE_SELL_LOAD + "." + SELL_LOAD_COLUMN_NUMBER + "=" + TABLE_PHONEBOOK + "." + PHONEBOOK_COLUMN_NUMBER +
                    " AND " + TABLE_BALANCE + "." + BALANCE_COLUMN_TABLE_NAME + "='" + TABLE_SELL_LOAD + "' " +
                "ORDER BY " + COMPARED_DATETIME + " ASC, " +
                    TABLE_BALANCE + "." + BALANCE_COLUMN_ID + " ASC";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if(cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex(BALANCE_COLUMN_ID));   // id of balance table
                int tableId = cursor.getInt(cursor.getColumnIndex(TABLE_ID));   // id either in sell_load or deposit table
                String tableName = cursor.getString(cursor.getColumnIndex(BALANCE_COLUMN_TABLE_NAME));
                double balance = cursor.getDouble(cursor.getColumnIndex(BALANCE_COLUMN_BALANCE));

                if(tableName.equals(TABLE_SELL_LOAD)) {
                    String product = cursor.getString(cursor.getColumnIndex(SELL_LOAD_COLUMN_PRODUCT));
                    double amount = cursor.getDouble(cursor.getColumnIndex(SELL_LOAD_AMOUNT));
                    String number = cursor.getString(cursor.getColumnIndex(SELL_LOAD_COLUMN_NUMBER));
                    String dateTime = cursor.getString(cursor.getColumnIndex(SELL_LOAD_COLUMN_DATETIME_SOLD));
                    String description = (cursor.isNull(cursor.getColumnIndex(SELL_LOAD_COLUMN_DESCRIPTION))) ? "" : cursor.getString(cursor.getColumnIndex(SELL_LOAD_COLUMN_DESCRIPTION));
                    boolean isPaid = cursor.getInt(cursor.getColumnIndex(SELL_LOAD_COLUMN_STATUS)) != 0;

                    String productCode = cursor.getString(cursor.getColumnIndex(PRODUCT_CODE_COLUMN_CODE));
                    double productDiscount = cursor.getDouble(cursor.getColumnIndex(PRODUCT_CODE_COLUMN_DISCOUNT));
                    String productDescription = cursor.getString(cursor.getColumnIndex(PRODUCT_DESCRIPTION));
                    String productName = cursor.getString(cursor.getColumnIndex(PRODUCT_NAME_COLUMN_NAME));
                    String productCategoryName = cursor.getString(cursor.getColumnIndex(PRODUCT_CATEGORY_COLUMN_CATEGORY));

                    String phonebookName = (cursor.isNull(cursor.getColumnIndex(PHONEBOOK_NAME))) ? null : cursor.getString(cursor.getColumnIndex(PHONEBOOK_NAME));

                    MyUtility.logMessage(productCode + " " + productDiscount);

                    transactionRecordInfos.add(
                            new TransactionRecordInfo(id, tableName, balance,
                                    new SoldLoadInfo(tableId, product, amount, number, phonebookName, dateTime, balance, description, isPaid,
                                            new ProductInfo(new ProductCodeInfo(productCode, productDescription, productDiscount), productName, productCategoryName)
                                    )
                            )
                    );
                }
                else if(tableName.equals(TABLE_DEPOSIT)) {
                    double amount = cursor.getDouble(cursor.getColumnIndex(DEPOSIT_AMOUNT));
                    String dateTime = cursor.getString(cursor.getColumnIndex(DATETIME_DEPOSITED));

                    transactionRecordInfos.add(
                            new TransactionRecordInfo(id, tableName, balance,
                                new DepositInfo(tableId, amount, dateTime)
                            )
                    );
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return transactionRecordInfos;
    }

    public long addPhonebook(String name, String number) {
        ContentValues values = new ContentValues();
        values.put(PHONEBOOK_COLUMN_NAME, name);
        values.put(PHONEBOOK_COLUMN_NUMBER, number);

        SQLiteDatabase db = this.getWritableDatabase();

        long newId = db.insert(TABLE_PHONEBOOK, null, values);

        db.close();

        return newId;
    }
    public boolean deletePhonebook(int id) {
        boolean bRet;
        SQLiteDatabase db = this.getWritableDatabase();

        bRet = db.delete(TABLE_PHONEBOOK, PHONEBOOK_COLUMN_ID + "=" + id, null) > 0;

        db.close();

        return bRet;
    }
    public boolean updatePhoneBook(int id, String name, String number) {
        boolean bRet;
        ContentValues values = new ContentValues();
        values.put(PHONEBOOK_COLUMN_NAME, name);
        values.put(PHONEBOOK_COLUMN_NUMBER, number);
        SQLiteDatabase db = this.getWritableDatabase();

        bRet = db.update(TABLE_PHONEBOOK, values, PHONEBOOK_COLUMN_ID + "=" + id, null) > 0;

        db.close();

        return bRet;
    }
    public boolean isNumberExists(String number) {
        String query = "SELECT * FROM " +
                TABLE_PHONEBOOK + " WHERE " +
                PHONEBOOK_COLUMN_NUMBER + " = " + number + " LIMIT 1";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        boolean isExists = false;

        if(cursor.moveToFirst()) {
            isExists = true;
        }

        cursor.close();
        db.close();

        return isExists;
    }
    public ArrayList<PhonebookInfo> getPhonebookList() {
        ArrayList<PhonebookInfo> arrayOfPhonebookInfo = new ArrayList<>();

        String query = "SELECT * FROM " +
                TABLE_PHONEBOOK + " ORDER BY " +
                PHONEBOOK_COLUMN_NAME + ", " +
                PHONEBOOK_COLUMN_ID;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if(cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex(PHONEBOOK_COLUMN_ID));
                String name = cursor.getString(cursor.getColumnIndex(PHONEBOOK_COLUMN_NAME));
                String number = cursor.getString(cursor.getColumnIndex(PHONEBOOK_COLUMN_NUMBER));

                PhonebookInfo phonebookInfo = new PhonebookInfo(id, name, number);
                arrayOfPhonebookInfo.add(phonebookInfo);
            } while (cursor.moveToNext());
        }
        else
            arrayOfPhonebookInfo.clear();

        cursor.close();
        db.close();

        return arrayOfPhonebookInfo;
    }

    private boolean isTableEmpty(SQLiteDatabase db, String tableName) {
        boolean bRet = true;
        String query = "SELECT COUNT(*) FROM " + tableName;
        Cursor cursor = db.rawQuery(query, null);

        if(cursor.moveToFirst()) {
            bRet = (cursor.getInt(0)) <= 0;
        }

        cursor.close();

        return bRet;
    }
}
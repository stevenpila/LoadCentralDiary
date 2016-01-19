package com.example.stevenpila.loadcentraldiary;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    private ListView mTransactionRecordListView;
    private DatabaseHandler mDBHandler;

    static final String DEPOSIT = "DEPOSIT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View navHeaderView = LayoutInflater.from(this).inflate(R.layout.nav_header_home, null); // add header view
        navigationView.addHeaderView(navHeaderView);

        mDBHandler = new DatabaseHandler(this); // initialize database first

        setCurrentBalance(navHeaderView);       // initialize current balance
        setTransactionRecordListView();         // initialize list view
        setDateRangeSpinner();                  // initialize date range spinner
        setPaidStatusSpinner();                 // initialize paid status spinner
        setSearchEditText();                    // initialize search text

        // for testing & debugging...
//        TextView testView = (TextView) findViewById(R.id.textView);
//        PDFFileParser pdfFileParser = new PDFFileParser();
//        if(!pdfFileParser.loadProductLoadList(this))
//            testView.setText(pdfFileParser.getErrorMessage());
//        else
//            testView.setText(pdfFileParser.m_response);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_settings: break;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        switch(id) {
            case R.id.nav_deposit:
                startActivity(new Intent(this, DepositActivity.class));
                break;
            case R.id.nav_sell_load:
                startActivity(new Intent(this, SellLoadActivity.class));
                break;
            case R.id.nav_phonebook:
                startActivity(new Intent(this, PhonebookActivity.class));
                break;
            case R.id.nav_report:
                startActivity(new Intent(this, HomeActivity.class));
                break;
        }

        return true;
    }

    // jeff

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        if(view.getId() == R.id.contentHomeTransactionRecordLV) {
            MenuInflater menuInflater = getMenuInflater();
            menuInflater.inflate(R.menu.list_view_item_menu_home, menu);
            AdapterView.AdapterContextMenuInfo adapterContextMenuInfo = (AdapterView.AdapterContextMenuInfo) menuInfo;
            int position = adapterContextMenuInfo.position;
            TransactionRecordInfo transactionRecordInfo = (TransactionRecordInfo) mTransactionRecordListView.getItemAtPosition(position);

            if(transactionRecordInfo.mTableName.equals(DatabaseHandler.TABLE_SELL_LOAD)) {
                boolean isPaid = ((TransactionRecordInfo) mTransactionRecordListView.getItemAtPosition(position)).mSoldLoadInfo.mIsPaid;

                if(isPaid)  // hide Paid button if sell load record is already paid
                    menu.findItem(R.id.paidItem).setVisible(false);
            }
            else if(transactionRecordInfo.mTableName.equals(DatabaseHandler.TABLE_DEPOSIT)) // hide Paid button if deposit record
                menu.findItem(R.id.paidItem).setVisible(false);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo adapterContextMenuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int position = adapterContextMenuInfo.position;
        TransactionRecordInfo transactionRecordInfo = (TransactionRecordInfo) mTransactionRecordListView.getItemAtPosition(position);

        switch (item.getItemId()) {
            case R.id.paidItem:
                if(mDBHandler.setSellLoadPaid(transactionRecordInfo.mSoldLoadInfo.mId)) {  // if successfully updated sell load record paid status to true
                    ImageView paidImage = (ImageView) adapterContextMenuInfo.targetView.findViewById(R.id.listViewItemHomeSellLoadPaidIV);   // get paid ImageView

                    ((TransactionRecordInfo) mTransactionRecordListView.getItemAtPosition(position)).mSoldLoadInfo.mIsPaid = true; // update sell load record paid status to true
                    paidImage.setBackgroundColor(getResources().getColor(R.color.colorGreen));  // update background color of paid ImageView

                    MyUtility.showToast(this, "Paid successfully.", MyUtility.ToastLength.LONG);
                }
                else
                    MyUtility.logMessage("Failed to set status to paid.");

                return true;
            case R.id.editItem:     // TODO - do something here...
                return true;
            case R.id.deleteItem:   // TODO - do something here...
                return true;
            case R.id.detailsItem:
                int actualAmount = MyUtility.getProductAndAmountFromString(transactionRecordInfo.mSoldLoadInfo.mProduct).m_second;
                double discountedAmount = MyUtility.roundOff((actualAmount * transactionRecordInfo.mSoldLoadInfo.mProductInfo.mProductCodeInfo.mDiscount) / 100, 2);
                String phonebookName = (transactionRecordInfo.mSoldLoadInfo.mNumberName != null) ? transactionRecordInfo.mSoldLoadInfo.mNumberName : "Unknown";

                String details = "Number: " + transactionRecordInfo.mSoldLoadInfo.mNumber + " (" + phonebookName + ")\n" +
                        "Product: " + transactionRecordInfo.mSoldLoadInfo.mProduct + "\n" +
                        "Amount: " + MyUtility.PESO_SIGN + actualAmount + "\n" +
                        "Discount: " + MyUtility.setDecimalPlaces(2, transactionRecordInfo.mSoldLoadInfo.mProductInfo.mProductCodeInfo.mDiscount) + "%\n" +
                        "Discounted Amount: " + MyUtility.PESO_SIGN + MyUtility.setDecimalPlaces(2, actualAmount - discountedAmount) + "\n" +
                        "Profit: " + MyUtility.PESO_SIGN + MyUtility.setDecimalPlaces(2, actualAmount - (actualAmount - discountedAmount));

                alertDialog("Summary Details", details);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void alertDialog(String title, String description) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(description)
                .setPositiveButton("OK", null)
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }

    private void setCurrentBalance(View view) {
        double currentBalance = mDBHandler.getLatestBalance();

        if(currentBalance < 0) {
            currentBalance = 0.0;

            MyUtility.logMessage("Failed to get current balance.");
        }

        TextView balanceTV = (TextView) view.findViewById(R.id.balance);
        MyUtility.setTextViewValue(balanceTV, currentBalance); // initializes TextView value with the current balance
    }

    private void setTransactionRecordListView() {
        mTransactionRecordListView = (ListView) findViewById(R.id.contentHomeTransactionRecordLV);

        ArrayList<TransactionRecordInfo> transactionRecordInfos = mDBHandler.getTransactionRecordList();
        mTransactionRecordListView.setAdapter(new TransactionRecordListViewAdapter(this, transactionRecordInfos));
        registerForContextMenu(mTransactionRecordListView); // triggered by long-pressed click

        mTransactionRecordListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TransactionRecordInfo transactionRecordInfo = (TransactionRecordInfo) parent.getItemAtPosition(position);

                if (transactionRecordInfo.mTableName.equals(DatabaseHandler.TABLE_SELL_LOAD) && !transactionRecordInfo.mSoldLoadInfo.mDescription.isEmpty()) {
                    alertDialog("Description", transactionRecordInfo.mSoldLoadInfo.mDescription);
                }
            }
        });
    }

    private void setDateRangeSpinner() {
        MyDateRangeSpinner dateRangeSpinner = (MyDateRangeSpinner) findViewById(R.id.contentHomeDateRangeSpinner);
        String[] dateRanges = getResources().getStringArray(R.array.date_ranges);

        dateRangeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 6:
                        showCustomRangeDatePickerDialog(position);
                        return;
                }

                ((TransactionRecordListViewAdapter) mTransactionRecordListView.getAdapter()).setArrayListByDateRange(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        dateRanges[0] += " (" + MyUtility.getMonth(MyUtility.getCalendar().get(Calendar.MONTH)) + ")";
        ArrayAdapter dateRangeAdapter = new ArrayAdapter(this, R.layout.spinner_item_home_date_range, R.id.spinnerItemHomeDateRangeTV, dateRanges);
        dateRangeSpinner.setAdapter(dateRangeAdapter);
    }
    private void showCustomRangeDatePickerDialog(final int position) {
        final MyUtility.Pair<String, String> dateFromTo = new MyUtility.Pair<>(null, null);

        new DatePickerDialog(HomeActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                dateFromTo.m_first = MyUtility.getCurrentDate(year, monthOfYear, dayOfMonth);
                new DatePickerDialog(HomeActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        dateFromTo.m_second = MyUtility.getCurrentDate(year, monthOfYear, dayOfMonth);

                        ((TransactionRecordListViewAdapter) mTransactionRecordListView.getAdapter()).setArrayListByDateRange(position, dateFromTo);
                    }
                }, MyUtility.getCalendar().get(Calendar.YEAR), MyUtility.getCalendar().get(Calendar.MONTH), MyUtility.getCalendar().get(Calendar.DAY_OF_MONTH)).show();
            }
        }, MyUtility.getCalendar().get(Calendar.YEAR), MyUtility.getCalendar().get(Calendar.MONTH), MyUtility.getCalendar().get(Calendar.DAY_OF_MONTH)).show();
    }

    private void setPaidStatusSpinner() {
        Spinner paidStatusSpinner = (Spinner) findViewById(R.id.contentHomePaidStatusSPinner);
        String[] paidStatus = getResources().getStringArray(R.array.paid_status);
        paidStatusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TransactionRecordListViewAdapter) mTransactionRecordListView.getAdapter()).setArrayListByPaidStatus(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        ArrayAdapter paidStatusAdapter = new ArrayAdapter(this, R.layout.spinner_item_home_date_range, R.id.spinnerItemHomeDateRangeTV, paidStatus);
        paidStatusSpinner.setAdapter(paidStatusAdapter);
    }

    private void setSearchEditText() {
        EditText searchET = (EditText) findViewById(R.id.contentHomeSearchET);
        searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ((TransactionRecordListViewAdapter) mTransactionRecordListView.getAdapter()).getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
}
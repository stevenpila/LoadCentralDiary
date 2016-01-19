package com.example.stevenpila.loadcentraldiary;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.DatePicker;
import android.widget.TextView;

import java.util.Calendar;

public class DepositActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private TextView balanceView;
    private MyEditText dateTxt;
    private MyEditText amountTxt;

    private boolean isFocusAmountTxt = false;
    private double m_currentBalance;

    private DatabaseHandler m_dbHandler;


    private static int DATEPICKER_DIALOG_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deposit);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // jeff
        m_dbHandler = new DatabaseHandler(this);    // initialize database first

        dateTxt = (MyEditText) findViewById(R.id.date);
        amountTxt = (MyEditText) findViewById(R.id.amount);

        View navHeaderView = LayoutInflater.from(this).inflate(R.layout.nav_header_deposit, null);
        navigationView.addHeaderView(navHeaderView);

        balanceView = (TextView) navHeaderView.findViewById(R.id.balance);

        setCurrentBalance();    // initialize current balance

        MyUtility.setTextViewValue(balanceView, m_currentBalance); // initializes the current balance upon application start
        dateTxt.setInputType(InputType.TYPE_NULL);
        dateTxt.setText(MyUtility.getCurrentDate());    // set default value to current date (format: yyyy-mm-dd)
        amountTxt.setOnFocusChangeListener(new View.OnFocusChangeListener() {   // if value is empty, set to default value of 0
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    isFocusAmountTxt = false;

                    if (amountTxt.getText().toString().isEmpty()) {
                        amountTxt.setText("0");
                    }
                } else {
                    isFocusAmountTxt = true;

                    if (amountTxt.getText().toString().charAt(0) == '0') {
                        amountTxt.setText("");
                    }
                }
            }
        });
        dateTxt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });
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
        getMenuInflater().inflate(R.menu.deposit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
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
    public void showDatePickerDialog() {
        MyDatePickerDialog myDatePickerDialog = new MyDatePickerDialog();
        myDatePickerDialog.setEditText(dateTxt);
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        myDatePickerDialog.show(fragmentTransaction, "DatePicker");
    }

    public void submitButtonOnClick(View view) {
        if(!ValidateRequiredFields())
            return;

        String dateTimeStr = dateTxt.getText().toString().trim() + " 00:00:00"; // datetime
        int amount = Integer.parseInt(amountTxt.getText().toString().trim());   // amount

        if(amount > 0)
            addDeposit(amount, dateTimeStr);
    }

    public void clearButtonOnClick(View view) {
        dateTxt.setText(MyUtility.getCurrentDate());
        if(isFocusAmountTxt)
            amountTxt.setText("");
        else if(!amountTxt.getText().toString().isEmpty())
            amountTxt.setText("0");
    }

    private void addDeposit(int amount, String dateTime) {
        double newAmount = Double.parseDouble(String.valueOf(amount));  // amount as double data type

        long newId = m_dbHandler.addDeposit(newAmount, dateTime);
        String insertMessage;

        if(newId < 0) // add new deposit failed
            insertMessage = "Failed to deposit " + amount + " pesos.";
        else {  // add new deposit success
            insertMessage = "Successfully deposited " + amount + " pesos.";
            double newBalance = Double.parseDouble(balanceView.getText().toString()) + newAmount;

            MyUtility.setTextViewValue(balanceView, newBalance); // add deposited amount to current balance
        }

        MyUtility.showToast(this, insertMessage, MyUtility.ToastLength.LONG);
    }

    private void setCurrentBalance() {
        double currentBalance = m_dbHandler.getLatestBalance();

        if(currentBalance < 0) {
            m_currentBalance = 0.0;
            MyUtility.showToast(this, "Failed to get current balance.", MyUtility.ToastLength.LONG);
        }
        else {
            m_currentBalance = currentBalance;
        }
    }

    private boolean ValidateRequiredFields() {
        boolean bRet = true;

        if(dateTxt.getText().toString().isEmpty()) {
            dateTxt.setError(true);
            bRet = false;
        }
        if(amountTxt.getText().toString().isEmpty()) {
            amountTxt.setError(true);
            bRet = false;
        }

        return bRet;
    }
}

package com.example.stevenpila.loadcentraldiary;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class SellLoadActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private TextView m_balanceView;
    private MyEditText m_dateTxt;
    private MyEditText m_numberTxt;
    private MyAutoCompleteTextView m_productTxt;
    private MyEditText m_balanceTxt;
    private MyEditText m_descriptionTxt;
    private RadioButton m_paidRadioBtn;

    private boolean m_isPaid = true;
    private boolean m_isFocusBalanceTxt = false;
    private double m_currentBalance;

    private DatabaseHandler m_dbHandler;

    private PhonebookListViewAdapter m_phonebookListViewAdapterForSearch = null;
    private ArrayList<MyUtility.Pair<String, MyUtility.Pair<Double, MyUtility.Pair<Integer, Integer>>>> m_productCodesWithAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sell_load);
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
        View navHeaderView = LayoutInflater.from(this).inflate(R.layout.nav_header_sell_load, null);
        navigationView.addHeaderView(navHeaderView);

        m_dbHandler = new DatabaseHandler(this);    // initialize database...
        initializeFields(navHeaderView);  // initialize edittexts & textviews...
        setCurrentBalance();    // initialize current balance...

        MyUtility.setTextViewValue(m_balanceView, m_currentBalance); // initializes the current balance upon application start
        m_balanceTxt.setText(MyUtility.setDecimalPlaces(2, m_currentBalance)); // initializes the current balance upon application start

        m_dateTxt.setText(MyUtility.getCurrentDate());    // set default value to current date (format: yyyy-mm-dd)
        m_productTxt.setOnFocusChangeListener(new View.OnFocusChangeListener() {   // if value is empty, set to default value of 0.0
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {    // if lost focus
                    String productStr = m_productTxt.getText().toString().trim();

                    calculateNewBalance(productStr);
                }
            }
        });
        m_balanceTxt.setOnFocusChangeListener(new View.OnFocusChangeListener() {   // if value is empty, set to default value of 0.0
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {    // if lost focus
                    m_isFocusBalanceTxt = false;

                    if (m_balanceTxt.getText().toString().isEmpty()) {
                        m_balanceTxt.setText("0.00");
                    }
                } else {    // if focus
                    m_isFocusBalanceTxt = true;

                    if (m_balanceTxt.getText().toString().equals("0.00")) {
                        m_balanceTxt.setText("");
                    }
                }
            }
        });
        m_productTxt.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ProductLoadInfo productLoadInfo = (ProductLoadInfo) parent.getAdapter().getItem(position);

                m_productTxt.setText(productLoadInfo.m_product.replace("<amount>", ""));
                calculateNewBalance(productLoadInfo.m_product.replace("<amount>", ""));
            }
        });
        ArrayList<ProductLoadInfo> full_product_code_list = m_dbHandler.getProductCodeList();
        ArrayAdapter<ProductLoadInfo> autocompleteAdapter = new AutocompleteProductCodeAdapter(this, R.layout.list_view_item_sell_load, full_product_code_list);
        m_productTxt.setAdapter(autocompleteAdapter);
        m_productTxt.setBackground(getResources().getDrawable(R.drawable.my_edit_text_normal));
        m_productCodesWithAmount = m_dbHandler.getProductCodeWithAmount();
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
        getMenuInflater().inflate(R.menu.sell_load, menu);
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
    public void initializeFields(View view) {
        m_dateTxt = (MyEditText) findViewById(R.id.dateTxt);
        m_numberTxt = (MyEditText) findViewById(R.id.numberTxt);
        m_productTxt = (MyAutoCompleteTextView) findViewById(R.id.productTxt);
        m_balanceTxt = (MyEditText) findViewById(R.id.balanceTxt);
        m_descriptionTxt = (MyEditText) findViewById(R.id.descriptionTxt);
        m_paidRadioBtn = (RadioButton) findViewById(R.id.paidRadioBtn);
        m_balanceView = (TextView) view.findViewById(R.id.balance);
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

    public void submitButtonOnClick(View view) {
        if(!ValidateRequiredFields())
            return;

        String dateTimeStr = m_dateTxt.getText().toString().trim() + " 00:00:00"; // datetime
        String numberStr = m_numberTxt.getText().toString().trim();   // number
        String productStr = m_productTxt.getText().toString().trim();   // product
        MyUtility.Pair<String, Integer> ProductAndAmount = MyUtility.getProductAndAmountFromString(productStr);    // product code and amount from product string
        double userBalance = Double.parseDouble(m_balanceTxt.getText().toString().trim());  // balance
        String descStr = (m_descriptionTxt.getText().toString().isEmpty()) ? "" : m_descriptionTxt.getText().toString().trim();

        if(ProductAndAmount.m_second > 0 && userBalance > 0) {
            String tempProductStr = productStr;
            double tempDiscount = -2;
            for (MyUtility.Pair<String, MyUtility.Pair<Double, MyUtility.Pair<Integer, Integer>>> productCodeWithInfo : m_productCodesWithAmount) {
                if(productCodeWithInfo.m_first.indexOf(ProductAndAmount.m_first) > -1) {
                    tempDiscount = -1;
                    tempProductStr = productCodeWithInfo.m_first;
                    if(ProductAndAmount.m_second >= productCodeWithInfo.m_second.m_second.m_first && ProductAndAmount.m_second <= productCodeWithInfo.m_second.m_second.m_second) {
                        tempDiscount = productCodeWithInfo.m_second.m_first;
                        MyUtility.showToast(this, "Product: " + productCodeWithInfo.m_first + " Discount: " + productCodeWithInfo.m_second.m_first + " Min: " + productCodeWithInfo.m_second.m_second.m_first + " Max: " + productCodeWithInfo.m_second.m_second.m_second, MyUtility.ToastLength.LONG);
                        break;
                    }
                }
            }
            if(tempDiscount == -1) {
                MyUtility.showToast(this, "Amount (" + ProductAndAmount.m_second + ") is too low/high." , MyUtility.ToastLength.LONG);
                return;
            }
            double productDiscount;
            if((productDiscount = m_dbHandler.getDiscountIfProductCodeExist(tempProductStr)) > -1) {
                if(tempDiscount > -1)
                    productDiscount = tempDiscount;

                double discountedAmount = MyUtility.roundOff((ProductAndAmount.m_second * productDiscount) / 100, 2);
                double totalDiscountedAmount = ProductAndAmount.m_second - discountedAmount;
                double actualBalance = Double.parseDouble(MyUtility.setDecimalPlaces(2, m_currentBalance - totalDiscountedAmount));

                if(m_currentBalance >= totalDiscountedAmount) {
                    SoldLoadInfo soldLoadInfo = new SoldLoadInfo(0, productStr, numberStr, dateTimeStr, userBalance, descStr, m_isPaid, false); // TODO - userBalance or actualBalance?
                    confirmSellLoadInfoDialog(soldLoadInfo, ProductAndAmount.m_second, userBalance, actualBalance);
                }
                else {
                    m_productTxt.setError(true);
                    m_balanceTxt.setText(MyUtility.setDecimalPlaces(2, m_currentBalance));
                    MyUtility.showToast(this, "Insufficient balance.", MyUtility.ToastLength.LONG);
                }
            }
            else
                MyUtility.showToast(this, "Invalid \"" + productStr + "\" product.", MyUtility.ToastLength.LONG);
        }
    }

    public void clearButtonOnClick(View view) {
        m_dateTxt.setText(MyUtility.getCurrentDate());
        m_numberTxt.setText("");
        m_productTxt.setText("");
        m_descriptionTxt.setText("");

        if(m_isFocusBalanceTxt)
            m_balanceTxt.setText("");
        else if(!m_balanceTxt.getText().toString().isEmpty())
            m_balanceTxt.setText(MyUtility.setDecimalPlaces(2, m_currentBalance));

        m_paidRadioBtn.setChecked(true);
    }

    public void paidUnpaidButtonClick(View view) {
        boolean isChecked = ((RadioButton) view).isChecked();

        if(isChecked)
            switch (view.getId()) {
                case R.id.paidRadioBtn:
                    m_isPaid = true;
                    break;
                case R.id.unpaidRadioBtn:
                    m_isPaid = false;
                    break;
            }
    }

    private void confirmSellLoadInfoDialog(final SoldLoadInfo soldLoadInfo, final int amount, final double userBalance, final double actualBalance) {
        String dialogMessage = "Product: " + soldLoadInfo.m_product + "\n" +
                "Number: " + soldLoadInfo.m_number + "\n" +
                "Balance: " + userBalance + " (Actual: " + actualBalance + ")";

        new AlertDialog.Builder(this)
                .setTitle("Confirm")
                .setMessage(dialogMessage)
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (userBalance != actualBalance)
                            confirmUserBalanceDialog(soldLoadInfo, amount, actualBalance);
                        else
                            confirmSendToLoadCentralDialog(amount, soldLoadInfo);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    private void confirmUserBalanceDialog(final SoldLoadInfo soldLoadInfo, final int amount, final double actualBalance) {
        String dialogMessage = "Your balance (" + MyUtility.setDecimalPlaces(2, soldLoadInfo.m_balance) + ") is not equal to the actual balance (" + MyUtility.setDecimalPlaces(2, actualBalance) + ").\n\n" +
                "Are you sure you want to proceed?";

        new AlertDialog.Builder(this)
                .setMessage(dialogMessage)
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        confirmSendToLoadCentralDialog(amount, soldLoadInfo);
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
    private void confirmSendToLoadCentralDialog(final int amount, final SoldLoadInfo soldLoadInfo) {
        new AlertDialog.Builder(this)
                .setMessage("Do you want to send this to LoadCentral?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(SellLoadActivity.this);
                            alertDialog.setTitle("PIN");

                            final EditText pinNumberTxt = new EditText(SellLoadActivity.this);

                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.MATCH_PARENT
                            );
                            pinNumberTxt.setHint("Enter here");
                            pinNumberTxt.setLayoutParams(layoutParams);
                            pinNumberTxt.setInputType(InputType.TYPE_CLASS_NUMBER);

                            alertDialog.setView(pinNumberTxt)
                                    .setIcon(R.drawable.my_key)
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            final String pinNumber = pinNumberTxt.getText().toString().trim();

                                            if (!pinNumber.isEmpty())
                                                addSellLoad(amount, soldLoadInfo, pinNumber);
                                        }
                                    })
                                    .setNegativeButton("Cancel", null)
                                    .setCancelable(false)
                                    .show();
                        }
                    }
                )
                .setNegativeButton("No", null)
                .show();
    }

    private void addSellLoad(int amount, SoldLoadInfo soldLoadInfo, String pinNumber) {
        double newAmount = Double.parseDouble(String.valueOf(amount));

        long newId = m_dbHandler.addSellLoad(soldLoadInfo.m_dateTime, soldLoadInfo.m_number, soldLoadInfo.m_product, newAmount, soldLoadInfo.m_balance, soldLoadInfo.m_description, soldLoadInfo.m_isPaid);
        String insertMessage;

        if(newId < 0) // add new sell load failed
            insertMessage = "Failed to sold " + soldLoadInfo.m_product + " to " + soldLoadInfo.m_number + ".";
        else {
            insertMessage = "Successfully sold " + soldLoadInfo.m_product + " to " + soldLoadInfo.m_number + ".";

            MyUtility.setTextViewValue(m_balanceView, soldLoadInfo.m_balance); // add deposited amount to current balance
            m_currentBalance = soldLoadInfo.m_balance;

            String loadMessage = MyUtility.createSellLoadFormat(soldLoadInfo.m_product, pinNumber, soldLoadInfo.m_number);

            chooseFromAccessNumbers(loadMessage);
        }

        MyUtility.showToast(this, insertMessage, MyUtility.ToastLength.LONG);
    }

    private boolean ValidateRequiredFields() {
        boolean bRet = true;

        if(m_dateTxt.getText().toString().isEmpty()) {
            m_dateTxt.setError(true);
            bRet = false;
        }
        if(m_numberTxt.getText().toString().isEmpty()) {
            m_numberTxt.setError(true);
            bRet = false;
        }
        if(m_productTxt.getText().toString().isEmpty()) {
            m_productTxt.setError(true);
            bRet = false;
        }
        if(m_balanceTxt.getText().toString().isEmpty()) {
            m_balanceTxt.setError(true);
            bRet = false;
        }

        return bRet;
    }

    public void chooseFromPhonebook(View view) {
        final ArrayList<PhonebookInfo> arrayOfPhonebookInfo = m_dbHandler.getPhonebookList();

        // adding items to list view...
        if(arrayOfPhonebookInfo.isEmpty()) {
            MyUtility.showToast(this, "Phonebook is empty.", MyUtility.ToastLength.LONG);
        }
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View customTitleBarView = getLayoutInflater().inflate(R.layout.dialog_title_bar_sell_load, null);
            EditText searchEdiText = (EditText) customTitleBarView.findViewById(R.id.dialogTitleBarSellLoadSearch);
            builder.setCustomTitle(customTitleBarView);

            ListView modeList = new ListView(this);
            m_phonebookListViewAdapterForSearch = new PhonebookListViewAdapter(this, arrayOfPhonebookInfo);
            modeList.setAdapter(m_phonebookListViewAdapterForSearch);
            modeList.setPadding(10, 10, 10, 10);
            modeList.setTextFilterEnabled(true);

            builder.setView(modeList);
            final Dialog dialog = builder.create();

            dialog.show();

            searchEdiText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    m_phonebookListViewAdapterForSearch.getFilter().filter(s);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            modeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    PhonebookInfo phonebookInfo = (PhonebookInfo) parent.getItemAtPosition(position);

                    m_numberTxt.setText(phonebookInfo.m_number);
                    dialog.dismiss();
                }
            });
        }
    }
    private void chooseFromAccessNumbers(final String loadMessage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final String[] accessNumber = getResources().getStringArray(R.array.access_numbers);
        final ArrayAdapter accessNumbersAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_2, android.R.id.text1, accessNumber) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                TextView text2 = (TextView) view.findViewById(android.R.id.text2);

                String numberStr = (String) getItem(position);
                String numberAndType[] = numberStr.split(",");

                text2.setText(numberAndType[0]);    // Type (E.g., GLOBE...)
                text1.setText(numberAndType[1]);    // Number

                return view;
            }
        };
        builder.setTitle("Choose an Access Number");
        builder.setAdapter(accessNumbersAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String numberStr = (String) accessNumbersAdapter.getItem(which);

                // TODO - send SMS here...
                MyUtility.sendSMS(SellLoadActivity.this, numberStr.split(",")[1], loadMessage);
            }
        });
        builder.show();
    }

    private void calculateNewBalance(String productStr) {
        if (!productStr.isEmpty()) {
            MyUtility.Pair<String, Integer> ProductAndAmount = MyUtility.getProductAndAmountFromString(productStr);    // get product code and amount from product string (E.g., SNXTU200 -> m_first = SNXTU, m_second = 200)
            if(ProductAndAmount.m_second > 0) {
                double tempDiscount = -2;
                for (MyUtility.Pair<String, MyUtility.Pair<Double, MyUtility.Pair<Integer, Integer>>> productCodeWithInfo : m_productCodesWithAmount) {
                    if(productCodeWithInfo.m_first.indexOf(ProductAndAmount.m_first) > -1) {
                        productStr = productCodeWithInfo.m_first;
                        tempDiscount = -1;
                        if(ProductAndAmount.m_second >= productCodeWithInfo.m_second.m_second.m_first && ProductAndAmount.m_second <= productCodeWithInfo.m_second.m_second.m_second) {
                            tempDiscount = productCodeWithInfo.m_second.m_first;
                            MyUtility.showToast(this, "Product: " + productCodeWithInfo.m_first + " Discount: " + productCodeWithInfo.m_second.m_first + " Min: " + productCodeWithInfo.m_second.m_second.m_first + " Max: " + productCodeWithInfo.m_second.m_second.m_second, MyUtility.ToastLength.LONG);
                            break;
                        }
                    }
                }
                if(tempDiscount == -1) {
                    MyUtility.showToast(this, "Amount (" + ProductAndAmount.m_second + ") is too low/high." , MyUtility.ToastLength.LONG);
                    return;
                }
                double productDiscount;
                if((productDiscount = m_dbHandler.getDiscountIfProductCodeExist(productStr)) > -1) {
                    if(tempDiscount > -1)
                        productDiscount = tempDiscount;

                    MyUtility.showToast(this, "Product: " + productStr + " Discount: " + productDiscount, MyUtility.ToastLength.LONG);

                    double discountedAmount = MyUtility.roundOff((ProductAndAmount.m_second * productDiscount) / 100, 2);
                    double totalDiscountedAmount = MyUtility.roundOff(ProductAndAmount.m_second - discountedAmount, 2);
                    double newBalance = MyUtility.roundOff(m_currentBalance - totalDiscountedAmount, 2);

                    if(m_currentBalance >= totalDiscountedAmount)
                        m_balanceTxt.setText(MyUtility.setDecimalPlaces(2, newBalance));
                    else {
                        m_productTxt.setError(true);
                        m_balanceTxt.setText(MyUtility.setDecimalPlaces(2, m_currentBalance));
                        MyUtility.showToast(this, "Insufficient balance.", MyUtility.ToastLength.LONG);
                    }
                }
                else
                    MyUtility.showToast(this, "Product \"" + productStr + "\" does not exists.", MyUtility.ToastLength.LONG);
            }
            else {
                m_productTxt.setError(true);
                MyUtility.showToast(this, "Please specify an amount.", MyUtility.ToastLength.LONG);
            }
        }
    }
}

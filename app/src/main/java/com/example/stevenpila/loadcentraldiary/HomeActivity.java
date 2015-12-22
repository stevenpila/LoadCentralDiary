package com.example.stevenpila.loadcentraldiary;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ListView m_listView;
//    private NavigationView m_navigationView;

    private double m_currentBalance;

    private DatabaseHandler m_dbHandler;

    private static HomeActivity m_homeActivityInstance = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
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

        m_dbHandler = new DatabaseHandler(this);    // initialize database first

        m_listView = (ListView) findViewById(R.id.listView);

        View navHeaderView = LayoutInflater.from(this).inflate(R.layout.nav_header_home, null);
        navigationView.addHeaderView(navHeaderView);

        TextView balanceView = (TextView) navHeaderView.findViewById(R.id.balance);

        setCurrentBalance();    // initialize current balance
        MyUtility.setTextViewValue(balanceView, m_currentBalance); // initializes the current balance upon application start

        setListView();  // initialize list view
        m_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HistoryRecordInfo historyRecordInfo = (HistoryRecordInfo) parent.getItemAtPosition(position);

                if(historyRecordInfo.mTableName.equals(DatabaseHandler.TABLE_SELL_LOAD) && !historyRecordInfo.mSoldLoadInfo.m_description.isEmpty()) {
                    alertDialog(historyRecordInfo.mSoldLoadInfo.m_description);
                }
            }
        });

        // for testing...
//        TextView testView = (TextView) findViewById(R.id.textView);
//        PDFFileParser pdfFileParser = new PDFFileParser();
//        if(!pdfFileParser.loadProductLoadList(this))
//            testView.setText(pdfFileParser.getErrorMessage());
//        else
//            testView.setText(pdfFileParser.m_response);

//        IntentFilter intentFilter = new IntentFilter(MySMSListener.SMS_LISTENER_ACTION);
//        registerReceiver(new MySMSListener(), intentFilter);
        m_homeActivityInstance = this;
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

    private void setListView() {
        ArrayList<HistoryRecordInfo> arrayOfHistoryRecordInfos = m_dbHandler.getHistoryRecordList();
        ListViewAdapter listViewAdapter = new ListViewAdapter(this, arrayOfHistoryRecordInfos);
        m_listView.setAdapter(listViewAdapter);
        registerForContextMenu(m_listView); // triggered by long-pressed click
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        if(view.getId() == R.id.listView) {
            MenuInflater menuInflater = getMenuInflater();
            menuInflater.inflate(R.menu.list_view_item_menu_home, menu);
            AdapterView.AdapterContextMenuInfo adapterContextMenuInfo = (AdapterView.AdapterContextMenuInfo) menuInfo;
            int position = adapterContextMenuInfo.position;
            HistoryRecordInfo historyRecordInfo = (HistoryRecordInfo) m_listView.getItemAtPosition(position);

            if(historyRecordInfo.mTableName.equals(DatabaseHandler.TABLE_SELL_LOAD)) {
                boolean isPaid = ((HistoryRecordInfo) m_listView.getItemAtPosition(position)).mSoldLoadInfo.m_isPaid;

                if(isPaid) {    // hide Paid button if sold load is already paid
                    menu.findItem(R.id.paidItem).setVisible(false);
                }
            }
            else if(historyRecordInfo.mTableName.equals(DatabaseHandler.TABLE_DEPOSIT))
                menu.findItem(R.id.paidItem).setVisible(false);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo adapterContextMenuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int position = adapterContextMenuInfo.position;
        HistoryRecordInfo historyRecordInfo = (HistoryRecordInfo) m_listView.getItemAtPosition(position);

        String messageStr;
        switch (item.getItemId()) {
            case R.id.paidItem:
                if(m_dbHandler.setSellLoadPaid(historyRecordInfo.mSoldLoadInfo.m_id)) {
//                    adapterContextMenuInfo.targetView.setBackgroundColor(getResources().getColor(R.color.colorDefault));
                    ImageView paidImage = (ImageView) adapterContextMenuInfo.targetView.findViewById(R.id.homeListViewItemPaidImage);

                    ((HistoryRecordInfo) m_listView.getItemAtPosition(position)).mSoldLoadInfo.m_isPaid = true;
                    paidImage.setBackgroundColor(getResources().getColor(R.color.colorGreen));
                    messageStr = "Successfully set status to PAID.";
                }
                else
                    messageStr = "Failed to set status to PAID.";

                MyUtility.showToast(this, messageStr, MyUtility.ToastLength.LONG);

                return true;
            case R.id.editItem:
                return true;
            case R.id.deleteItem:
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void alertDialog(String description) {
        new AlertDialog.Builder(this)
                .setTitle("Description")
                .setMessage(description)
                .setPositiveButton("OK", null)
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }

    static public HomeActivity getInstance() {
        return m_homeActivityInstance;
    }

    public void setValidSellLoad(int id) {
        ListViewAdapter listViewAdapter = (ListViewAdapter) m_listView.getAdapter();

        for(int i = 0; i < listViewAdapter.getCount(); ++i) {
            HistoryRecordInfo historyRecordInfo = listViewAdapter.getItem(i);

            if(historyRecordInfo.mTableName.equals(DatabaseHandler.TABLE_SELL_LOAD)) {
                if (historyRecordInfo.mSoldLoadInfo.m_id == id) {
                    listViewAdapter.getItem(i).mSoldLoadInfo.m_isValidated = true;
                    ((ListViewAdapter) m_listView.getAdapter()).notifyDataSetChanged();

                    MyUtility.showNotification(this, "Sold Load Validated", "Product: " + historyRecordInfo.mSoldLoadInfo.m_product + "\nNumber: " + historyRecordInfo.mSoldLoadInfo.m_number + "\nBalance: " + historyRecordInfo.mSoldLoadInfo.m_balance);
                }
            }
        }
    }
}

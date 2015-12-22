package com.example.stevenpila.loadcentraldiary;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class PhonebookActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    private ListView m_listView;
    private EditText m_searchTxt;
//    private NavigationView m_navigationView;

    private double m_currentBalance;

    private DatabaseHandler m_dbHandler;
    PhonebookListViewAdapter m_phonebookListViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phonebook);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final Context localContext = this;
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(localContext, AddPhonebookActivity.class));
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // jeff
        m_dbHandler = new DatabaseHandler(this);    // initialize database first
        m_listView = (ListView) findViewById(R.id.phonebookListView);

        View navHeaderView = LayoutInflater.from(this).inflate(R.layout.nav_header_phonebook, null);
        navigationView.addHeaderView(navHeaderView);

        TextView balanceView = (TextView) navHeaderView.findViewById(R.id.balance);
        m_searchTxt = (EditText) findViewById(R.id.phonebookSearchEditText);

        setCurrentBalance();    // initialize current balance
        MyUtility.setTextViewValue(balanceView, m_currentBalance); // initializes the current balance upon application start

        setListView();  // initialize list view
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
        getMenuInflater().inflate(R.menu.phonebook, menu);
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
            startActivity(new Intent(this, PhonebookSettingsActivity.class));
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
    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        if(view.getId() == R.id.phonebookListView) {
            MenuInflater menuInflater = getMenuInflater();
            menuInflater.inflate(R.menu.list_view_item_menu_phonebook, menu);
//            AdapterView.AdapterContextMenuInfo adapterContextMenuInfo = (AdapterView.AdapterContextMenuInfo) menuInfo;
//            int position = adapterContextMenuInfo.position;
//            PhonebookInfo phonebookInfo = (PhonebookInfo) m_listView.getItemAtPosition(position);   // TODO - this might be used in the near future...
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo adapterContextMenuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int position = adapterContextMenuInfo.position;
        PhonebookInfo phonebookInfo = (PhonebookInfo) m_listView.getItemAtPosition(position);

//        String messageStr = "";
        switch (item.getItemId()) {
            case R.id.phonebookListViewItemMenuEditItem:
                Intent editPhonebookIntent = new Intent(this, AddPhonebookActivity.class);
                editPhonebookIntent.putExtra("phonebookInfoId", phonebookInfo.m_id);
                editPhonebookIntent.putExtra("phonebookInfoName", phonebookInfo.m_name);
                editPhonebookIntent.putExtra("phonebookInfoNumber", phonebookInfo.m_number);
                startActivity(editPhonebookIntent);
                return true;
            case R.id.phonebookListViewItemMenuDeleteItem:
                confirmDialog(phonebookInfo, position);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
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

    private void setListView() {
        ArrayList<PhonebookInfo> arrayOfPhonebookInfo = m_dbHandler.getPhonebookList();

        // adding items to list view...
        if(!arrayOfPhonebookInfo.isEmpty()) {
            m_phonebookListViewAdapter = new PhonebookListViewAdapter(this, arrayOfPhonebookInfo);
            m_listView.setAdapter(m_phonebookListViewAdapter);
            registerForContextMenu(m_listView); // triggered by long-pressed click

            m_searchTxt.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    m_phonebookListViewAdapter.getFilter().filter(s);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        }
        else {
            MyUtility.showToast(this, "Phonebook is empty.", MyUtility.ToastLength.LONG);
        }
    }

    private void confirmDialog(final PhonebookInfo phonebookInfo, final int position) {
        String dialogMessage = "Are you sure you want to delete \"" + phonebookInfo.m_name + " (" + phonebookInfo.m_number + ")\"?";

        new AlertDialog.Builder(this)
                .setMessage(dialogMessage)
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String messageStr;

                        if(m_dbHandler.deletePhonebook(phonebookInfo.m_id)) {
                            messageStr = "Successfully deleted \"" + phonebookInfo.m_name + "\".";
                            m_phonebookListViewAdapter.remove(m_phonebookListViewAdapter.getItem(position));
                            m_phonebookListViewAdapter.notifyDataSetChanged();
                        }
                        else
                            messageStr = "Failed to delete \"" + phonebookInfo.m_name + "\".";

                        MyUtility.showToast(PhonebookActivity.this, messageStr, MyUtility.ToastLength.LONG);
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
}

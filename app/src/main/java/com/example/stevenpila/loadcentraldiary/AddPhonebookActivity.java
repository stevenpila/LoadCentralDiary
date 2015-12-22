package com.example.stevenpila.loadcentraldiary;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class AddPhonebookActivity extends AppCompatActivity {
    private MyEditText m_nameEditText;
    private MyEditText m_numberEditText;

    private DatabaseHandler m_dbHandler;

    private int m_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_phonebook);
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
        assert getSupportActionBar() != null;
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException e) {
            MyUtility.logMessage(this, e.getMessage());
        }

        // jeff
//        m_rootView = findViewById(android.R.id.content);  // get root view
        m_dbHandler = new DatabaseHandler(this);

        m_nameEditText = (MyEditText) findViewById(R.id.addPhonebookNameEditText);
        m_numberEditText = (MyEditText) findViewById(R.id.addPhonebookNumberEditText);

        m_id = -1;

        Intent editPhonebookIntent = getIntent();
        Bundle extras = editPhonebookIntent.getExtras();
        if(extras != null)
            if(extras.containsKey("phonebookInfoId") && extras.containsKey("phonebookInfoName") && extras.containsKey("phonebookInfoNumber")) {
                m_id = extras.getInt("phonebookInfoId", -1);
                PhonebookInfo phonebookInfo = new PhonebookInfo(m_id, extras.getString("phonebookInfoName", ""), extras.getString("phonebookInfoNumber", ""));

                if(m_id > -1) {
                    setTitle("Edit Contact");
                    m_nameEditText.setText(phonebookInfo.m_name);
                    m_numberEditText.setText(phonebookInfo.m_number);
                }
            }
    }

    // jeff
    public void PhonebookSubmitButtonOnClick(View view) {
        if(!ValidateRequiredFields())
            return;

        String name = m_nameEditText.getText().toString().trim();
        String number = m_numberEditText.getText().toString().trim();

        String messageStr;
        if(m_id > -1)
            if(!m_dbHandler.updatePhoneBook(m_id, name, number))
                messageStr = "Failed to update contact.";
            else
                messageStr = "Successfully updated contact.";
        else
            if(m_dbHandler.addPhonebook(name, number) < 0)
                messageStr = "Failed to add new contact.";
            else
                messageStr = "Successfully added new contact.";


        MyUtility.showToast(this, messageStr, MyUtility.ToastLength.LONG);
    }

    public void PhonebookClearButtonOnClick(View view) {
        m_nameEditText.setText("");
        m_numberEditText.setText("");
    }

    private boolean ValidateRequiredFields() {
        boolean bRet = true;

        if(m_nameEditText.getText().toString().isEmpty()) {
            m_nameEditText.setError(true);
            bRet = false;
        }
        if(m_numberEditText.getText().toString().isEmpty()) {
            m_numberEditText.setError(true);
            bRet = false;
        }

        return bRet;
    }
}
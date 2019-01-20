package com.example.RvOnclick;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.List;

public class ActivityConfig extends AppCompatActivity {

    Spinner spPriceList;
    public static StDatabase stDatabase;
    PriceList priceList;
    Button btConfigSave;
    static String defaultPriceList;
    EditText etSgstAccount, etCgstAccount, etIgstAccount;
    String cgstAccount, sgstAccount, igstAccount;
    static String CONFIG_CGST_NAME, CONFIG_SGST_NAME, CONFIG_IGST_NAME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        stDatabase = Room.databaseBuilder(getApplicationContext(), StDatabase.class, "StDB")
                .allowMainThreadQueries().build();

        final ApplicationController ac = new ApplicationController();

        spPriceList = findViewById(R.id.sp_priceList);
        btConfigSave = findViewById(R.id.bt_config_save);
        etCgstAccount = findViewById(R.id.et_CGST_account);
        etSgstAccount = findViewById(R.id.et_SGST_account);
        etIgstAccount = findViewById(R.id.et_IGST_account);

        CONFIG_CGST_NAME = "cgstAccountName";
        CONFIG_IGST_NAME = "igstAccountName";
        CONFIG_SGST_NAME = "sgstAccountName";

        defaultPriceList = "defaultPriceList";
        final TblSettings setting = new TblSettings();
        String[] priceListNames;
        List<PriceList> priceLists = stDatabase.stDao().getSellingPriceLists();
        priceListNames = new String[priceLists.size()];
        int counter = 0;
        for (PriceList pl : priceLists){
            priceListNames[counter] = pl.getPriceListName();
            counter++;
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, priceListNames);
        spPriceList.setAdapter(adapter);

        btConfigSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String configName = defaultPriceList;
                String configValue = spPriceList.getSelectedItem().toString();
                ac.checkConfigExistence(configName,configValue,stDatabase);

                sgstAccount = etSgstAccount.getText().toString();
                cgstAccount = etCgstAccount.getText().toString();
                igstAccount = etIgstAccount.getText().toString();

                ac.checkConfigExistence(CONFIG_CGST_NAME,cgstAccount,stDatabase);
                ac.checkConfigExistence(CONFIG_SGST_NAME,sgstAccount,stDatabase);
                ac.checkConfigExistence(CONFIG_IGST_NAME,igstAccount,stDatabase);

                /*
                List<TblSettings> settings = stDatabase.stDao().getAllSettings();
                int counter = 0;
                for (TblSettings st: settings){
                    if (st.getDefn().equals(defaultPriceList)){
                        st.setSvalues(spPriceList.getSelectedItem().toString());
                    } else{
                        setting.setDefn(defaultPriceList);
                        setting.setDefn(spPriceList.getSelectedItem().toString());
                    }

                }*/



            }
        });


    }


}

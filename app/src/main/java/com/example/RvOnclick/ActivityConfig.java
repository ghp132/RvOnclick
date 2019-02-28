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
import android.widget.Switch;
import android.widget.Toast;

import java.util.List;

public class ActivityConfig extends AppCompatActivity {

    Spinner spPriceList;
    public static StDatabase stDatabase;
    PriceList priceList;
    Button btConfigSave;
    Switch swLocationFilter;
    static String defaultPriceList;
    EditText etSgstAccount, etCgstAccount, etIgstAccount;
    String cgstAccount, sgstAccount, igstAccount, useLocationFilter;
    public static String CONFIG_CGST_NAME, CONFIG_SGST_NAME, CONFIG_IGST_NAME,
        CONFIG_USE_LOCATION_FILTER;

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
        swLocationFilter = findViewById(R.id.sw_locationFilter);

        CONFIG_CGST_NAME = "cgstAccountName";
        CONFIG_IGST_NAME = "igstAccountName";
        CONFIG_SGST_NAME = "sgstAccountName";
        CONFIG_USE_LOCATION_FILTER = "useLocationFilter";

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


        //populating saved values
        if (stDatabase.stDao().getConfigByName(defaultPriceList)!=null){
            spPriceList.setId(adapter.getPosition(stDatabase.stDao().getConfigByName(defaultPriceList).getSvalues()));
        }
        TblSettings configSgst, configCgst, configIgst, configLocationFilter;
        configSgst = stDatabase.stDao().getConfigByName(CONFIG_SGST_NAME);
        configCgst = stDatabase.stDao().getConfigByName(CONFIG_CGST_NAME);
        configIgst = stDatabase.stDao().getConfigByName(CONFIG_IGST_NAME);
        configLocationFilter = stDatabase.stDao().getConfigByName(CONFIG_USE_LOCATION_FILTER);

        if (configSgst!=null){
            etSgstAccount.setText(configSgst.getSvalues());
        }
        if (configCgst!=null){
            etCgstAccount.setText(configCgst.getSvalues());
        }
        if (configIgst!=null){
            etIgstAccount.setText(configIgst.getSvalues());
        }
        if (configLocationFilter!=null){
            if (configLocationFilter.getSvalues().equals("1")){
                swLocationFilter.setChecked(true);
            }
        }


        btConfigSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String configName = defaultPriceList;
                Boolean locationSwitchValue = swLocationFilter.isChecked();
                if (locationSwitchValue){
                    useLocationFilter = "1";
                } else {
                    useLocationFilter = "0";
                }

                String configValue=null;
                if (spPriceList.getSelectedItem()!= null) {
                    configValue = spPriceList.getSelectedItem().toString();
                }

                sgstAccount = etSgstAccount.getText().toString();
                cgstAccount = etCgstAccount.getText().toString();
                igstAccount = etIgstAccount.getText().toString();

                ac.checkConfigExistence(configName,configValue,stDatabase);

                ac.checkConfigExistence(CONFIG_CGST_NAME,cgstAccount,stDatabase);
                ac.checkConfigExistence(CONFIG_SGST_NAME,sgstAccount,stDatabase);
                ac.checkConfigExistence(CONFIG_IGST_NAME,igstAccount,stDatabase);

                ac.checkConfigExistence(CONFIG_USE_LOCATION_FILTER,useLocationFilter,stDatabase);

                ac.makeShortToast(getApplicationContext(),"Saved");





            }
        });


    }


}

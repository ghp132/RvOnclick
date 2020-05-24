package com.example.RvOnclick.AllFragements;


import android.arch.persistence.room.Room;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.example.RvOnclick.Main2Activity;
import com.example.RvOnclick.R;
import com.example.RvOnclick.StDatabase;
import com.example.RvOnclick.Utils;
import com.example.RvOnclick.Warehouse;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragmentCompat {

    public static StDatabase stDatabase;
    private Preference defaultWarehouse;
    private Preference swAllowMultipleItems;
    private static String TAG = "SettingsFragment";

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        stDatabase = Room.databaseBuilder(getActivity(), StDatabase.class, "StDB")
                .allowMainThreadQueries().fallbackToDestructiveMigration().build();

        setPreferencesFromResource(R.xml.settings, "main_settings");
        final ListPreference defaultWarehousePref = (ListPreference) findPreference("default_warehouse");
        defaultWarehouse = findPreference("def_warehouse");
        swAllowMultipleItems = findPreference("allow_multiple_items");

        addWarehousesToList(defaultWarehousePref);

        defaultWarehousePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                addWarehousesToList(defaultWarehousePref);
                return false;
            }
        });

        defaultWarehouse.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent m2aIntent = new Intent(getActivity(), Main2Activity.class);
                m2aIntent.putExtra("fragment", Utils.WAREHOUSE_SELECTION_FRAGMENT);
                getActivity().startActivity(m2aIntent);
                return false;
            }
        });
/*
        swAllowMultipleItems.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                //boolean allowMultipleItems=swAllowMultipleItems
                return true;
            }
        });

        swAllowMultipleItems.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                //if (swAllowMultipleItems.che)
                return false;
            }
        });
        */
    }



    /*assuming this is not necessary
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        TextView textView = new TextView(getActivity());
        textView.setText(R.string.hello_blank_fragment);
        return textView;
    }*/

    private void addWarehousesToList(ListPreference lp) {
        List<Warehouse> warehouses = stDatabase.stDao().getAllNonGroupWarehouses();
        String[] warehouseArray = new String[warehouses.size()];
        int counter = 0;
        for (Warehouse warehouse : warehouses) {
            warehouseArray[counter] = warehouse.getName();
            counter++;

        }
        lp.setEntries(warehouseArray);
        lp.setEntryValues(warehouseArray);
        lp.setDefaultValue(warehouseArray[0]);
    }

}

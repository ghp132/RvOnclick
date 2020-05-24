package com.example.RvOnclick.AllFragements;


import android.arch.persistence.room.Room;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.RvOnclick.Company;
import com.example.RvOnclick.R;
import com.example.RvOnclick.StDatabase;
import com.example.RvOnclick.Utils;
import com.example.RvOnclick.Warehouse;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class WarehouseSelectionFragment extends DialogFragment {

    StDatabase stDatabase;
    Spinner spCompanies;
    RadioGroup rgWarehouse;
    Button btDone;
    int idOfRadioButton = 1;


    public WarehouseSelectionFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_warehouse_selection, container, false);

        stDatabase = Room.databaseBuilder(getActivity(), StDatabase.class, "StDB")
                .allowMainThreadQueries().fallbackToDestructiveMigration().build();

        spCompanies = view.findViewById(R.id.sp_companyForWarehouse);
        btDone = view.findViewById(R.id.bt_warehouseSelectionDone);

        List<Company> companyList = stDatabase.stDao().getAllCompanies();
        String[] companyArray = new String[companyList.size()];
        //populating array for spinner adapter
        int counter = 0;
        for (Company comp : companyList) {
            companyArray[counter] = comp.getCompanyName();
            counter++;
        }
        ArrayAdapter<String> companyAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, companyArray);
        spCompanies.setAdapter(companyAdapter);

        spCompanies.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                rgWarehouse.removeAllViews();
                String selectedCompanyName = spCompanies.getSelectedItem().toString();

                SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Utils.PREFS, getActivity().MODE_PRIVATE);
                String companyAbbr = stDatabase.stDao().getAbbrByCompanyName(selectedCompanyName);
                String defWarehouse = sharedPreferences.getString(selectedCompanyName + "-warehouse", "Stores - " + companyAbbr);
                int idOfDefWarehouse = 0;
                List<Warehouse> warehouses = stDatabase.stDao().getNonGroupWarehouseByCompany(selectedCompanyName);
                for (Warehouse wh : warehouses) {
                    final RadioButton radioButton = new RadioButton(getContext());
                    radioButton.setText(wh.getName());
                    radioButton.setId(idOfRadioButton);
                    if (wh.getName().equals(defWarehouse)) {
                        idOfDefWarehouse = idOfRadioButton;
                    }

                    radioButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onRadioButtonClicked(radioButton);
                        }
                    });
                    rgWarehouse.addView(radioButton);
                    idOfRadioButton++;

                }
                rgWarehouse.check(idOfDefWarehouse);
                Toast.makeText(getActivity(), String.valueOf(idOfDefWarehouse), Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        rgWarehouse = view.findViewById(R.id.rg_defaultWarehouse);

        return view;
    }

    public void onRadioButtonClicked(View v) {
        boolean isChecked = ((RadioButton) v).isChecked();
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Utils.PREFS, getActivity().MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = sharedPreferences.edit();
        prefEditor.putString(spCompanies.getSelectedItem().toString() + "-warehouse", ((RadioButton) v).getText().toString());
        prefEditor.apply();

        //String testWarehouse = sharedPreferences.getString(spCompanies.getSelectedItem().toString()+"-warehouse","Stores");
        //Toast.makeText(getActivity(), testWarehouse, Toast.LENGTH_SHORT).show();
    }

}

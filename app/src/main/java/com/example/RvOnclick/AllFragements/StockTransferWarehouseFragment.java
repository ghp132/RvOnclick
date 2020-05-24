package com.example.RvOnclick.AllFragements;


import android.arch.persistence.room.Room;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
public class StockTransferWarehouseFragment extends DialogFragment {


    public StockTransferWarehouseFragment() {
        // Required empty public constructor
    }

    StDatabase stDatabase;
    Spinner spCompany, spFromWarehouse, spToWarehouse;
    String companyName;
    Button btNext;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        stDatabase = Room.databaseBuilder(getActivity().getApplicationContext(), StDatabase.class, "StDB")
                .allowMainThreadQueries().build();
        View view = inflater.inflate(R.layout.fragment_stock_transfer_warehouse, container, false);

        spToWarehouse = view.findViewById(R.id.sp_toWarehouse);
        spFromWarehouse = view.findViewById(R.id.sp_fromWarehouse);
        spCompany = view.findViewById(R.id.sp_whSelectionCompany);
        btNext = view.findViewById(R.id.bt_warehouseSelectionNext);

        List<Company> companyList = stDatabase.stDao().getAllCompanies();
        String companyArray[] = new String[companyList.size()];
        int counter = 0;
        for (Company comp : companyList) {
            companyArray[counter] = comp.getCompanyName();
            counter++;
        }

        ArrayAdapter<String> companyAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, companyArray);
        spCompany.setAdapter(companyAdapter);

        spCompany.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //companyName = spCompany.getSelectedItem().toString();
                populateSpinners();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        populateSpinners();
/*
        List<Warehouse> warehouseList = stDatabase.stDao().getNonGroupWarehouseByCompany(companyName);
        String warehouseArray[] = new String[warehouseList.size()];
        counter = 0;
        for (Warehouse wh : warehouseList) {
            warehouseArray[counter] = wh.getName();
            counter++;
        }

        ArrayAdapter<String> warehouseAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, warehouseArray);
        spFromWarehouse.setAdapter(warehouseAdapter);
        spToWarehouse.setAdapter(warehouseAdapter);
        */

        btNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fromWarehouse = spFromWarehouse.getSelectedItem().toString();
                String toWarehouse = spToWarehouse.getSelectedItem().toString();
                if (fromWarehouse.equals(toWarehouse)) {
                    Toast.makeText(getActivity(), "From and To Warehouses are same!", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = getActivity().getIntent();
                    intent.putExtra("company", companyName);
                    intent.putExtra(Utils.KEY_FROM_WAREHOUSE, fromWarehouse);
                    intent.putExtra(Utils.KEY_TO_WAREHOUSE, toWarehouse);
                    intent.putExtra(Utils.KEY_PRODUCT_LIST_ID, -1);
                    ProductListFragment productListFragment = new ProductListFragment();
                    FragmentManager fm = getActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fm.beginTransaction();
                    fragmentTransaction.replace(R.id.fragement_container, productListFragment, Utils.PRODUCT_LIST_FRAGMENT);
                    fragmentTransaction.commit();

                }
            }
        });

        return view;
    }

    private void populateSpinners() {
        companyName = spCompany.getSelectedItem().toString();
        List<Warehouse> warehouseList = stDatabase.stDao().getNonGroupWarehouseByCompany(companyName);
        String warehouseArray[] = new String[warehouseList.size()];
        int counter = 0;
        for (Warehouse wh : warehouseList) {
            warehouseArray[counter] = wh.getName();
            counter++;
        }

        ArrayAdapter<String> warehouseAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, warehouseArray);
        spFromWarehouse.setAdapter(warehouseAdapter);
        spToWarehouse.setAdapter(warehouseAdapter);

    }

}

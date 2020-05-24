package com.example.RvOnclick;


import android.arch.persistence.room.Room;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class TabFragment1 extends Fragment implements CustomerAdapter.OnItemClickListener {


    private TextView mTextView;
    EditText etCustomerSearch;
    public static StDatabase stDatabase;
    private FusedLocationProviderClient fusedLocationProviderClient;
    final ApplicationController applicationController = new ApplicationController();
    public RecyclerView recyclerView;
    public CustomerAdapter customerAdapter;
    private String TAG  = "TabFragment1";
    private Spinner spTerritoryList;



    public static List<Customer> customerList, searchableList;
    private List<Territory> territories;


    private CustomerAdapter.OnItemClickListener listener;
    //public CustomerAdapter customerAdapter = new CustomerAdapter(listener,customerList, getActivity());


    public TabFragment1() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tab_fragment1, container, false);
        spTerritoryList = view.findViewById(R.id.sp_routeList);


        stDatabase = Room.databaseBuilder(getActivity().getApplicationContext(), StDatabase.class, "StDB")
                .allowMainThreadQueries().build();

        territories = applicationController.sortTerritoryList(stDatabase.stDao()
                .getTerritoriesByType(false));
        String[] territoryAdapter = new String[territories.size() + 1];

        //adding "All" as the first item of the array adapter
        territoryAdapter[0] = "All";

        //creating array adapter for spinner
        int tc = 1;
        for (Territory territory : territories) {
            territoryAdapter[tc] = territory.getTerritoryName();
            tc++;
        }
        ArrayAdapter<String> tAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, territoryAdapter);
        spTerritoryList.setAdapter(tAdapter);


        customerList = stDatabase.stDao().getEnabledCustomers(false);

        etCustomerSearch = view.findViewById(R.id.et_searchCustomerList);

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));


        customerList = applicationController.sortCustomerList(customerList);


        listener = this;
        customerAdapter = new CustomerAdapter(listener, customerList, getActivity());
        recyclerView.setAdapter(customerAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        etCustomerSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //autogenerated - serves no purpose
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //fires up when there's any change in the search text box
                filterCustomers();
                /*List<Customer> filtered = new ArrayList<>();
                searchableList = stDatabase.stDao().getCustomer();
                for (Customer customer : searchableList) {
                    String searchString;
                    if (customer.getDisplay_name() == null || customer.getDisplay_name().equals("null")) {
                        searchString = customer.getCustomer_id().toLowerCase();
                    } else {
                        searchString = customer.getCustomer_id().toLowerCase();
                    }
                    String searchedString = etCustomerSearch.getText().toString().toLowerCase();
                    if (searchString.contains(searchedString)) {
                        filtered.add(customer);
                    }
                }
                customerList = applicationController.sortCustomerList(filtered);
                customerAdapter = new CustomerAdapter(listener, customerList, getActivity());
                recyclerView.setAdapter(customerAdapter);
                customerAdapter.notifyDataSetChanged();*/

            }


            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        spTerritoryList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterCustomers();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        return view;
    }



    @Override
    public void onItemClicked(View v, int position) {

        String custCode = customerList.get(position).getCustomer_id();
        //Toast.makeText(getActivity(), custCode, Toast.LENGTH_SHORT).show();
        /*Bundle args = new Bundle();
        args.putString("custCode", custCode);

        DialogFragment_ProductRateInfo df = new DialogFragment_ProductRateInfo();
        df.setArguments(args);
        df.show(getFragmentManager(),"Dialog");*/

        Intent intent = new Intent(getActivity(), CustomerTransactionActivity.class);
        intent.putExtra("custCode", custCode);
        getActivity().startActivity(intent);
    }

    @Override
    public void onResume(){
        etCustomerSearch.requestFocus();
        etCustomerSearch.selectAll();
        applicationController.showKeyboard(etCustomerSearch,getActivity());
        etCustomerSearch.selectAll();
        super.onResume();
    }

    private void filterCustomers() {
        List<Customer> filtered = new ArrayList<>();
        searchableList = stDatabase.stDao().getCustomer();

        for (Customer customer : searchableList) {
            String searchString;
            if (customer.getDisplay_name() == null || customer.getDisplay_name().equals("null")) {
                searchString = customer.getCustomer_id().toLowerCase();
            } else {
                searchString = customer.getCustomer_id().toLowerCase();
            }
            String searchedString = etCustomerSearch.getText().toString().toLowerCase();
            if (searchString.contains(searchedString)) {
                if (spTerritoryList.getSelectedItemId() != 0) {
                    String territoryName = spTerritoryList.getSelectedItem().toString();
                    if (customer.getTerritory().equals(territoryName)) {

                        filtered.add(customer);
                    }
                } else {
                    filtered.add(customer);
                }
            }
        }
        customerList.clear();
        customerList.addAll(applicationController.sortCustomerList(filtered));
        customerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        applicationController.hideSoftKeyboard(getActivity());
        super.onPause();
    }
}

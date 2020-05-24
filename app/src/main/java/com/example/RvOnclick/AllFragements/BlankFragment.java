package com.example.RvOnclick.AllFragements;


import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.RvOnclick.NetworkOperations.StockEntryCreator;
import com.example.RvOnclick.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class BlankFragment extends DialogFragment implements StockEntryCreator.StockEntryPostedListener {


    public BlankFragment() {
        // Required empty public constructor
    }

    TextView textView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_blank, container, false);
        textView = view.findViewById(R.id.textView12);
        textView.setText("Please wait...");


        return view;
    }

    @Override
    public void onStockEntryPosted(int synced, int requestCode, int resultCode, String info) {
        getDialog().dismiss();
    }
}

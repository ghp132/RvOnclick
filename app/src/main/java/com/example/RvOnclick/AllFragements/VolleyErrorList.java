package com.example.RvOnclick.AllFragements;


import android.arch.persistence.room.Room;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.RvOnclick.ApplicationController;
import com.example.RvOnclick.R;
import com.example.RvOnclick.Rv1Adapter;
import com.example.RvOnclick.Rv1Item;
import com.example.RvOnclick.StDatabase;
import com.example.RvOnclick.VolleyErrorRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link VolleyErrorList#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VolleyErrorList extends Fragment implements Rv1Adapter.OnItemClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    List<VolleyErrorRecord> errorRecords = new ArrayList<>();
    RecyclerView recyclerView;
    Rv1Adapter adapter;
    Rv1Adapter.OnItemClickListener listener;
    StDatabase stDatabase;
    List<Rv1Item> rv1ItemList = new ArrayList<>();
    ApplicationController ac = new ApplicationController();
    Button btClearErrorList;
    private String TAG = "VolleyErrorList";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    public VolleyErrorList() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment VolleyErrorList.
     */
    // TODO: Rename and change types and number of parameters
    public static VolleyErrorList newInstance(String param1, String param2) {
        VolleyErrorList fragment = new VolleyErrorList();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_volley_error_list, container, false);
        btClearErrorList = view.findViewById(R.id.bt_clearVolleyErrorList);

        stDatabase = Room.databaseBuilder(getActivity().getApplicationContext(), StDatabase.class, "StDB")
                .allowMainThreadQueries().build();
        errorRecords = stDatabase.stDao().getAllVolleyErrorRecords();
        List<Rv1Item> itemList = createListForRV(errorRecords);
        rv1ItemList.clear();
        rv1ItemList.addAll(itemList);

        recyclerView = view.findViewById(R.id.rv_volleyErrorList);
        listener = this;
        adapter = new Rv1Adapter(listener, rv1ItemList, getActivity());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());


        btClearErrorList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stDatabase.stDao().deleteAllErrorRecords();
                rv1ItemList.clear();
                adapter.notifyDataSetChanged();
            }
        });

        return view;
    }

    private List<Rv1Item> createListForRV(List<VolleyErrorRecord> errorRecords1) {
        List<Rv1Item> itemList = new ArrayList<>();
        for (VolleyErrorRecord e : errorRecords1) {
            Rv1Item item = new Rv1Item();
            item.setHeading(e.getOrgin());
            item.setInfo2(e.getTimeStamp());
            item.setIntId(e.getErrorId().intValue());
            itemList.add(item);
        }

        return itemList;
    }

    @Override
    public void onItemClicked(View view, int position) {
        VolleyErrorRecord errorRecord = stDatabase.stDao()
                .getVolleyErrorRecordById(Long.valueOf(rv1ItemList.get(position).getIntId()));
        ac.displayNetworkError(errorRecord.getErrorBody(), getActivity());
    }
}

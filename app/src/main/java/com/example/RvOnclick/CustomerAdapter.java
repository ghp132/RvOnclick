package com.example.RvOnclick;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.CustViewHolder> {

    public interface OnItemClickListener {
        void onItemClicked(View view, int position);
    }

    private OnItemClickListener listener;
    private Context context;
    private List<Customer> customerList;
    @NonNull
    @Override
    public CustomerAdapter.CustViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.rv_item_customer,viewGroup,false);
        return new CustViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomerAdapter.CustViewHolder custViewHolder, int i) {
        Customer customer = customerList.get(i);
        custViewHolder.tvCustomerName.setText(customer.getCustomer_name());

    }

    @Override
    public int getItemCount() {
        return customerList.size();
    }

    public class CustViewHolder extends RecyclerView.ViewHolder {
        public TextView tvCustomerName;

        public CustViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            Context context = itemView.getContext();
            itemView.setClickable(true);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClicked(v, getAdapterPosition());

                }
            });

        }

    }

    public CustomerAdapter(OnItemClickListener listener,List<Customer> customerList,Context context){
        this.customerList = customerList;
        this.listener = listener;
        this.context = context;

    }
}

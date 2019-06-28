package com.example.RvOnclick;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class InvoiceAdapter extends RecyclerView.Adapter<InvoiceAdapter.InvViewHolder> {

    public interface OnItemClickListener {
        void onItemClicked(View view, int position);
    }

    private OnItemClickListener listener;
    private Context context;
    private List<Invoice> invoiceList;

    @NonNull
    @Override
    public InvoiceAdapter.InvViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.rv_item_invoice,viewGroup,false);
        return new InvViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull InvoiceAdapter.InvViewHolder invViewHolder, int i) {
        Invoice invoice = invoiceList.get(i);
        if (invoice.getDocStatus() == -1) {
            invViewHolder.tvInvoiceNumber.setText("Unsynced " + invoice.getOrderId());
        } else {
            invViewHolder.tvInvoiceNumber.setText(invoice.getInvoiceNumber());
        }
        invViewHolder.tvPaid.setText(Double.toString(invoice.getPaidAmount()));
        invViewHolder.tvGrandTotal.setText(Double.toString(invoice.getGrandTotal()));
        invViewHolder.tvOutstanding.setText(Double.toString(invoice.getOutstanding()));



    }

    @Override
    public int getItemCount() {
        return invoiceList.size();
    }

    public class InvViewHolder extends RecyclerView.ViewHolder {

        public TextView tvInvoiceNumber, tvGrandTotal, tvOutstanding, tvPaid, tvInvoiceDate;


        public InvViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInvoiceNumber = itemView.findViewById(R.id.tvInvoiceNumber);
            tvGrandTotal = itemView.findViewById(R.id.tvGrandTotal);
            tvOutstanding = itemView.findViewById(R.id.tvOutstanding);
            tvPaid = itemView.findViewById(R.id.tvPaid);
            tvInvoiceDate = itemView.findViewById(R.id.tv_invoiceDate);
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

    public InvoiceAdapter (OnItemClickListener listener, List<Invoice>invoiceList, Context context){
        this.invoiceList = invoiceList;
        this.listener = listener;
        this.context = context;
    }

}


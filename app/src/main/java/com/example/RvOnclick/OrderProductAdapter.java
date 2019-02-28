package com.example.RvOnclick;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.math.RoundingMode;
import java.util.List;

public class OrderProductAdapter extends RecyclerView.Adapter<OrderProductAdapter.ProdViewHolder> {

    public interface OnItemClickListener {
        void onItemClicked(View view, int position);
        void onLongClick(View view, int position);
    }

    private OnItemClickListener listener;
    private Context context;
    private List<OrderProduct> productList;


    @NonNull
    @Override
    public OrderProductAdapter.ProdViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.rv_item_product,viewGroup,false);
        return new ProdViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderProductAdapter.ProdViewHolder prodViewHolder, int i) {
        OrderProduct product = productList.get(i);
        String prodCode = product.getProductCode();
        double qty = product.getQty();
        String qtyStr = String.format("%.2f", qty);
        double rate = product.getRate();
        String rateStr = String.format("%.2f",rate);
        Long prodTotal = Math.round(rate*qty);
        String prodTotalStr = String.valueOf(prodTotal);

        prodViewHolder.tvProductName.setText(product.getProductCode());
        prodViewHolder.tvCount.setText("Qty: " + qtyStr);
        prodViewHolder.tvOrder.setText("Rate: "+ rateStr);
        prodViewHolder.tvAdded.setText("Total: " + prodTotalStr);


    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public class ProdViewHolder extends RecyclerView.ViewHolder {

        public TextView tvProductName, tvCount, tvOrder, tvAdded;


        public ProdViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvCount = itemView.findViewById(R.id.tvCount);
            tvOrder = itemView.findViewById(R.id.tvOrder);
            tvAdded = itemView.findViewById(R.id.tvAdded);
            Context context = itemView.getContext();
            itemView.setClickable(true);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClicked(v, getAdapterPosition());
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    listener.onLongClick(v,getAdapterPosition());
                    return true;
                }
            });
        }
    }

    public OrderProductAdapter (OnItemClickListener listener, List<OrderProduct>productList, Context context){
        this.productList = productList;
        this.listener = listener;
        this.context = context;
    }

}


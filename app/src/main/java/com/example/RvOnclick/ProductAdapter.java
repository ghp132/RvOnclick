package com.example.RvOnclick;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProdViewHolder> {

    public interface OnItemClickListener {
        void onItemClicked(View view, int position);
    }

    private OnItemClickListener listener;
    private Context context;
    private List<Product> productList;

    @NonNull
    @Override
    public ProductAdapter.ProdViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.rv_item_product,viewGroup,false);
        return new ProdViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductAdapter.ProdViewHolder prodViewHolder, int i) {
        Product product = productList.get(i);
        prodViewHolder.tvProductName.setText(product.getProductName());
        prodViewHolder.tvAdded.setText(String.valueOf(product.getProductRate()));
        prodViewHolder.tvCount.setText(String.valueOf(product.getStock()));
        double currentOrderQty = product.getCurrentOrderQty();
        double currentOrderFreeQty = product.getCurrentOrderFreeQty();
        String orderQty="";
        if (currentOrderQty!=0){
            orderQty=String.valueOf(currentOrderQty);
            if (currentOrderFreeQty!=0){
                orderQty = orderQty + "(" + currentOrderFreeQty + ")";
            }
        }
        prodViewHolder.tvOrder.setText(orderQty);
        //prodViewHolder.itemView.setOutlineSpotShadowColor(0xff00ff00);

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
        }
    }

    public ProductAdapter (OnItemClickListener listener, List<Product>productList, Context context){
        this.productList = productList;
        this.listener = listener;
        this.context = context;
    }

}

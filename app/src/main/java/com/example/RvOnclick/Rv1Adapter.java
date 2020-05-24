package com.example.RvOnclick;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class Rv1Adapter extends RecyclerView.Adapter<Rv1Adapter.Rv1ViewHolder> {
    public interface OnItemClickListener{
        void onItemClicked(View view, int position);
    }

    public interface OnItemLongClickListener {
        void onItemLongClicked(View view, int position);
    }
    private OnItemClickListener listener;
    private OnItemLongClickListener longClickListener;
    private Context context;
    private List<Rv1Item> itemList;

    @NonNull
    @Override
    public Rv1ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.rv1_item,viewGroup,false);
        return new Rv1ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull Rv1ViewHolder rv1ViewHolder, int i) {
        Rv1Item item = itemList.get(i);
        rv1ViewHolder.tvHeading.setText(item.getHeading());
        rv1ViewHolder.tvInfo1.setText(item.getInfo1());
        rv1ViewHolder.tvInfo2.setText(item.getInfo2());
        rv1ViewHolder.tvInfo3.setText(item.getInfo3());
        rv1ViewHolder.tvInfo4.setText(item.getInfo4());

    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public class Rv1ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvHeading, tvInfo1, tvInfo2, tvInfo3, tvInfo4;
        public Rv1ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHeading = itemView.findViewById(R.id.tv_rv1Heading);
            tvInfo1 = itemView.findViewById(R.id.tv_rv1Info1);
            tvInfo2 = itemView.findViewById(R.id.tv_rv1Info2);
            tvInfo3 = itemView.findViewById(R.id.tv_rv1Info3);
            tvInfo4 = itemView.findViewById(R.id.tv_rv1Info4);
            Context context = itemView.getContext();
            itemView.setClickable(true);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClicked(v, getAdapterPosition());
                }
            });
            itemView.setLongClickable(true);
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    longClickListener.onItemLongClicked(v, getAdapterPosition());
                    return false;
                }
            });

        }
    }

    public Rv1Adapter(OnItemClickListener listener, OnItemLongClickListener longClickListener, List<Rv1Item> itemList, Context context) {
        this.context = context;
        this.itemList = itemList;
        this.listener = listener;
        this.longClickListener = longClickListener;
    }

    public void updateRv1Adapter(List<Rv1Item> newList){
        this.itemList.clear();
        this.itemList.addAll(newList);
        this.notifyDataSetChanged();

    }
}

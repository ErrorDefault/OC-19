package com.errorDefault.oc_19.data;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.errorDefault.oc_19.R;
import com.errorDefault.oc_19.model.City;

import java.util.ArrayList;

public class OC19ConfigAdapter extends RecyclerView.Adapter<OC19ConfigAdapter.ViewHolder> {

    private final ArrayList<City> localDataSet;
    private OnItemClickListener mListener;

    public interface OnItemClickListener{
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        mListener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private final TextView textView;

        public ViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            textView = itemView.findViewById(R.id.item_citySelection);
            itemView.setOnClickListener(v -> {
                if(listener != null){
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION){
                        listener.onItemClick(position);
                    }
                }
            });
        }

        public TextView getTextView(){
            return textView;
        }
    }

    public OC19ConfigAdapter(ArrayList<City> dataSet){
        localDataSet = dataSet;
    }

    @NonNull
    @Override
    public OC19ConfigAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
        return new ViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull OC19ConfigAdapter.ViewHolder holder, int position) {
        holder.getTextView().setText(localDataSet.get(position).getCityName());
    }

    @Override
    public int getItemCount() {
        return localDataSet.size();
    }
}

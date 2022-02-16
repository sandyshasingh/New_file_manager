package com.editor.hiderx;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;

/**
 * Created by ashishsaini on 3/8/17.
 */

public class KeyValueAdapter extends RecyclerView.Adapter<KeyValueAdapter.ViewHolder> {
    private final ArrayList<KeyValueModel> keyValueModelArrayList;

    public KeyValueAdapter(ArrayList<KeyValueModel> keyValueModelArrayList){
        this.keyValueModelArrayList =keyValueModelArrayList;
    }



    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflatedView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.key_value_list_item, parent, false);
        return new ViewHolder(inflatedView);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        viewHolder.keyTextView.setText(keyValueModelArrayList.get(position).getKey());
        viewHolder.valueTextView.setText(keyValueModelArrayList.get(position).getValue());
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getItemCount() {
        return keyValueModelArrayList.size();
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView keyTextView;
        TextView valueTextView;

        ViewHolder(View v) {
            super(v);
            keyTextView = (TextView) v.findViewById(R.id.keytextid);
            valueTextView = (TextView) v.findViewById(R.id.valuetextid);
        }
    }

}
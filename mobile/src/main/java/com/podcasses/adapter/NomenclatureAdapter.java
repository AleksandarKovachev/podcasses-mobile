package com.podcasses.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.podcasses.model.entity.Nomenclature;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created by aleksandar.kovachev.
 */
public class NomenclatureAdapter extends ArrayAdapter {

    private List<Nomenclature> nomenclatures;

    public NomenclatureAdapter(Context context, int textViewResourceId, List<Nomenclature> nomenclatures, String prompt) {
        super(context, textViewResourceId, nomenclatures);
        this.nomenclatures = nomenclatures;
        Nomenclature nomenclature = new Nomenclature();
        nomenclature.setName(prompt);
        nomenclature.setId(-1);
        this.nomenclatures.add(0, nomenclature);
    }

    @Override
    public int getCount() {
        return nomenclatures.size();
    }

    @Nullable
    @Override
    public Object getItem(int position) {
        return nomenclatures.get(position);
    }

    @Override
    public long getItemId(int position) {
        return nomenclatures.get(position).getId();
    }

    @Override
    public boolean isEnabled(int position) {
        return position != 0;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        TextView label = (TextView) super.getView(position, convertView, parent);
        label.setText(nomenclatures.get(position).getName());
        return label;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        TextView label = (TextView) super.getDropDownView(position, convertView, parent);
        label.setText(nomenclatures.get(position).getName());
        if (position == 0) {
            label.setTextColor(Color.GRAY);
        }
        return label;
    }

}

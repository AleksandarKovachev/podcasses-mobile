package com.podcasses.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.podcasses.model.response.Language;

import java.util.List;

/**
 * Created by aleksandar.kovachev.
 */
public class LanguageAdapter extends ArrayAdapter {

    private List<Language> languages;

    public LanguageAdapter(Context context, int textViewResourceId, List<Language> languages, String prompt) {
        super(context, textViewResourceId, languages);
        this.languages = languages;
        if (languages.size() > 0 && languages.get(languages.size() - 1).getId() != -1) {
            Language language = new Language();
            language.setName(prompt);
            language.setId(-1);
            languages.add(language);
        }
    }

    @Override
    public int getCount() {
        return languages.size() > 0 ? languages.size() - 1 : languages.size();
    }

    @Nullable
    @Override
    public Object getItem(int position) {
        return languages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return languages.get(position).getId();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        TextView label = (TextView) super.getView(position, convertView, parent);
        label.setText(languages.get(position).getName() + " (" + languages.get(position).getNativeName() + ")");
        return label;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        TextView label = (TextView) super.getDropDownView(position, convertView, parent);
        label.setText(languages.get(position).getName() + " (" + languages.get(position).getNativeName() + ")");
        return label;
    }

}

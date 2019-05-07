package com.podcasses.util;

import android.graphics.PorterDuff;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;

import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.content.ContextCompat;
import androidx.databinding.BindingAdapter;
import androidx.databinding.InverseBindingAdapter;
import androidx.databinding.InverseBindingListener;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.ohoussein.playpause.PlayPauseView;
import com.onegravity.rteditor.RTEditText;
import com.onegravity.rteditor.api.format.RTFormat;
import com.podcasses.R;
import com.podcasses.adapter.LanguageAdapter;
import com.podcasses.adapter.NomenclatureAdapter;
import com.podcasses.model.entity.Nomenclature;
import com.podcasses.model.response.Language;

import java.util.List;

/**
 * Created by aleksandar.kovachev.
 */
public class CustomViewBindings {

    public static final String PROFILE_IMAGE = "/account/image/";

    public static final String COVER_IMAGE = "/account/cover/";

    public static final String PODCAST_IMAGE = "/podcast/image/";

    @BindingAdapter("setAdapter")
    public static void bindRecyclerViewAdapter(RecyclerView recyclerView, RecyclerView.Adapter<?> adapter) {
        recyclerView.setAdapter(adapter);
    }

    @BindingAdapter("imageUrl")
    public static void loadImage(ImageView view, String url) {
        Glide.with(view).load(url).apply(RequestOptions.placeholderOf(R.drawable.cover_placeholder)).into(view);
    }

    @BindingAdapter(value = {"playPauseStatus", "position"}, requireAll = false)
    public static void playPauseStatus(PlayPauseView view, Integer position, Integer playingIndex) {
        if (position == -1 || playingIndex == -1) {
            view.change(true);
        } else {
            view.change(!position.equals(playingIndex));
        }
    }

    @BindingAdapter(value = {"isSubscribed"})
    public static void isSubscribed(View view, boolean isSubscribed) {
        int selectedColor = ContextCompat.getColor(view.getContext(), R.color.colorPrimaryLight);
        int defaultColor = ContextCompat.getColor(view.getContext(), R.color.colorAccent);
        view.getBackground().setColorFilter(isSubscribed ? selectedColor : defaultColor, PorterDuff.Mode.MULTIPLY);
    }

    @BindingAdapter(value = {"isLiked"})
    public static void isLiked(View view, boolean isLiked) {
        view.setSelected(isLiked);
    }

    @BindingAdapter(value = {"isDisliked"})
    public static void isDisliked(View view, boolean isDisliked) {
        view.setSelected(isDisliked);
    }

    @BindingAdapter(value = {"languages", "selectedLanguage", "selectedLanguageAttrChanged"}, requireAll = false)
    public static void setLanguages(AppCompatSpinner spinner, List<Language> languages, int selectedLanguage, InverseBindingListener listener) {
        if (languages == null) {
            return;
        }
        spinner.setAdapter(
                new LanguageAdapter(spinner.getContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        languages,
                        spinner.getContext().getString(R.string.podcast_language)));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                listener.onChange();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @InverseBindingAdapter(attribute = "selectedLanguage", event = "selectedLanguageAttrChanged")
    public static Integer getSelectedLanguage(AppCompatSpinner spinner) {
        return ((Language) spinner.getSelectedItem()).getId();
    }

    @BindingAdapter(value = {"privacies", "selectedPrivacy", "selectedPrivacyAttrChanged"}, requireAll = false)
    public static void setPrivacies(AppCompatSpinner spinner, List<Nomenclature> privacies, Integer selectedPrivacy, InverseBindingListener listener) {
        if (privacies == null) {
            return;
        }
        spinner.setAdapter(new NomenclatureAdapter(spinner.getContext(), android.R.layout.simple_spinner_dropdown_item, privacies, spinner.getContext().getString(R.string.podcast_privacy)));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                listener.onChange();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @InverseBindingAdapter(attribute = "selectedPrivacy", event = "selectedPrivacyAttrChanged")
    public static Integer getSelectedPrivacy(AppCompatSpinner spinner) {
        return ((Nomenclature) spinner.getSelectedItem()).getId();
    }

    @BindingAdapter(value = {"categories", "selectedCategory", "selectedCategoryAttrChanged"}, requireAll = false)
    public static void setCategoriesAdapter(AppCompatSpinner spinner, List<Nomenclature> categories, Integer selectedCategory, InverseBindingListener listener) {
        if (categories == null) {
            return;
        }
        spinner.setAdapter(new NomenclatureAdapter(spinner.getContext(), android.R.layout.simple_spinner_dropdown_item, categories, spinner.getContext().getString(R.string.podcast_category)));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                listener.onChange();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @InverseBindingAdapter(attribute = "selectedCategory", event = "selectedCategoryAttrChanged")
    public static Integer getSelectedCategory(AppCompatSpinner spinner) {
        return ((Nomenclature) spinner.getSelectedItem()).getId();
    }

    @BindingAdapter(value = {"htmlText", "htmlTextAttrChanged"}, requireAll = false)
    public static void setHtmlText(RTEditText editText, String value, InverseBindingListener listener) {
        editText.setText(value);
        editText.setSelection(editText.getText().length());
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                listener.onChange();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @InverseBindingAdapter(attribute = "htmlText")
    public static String getHtmlText(RTEditText editText) {
        return editText.getText(RTFormat.HTML);
    }

}

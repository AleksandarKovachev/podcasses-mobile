package com.podcasses.viewmodel;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.onegravity.rteditor.RTEditText;
import com.onegravity.rteditor.api.format.RTFormat;
import com.podcasses.BR;
import com.podcasses.R;
import com.podcasses.adapter.LanguageAdapter;
import com.podcasses.adapter.NomenclatureAdapter;
import com.podcasses.model.entity.Nomenclature;
import com.podcasses.model.entity.Podcast;
import com.podcasses.model.repository.MainDataRepository;
import com.podcasses.model.response.Language;
import com.podcasses.viewmodel.base.BaseViewModel;

import java.util.List;

import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.content.ContextCompat;
import androidx.databinding.Bindable;
import androidx.databinding.BindingAdapter;
import androidx.databinding.InverseBindingAdapter;
import androidx.databinding.InverseBindingListener;
import androidx.databinding.Observable;
import androidx.databinding.ObservableField;
import androidx.databinding.PropertyChangeRegistry;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import retrofit2.Response;

import static android.graphics.Color.GREEN;

/**
 * Created by aleksandar.kovachev.
 */
public class UploadViewModel extends BaseViewModel implements Observable {

    private PropertyChangeRegistry callbacks = new PropertyChangeRegistry();

    private MutableLiveData<List<Nomenclature>> categories = new MutableLiveData<>();
    private MutableLiveData<List<Nomenclature>> privacies = new MutableLiveData<>();
    private MutableLiveData<List<Language>> languages = new MutableLiveData<>();
    private ObservableField<String> podcastImage = new ObservableField<>();
    private ObservableField<Integer> podcastUploadProgress = new ObservableField<>();

    private Podcast podcast = new Podcast();

    UploadViewModel(MainDataRepository repository) {
        super(repository);
    }

    @Override
    public void addOnPropertyChangedCallback(Observable.OnPropertyChangedCallback callback) {
        callbacks.add(callback);
    }

    @Override
    public void removeOnPropertyChangedCallback(Observable.OnPropertyChangedCallback callback) {
        callbacks.remove(callback);
    }

    public LiveData<List<Nomenclature>> getCategoryNomenclatures() {
        return repository.getCategories();
    }

    public LiveData<List<Nomenclature>> getPrivacyNomenclatures() {
        return repository.getPrivacies();
    }

    public LiveData<List<Language>> getLanguageNomenclatures() {
        return repository.getLanguages();
    }

    public Podcast getPodcast() {
        return podcast;
    }

    @Bindable
    public List<Nomenclature> getCategories() {
        return categories.getValue();
    }

    public void setCategories(List<Nomenclature> categories) {
        this.categories.setValue(categories);
        notifyPropertyChanged(BR.categories);
    }

    @Bindable
    public List<Language> getLanguages() {
        return languages.getValue();
    }

    public void setLanguages(List<Language> languages) {
        this.languages.setValue(languages);
        notifyPropertyChanged(BR.languages);
    }

    @Bindable
    public List<Nomenclature> getPrivacies() {
        return privacies.getValue();
    }

    public void setPrivacies(List<Nomenclature> privacies) {
        this.privacies.setValue(privacies);
        notifyPropertyChanged(BR.privacies);
    }

    @Bindable
    public String getPodcastImage() {
        return podcastImage.get();
    }

    public void setPodcastImage(String podcastImage) {
        this.podcastImage.set(podcastImage);
        notifyPropertyChanged(BR.podcastImage);
    }

    @Bindable
    public Integer getPodcastUploadProgress() {
        return podcastUploadProgress.get();
    }

    public void setPodcastUploadProgress(Double progress) {
        this.podcastUploadProgress.set(progress.intValue());
        notifyPropertyChanged(BR.podcastUploadProgress);
    }

    @BindingAdapter(value = {"progress_current"}, requireAll = false)
    public static void setCurrentProgress(NumberProgressBar progressBar, Integer progress) {
        if (progress != null) {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(progress);
            if (progress == 100) {
                progressBar.setReachedBarColor(GREEN);
                progressBar.setProgressTextColor(GREEN);
            } else {
                progressBar.setReachedBarColor(ContextCompat.getColor(progressBar.getContext(), R.color.colorAccent));
                progressBar.setProgressTextColor(ContextCompat.getColor(progressBar.getContext(), R.color.colorAccent));
            }
        }
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

    public void savePodcast(Response<Podcast> response) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                repository.savePodcast(response.body());
            }
        };
        thread.start();
    }

    private void notifyPropertyChanged(int fieldId) {
        callbacks.notifyCallbacks(this, fieldId, null);
    }

}

package com.podcasses.view.base;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;

import androidx.appcompat.app.AppCompatActivity;

import com.podcasses.dagger.BaseApplication;

import java.util.Locale;

/**
 * Created by aleksandar.kovachev.
 */
public class BaseActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(updateBaseContextLocale(base));
    }

    protected Locale getLocale() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return getResources().getConfiguration().getLocales().get(0);
        } else {
            return getResources().getConfiguration().locale;
        }
    }

    private Context updateBaseContextLocale(Context context) {
        String language = ((BaseApplication) context.getApplicationContext()).getSharedPreferencesManager().getLocale();
        if (language == null) {
            return context;
        }
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());
        config.setLocale(locale);
        config.locale = locale;
        res.updateConfiguration(config, res.getDisplayMetrics());
        return context.createConfigurationContext(config);
    }

}

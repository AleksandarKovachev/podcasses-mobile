package com.podcasses.view.base;

import androidx.lifecycle.MutableLiveData;

/**
 * Created by aleksandar.kovachev.
 */
public interface AuthenticationTokenTask {

    void startAuthenticationActivity(MutableLiveData<String> token);

}

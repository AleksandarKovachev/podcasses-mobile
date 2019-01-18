package com.podcasses.view.base;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

/**
 * Created by aleksandar.kovachev.
 */
public class BaseActivity extends AppCompatActivity {

    protected void addFragment(Fragment fragment, int frameId, boolean addToBackStack, String tag) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction().add(frameId, fragment);
        inTransaction(fragmentTransaction, addToBackStack, tag);
    }

    protected void replaceFragment(Fragment fragment, int frameId, boolean addToBackStack, String tag) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction().replace(frameId, fragment);
        inTransaction(fragmentTransaction, addToBackStack, tag);
    }

    private void inTransaction(FragmentTransaction fragmentTransaction, boolean addToBackStack, String tag) {
        if (addToBackStack) fragmentTransaction.addToBackStack(tag);
        fragmentTransaction.commit();
    }

}

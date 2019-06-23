package com.podcasses.view.base;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * Created by aleksandar.kovachev.
 */
public class BaseFragment extends Fragment {

    public static final String ARGS_INSTANCE = "com.podcasses.argsInstance";

    protected FragmentNavigation fragmentNavigation;
    protected int fragmentCount;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            fragmentCount = args.getInt(ARGS_INSTANCE);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof FragmentNavigation) {
            fragmentNavigation = (FragmentNavigation) context;
        }
    }

    public interface FragmentNavigation {
        void pushFragment(Fragment fragment);

        void popFragment();
    }

}

package com.podcasses.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.podcasses.BuildConfig;
import com.podcasses.R;
import com.podcasses.dagger.BaseApplication;
import com.podcasses.databinding.FragmentEditAccountBinding;
import com.podcasses.model.entity.Account;
import com.podcasses.model.request.AccountRequest;
import com.podcasses.retrofit.ApiCallInterface;
import com.podcasses.util.AuthenticationUtil;
import com.podcasses.util.CustomViewBindings;
import com.podcasses.util.FileUploadUtil;
import com.podcasses.util.NetworkRequestsUtil;
import com.podcasses.view.base.BaseFragment;
import com.podcasses.viewmodel.EditAccountViewModel;
import com.podcasses.viewmodel.ViewModelFactory;

import org.jetbrains.annotations.NotNull;

import java.io.File;

import javax.inject.Inject;

import static android.app.Activity.RESULT_OK;

/**
 * Created by aleksandar.kovachev.
 */
public class EditAccountFragment extends BaseFragment {

    @Inject
    ViewModelFactory viewModelFactory;

    @Inject
    ApiCallInterface apiCallInterface;

    private LiveData<String> token;

    private EditAccountViewModel viewModel;
    private static Account account;

    static EditAccountFragment newInstance(int instance, Account accountData) {
        Bundle args = new Bundle();
        account = accountData;
        args.putInt(BaseFragment.ARGS_INSTANCE, instance);
        EditAccountFragment fragment = new EditAccountFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        FragmentEditAccountBinding binder = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_account, container, false);
        ((BaseApplication) getActivity().getApplication()).getAppComponent().inject(this);
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(EditAccountViewModel.class);
        binder.setLifecycleOwner(this);
        binder.setViewModel(viewModel);
        binder.updateAccountBtn.setOnClickListener(updateAccountClickListener);
        token = AuthenticationUtil.getAuthenticationToken(getContext());
        binder.accountCoverImageUpload.setOnClickListener(onAccountCoverClick);
        binder.accountProfileImageUpload.setOnClickListener(onAccountProfileImageClick);
        return binder.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel.setAccount(AccountRequest.toAccountRequest(account));
        viewModel.setProfileImage(BuildConfig.API_GATEWAY_URL + CustomViewBindings.PROFILE_IMAGE + account.getId());
        viewModel.setCoverImage(BuildConfig.API_GATEWAY_URL + CustomViewBindings.COVER_IMAGE + account.getId());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String permissions[], @NotNull int[] grantResults) {
        if (requestCode == 1 || requestCode == 2) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImage(requestCode);
            } else {
                Toast.makeText(getContext(), getString(R.string.storage_permission_image), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                sendAccountCoverUploadRequest(data);
            } else if (requestCode == 2) {
                sendAccountProfileImageUploadRequest(data);
            }
        }
    }

    private void sendAccountCoverUploadRequest(Intent data) {
        File image = new File(FileUploadUtil.getRealPathFromURIPath(data.getData(), getContext()));
        FileUploadUtil.uploadFileToServer(getContext(), token.getValue(), image, "/account/cover", "imageFile");
    }

    private void sendAccountProfileImageUploadRequest(Intent data) {
        File image = new File(FileUploadUtil.getRealPathFromURIPath(data.getData(), getContext()));
        FileUploadUtil.uploadFileToServer(getContext(), token.getValue(), image, "/account/image", "imageFile");
    }

    private View.OnClickListener onAccountCoverClick = v -> {
        processImageSelect(1);
    };

    private View.OnClickListener onAccountProfileImageClick = v -> {
        processImageSelect(2);
    };

    private void processImageSelect(int requestCode) {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, requestCode);
        } else {
            selectImage(requestCode);
        }
    }

    private void selectImage(int requestCode) {
        Intent pickImageIntent = new Intent(
                Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickImageIntent, requestCode);
    }

    private View.OnClickListener updateAccountClickListener =
            v -> {
                LiveData<Account> response =
                        NetworkRequestsUtil.sendUpdateAccountRequest(apiCallInterface, token.getValue(), viewModel.getAccount(), getContext());
                response.observe(this, accountResponse -> {
                    fragmentNavigation.popFragment();
                    if (accountResponse != null) {
                        account = accountResponse;
                    }
                    response.removeObservers(getViewLifecycleOwner());
                });
            };

}

package com.podcasses.view;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.Observable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.common.util.Strings;
import com.google.gson.Gson;
import com.onegravity.rteditor.RTManager;
import com.onegravity.rteditor.api.RTApi;
import com.onegravity.rteditor.api.RTMediaFactoryImpl;
import com.onegravity.rteditor.api.RTProxyImpl;
import com.podcasses.R;
import com.podcasses.authentication.AccountAuthenticator;
import com.podcasses.dagger.BaseApplication;
import com.podcasses.databinding.FragmentPodcastChannelAddBinding;
import com.podcasses.retrofit.ApiCallInterface;
import com.podcasses.util.AuthenticationUtil;
import com.podcasses.util.FileUploadUtil;
import com.podcasses.view.base.BaseFragment;
import com.podcasses.viewmodel.PodcastChannelAddViewModel;
import com.podcasses.viewmodel.ViewModelFactory;

import net.gotev.uploadservice.UploadService;
import net.gotev.uploadservice.okhttp.OkHttpStack;

import org.jetbrains.annotations.NotNull;

import java.io.File;

import javax.inject.Inject;

import okhttp3.OkHttpClient;

import static android.app.Activity.RESULT_OK;

/**
 * Created by aleksandar.kovachev.
 */
public class PodcastChannelAddFragment extends BaseFragment {

    @Inject
    ViewModelFactory viewModelFactory;

    @Inject
    ApiCallInterface apiCallInterface;

    @Inject
    OkHttpClient okHttpClient;

    @Inject
    Gson gson;

    private PodcastChannelAddViewModel viewModel;

    private RTManager rtManager;

    private LiveData<String> token;

    private FragmentPodcastChannelAddBinding binder;

    static PodcastChannelAddFragment newInstance(int instance) {
        Bundle args = new Bundle();
        args.putInt(BaseFragment.ARGS_INSTANCE, instance);
        PodcastChannelAddFragment fragment = new PodcastChannelAddFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binder = DataBindingUtil.inflate(inflater, R.layout.fragment_podcast_channel_add, container, false);
        ((BaseApplication) getActivity().getApplication()).getAppComponent().inject(this);
        UploadService.HTTP_STACK = new OkHttpStack(okHttpClient);
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(PodcastChannelAddViewModel.class);
        binder.setLifecycleOwner(this);
        binder.setViewModel(viewModel);
        binder.podcastChannelImageUpload.setOnClickListener(onPodcastChannelImageUpload);
        return binder.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        token = AuthenticationUtil.getAuthenticationToken(getContext());
        if (token != null) {
            token.observe(this, s -> {
                if (!Strings.isEmptyOrWhitespace(s)) {
                    binder.setToken(s);
                    token.removeObservers(this);
                } else if (AccountManager.get(getContext()).getAccountsByType(AccountAuthenticator.ACCOUNT_TYPE).length != 0) {
                    token.removeObservers(this);
                }
            });
        }
        RTApi rtApi = new RTApi(getContext(), new RTProxyImpl((Activity) getContext()), new RTMediaFactoryImpl(getContext(), true));
        rtManager = new RTManager(rtApi, savedInstanceState);

        rtManager.registerToolbar(binder.rteToolbarContainer, binder.rteToolbarContainer.findViewById(R.id.rte_toolbar_character));
        rtManager.registerToolbar(binder.rteToolbarContainer, binder.rteToolbarContainer.findViewById(R.id.rte_toolbar_paragraph));
        rtManager.registerEditor(binder.podcastChannelDescription, true);

        viewModel.getIsSuccessfullyAdded().addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                if (viewModel.getIsSuccessfullyAdded().get()) {
                    fragmentNavigation.popFragment();
                }
            }
        });
    }

    @Override
    public void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        rtManager.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        rtManager.onDestroy(true);
    }

    private View.OnClickListener onPodcastChannelImageUpload = v -> {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
        } else {
            selectImage();
        }
    };

    private void selectImage() {
        Intent pickImageIntent = new Intent(
                Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickImageIntent, 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            File image = new File(FileUploadUtil.getRealPathFromURIPath(data.getData(), getContext()));
            viewModel.getPodcastChannelRequest().setImageFileName(image.getName());

            FileUploadUtil.uploadFileToServer(getContext(), token.getValue(), image,
                    "/api-gateway/podcast/podcastChannel/image", "imageFile", binder.podcastChannelImage);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String permissions[], @NotNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            selectImage();
        } else {
            Toast.makeText(getContext(), getString(R.string.storage_permission_image), Toast.LENGTH_SHORT).show();
        }
    }

}

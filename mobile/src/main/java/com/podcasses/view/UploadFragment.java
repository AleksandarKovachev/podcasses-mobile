package com.podcasses.view;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.onegravity.rteditor.RTManager;
import com.onegravity.rteditor.api.RTApi;
import com.onegravity.rteditor.api.RTMediaFactoryImpl;
import com.onegravity.rteditor.api.RTProxyImpl;
import com.podcasses.R;
import com.podcasses.dagger.BaseApplication;
import com.podcasses.databinding.FragmentUploadBinding;
import com.podcasses.model.entity.Nomenclature;
import com.podcasses.retrofit.ApiFileUploadInterface;
import com.podcasses.retrofit.util.ProgressRequestBody;
import com.podcasses.view.base.BaseFragment;
import com.podcasses.viewmodel.UploadViewModel;
import com.podcasses.viewmodel.ViewModelFactory;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Created by aleksandar.kovachev.
 */
public class UploadFragment extends BaseFragment {

    @Inject
    ViewModelFactory viewModelFactory;

    @Inject
    ApiFileUploadInterface fileUploadInterface;

    private UploadViewModel viewModel;

    private RTManager rtManager;

    private LifecycleOwner lifecycleOwner;

    private LiveData<String> token;

    private FragmentUploadBinding binder;

    static UploadFragment newInstance(int instance) {
        Bundle args = new Bundle();
        args.putInt(BaseFragment.ARGS_INSTANCE, instance);
        UploadFragment fragment = new UploadFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binder = DataBindingUtil.inflate(inflater, R.layout.fragment_upload, container, false);

        ((BaseApplication) getActivity().getApplication()).getAppComponent().inject(this);

        token = isAuthenticated();

        lifecycleOwner = this;

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(UploadViewModel.class);
        binder.setViewModel(viewModel);

        RTApi rtApi = new RTApi(getContext(), new RTProxyImpl((Activity) getContext()), new RTMediaFactoryImpl(getContext(), true));
        rtManager = new RTManager(rtApi, savedInstanceState);

        rtManager.registerToolbar(binder.rteToolbarContainer, binder.rteToolbarContainer.findViewById(R.id.rte_toolbar_character));
        rtManager.registerToolbar(binder.rteToolbarContainer, binder.rteToolbarContainer.findViewById(R.id.rte_toolbar_paragraph));
        rtManager.registerEditor(binder.podcastDescription, true);

        binder.podcastUpload.setOnClickListener(onPodcastUpload);
        binder.podcastUploadFab.setOnClickListener(onPodcastUpload);
        binder.podcastImageUpload.setOnClickListener(onPodcastImageUpload);

        setPrivacies();
        setCategories();

        return binder.getRoot();
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

    private void setCategories() {
        LiveData<List<Nomenclature>> categories = viewModel.getCategoryNomenclatures();
        categories.observe(lifecycleOwner, nomenclatures -> {
            categories.removeObservers(lifecycleOwner);
            viewModel.setCategories(nomenclatures);
        });
    }

    private void setPrivacies() {
        LiveData<List<Nomenclature>> privacies = viewModel.getPrivacyNomenclatures();
        privacies.observe(lifecycleOwner, nomenclatures -> {
            privacies.removeObservers(lifecycleOwner);
            viewModel.setPrivacies(nomenclatures);
        });
    }

    private View.OnClickListener onPodcastUpload = v -> {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 4);
        } else {
            selectAudio();
        }
    };

    private View.OnClickListener onPodcastImageUpload = v -> {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 3);
        } else {
            selectImage();
        }
    };

    private void selectAudio() {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, 1);
    }

    private void selectImage() {
        Intent pickImageIntent = new Intent(
                Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickImageIntent, 2);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                sendPodcastUploadRequest(data);
            } else if (requestCode == 2) {
                sendPodcastImageUploadRequest(data);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String permissions[], @NotNull int[] grantResults) {
        if (requestCode == 3) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImage();
            } else {
                Toast.makeText(getContext(), getString(R.string.storage_permission_image), Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == 4) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectAudio();
            } else {
                Toast.makeText(getContext(), getString(R.string.storage_permission_audio), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void sendPodcastUploadRequest(Intent data) {
        File podcast = new File(getRealPathFromURIPath(data.getData(), getContext()));
        RequestBody requestBody = RequestBody.create(MediaType.parse("audio/*"), podcast);

        binder.podcastUpload.setVisibility(GONE);
        binder.podcastUploadFab.setVisibility(VISIBLE);
        binder.podcastFileName.setVisibility(VISIBLE);

        viewModel.setPodcastFileName(podcast.getName());

        ProgressRequestBody fileBody = new ProgressRequestBody(requestBody,
                (bytesWritten, contentLength) -> viewModel.setPodcastUploadProgress(((double) bytesWritten / contentLength) * 100));

        Call<ResponseBody> call = fileUploadInterface.podcastUpload(
                "Bearer " + token.getValue(),
                MultipartBody.Part.createFormData("podcastFile", podcast.getName(), fileBody));
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(getTag(), "onFailure: ", t);
            }
        });
    }

    private void sendPodcastImageUploadRequest(Intent data) {
        File image = new File(getRealPathFromURIPath(data.getData(), getContext()));
        RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), image);

        Call<ResponseBody> call = fileUploadInterface.podcastImage(
                "Bearer " + token.getValue(),
                MultipartBody.Part.createFormData("imageFile", image.getName(), requestBody));
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                viewModel.setPodcastImage(image.getAbsolutePath());
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(getTag(), "onFailure: ", t);
            }
        });
    }

    private String getRealPathFromURIPath(Uri contentURI, Context context) {
        Cursor cursor = context.getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) {
            return contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(idx);
        }
    }

}

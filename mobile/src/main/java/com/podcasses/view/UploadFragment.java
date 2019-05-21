package com.podcasses.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
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

import com.google.gson.Gson;
import com.onegravity.rteditor.RTManager;
import com.onegravity.rteditor.api.RTApi;
import com.onegravity.rteditor.api.RTMediaFactoryImpl;
import com.onegravity.rteditor.api.RTProxyImpl;
import com.podcasses.BuildConfig;
import com.podcasses.R;
import com.podcasses.dagger.BaseApplication;
import com.podcasses.databinding.FragmentUploadBinding;
import com.podcasses.model.entity.Nomenclature;
import com.podcasses.model.entity.Podcast;
import com.podcasses.model.response.ErrorResultResponse;
import com.podcasses.model.response.FieldErrorResponse;
import com.podcasses.model.response.Language;
import com.podcasses.retrofit.ApiCallInterface;
import com.podcasses.util.AuthenticationUtil;
import com.podcasses.util.DialogUtil;
import com.podcasses.view.base.BaseFragment;
import com.podcasses.viewmodel.UploadViewModel;
import com.podcasses.viewmodel.ViewModelFactory;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadService;
import net.gotev.uploadservice.okhttp.OkHttpStack;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import es.dmoral.toasty.Toasty;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;
import static android.view.View.VISIBLE;

/**
 * Created by aleksandar.kovachev.
 */
public class UploadFragment extends BaseFragment {

    @Inject
    ViewModelFactory viewModelFactory;

    @Inject
    ApiCallInterface apiCallInterface;

    @Inject
    OkHttpClient okHttpClient;

    @Inject
    Gson gson;

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
        UploadService.HTTP_STACK = new OkHttpStack(okHttpClient);
        lifecycleOwner = this;
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(UploadViewModel.class);
        binder.setViewModel(viewModel);
        binder.podcastUpload.setOnClickListener(onPodcastUpload);
        binder.podcastUploadFab.setOnClickListener(onPodcastUpload);
        binder.podcastImageUpload.setOnClickListener(onPodcastImageUpload);
        binder.podcastAdd.setOnClickListener(onPodcastAdd);
        return binder.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        token = AuthenticationUtil.isAuthenticated(getContext(), this);
        RTApi rtApi = new RTApi(getContext(), new RTProxyImpl((Activity) getContext()), new RTMediaFactoryImpl(getContext(), true));
        rtManager = new RTManager(rtApi, savedInstanceState);

        rtManager.registerToolbar(binder.rteToolbarContainer, binder.rteToolbarContainer.findViewById(R.id.rte_toolbar_character));
        rtManager.registerToolbar(binder.rteToolbarContainer, binder.rteToolbarContainer.findViewById(R.id.rte_toolbar_paragraph));
        rtManager.registerEditor(binder.podcastDescription, true);

        setPrivacies();
        setLanguages();
        setCategories();
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

    @SuppressLint("RestrictedApi")
    private void sendPodcastUploadRequest(Intent data) {
        binder.podcastUpload.setVisibility(View.INVISIBLE);
        binder.podcastUploadFab.setVisibility(VISIBLE);
        binder.podcastFileName.setVisibility(VISIBLE);
        binder.podcastFileName.setSelected(true);

        File podcast = new File(getRealPathFromURIPath(data.getData(), getContext()));
        viewModel.getPodcast().setPodcastFileName(podcast.getName());

        uploadFileToServer(podcast, "/podcast/upload", "podcastFile");
    }

    private void sendPodcastImageUploadRequest(Intent data) {
        File image = new File(getRealPathFromURIPath(data.getData(), getContext()));
        viewModel.getPodcast().setImageFileName(image.getName());

        uploadFileToServer(image, "/podcast/image", "imageFile");
    }

    private void uploadFileToServer(File file, String url, String multipartName) {
        try {
            MultipartUploadRequest request = new MultipartUploadRequest(
                    getContext(),
                    UUID.randomUUID().toString(),
                    BuildConfig.API_GATEWAY_URL.concat(url));

            request.addHeader("Authorization", "Bearer " + token.getValue());
            request.addFileToUpload(file.getPath(), multipartName);
            request.setNotificationConfig(new UploadNotificationConfig());
            request.setMaxRetries(2);
            request.startUpload();
        } catch (Exception e) {
            Log.e(getTag(), "onFailure: ", e);
        }
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

    private void setLanguages() {
        LiveData<List<Language>> languages = viewModel.getLanguageNomenclatures();
        languages.observe(lifecycleOwner, language -> {
            languages.removeObservers(lifecycleOwner);
            viewModel.setLanguages(language);
        });
    }

    private View.OnClickListener onPodcastAdd = v -> {
        ProgressDialog progressDialog = DialogUtil.getProgressDialog(v.getContext());
        progressDialog.show();
        Call<Podcast> call = apiCallInterface.podcast("Bearer " + token.getValue(), viewModel.getPodcast());
        call.enqueue(new Callback<Podcast>() {
            @Override
            public void onResponse(Call<Podcast> call, Response<Podcast> response) {
                progressDialog.dismiss();
                if (response.isSuccessful()) {
                    Toasty.success(getContext(), getString(R.string.podcast_successfully_added), Toast.LENGTH_SHORT, true).show();
                    viewModel.savePodcast(response);
                } else {
                    try {
                        ErrorResultResponse errorResponse = gson.fromJson(response.errorBody().string(), ErrorResultResponse.class);

                        StringBuilder stringBuilder = new StringBuilder();
                        for (FieldErrorResponse fieldError : errorResponse.getError().getFieldErrors()) {
                            stringBuilder.append(fieldError.getError());
                        }
                        Toasty.error(getContext(), stringBuilder.toString(), Toast.LENGTH_SHORT, true).show();
                    } catch (IOException e) {
                        Log.e(getTag(), "onResponse: ", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<Podcast> call, Throwable t) {
                progressDialog.dismiss();
                Toasty.error(getContext(), getString(R.string.error_response), Toast.LENGTH_SHORT, true).show();
            }
        });
    };

}

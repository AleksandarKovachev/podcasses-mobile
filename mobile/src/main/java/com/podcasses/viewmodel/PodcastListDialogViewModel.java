package com.podcasses.viewmodel;

import android.view.View;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.podcasses.R;
import com.podcasses.adapter.PodcastListAdapter;
import com.podcasses.model.dto.PodcastListCheckbox;
import com.podcasses.model.request.AccountListRequest;
import com.podcasses.model.response.AccountList;
import com.podcasses.model.response.ApiResponse;
import com.podcasses.repository.MainDataRepository;
import com.podcasses.retrofit.ApiCallInterface;
import com.podcasses.util.LogErrorResponseUtil;
import com.podcasses.viewmodel.base.BaseViewModel;

import java.util.List;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by aleksandar.kovachev.
 */
public class PodcastListDialogViewModel extends BaseViewModel {

    private ApiCallInterface apiCallInterface;

    private MutableLiveData<List<AccountList>> checkedAccountLists = new MutableLiveData<>();
    private MutableLiveData<List<PodcastListCheckbox>> podcastListCheckboxes = new MutableLiveData<>();
    private PodcastListAdapter podcastListAdapter = new PodcastListAdapter(this);

    PodcastListDialogViewModel(MainDataRepository repository, ApiCallInterface apiCallInterface) {
        super(repository);
        this.apiCallInterface = apiCallInterface;
    }

    public PodcastListAdapter getPodcastListAdapter() {
        return podcastListAdapter;
    }

    public LiveData<ApiResponse> accountLists(String token, String podcastId) {
        return repository.getAccountLists(token, podcastId);
    }

    public void setPodcastListCheckboxes(List<PodcastListCheckbox> podcastListCheckboxes) {
        this.podcastListCheckboxes.setValue(podcastListCheckboxes);
        this.podcastListAdapter.setPodcastListCheckBoxes(podcastListCheckboxes);
    }

    public void addPodcastListCheckBox(PodcastListCheckbox podcastListCheckbox) {
        this.podcastListCheckboxes.getValue().add(podcastListCheckbox);
        this.podcastListAdapter.addPodcastListCheckBox(podcastListCheckbox);
    }

    public List<PodcastListCheckbox> getPodcastListCheckboxes() {
        return podcastListCheckboxes.getValue();
    }

    public void setCheckedAccountLists(List<AccountList> checkedAccountLists) {
        this.checkedAccountLists.setValue(checkedAccountLists);
    }

    public PodcastListCheckbox getPodcastListCheckboxAt(Integer index) {
        if (podcastListCheckboxes.getValue() != null && index != null && podcastListCheckboxes.getValue().size() > index) {
            return podcastListCheckboxes.getValue().get(index);
        }
        return null;
    }

    public void onCheckedChange(View view, Integer index) {
        if (podcastListAdapter.getPodcastListCheckboxes() != null && index != null && podcastListAdapter.getPodcastListCheckboxes().size() > index) {
            podcastListAdapter.getPodcastListCheckboxes().get(index).setChecked(!podcastListAdapter.getPodcastListCheckboxes().get(index).isChecked());
        }
    }

    public void onAddPodcastListButtonClick(View view, String token, String podcastId, DialogFragment dialogFragment) {
        if (token == null) {
            return;
        }

        for (PodcastListCheckbox podcastListCheckbox : podcastListAdapter.getPodcastListCheckboxes()) {
            if (shouldSkip(podcastListCheckbox)) continue;

            AccountListRequest accountListRequest = new AccountListRequest();
            accountListRequest.setAccountListId(podcastListCheckbox.getId());
            accountListRequest.setPodcastId(podcastId);

            Call<AccountList> accountListCall = apiCallInterface.accountList("Bearer " + token, accountListRequest);
            accountListCall.enqueue(new Callback<AccountList>() {
                @Override
                public void onResponse(Call<AccountList> call, Response<AccountList> response) {
                    if (response.isSuccessful()) {
                        for (PodcastListCheckbox podcastListCheckbox : podcastListCheckboxes.getValue()) {
                            if (podcastListCheckbox.getId() == response.body().getId()) {
                                podcastListCheckbox.setChecked(true);
                            }
                        }
                        Toasty.success(view.getContext(), view.getContext().getString(R.string.podcast_successfully_added_to_list), Toast.LENGTH_SHORT, true).show();
                    } else {
                        LogErrorResponseUtil.logErrorResponse(response, view.getContext());
                    }
                }

                @Override
                public void onFailure(Call<AccountList> call, Throwable t) {
                    LogErrorResponseUtil.logFailure(t, view.getContext());
                }
            });
        }

        dialogFragment.dismiss();
    }

    private boolean shouldSkip(PodcastListCheckbox podcastListCheckbox) {
        boolean isPodcastListCheckboxInitiallyChecked = false;
        for (AccountList accountList : checkedAccountLists.getValue()) {
            if (podcastListCheckbox.getId() == accountList.getId()) {
                isPodcastListCheckboxInitiallyChecked = true;
            }
        }

        return podcastListCheckbox.isChecked() && isPodcastListCheckboxInitiallyChecked ||
                !podcastListCheckbox.isChecked() && !isPodcastListCheckboxInitiallyChecked;
    }

}

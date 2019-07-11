package com.podcasses.repository;

import androidx.lifecycle.LiveData;

import com.podcasses.database.dao.AccountDao;
import com.podcasses.database.dao.AccountPodcastDao;
import com.podcasses.database.dao.PodcastDao;
import com.podcasses.database.dao.PodcastFileDao;
import com.podcasses.database.dao.PodcastTypeDao;
import com.podcasses.model.entity.Account;
import com.podcasses.model.entity.AccountPodcast;
import com.podcasses.model.entity.Podcast;
import com.podcasses.model.entity.PodcastFile;
import com.podcasses.model.entity.PodcastType;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;

import javax.inject.Inject;

/**
 * Created by aleksandar.kovachev.
 */
public class LocalDataSource {

    private PodcastDao podcastDao;

    private AccountDao accountDao;

    private AccountPodcastDao accountPodcastDao;

    private PodcastFileDao podcastFileDao;

    private PodcastTypeDao podcastTypeDao;

    @Inject
    public LocalDataSource(PodcastDao podcastDao, AccountDao accountDao, AccountPodcastDao accountPodcastDao,
                           PodcastFileDao podcastFileDao, PodcastTypeDao podcastTypeDao) {
        this.podcastDao = podcastDao;
        this.accountDao = accountDao;
        this.accountPodcastDao = accountPodcastDao;
        this.podcastFileDao = podcastFileDao;
        this.podcastTypeDao = podcastTypeDao;
    }

    LiveData<List<Podcast>> getPodcasts(Integer type, int page) {
        return podcastDao.getPodcasts(type, page * 10);
    }

    LiveData<List<Podcast>> getPodcastsInProgress(int page) {
        return podcastDao.getPodcastsInProgress(page * 10);
    }

    LiveData<Podcast> getPodcastById(String podcastId) {
        return podcastDao.getPodcastById(podcastId);
    }

    void insertPodcasts(Integer type, Podcast... podcasts) {
        Executors.newSingleThreadExecutor().execute(() -> {
            for (Podcast podcast : podcasts) {
                podcastDao.insertAll(podcast);
                if (podcastTypeDao.getPodcastType(podcast.getId(), type) == null) {
                    PodcastType podcastType = new PodcastType();
                    podcastType.setCreatedTimestamp(new Date());
                    podcastType.setPodcastId(podcast.getId());
                    podcastType.setPodcastType(type);
                    podcastTypeDao.insertAll(podcastType);
                }
            }
        });
    }

    void deletePodcast(Integer podcastType, String podcastId) {
        Executors.newSingleThreadExecutor().execute(() -> {
            podcastTypeDao.deletePodcastType(podcastType, podcastId);
            podcastDao.deletePodcasts();
        });
    }

    void deletePodcastsByType(Integer podcastType) {
        Executors.newSingleThreadExecutor().execute(() -> {
            podcastTypeDao.deletePodcastType(podcastType);
            podcastDao.deletePodcasts();
        });
    }

    LiveData<Account> getMyAccountData() {
        return accountDao.getMyAccountData();
    }

    LiveData<Account> getAccount(String username, String id) {
        return accountDao.getAccount(username, id);
    }

    void insertAccount(Account account) {
        Executors.newSingleThreadExecutor().execute(() ->
                accountDao.insert(account));
    }

    LiveData<List<AccountPodcast>> getAccountPodcasts(List<String> podcastIds) {
        return accountPodcastDao.getAccountPodcasts(podcastIds);
    }

    LiveData<AccountPodcast> getAccountPodcast(String podcastId) {
        return accountPodcastDao.getAccountPodcast(podcastId);
    }

    List<AccountPodcast> getNotSyncedAccountPodcasts() {
        return accountPodcastDao.getNotSyncedAccountPodcasts();
    }

    void insertAccountPodcasts(AccountPodcast... accountPodcasts) {
        Executors.newSingleThreadExecutor().execute(() ->
                accountPodcastDao.insertAll(accountPodcasts));
    }

    LiveData<List<PodcastFile>> getPodcastFiles() {
        return podcastFileDao.getPodcastFiles();
    }

    void insertPodcastFiles(PodcastFile... podcastFiles) {
        Executors.newSingleThreadExecutor().execute(() -> {
            podcastFileDao.deletePodcastFiles();
            podcastFileDao.insertAll(podcastFiles);
        });
    }

    void deletePodcastFile(String id) {
        Executors.newSingleThreadExecutor().execute(() ->
                podcastFileDao.deletePodcastFile(id));
    }

    void removeAllLocalData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            accountDao.deleteAll();
            podcastTypeDao.deleteAll();
            podcastDao.deleteAll();
            podcastFileDao.deletePodcastFiles();
            accountPodcastDao.deleteAll();
        });
    }

}

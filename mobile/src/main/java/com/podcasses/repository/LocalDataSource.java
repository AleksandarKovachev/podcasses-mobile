package com.podcasses.repository;

import androidx.lifecycle.LiveData;

import com.podcasses.constant.PodcastType;
import com.podcasses.database.dao.AccountPodcastDao;
import com.podcasses.database.dao.PodcastChannelDao;
import com.podcasses.database.dao.PodcastDao;
import com.podcasses.database.dao.PodcastFileDao;
import com.podcasses.model.entity.AccountPodcast;
import com.podcasses.model.entity.DownloadedPodcast;
import com.podcasses.model.entity.HistoryPodcast;
import com.podcasses.model.entity.LikedPodcast;
import com.podcasses.model.entity.NewPodcast;
import com.podcasses.model.entity.PodcastChannel;
import com.podcasses.model.entity.PodcastFile;
import com.podcasses.model.entity.ProgressPodcast;
import com.podcasses.model.entity.TrendingPodcast;
import com.podcasses.model.entity.UserPodcast;
import com.podcasses.model.entity.base.Podcast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import javax.inject.Inject;

/**
 * Created by aleksandar.kovachev.
 */
public class LocalDataSource {

    private PodcastDao podcastDao;

    private PodcastChannelDao podcastChannelDao;

    private AccountPodcastDao accountPodcastDao;

    private PodcastFileDao podcastFileDao;

    @Inject
    public LocalDataSource(PodcastDao podcastDao, PodcastChannelDao podcastChannelDao, AccountPodcastDao accountPodcastDao,
                           PodcastFileDao podcastFileDao) {
        this.podcastDao = podcastDao;
        this.podcastChannelDao = podcastChannelDao;
        this.accountPodcastDao = accountPodcastDao;
        this.podcastFileDao = podcastFileDao;
    }

    LiveData<Podcast> getUserPodcastById(String id) {
        return podcastDao.getUserPodcastById(id);
    }

    LiveData<List<Podcast>> getUserPodcastsByUserId(String userId, int page) {
        return podcastDao.getUserPodcastsByUserId(userId, page * 10);
    }

    LiveData<List<PodcastChannel>> getPodcastChannelsByUserId(String userId) {
        return podcastChannelDao.getPodcastChannelsByUserId(userId);
    }


    void insertPodcastChannels(List<PodcastChannel> podcastChannels) {
        Executors.newSingleThreadExecutor().execute(() -> podcastChannelDao.insertAll(podcastChannels));
    }

    void deletePodcastChannelsByUserId(String userId) {
        Executors.newSingleThreadExecutor().execute(() -> podcastChannelDao.deletePodcastChannelsByUserId(userId));
    }

    LiveData<List<Podcast>> getPodcasts(PodcastType type, int page) {
        switch (type) {
            case LIKED_PODCASTS:
                return podcastDao.getLikedPodcasts(page * 10);
            case MY_PODCASTS:
                return podcastDao.getUserPodcasts(page * 10);
            case FROM_SUBSCRIPTIONS:
                return podcastDao.getNewPodcasts(page * 10);
            case TRENDING:
                return podcastDao.getTrendingPodcasts(page * 10);
            case IN_PROGRESS:
                return podcastDao.getProgressPodcasts(page * 10);
            case HISTORY:
                return podcastDao.getHistoryPodcasts(page * 10);
            case DOWNLOADED:
                return podcastDao.getDownloadedPodcasts(page * 10);
            default:
                break;
        }
        return null;
    }

    void insertPodcasts(PodcastType type, List<Podcast> podcasts) {
        Executors.newSingleThreadExecutor().execute(() -> {
            switch (type) {
                case LIKED_PODCASTS:
                    List<LikedPodcast> likedPodcasts = new ArrayList<>();
                    for (Podcast podcast : podcasts) {
                        likedPodcasts.add(new LikedPodcast(podcast));
                    }
                    podcastDao.insertLikedPodcasts(likedPodcasts);
                    break;
                case MY_PODCASTS:
                    List<UserPodcast> userPodcasts = new ArrayList<>();
                    for (Podcast podcast : podcasts) {
                        userPodcasts.add(new UserPodcast(podcast));
                    }
                    podcastDao.insertUserPodcasts(userPodcasts);
                    break;
                case FROM_SUBSCRIPTIONS:
                    List<NewPodcast> newPodcasts = new ArrayList<>();
                    for (Podcast podcast : podcasts) {
                        newPodcasts.add(new NewPodcast(podcast));
                    }
                    podcastDao.insertNewPodcasts(newPodcasts);
                    break;
                case TRENDING:
                    List<TrendingPodcast> trendingPodcasts = new ArrayList<>();
                    for (Podcast podcast : podcasts) {
                        trendingPodcasts.add(new TrendingPodcast(podcast));
                    }
                    podcastDao.insertTrendingPodcasts(trendingPodcasts);
                    break;
                case IN_PROGRESS:
                    List<ProgressPodcast> progressPodcasts = new ArrayList<>();
                    for (Podcast podcast : podcasts) {
                        progressPodcasts.add(new ProgressPodcast(podcast));
                    }
                    podcastDao.insertProgressPodcasts(progressPodcasts);
                    break;
                case HISTORY:
                    List<HistoryPodcast> historyPodcasts = new ArrayList<>();
                    for (Podcast podcast : podcasts) {
                        historyPodcasts.add(new HistoryPodcast(podcast));
                    }
                    podcastDao.insertHistoryPodcasts(historyPodcasts);
                    break;
                case DOWNLOADED:
                    List<DownloadedPodcast> downloadedPodcasts = new ArrayList<>();
                    for (Podcast podcast : podcasts) {
                        downloadedPodcasts.add(new DownloadedPodcast(podcast));
                    }
                    podcastDao.insertDownloadedPodcasts(downloadedPodcasts);
                    break;
                default:
                    break;
            }
        });
    }

    void deletePodcastsByType(PodcastType type) {
        Executors.newSingleThreadExecutor().execute(() -> {
            switch (type) {
                case LIKED_PODCASTS:
                    podcastDao.deleteLikedPodcasts();
                    break;
                case MY_PODCASTS:
                    podcastDao.deleteUserPodcasts();
                    break;
                case FROM_SUBSCRIPTIONS:
                    podcastDao.deleteNewPodcasts();
                    break;
                case TRENDING:
                    podcastDao.deleteTrendingPodcasts();
                    break;
                case IN_PROGRESS:
                    podcastDao.deleteProgressPodcasts();
                    break;
                case HISTORY:
                    podcastDao.deleteHistoryPodcasts();
                    break;
                case DOWNLOADED:
                    podcastDao.deleteDownloadedPodcasts();
                    break;
                default:
                    break;
            }
        });
    }

    void deleteDownloadedPodcast(String id) {
        Executors.newSingleThreadExecutor().execute(() -> podcastDao.deleteDownloadedPodcast(id));
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
            podcastDao.deleteLikedPodcasts();
            podcastDao.deleteUserPodcasts();
            podcastDao.deleteNewPodcasts();
            podcastDao.deleteProgressPodcasts();
            podcastDao.deleteHistoryPodcasts();
            podcastChannelDao.deletePodcastChannels();
            podcastFileDao.deletePodcastFiles();
            accountPodcastDao.deleteAll();
        });
    }

}

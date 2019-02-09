package com.podcasses.model.repository;

import com.podcasses.database.dao.AccountDao;
import com.podcasses.database.dao.PodcastDao;
import com.podcasses.model.entity.Account;
import com.podcasses.model.entity.Podcast;

import java.util.List;

import javax.inject.Inject;

import androidx.lifecycle.LiveData;

/**
 * Created by aleksandar.kovachev.
 */
public class LocalDataSource {

    private PodcastDao podcastDao;

    private AccountDao accountDao;

    @Inject
    public LocalDataSource(PodcastDao podcastDao, AccountDao accountDao) {
        this.podcastDao = podcastDao;
        this.accountDao = accountDao;
    }

    LiveData<List<Podcast>> getUserPodcasts(String userId) {
        return podcastDao.getUserPodcasts(userId);
    }

    LiveData<Podcast> getPodcastById(String podcastId) {
        return podcastDao.getPodcastById(podcastId);
    }

    void insertPodcasts(Podcast... podcasts) {
        podcastDao.insertAll(podcasts);
    }

    LiveData<Account> getAccountById(String accountId) {
        return accountDao.getAccountById(accountId);
    }

    LiveData<Account> getAccountByUsername(String username) {
        return accountDao.getAccountByUsername(username);
    }

    void insertAccount(Account account) {
        accountDao.insert(account);
    }

    void updateAccountSubscribes(int subscribes, String accountId) {
        accountDao.update(subscribes, accountId);
    }

}

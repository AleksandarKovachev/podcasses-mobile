package com.podcasses.model.repository;

import com.podcasses.database.dao.AccountDao;
import com.podcasses.database.dao.AccountPodcastDao;
import com.podcasses.database.dao.PodcastDao;
import com.podcasses.model.entity.Account;
import com.podcasses.model.entity.AccountPodcast;
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

    private AccountPodcastDao accountPodcastDao;

    @Inject
    public LocalDataSource(PodcastDao podcastDao, AccountDao accountDao, AccountPodcastDao accountPodcastDao) {
        this.podcastDao = podcastDao;
        this.accountDao = accountDao;
        this.accountPodcastDao = accountPodcastDao;
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

    void deleteAllPodcasts() {
        podcastDao.deleteAll();
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

    LiveData<AccountPodcast> getAccountPodcast(String accountId, String podcastId) {
        return accountPodcastDao.getAccountPodcast(accountId, podcastId);
    }

    LiveData<List<AccountPodcast>> getAccountPodcasts(String accountId) {
        return accountPodcastDao.getAccountPodcasts(accountId);
    }

    void insertAccountPodcasts(AccountPodcast... accountPodcasts) {
        accountPodcastDao.insertAll(accountPodcasts);
    }

}

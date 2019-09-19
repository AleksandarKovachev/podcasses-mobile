package com.podcasses.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.podcasses.model.entity.Account;
import com.podcasses.model.entity.AccountPodcast;

import java.util.List;

/**
 * Created by aleksandar.kovachev.
 */
@Dao
public interface AccountDao {

    @Query("SELECT * FROM Account")
    LiveData<Account> getAccount();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Account account);

    @Query("DELETE FROM Account")
    void deleteAll();

}

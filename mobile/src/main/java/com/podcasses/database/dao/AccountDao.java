package com.podcasses.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.podcasses.model.entity.Account;

/**
 * Created by aleksandar.kovachev.
 */
@Dao
public interface AccountDao {

    @Query("SELECT * FROM account WHERE isMyAccount = 1")
    LiveData<Account> getMyAccountData();

    @Query("SELECT * FROM account WHERE username = (:username) OR id = (:id)")
    LiveData<Account> getAccount(String username, String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Account account);

}

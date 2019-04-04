package com.podcasses.database.dao;

import com.podcasses.model.entity.Account;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

/**
 * Created by aleksandar.kovachev.
 */
@Dao
public interface AccountDao {

    @Query("SELECT * FROM account WHERE keycloakId = (:accountId)")
    LiveData<Account> getAccountById(String accountId);

    @Query("SELECT * FROM account WHERE username = (:username)")
    LiveData<Account> getAccountByUsername(String username);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Account account);

}

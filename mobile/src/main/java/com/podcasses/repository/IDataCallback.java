package com.podcasses.repository;

/**
 * Created by aleksandar.kovachev.
 */
interface IDataCallback<T> {

    void onSuccess(T data);

    void onFailure(Throwable error);

}

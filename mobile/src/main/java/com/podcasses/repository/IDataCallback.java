package com.podcasses.repository;

/**
 * Created by aleksandar.kovachev.
 */
interface IDataCallback<T> {

    void onSuccess(T data, String url);

    void onFailure(Throwable error, String url);

}

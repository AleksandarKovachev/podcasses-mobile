package com.podcasses.model.repository;

/**
 * Created by aleksandar.kovachev.
 */
interface IDataCallback<T> {

    void onSuccess(T data);

    void onFailure(Throwable error);

}

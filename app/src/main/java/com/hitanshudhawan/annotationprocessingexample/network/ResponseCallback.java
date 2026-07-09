package com.hitanshudhawan.annotationprocessingexample.network;

/**
 * Network response callback.
 *
 * @param <S> Success
 * @param <E> Error
 */
public interface ResponseCallback<S, E> {

    void onSuccess(S successResponse);

    void onError(E errorResponse);

}

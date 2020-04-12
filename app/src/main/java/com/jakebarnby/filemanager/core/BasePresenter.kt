package com.jakebarnby.filemanager.core

interface BasePresenter<T> {
    var view: T?

    fun subscribe(view: T) {
        this.view = view
    }

    fun unsubscribe() {
        this.view = null
    }
}
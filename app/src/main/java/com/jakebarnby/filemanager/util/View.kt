package com.jakebarnby.filemanager.util

import android.view.View
import android.view.ViewTreeObserver

inline fun View.afterLayout(
    crossinline action: () -> Unit
) {
    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            action()
            viewTreeObserver.removeOnGlobalLayoutListener(this)
        }
    })
}
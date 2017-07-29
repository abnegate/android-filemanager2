package com.jakebarnby.filemanager.listeners;

/**
 * Created by Jake on 7/29/2017.
 */
@FunctionalInterface
public interface OnSpaceCheckListener {
    void complete(boolean success);
}

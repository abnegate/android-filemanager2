package com.jakebarnby.filemanager.util

/**
 * Created by Jake on 9/26/2017.
 */
object Intents {
    const val ACTION_COMPLETE = "com.jakebarnby.filemanager.services.action.COMPLETE"
    const val ACTION_SHOW_DIALOG = "com.jakebarnby.filemanager.services.action.SHOW_DIALOG"
    const val ACTION_UPDATE_DIALOG = "com.jakebarnby.filemanager.services.action.UPDATE_DIALOG"
    const val ACTION_SHOW_ERROR = "com.jakebarnby.filemanager.services.action.SHOW_ERROR"
    const val ACTION_COPY = "com.jakebarnby.filemanager.services.action.COPY"
    const val ACTION_MOVE = "com.jakebarnby.filemanager.services.action.MOVE"
    const val ACTION_DELETE = "com.jakebarnby.filemanager.services.action.DELETE"
    const val ACTION_RENAME = "com.jakebarnby.filemanager.services.action.RENAME"
    const val ACTION_OPEN = "com.jakebarnby.filemanager.services.action.OPEN"
    const val ACTION_ZIP = "com.jakebarnby.filemanager.services.action.ZIP"
    const val ACTION_CLEAR_CACHE = "com.jakebarnby.filemanager.services.action.CLEAR_CACHE"
    const val EXTRA_OPERATION_ID = "com.jakebarnby.filemanager.services.extra.OPERATION_ID"
    const val EXTRA_DIALOG_CURRENT_VALUE = "com.jakebarnby.filemanager.services.extra.CURRENT_COUNT"
    const val EXTRA_DIALOG_MAX_VALUE = "com.jakebarnby.filemanager.services.extra.TOTAL_COUNT"
    const val EXTRA_DIALOG_TITLE = "com.jakebarnby.filemanager.services.extra.DIALOG_TITLE"
    const val EXTRA_NEW_NAME = "com.jakebarnby.filemanager.services.extra.NAME"
    const val EXTRA_TO_OPEN_PATH = "com.jakebarnby.filemanager.services.extra.TO_OPEN"
    const val EXTRA_DIALOG_MESSAGE = "com.jakebarnby.filemanager.services.extra.DIALOG_MESSAGE"
    const val EXTRA_ZIP_FILENAME = "com.jakebarnby.filemanager.services.extra.ZIP_FILENAME"
}
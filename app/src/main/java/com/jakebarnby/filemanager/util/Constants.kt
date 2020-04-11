package com.jakebarnby.filemanager.util

/**
 * Created by Jake on 6/1/2017.
 */
object Constants {
    const val DIALOG_TITLE_KEY = "DIALOG_TITLE"
    const val FILE_PATH_KEY = "FILE_PATH"
    const val FRAGMENT_TITLE = "TITLE"
    const val BYTES_TO_GIGABYTE = 1073741824.0
    const val BYTES_TO_MEGABYTE = 1048576.0
    const val LOCAL_ROOT = "ROOT_PATH"
    const val ALL = "All"
    const val DATE_TIME_FORMAT = "%02d:%02d %02d/%02d/%02d"
    const val GRID_SIZE = 4
    const val ADS_MENU_POSITION = 9
    const val ADS_MENU_ID = 666666

    object Sources {
        const val LOCAL = "Local"
        const val DROPBOX = "Dropbox"
        const val GOOGLE_DRIVE = "Google Drive"
        const val GOOGLE_DRIVE_FOLDER_MIME = "application/vnd.google-apps.folder"
        const val ONEDRIVE = "OneDrive"
        const val ONEDRIVE_INVALID_CHARS = "[\\/:*?\"<>|#% ]"
        const val DROPBOX_CLIENT_ID = "rse09cxjnnn2yc1"
        const val MAX_FILENAME_LENGTH = 255
    }

    object RequestCodes {
        const val STORAGE_PERMISSIONS = 100
        const val GOOGLE_SIGN_IN = 103
        const val GOOGLE_PLAY_SERVICES = 105
        const val ACCOUNTS_PERMISSIONS = 106
        const val ACCOUNT_PICKER = 107
    }

    object Prefs {
        const val PREFS = "filemanager-prefs"
        const val DROPBOX_TOKEN_KEY = "dropbox-access-token"
        const val GOOGLE_TOKEN_KEY = "google drive-access-token"
        const val GOOGLE_NAME_KEY = "google drive-name"
        const val ONEDRIVE_TOKEN_KEY = "onedrive-access-token"
        const val ONEDRIVE_NAME_KEY = "onedrive-name"
        const val TUT_SEEN_KEY = "seen-tutorial"
        const val VIEW_TYPE_KEY = "view-type"
        const val SORT_TYPE_KEY = "sort-type"
        const val ORDER_TYPE_KEY = "order-type"
        const val HIDDEN_FOLDER_KEY = "hidden-folder"
        const val FOLDER_FIRST_KEY = "folders-first"
        const val HIDE_ADS_KEY = "premium-enabled"
        const val OPERATION_COUNT_KEY = "operation-count"
    }

    object Animation {
        const val PROGRESS_DURATION = 1200
    }

    object RemoteConfig {
        const val RC_CACHE_EXPIRATION_SECONDS = 60
        const val TUT_PAGE_COUNT_KEY = "tut_page_count"
        const val TUT_PAGE1_TITLE_KEY = "tut_page1_title"
        const val TUT_PAGE1_CONTENT_KEY = "tut_page1_content"
        const val TUT_PAGE1_SUMMARY_KEY = "tut_page1_summary"
        const val TUT_PAGE1_IMAGE_KEY = "tut_page1_image"
        const val TUT_PAGE1_BGCOLOR_KEY = "tut_page1_bgcolor"
        const val TUT_PAGE2_TITLE_KEY = "tut_page2_title"
        const val TUT_PAGE2_CONTENT_KEY = "tut_page2_content"
        const val TUT_PAGE2_SUMMARY_KEY = "tut_page2_summary"
        const val TUT_PAGE2_IMAGE_KEY = "tut_page2_image"
        const val TUT_PAGE2_BGCOLOR_KEY = "tut_page2_bgcolor"
        const val TUT_PAGE3_TITLE_KEY = "tut_page3_title"
        const val TUT_PAGE3_CONTENT_KEY = "tut_page3_content"
        const val TUT_PAGE3_SUMMARY_KEY = "tut_page3_summary"
        const val TUT_PAGE3_IMAGE_KEY = "tut_page3_image"
        const val TUT_PAGE3_BGCOLOR_KEY = "tut_page3_bgcolor"
    }

    object Analytics {
        const val NO_DESTINATION = "NO DESTINATION"
        const val PARAM_ERROR_VALUE = "error_value"
        const val PARAM_SOURCE_NAME = "source_name"
        const val PARAM_PURCHASE_PRICE = "purchase_price"
        const val PARAM_PURCHASE_SKU = "purchase_sku"
        const val EVENT_ERROR_DELETE = "error_deleting"
        const val EVENT_ERROR_CACHE_CLEAR = "error_clearing_cache"
        const val EVENT_ERROR_CREATE_FOLDER = "error_creating_folder"
        const val EVENT_ERROR_RENAMING = "error_renaming"
        const val EVENT_ERROR_OPENING_FILE = "error_opening_file"
        const val EVENT_ERROR_COPYING = "error_copying_file"
        const val EVENT_ERROR_DROPBOX_LOGOUT = "error_dropbox_logout"
        const val EVENT_ERROR_PURCHASE = "error_purchase"
        const val EVENT_SUCCESS_CREATE_FOLDER = "success_creating_folder"
        const val EVENT_SUCCESS_RENAMING = "success_renaming"
        const val EVENT_SUCCESS_OPEN_FILE = "success_opening_file"
        const val EVENT_SUCCESS_COPYING = "success_copying"
        const val EVENT_SUCCESS_DELETING = "success_deleting"
        const val EVENT_SUCCESS_DROPBOX_DOWNLOAD = "success_dropbox_download"
        const val EVENT_SUCCESS_GOOGLEDRIVE_DOWNLOAD = "success_googledrive_download"
        const val EVENT_SUCCESS_ONEDRIVE_DOWNLOAD = "success_onedrive_download"
        const val EVENT_SUCCESS_DROPBOX_UPLOAD = "success_dropbox_upload"
        const val EVENT_SUCCESS_GOOGLEDRIVE_UPLOAD = "success_googledrive_upload"
        const val EVENT_SUCCESS_ONEDRIVE_UPLOAD = "success_onedrive_upload"
        const val EVENT_SUCCESS_PURCHASE = "success_purchase"
        const val EVENT_LOGIN_DROPBOX = "login_dropbox"
        const val EVENT_LOGIN_GOOGLE_DRIVE = "login_googledrive"
        const val EVENT_LOGIN_ONEDRIVE = "login_onedrive"
        const val EVENT_LOGOUT_DROPBOX = "logout_dropbox"
        const val EVENT_LOGOUT_GOOGLEDRIVE = "logout_googledrive"
        const val EVENT_LOGOUT_ONEDRIVE = "logout_onedrive"
        const val EVENT_CANCELLED_PURCHASE = "cancelled_purchase"
        const val EVENT_ERROR_ZIPPING = "error_zipping"
    }

    object ViewTypes {
        const val LIST = 0
        const val DETAILED_LIST = 1
        const val GRID = 2
    }

    object SortTypes {
        const val NAME = 0
        const val TYPE = 1
        const val SIZE = 2
        const val MODIFIED_TIME = 3
    }

    object OrderTypes {
        const val ASCENDING = 0
        const val DESCENDING = 1
    }

    object DialogTags {
        const val PROPERTIES = "Properties"
        const val SORT_BY = "Sort by"
        const val CREATE_FOLDER = "Create folder"
        const val SETTINGS = "Settings"
    }

    object Billing {
        const val SKU_PREMIUM = "premium"
    }

    object Ads {
        const val ADMOB_ID = "ca-app-pub-6044629197845708~9185423482"
        const val INTERSTITIAL_ID = "ca-app-pub-6044629197845708/9815268432"
        const val SHOW_AD_COUNT = 6
    }
}
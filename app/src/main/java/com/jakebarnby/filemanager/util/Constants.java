package com.jakebarnby.filemanager.util;

/**
 * Created by Jake on 6/1/2017.
 */

public class Constants {
    public static final String DIALOG_TITLE_KEY = "DIALOG_TITLE";
    public static final String FILE_PATH_KEY = "FILE_PATH";
    public static final String FRAGMENT_TITLE = "TITLE";
    public static final double BYTES_TO_GIGABYTE = 1073741824d;
    public static final double BYTES_TO_MEGABYTE = 1048576d;
    public static final String LOCAL_ROOT = "ROOT_PATH";
    public static final String ALL = "All";
    public static final String DATE_TIME_FORMAT = "%02d:%02d %02d/%02d/%02d";
    public static final int GRID_SIZE = 4;

    public class Sources {
        public static final String LOCAL = "Local";
        public static final String DROPBOX = "Dropbox";
        public static final String GOOGLE_DRIVE = "Google Drive";
        public static final String GOOGLE_DRIVE_FOLDER_MIME = "application/vnd.google-apps.folder";
        public static final String ONEDRIVE = "OneDrive";
        public static final String ONEDRIVE_INVALID_CHARS = "[\\/:*?\"<>|#% ]";
        public static final String DROPBOX_CLIENT_ID = "rse09cxjnnn2yc1";
        public static final int MAX_FILENAME_LENGTH = 255;
    }

    public class RequestCodes {
        public static final int STORAGE_PERMISSIONS = 100;
        public static final int GOOGLE_SIGN_IN = 103;
        public static final int GOOGLE_PLAY_SERVICES = 105;
        public static final int ACCOUNTS_PERMISSIONS = 106;
        public static final int ACCOUNT_PICKER = 107;
    }

    public class Prefs {
        public static final String PREFS = "filemanager-prefs";
        public static final String DROPBOX_TOKEN_KEY = "dropbox-access-token";
        public static final String GOOGLE_TOKEN_KEY = "googledrive-access-token";
        public static final String GOOGLE_NAME_KEY = "googledrive-name";
        public static final String ONEDRIVE_TOKEN_KEY = "onedrive-access-token";
        public static final String ONEDRIVE_NAME_KEY = "onedrive-name";
        public static final String TUT_SEEN_KEY = "seen-tutorial";
        public static final String VIEW_TYPE_KEY = "view-type";
        public static final String SORT_TYPE_KEY = "sort-type";
        public static final String ORDER_TYPE_KEY = "order-type";
        public static final String HIDDEN_FOLDER_KEY = "hidden-folder";
        public static final String FOLDER_FIRST_KEY = "folders-first";
    }

    public class Animation {
        public static final int PROGRESS_DURATION = 1200;

    }

    public class RemoteConfig {
        public static final int RC_CACHE_EXPIRATION_SECONDS = 60;

        public static final String TUT_PAGE_COUNT_KEY = "tut_page_count";

        public static final String TUT_PAGE1_TITLE_KEY = "tut_page1_title";
        public static final String TUT_PAGE1_CONTENT_KEY = "tut_page1_content";
        public static final String TUT_PAGE1_SUMMARY_KEY = "tut_page1_summary";
        public static final String TUT_PAGE1_IMAGE_KEY = "tut_page1_image";
        public static final String TUT_PAGE1_BGCOLOR_KEY = "tut_page1_bgcolor";

        public static final String TUT_PAGE2_TITLE_KEY = "tut_page2_title";
        public static final String TUT_PAGE2_CONTENT_KEY = "tut_page2_content";
        public static final String TUT_PAGE2_SUMMARY_KEY = "tut_page2_summary";
        public static final String TUT_PAGE2_IMAGE_KEY = "tut_page2_image";
        public static final String TUT_PAGE2_BGCOLOR_KEY = "tut_page2_bgcolor";

        public static final String TUT_PAGE3_TITLE_KEY = "tut_page3_title";
        public static final String TUT_PAGE3_CONTENT_KEY = "tut_page3_content";
        public static final String TUT_PAGE3_SUMMARY_KEY = "tut_page3_summary";
        public static final String TUT_PAGE3_IMAGE_KEY = "tut_page3_image";
        public static final String TUT_PAGE3_BGCOLOR_KEY = "tut_page3_bgcolor";
    }

    public class Analytics {
        public static final String NO_DESTINATION = "NO DESTINATION";

        public static final String PARAM_ERROR_VALUE = "error_value";
        public static final String PARAM_SOURCE_NAME = "source_name";

        public static final String EVENT_ERROR_DELETE = "error_deleting";
        public static final String EVENT_ERROR_CACHE_CLEAR = "error_clearing_cache";
        public static final String EVENT_ERROR_CREATE_FOLDER = "error_creating_folder";
        public static final String EVENT_ERROR_RENAMING = "error_renaming";
        public static final String EVENT_ERROR_OPENING_FILE = "error_opening_file";
        public static final String EVENT_ERROR_COPYING = "error_copying_file";
        public static final String EVENT_ERROR_DROPBOX_LOGOUT = "error_dropbox_logout";

        public static final String EVENT_SUCCESS_CREATE_FOLDER = "success_creating_folder";
        public static final String EVENT_SUCCESS_RENAMING = "success_renaming";
        public static final String EVENT_SUCCESS_OPEN_FILE = "success_opening_file";
        public static final String EVENT_SUCCESS_COPYING = "success_copying";
        public static final String EVENT_SUCCESS_DELETING = "success_deleting";
        public static final String EVENT_SUCCESS_DROPBOX_DOWNLOAD = "success_dropbox_download";
        public static final String EVENT_SUCCESS_GOOGLEDRIVE_DOWNLOAD = "success_googledrive_download";
        public static final String EVENT_SUCCESS_ONEDRIVE_DOWNLOAD = "success_onedrive_download";
        public static final String EVENT_SUCCESS_DROPBOX_UPLOAD = "success_dropbox_upload";
        public static final String EVENT_SUCCESS_GOOGLEDRIVE_UPLOAD = "success_googledrive_upload";
        public static final String EVENT_SUCCESS_ONEDRIVE_UPLOAD = "success_onedrive_upload";

        public static final String EVENT_LOGIN_DROPBOX = "login_dropbox";
        public static final String EVENT_LOGIN_GOOGLE_DRIVE = "login_googledrive";
        public static final String EVENT_LOGIN_ONEDRIVE = "login_onedrive";

        public static final String EVENT_LOGOUT_DROPBOX = "logout_dropbox";
        public static final String EVENT_LOGOUT_GOOGLEDRIVE = "logout_googledrive";
        public static final String EVENT_LOGOUT_ONEDRIVE = "logout_onedrive";
    }

    public class ViewTypes {
        public static final int LIST = 0;
        public static final int DETAILED_LIST = 1;
        public static final int GRID = 2;
    }

    public class SortTypes {
        public static final int NAME = 0;
        public static final int TYPE = 1;
        public static final int SIZE = 2;
        public static final int MODIFIED_TIME = 3;
    }

    public class OrderTypes {
        public static final int ASCENDING = 0;
        public static final int DESCENDING = 1;
    }

    public class DialogTags {
        public static final String SORT_BY = "Sort by";
        public static final String CREATE_FOLDER = "Create folder";
        public static final String SETTINGS = "Settings";
    }
}

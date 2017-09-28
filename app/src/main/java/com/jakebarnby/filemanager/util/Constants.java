package com.jakebarnby.filemanager.util;

/**
 * Created by Jake on 6/1/2017.
 */

public class Constants {
    public static final String DIALOG_TITLE_KEY = "DIALOG_TITLE";
    public static final String FILE_PATH_KEY = "FILE_PATH";
    public static final String FRAGMENT_TITLE = "TITLE";
    public static final double BYTES_TO_GIGABYTE = 1073741824d;
    public static final String LOCAL_ROOT = "ROOT_PATH";
    public static final String ALL = "All";

    public class Sources {
        public static final String LOCAL = "Local";
        public static final String DROPBOX = "Dropbox";
        public static final String GOOGLE_DRIVE = "Google Drive";
        public static final String GOOGLE_DRIVE_FOLDER_MIME = "application/vnd.google-apps.folder";
        public static final String ONEDRIVE = "OneDrive";
        public static final String ONEDRIVE_INVALID_CHARS = "[\\/:*?\"<>|#% ]";

        public static final int MAX_FILENAME_LENGTH = 255;

        public class Keys {
            public static final String DROPBOX_CLIENT_ID = "rse09cxjnnn2yc1";
        }
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
    }

    public class Animation {
        public static final int PROGRESS_DURATION = 1200;
    }

    public class Analytics {
        public static final String NO_DESTINATION = "NO DESTINATION";

        public static final String PARAM_MESSAGE = "message";
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
}

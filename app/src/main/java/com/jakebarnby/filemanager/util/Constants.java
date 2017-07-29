package com.jakebarnby.filemanager.util;

/**
 * Created by Jake on 6/1/2017.
 */

public class Constants {
    public static final int PROGRESSBAR_ELEVATION = 10;
    public static final int NOTIFICATION_ID = 100;
    public static final String GOOGLE_DRIVE_FOLDER_MIME = "application/vnd.google-apps.folder";
    public static final String DIALOG_TITLE_KEY = "DIALOG_TITLE";
    public static final String ERROR_MESSAGE_KEY = "ERROR_MESSAGE";
    public static final String FILE_PATH_KEY = "FILE_PATH";
    public static final java.lang.String NAME_KEY = "NAME_KEY";
    public static final String FRAGMENT_TITLE = "TITLE";

    public class Sources {
        public static final String CALLBACK_URL = "https://google.com";
        public static final String GOOGLE_DRIVE_FOLDER_MIME = "application/vnd.google-apps.folder";
        public static final String DROPBOX = "DROPBOX";
        public static final String LOCAL = "LOCAL";
        public static final String GOOGLE_DRIVE = "GOOGLE DRIVE";
        public static final String ONEDRIVE = "ONEDRIVE";

        public class Keys {
            public static final String DROPBOX_CLIENT_ID       = "rse09cxjnnn2yc1";
        }
    }

    public class RequestCodes {
        public static final int STORAGE_PERMISSIONS = 100;
        public static final int RESOLVE_CONNECTION = 101;
        public static final int COMPLETE_AUTH = 102;
        public static final int GOOGLE_SIGN_IN = 103;
        public static final int GOOGLE_PLAY_SERVICES = 105;
        public static final int ACCOUNTS_PERMISSIONS = 106;
        public static final int ACCOUNT_PICKER = 107;
        public static final int FILE_PICKER = 108;
    }

    public class SharedPrefs {
        public static final String GOOGLE_ACCOUNT_NAME = "GOOGLE_ACCOUNT_NAME";
        public static final String GOOGLE_ACCESS_TOKEN = "googledrive-access-token";
    }
}

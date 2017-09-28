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
        public static final String TUT_SEEN_KEY = "SEEN_TUTORIAL";
    }

    public class Animation {
        public static final int PROGRESS_DURATION = 1200;

    }

    public class ConfigKeys {
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
}

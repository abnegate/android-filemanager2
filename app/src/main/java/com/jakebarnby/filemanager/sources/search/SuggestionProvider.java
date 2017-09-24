package com.jakebarnby.filemanager.sources.search;

import android.content.SearchRecentSuggestionsProvider;

/**
 * Created by Jake on 9/23/2017.
 */

public class SuggestionProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "com.jakebarnby.filemanager.sources.search.SuggestionProvider";
    public final static int MODE = DATABASE_MODE_QUERIES;

    public SuggestionProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}

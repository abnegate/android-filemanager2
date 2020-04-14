package com.jakebarnby.filemanager.sources.googledrive

import android.accounts.AccountManager
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.core.os.bundleOf
import com.google.firebase.analytics.FirebaseAnalytics
import com.jakebarnby.filemanager.models.SourceType
import com.jakebarnby.filemanager.ui.sources.SourceFragment
import com.jakebarnby.filemanager.util.Constants
import com.jakebarnby.filemanager.util.Constants.RequestCodes
import com.jakebarnby.filemanager.util.Constants.Sources
import com.jakebarnby.filemanager.util.Logger

/**
 * Created by Jake on 5/31/2017.
 */
class GoogleDriveFragment : SourceFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter.setFileSource( GoogleDriveSource(presenter))
    }

    override fun onResume() {
        if (!presenter.source.isLoggedIn) {
            (presenter.source as GoogleDriveSource).authGoogleSilent(this)
        }
        super.onResume()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            RequestCodes.GOOGLE_PLAY_SERVICES -> if (resultCode == Activity.RESULT_OK) {
                (presenter.source as GoogleDriveSource).getResultsFromApi(this)
            }
            RequestCodes.GOOGLE_SIGN_IN -> if (resultCode == Activity.RESULT_OK) {
                (presenter.source as GoogleDriveSource).saveUserToken(this)
                Logger.logFirebaseEvent(
                    FirebaseAnalytics.getInstance(context!!),
                    Constants.Analytics.EVENT_LOGIN_GOOGLE_DRIVE)
            }
            RequestCodes.ACCOUNT_PICKER -> if (resultCode == Activity.RESULT_OK && data != null && data.extras != null) {
                val accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                if (accountName != null) {
                    (presenter.source as GoogleDriveSource).saveUserAccount(this, accountName)
                }
            }
        }
    }

    companion object {
        fun newInstance(sourceId: Int): SourceFragment =
            GoogleDriveFragment().apply {
                arguments = bundleOf(
                    Constants.FRAGMENT_TITLE to SourceType.values()[sourceId]
                )
            }
    }
}
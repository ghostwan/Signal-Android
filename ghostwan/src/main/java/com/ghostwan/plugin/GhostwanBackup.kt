package com.ghostwan.plugin

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import java.io.File

@SuppressLint("LogNotSignal")
object GhostwanBackup {

    private const val TAG: String = "Ghostwan"
    private var cloudLayout : View? =null
    private var signInResult : ActivityResultLauncher<Intent>? = null
    private var mDriveServiceHelper:DriveServiceHelper? = null

    fun init(context: Context, view: View, caller: ActivityResultCaller){
        cloudLayout = view.findViewWithTag("fragment_backup_cloud_layout")

        cloudLayout?.setOnClickListener {
            // If is connected display a dialog to ask to disconnect
            // Else display a dialog to choose cloud provider
            requestSignIn(context)
        }

        signInResult = caller.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {result ->
            GoogleSignIn.getSignedInAccountFromIntent(result.data)
                .addOnSuccessListener { googleAccount: GoogleSignInAccount ->
                    // Use the authenticated account to sign in to the Drive service.
                    val credential = GoogleAccountCredential.usingOAuth2(context, setOf(DriveScopes.DRIVE_FILE))
                    credential.selectedAccount = googleAccount.account
                    val googleDriveService = Drive.Builder(
                            AndroidHttp.newCompatibleTransport(),
                            GsonFactory(),
                            credential)
                            .setApplicationName("Signal")
                            .build()

                    // The DriveServiceHelper encapsulates all REST API and SAF functionality.
                    // Its instantiation is required before handling any onClick actions.
                    mDriveServiceHelper = DriveServiceHelper(googleDriveService)
                }
                .addOnFailureListener { exception: Exception? -> Log.e(TAG, "Unable to sign in.", exception) }
        }
    }

    fun gone() {
        cloudLayout?.visibility = View.GONE
    }

    fun visible() {
        cloudLayout?.visibility = View.VISIBLE
    }

    fun destroy() {
        cloudLayout = null
    }

    /**
     * Starts a sign-in activity
     */
    private fun requestSignIn(context: Context) {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(Scope(DriveScopes.DRIVE_FILE))
                .build()
        val client = GoogleSignIn.getClient(context, signInOptions)

        signInResult?.launch(client.signInIntent)
    }

    fun uploadBackup(file: File) {
        mDriveServiceHelper?.createFile(file, "application/octet-stream")
                ?.addOnSuccessListener { fileId: String? ->
                    Log.i(TAG, "upload succeed $fileId")
                }
                ?.addOnFailureListener { exception: java.lang.Exception? -> Log.e(TAG, "Couldn't create file.", exception) }
    }
}
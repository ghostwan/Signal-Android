package com.ghostwan.plugin

import android.content.Context
import android.view.View
import android.widget.Toast

object GhostwanBackup {

    private var cloudLayout : View? =null

    fun init(context: Context, view: View){
        cloudLayout = view.findViewWithTag("fragment_backup_cloud_layout")

        cloudLayout?.setOnClickListener {
            Toast.makeText(context, "Test", Toast.LENGTH_SHORT).show()
        }
    }

    fun gone() {
        cloudLayout?.visibility = View.GONE
    }

    fun visible() {
        cloudLayout?.visibility = View.VISIBLE
    }

    private fun onConnectCloud() { //ghostwan
        // If is connected display a dialog to ask to disconnect
        // Else display a dialog to choose cloud provider
    }

    fun destroy() {
        cloudLayout = null
    }

}
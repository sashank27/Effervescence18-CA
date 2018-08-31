package org.effervescence.app18.ca

import android.app.Application
import com.cloudinary.android.MediaManager
import timber.log.Timber

class EffervescenceCA : Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())

        val config = HashMap<String, String>()
        config["cloud_name"] = "djvuib63r"
        MediaManager.init(this, config)
    }
}

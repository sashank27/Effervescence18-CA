package org.effervescence.app18.ca

import android.app.Application
import timber.log.Timber

class EffervescenceCA : Application() {

    companion object {
        val BASE_URL = "https://cb595495.ngrok.io"  //Base url for the API Server
    }

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}

package org.effervescence.app18.ca

import android.app.Application
import timber.log.Timber

class EffervescenceCA : Application() {

    companion object {
        val BASE_URL = "https://7a51c3e3.ngrok.io"  //Base url for the API's Server
    }

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}

package pnu.cse.onionmarket.preference

import android.app.Application

class PreferenceApplication: Application() {
    companion object {
        lateinit var prefs: PreferenceUtil
    }

    override fun onCreate() {
        prefs = PreferenceUtil(applicationContext)
        super.onCreate()
    }
}
package com.dagmawiasegid.lunchbox

import android.app.Application
import com.dagmawiasegid.lunchbox.util.DealsNotifier
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.osmdroid.config.Configuration

class LunchBoxApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.USE_FIREBASE_EMULATOR) {
            // Local dev/testing only: point the Firebase SDKs at the
            // Firebase Local Emulator Suite running on the host machine
            // instead of the real project. 10.0.2.2 is the Android
            // emulator's alias for the host's localhost.
            FirebaseAuth.getInstance().useEmulator("10.0.2.2", 9099)
            FirebaseFirestore.getInstance().useEmulator("10.0.2.2", 8081)
        }

        // OSM's tile usage policy requires a distinctive user agent and a
        // writable cache directory — without this, tile requests can get
        // silently rate-limited/blocked.
        Configuration.getInstance().load(
            this,
            android.preference.PreferenceManager.getDefaultSharedPreferences(this)
        )
        Configuration.getInstance().userAgentValue = packageName

        DealsNotifier.createChannel(this)
    }
}

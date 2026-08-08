package com.dagmawiasegid.lunchbox

import android.app.Application
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LunchBoxApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            // Local dev/demo build: point the Firebase SDKs at the Firebase
            // Local Emulator Suite running on the host machine instead of a
            // real project. 10.0.2.2 is the Android emulator's alias for
            // the host's localhost.
            FirebaseAuth.getInstance().useEmulator("10.0.2.2", 9099)
            FirebaseFirestore.getInstance().useEmulator("10.0.2.2", 8081)
        }
    }
}

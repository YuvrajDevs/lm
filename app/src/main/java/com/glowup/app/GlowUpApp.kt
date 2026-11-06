package com.glowup.app

import android.app.Application
import com.glowup.app.notify.AlarmScheduler
import com.glowup.app.notify.Notifications
import com.glowup.app.ui.data.FocusStore
import com.glowup.app.ui.data.Storage
import com.glowup.app.ui.data.Stores

class GlowUpApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Storage.init(this)
        Stores.loadAll()
        FocusStore.finishIfDue()
        Notifications.createChannels(this)
        Storage.onScheduleChanged = { AlarmScheduler.syncAll(this) }
        AlarmScheduler.syncAll(this)
    }
}

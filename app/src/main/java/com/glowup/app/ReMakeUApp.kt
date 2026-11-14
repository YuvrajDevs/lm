package com.glowup.app

import android.app.Application
import com.glowup.app.notify.AlarmScheduler
import com.glowup.app.notify.Notifications
import com.glowup.app.ui.data.FocusStore
import com.glowup.app.ui.data.Storage
import com.glowup.app.ui.data.Stores
import com.glowup.app.billing.Billing
import com.glowup.app.blocker.Blocker
import com.glowup.app.ui.data.PhotoVault
import com.glowup.app.widget.Widgets

class ReMakeUApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Storage.init(this)
        PhotoVault.init(noBackupFilesDir)
        Stores.loadAll()
        FocusStore.finishIfDue()
        Billing.start(this)
        Notifications.createChannels(this)
        Storage.onScheduleChanged = {
            AlarmScheduler.syncAll(this)
            Blocker.sync(this)
        }
        Storage.onDataChanged = { Widgets.requestUpdate(this) }
        AlarmScheduler.syncAll(this)
        Blocker.sync(this)
    }
}

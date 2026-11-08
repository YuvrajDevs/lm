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
        com.glowup.app.ui.data.PhotoVault.init(noBackupFilesDir)
        Stores.loadAll()
        FocusStore.finishIfDue()
        com.glowup.app.billing.Billing.start(this)
        Notifications.createChannels(this)
        Storage.onScheduleChanged = {
            AlarmScheduler.syncAll(this)
            com.glowup.app.blocker.Blocker.sync(this)
        }
        Storage.onDataChanged = { com.glowup.app.widget.Widgets.requestUpdate(this) }
        AlarmScheduler.syncAll(this)
        com.glowup.app.blocker.Blocker.sync(this)
    }
}

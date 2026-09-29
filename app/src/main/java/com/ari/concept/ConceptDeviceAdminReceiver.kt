package com.ari.concept

import android.app.admin.DeviceAdminReceiver
import android.content.ComponentName
import android.content.Context

class ConceptDeviceAdminReceiver : DeviceAdminReceiver() {
    override fun onEnabled(context: Context, intent: android.content.Intent) {
        val dpm = getManager(context)
        val admin = ComponentName(context, ConceptDeviceAdminReceiver::class.java)
        if (dpm.isDeviceOwnerApp(context.packageName)) {
            runCatching { dpm.setUninstallBlocked(admin, context.packageName, true) }
        }
    }
}

package com.ari.concept

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent

class ConceptDeviceAdminReceiver : DeviceAdminReceiver() {
    override fun onEnabled(context: Context, intent: Intent) {
        applyProtection(context)
    }

    companion object {
        fun applyProtection(context: Context) {
            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val admin = ComponentName(context, ConceptDeviceAdminReceiver::class.java)
            if (!dpm.isDeviceOwnerApp(context.packageName)) return

            runCatching { dpm.setUninstallBlocked(admin, context.packageName, true) }

            runCatching {
                dpm.setLockTaskPackages(admin, arrayOf(context.packageName))
            }

            runCatching {
                dpm.setLockTaskFeatures(admin, DevicePolicyManager.LOCK_TASK_FEATURE_NONE)
            }
        }
    }
}

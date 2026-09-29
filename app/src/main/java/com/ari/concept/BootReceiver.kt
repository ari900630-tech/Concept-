package com.ari.concept

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return

        val prefs = context.getSharedPreferences("concept_protection", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("enabled", false)) return

        val launch = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        runCatching { context.startActivity(launch) }

        val dpm = context.getSystemService(android.app.admin.DevicePolicyManager::class.java)
        val admin = android.content.ComponentName(context, ConceptDeviceAdminReceiver::class.java)
        if (dpm.isDeviceOwnerApp(context.packageName)) {
            runCatching { dpm.setLockTaskPackages(admin, arrayOf(context.packageName)) }
            runCatching { dpm.setLockTaskFeatures(admin, android.app.admin.DevicePolicyManager.LOCK_TASK_FEATURE_NONE) }
        }
    }
}

package com.ari.concept

import android.accessibilityservice.AccessibilityService
import android.graphics.Color
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView

class AppBlockAccessibilityService : AccessibilityService() {
    private var windowManager: WindowManager? = null
    private var gateView: View? = null
    private var uninstallView: View? = null
    private var uninstallApproved = false

    override fun onServiceConnected() {
        super.onServiceConnected()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onAccessibilityEvent(event: android.view.accessibility.AccessibilityEvent?) {
        val pkg = event?.packageName?.toString() ?: return
        if (pkg != "com.android.vending") hidePlayGate()
        if (pkg == "com.android.vending") {\n            val prefs = getSharedPreferences("play_gate", MODE_PRIVATE)\n            if (prefs.getBoolean("active", false)) showPlayGate()\n        }

        if (pkg == "com.android.vending") {
            val prefs = getSharedPreferences("play_gate", MODE_PRIVATE)
            if (prefs.getBoolean("active", false) && prefs.getString("target_pkg", "").orEmpty().isNotBlank()) {
                showPlayGate()
                if (prefs.getBoolean("install_requested", false)) clickPlayInstallButton()
            }
        }

        if (pkg == "com.android.settings" || pkg == "com.android.packageinstaller" || pkg == "com.google.android.packageinstaller" || pkg == "com.google.android.permissioncontroller") {
            val root = windows.firstOrNull { it.root?.packageName?.toString() == pkg }?.root
            if (root != null && containsUninstallRequest(root)) {\n                if (!uninstallApproved) showUninstallPassword()\n            }
        }

        if (pkg != packageName && getSharedPreferences("blocked", MODE_PRIVATE).getBoolean(pkg, false)) {
            performGlobalAction(GLOBAL_ACTION_HOME)
        }
    }

    private fun containsUninstallRequest(root: AccessibilityNodeInfo): Boolean {
        val text = rootText(root).lowercase()
        return ("הסר התקנה" in text || "uninstall" in text) &&
            (text.contains("concept") || text.contains("אפליקציה"))
    }

    private fun rootText(node: AccessibilityNodeInfo): String {
        val out = StringBuilder()
        fun walk(n: AccessibilityNodeInfo) {
            n.text?.let { out.append(' ').append(it) }
            n.contentDescription?.let { out.append(' ').append(it) }
            for (i in 0 until n.childCount) n.getChild(i)?.let { walk(it) }
        }
        walk(node)
        return out.toString()
    }

    private fun clickPlayInstallButton() {
        val root = windows.firstOrNull { it.root?.packageName?.toString() == "com.android.vending" }?.root ?: return
        val labels = listOf("התקנה", "התקן", "עדכון", "עדכן", "Install", "Update")
        var found: AccessibilityNodeInfo? = null
        fun search(node: AccessibilityNodeInfo) {
            if (found != null) return
            val s = (node.text?.toString() ?: "") + " " + (node.contentDescription?.toString() ?: "")
            if (labels.any { s.contains(it, ignoreCase = true) }) {
                found = if (node.isClickable) node else node.parent
                return
            }
            for (i in 0 until node.childCount) node.getChild(i)?.let { search(it) }
        }
        search(root)
        if (found?.performAction(AccessibilityNodeInfo.ACTION_CLICK) == true) {
            getSharedPreferences("play_gate", MODE_PRIVATE).edit().putBoolean("install_requested", false).apply()
        }
    }

    private fun showPlayGate() {
        if (gateView != null || windowManager == null) return
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.TRANSPARENT)
            isClickable = true
            isFocusable = true
        }
        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(16, 20, 16, 12)
            setBackgroundColor(0xEEFFFFFF.toInt())
        }
        val install = Button(this).apply {
            text = "התקן / עדכן"
            setOnClickListener {
                getSharedPreferences("play_gate", MODE_PRIVATE).edit().putBoolean("install_requested", true).apply()
                clickPlayInstallButton()
            }
        }
        val back = Button(this).apply {
            text = "חזור"
            setOnClickListener {
                performGlobalAction(GLOBAL_ACTION_BACK)
                hidePlayGate()
                getSharedPreferences("play_gate", MODE_PRIVATE).edit().clear().apply()
            }
        }
        bar.addView(install, LinearLayout.LayoutParams(0, 60, 1f))
        bar.addView(back, LinearLayout.LayoutParams(0, 60, 1f))
        root.addView(bar, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        root.addView(View(this), LinearLayout.LayoutParams.MATCH_PARENT, 0)
        gateView = root
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START
        windowManager?.addView(root, params)
    }

    private fun hidePlayGate() {
        gateView?.let { runCatching { windowManager?.removeView(it) } }
        gateView = null
    }

    private fun showUninstallPassword() {
        if (uninstallView != null || windowManager == null) return
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 32, 32, 32)
            setBackgroundColor(0xF5FFFFFF.toInt())
        }
        val title = TextView(this).apply {
            text = "הסרת האפליקציה חסומה"
            textSize = 22f
            gravity = Gravity.CENTER
        }
        val input = EditText(this).apply {
            hint = "סיסמה"
            inputType = 2
        }
        val button = Button(this).apply {
            text = "אישור"
            setOnClickListener {
                if (input.text.toString() == "0548425631") {
                    hideUninstallPassword()
                    val dpm = getSystemService(DEVICE_POLICY_SERVICE) as android.app.admin.DevicePolicyManager
                    val admin = android.content.ComponentName(this@AppBlockAccessibilityService, ConceptDeviceAdminReceiver::class.java)
                    if (dpm.isAdminActive(admin) && !dpm.isDeviceOwnerApp(packageName)) dpm.removeActiveAdmin(admin)
                } else {
                    input.text.clear()
                    input.error = "סיסמה שגויה"
                }
            }
        }
        root.addView(title, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        root.addView(input, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        root.addView(button, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        uninstallView = root
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.CENTER
        windowManager?.addView(root, params)
        input.requestFocus()
    }

    private fun hideUninstallPassword() {
        uninstallView?.let { runCatching { windowManager?.removeView(it) } }
        uninstallView = null
    }

    override fun onInterrupt() {
        hidePlayGate()
        hideUninstallPassword()
    }

    override fun onDestroy() {
        hidePlayGate()
        hideUninstallPassword()
        super.onDestroy()
    }
}

package com.ari.concept

import android.accessibilityservice.AccessibilityService
import android.graphics.PixelFormat
import android.graphics.Rect
import android.net.Uri
import android.content.Intent
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
    private var uninstallView: View? = null
    private var uninstallApproved = false
    private var disableApproved = false
    private val chromeGuards = mutableListOf<View>()

    override fun onServiceConnected() {
        super.onServiceConnected()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onAccessibilityEvent(event: android.view.accessibility.AccessibilityEvent?) {
        val pkg = event?.packageName?.toString() ?: return
        val root = event?.source
            ?: windows.firstOrNull { it.root?.packageName?.toString() == pkg }?.root

        if (pkg == "com.android.settings" ||
            pkg == "com.android.packageinstaller" ||
            pkg == "com.google.android.packageinstaller" ||
            pkg == "com.google.android.permissioncontroller"
        ) {
            if (root != null && !uninstallApproved && containsUninstallRequest(root)) {
                showUninstallPassword("הסרת האפליקציה חסומה")
            }
            if (root != null && !disableApproved && containsDisableRequest(root)) {
                showUninstallPassword("השבתת ההגנה חסומה")
            }
        }

        if (pkg == "com.android.chrome") {
            enforceChromeLock(root)
        } else {
            clearChromeGuards()
        }

        if (pkg != packageName &&
            pkg != "com.android.chrome" &&
            getSharedPreferences("blocked", MODE_PRIVATE).getBoolean(pkg, false)
        ) {
            performGlobalAction(GLOBAL_ACTION_HOME)
        }
    }

    private fun enforceChromeLock(root: AccessibilityNodeInfo?) {
        val prefs = getSharedPreferences("chrome_lock", MODE_PRIVATE)
        if (!prefs.getBoolean("enabled", false)) {
            performGlobalAction(GLOBAL_ACTION_HOME)
            return
        }
        if (root == null) return

        val allowedHost = prefs.getString("allowed_host", null)
        val currentUrl = findUrlText(root)
        if (allowedHost != null && currentUrl != null) {
            val currentHost = runCatching { Uri.parse(currentUrl).host }.getOrNull()
            if (currentHost != null && !currentHost.equals(allowedHost, ignoreCase = true)) {
                val allowedUrl = prefs.getString("allowed_url", null)
                if (!allowedUrl.isNullOrBlank()) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(allowedUrl)).apply {
                        setPackage("com.android.chrome")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    })
                }
                return
            }
        }

        clearChromeGuards()
        findNodes(root).forEach { node ->
            val label = ((node.contentDescription ?: "") .toString() + " " + (node.text ?: "")).lowercase()
            val blockedControl = listOf(
                "home", "דף הבית", "new tab", "new incognito tab",
                "כרטיסייה חדשה", "כרטיסיה חדשה", "גלישה בסתר",
                "search or type web address", "חיפוש או הקלדת כתובת",
                "כתובת אתר", "כתובת"
            ).any { it in label }
            if (blockedControl) {
                val bounds = Rect()
                node.getBoundsInScreen(bounds)
                if (bounds.width() > 0 && bounds.height() > 0) addChromeGuard(bounds)
            }
        }
    }

    private fun findUrlText(root: AccessibilityNodeInfo): String? {
        var result: String? = null
        fun walk(n: AccessibilityNodeInfo) {
            if (result != null) return
            val text = n.text?.toString()?.trim()
            val desc = n.contentDescription?.toString()?.trim()
            val value = text ?: desc
            if (!value.isNullOrBlank() && (
                value.startsWith("http://") || value.startsWith("https://") ||
                value.contains("www.") || value.contains(".co.il")
            )) {
                result = value
                return
            }
            for (i in 0 until n.childCount) {
                n.getChild(i)?.let { walk(it) }
            }
        }
        walk(root)
        return result
    }

    private fun findNodes(root: AccessibilityNodeInfo): List<AccessibilityNodeInfo> {
        val result = mutableListOf<AccessibilityNodeInfo>()
        fun walk(n: AccessibilityNodeInfo) {
            result.add(n)
            for (i in 0 until n.childCount) n.getChild(i)?.let { walk(it) }
        }
        walk(root)
        return result
    }

    private fun addChromeGuard(bounds: Rect) {
        val wm = windowManager ?: return
        val guard = View(this).apply {
            isClickable = true
            isFocusable = false
            setOnClickListener { }
            alpha = 0.01f
        }
        val params = WindowManager.LayoutParams(
            bounds.width(),
            bounds.height(),
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START
        params.x = bounds.left
        params.y = bounds.top
        runCatching { wm.addView(guard, params); chromeGuards.add(guard) }
    }

    private fun clearChromeGuards() {
        val wm = windowManager ?: return
        chromeGuards.forEach { runCatching { wm.removeView(it) } }
        chromeGuards.clear()
    }

    private fun containsDisableRequest(root: AccessibilityNodeInfo): Boolean {
        val text = rootText(root).lowercase()
        return ("השבת" in text || "ביטול הפעלה" in text || "disable" in text || "deactivate" in text) &&
            ("concept" in text || "מנהל מכשיר" in text || "device admin" in text || "device administrator" in text)
    }

    private fun containsUninstallRequest(root: AccessibilityNodeInfo): Boolean {
        val text = rootText(root).lowercase()
        val appLabel = applicationInfo.loadLabel(packageManager).toString().lowercase()
        val uninstall = "הסר התקנה" in text ||
            "הסר את ההתקנה" in text ||
            "uninstall" in text ||
            "remove app" in text
        return uninstall && (appLabel in text || "concept" in text)
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

    private fun showUninstallPassword(titleText: String) {
        if (uninstallView != null || windowManager == null) return
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 32, 32, 32)
            setBackgroundColor(0xF5FFFFFF.toInt())
            isClickable = true
            isFocusable = true
        }
        val title = TextView(this).apply {
            text = titleText
            textSize = 22f
            gravity = Gravity.CENTER
        }
        val input = EditText(this).apply {
            hint = "הזן סיסמה"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            gravity = Gravity.CENTER
        }
        val button = Button(this).apply {
            text = "אישור"
            setOnClickListener {
                val savedPassword = getSharedPreferences("concept_security", MODE_PRIVATE)
                    .getString("login_password", "") ?: ""
                if (savedPassword.isNotBlank() && input.text.toString() == savedPassword) {
                    uninstallApproved = true
                    disableApproved = true
                    hideUninstallPassword()
                    performGlobalAction(GLOBAL_ACTION_BACK)
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
        hideUninstallPassword()
        clearChromeGuards()
    }

    override fun onDestroy() {
        hideUninstallPassword()
        clearChromeGuards()
        super.onDestroy()
    }
}

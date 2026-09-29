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
    private var uninstallView: View? = null
    private var uninstallApproved = false

    override fun onServiceConnected() {
        super.onServiceConnected()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onAccessibilityEvent(event: android.view.accessibility.AccessibilityEvent?) {
        val pkg = event?.packageName?.toString() ?: return

        if (pkg == "com.android.settings" ||
            pkg == "com.android.packageinstaller" ||
            pkg == "com.google.android.packageinstaller" ||
            pkg == "com.google.android.permissioncontroller"
        ) {
            val root = windows.firstOrNull { it.root?.packageName?.toString() == pkg }?.root
            if (root != null && containsUninstallRequest(root) && !uninstallApproved) {
                showUninstallPassword()
            }
        }

        if (pkg != packageName &&
            getSharedPreferences("blocked", MODE_PRIVATE).getBoolean(pkg, false)
        ) {
            performGlobalAction(GLOBAL_ACTION_HOME)
        }
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
            for (i in 0 until n.childCount) {
                n.getChild(i)?.let { walk(it) }
            }
        }
        walk(node)
        return out.toString()
    }

    private fun showUninstallPassword() {
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
            text = "הסרת האפליקציה חסומה"
            textSize = 22f
            gravity = Gravity.CENTER
        }

        val input = EditText(this).apply {
            hint = "הזן סיסמה"
            inputType = 2
            gravity = Gravity.CENTER
        }

        val button = Button(this).apply {
            text = "אישור"
            setOnClickListener {
                if (input.text.toString() == "0548425631") {
                    uninstallApproved = true
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
    }

    override fun onDestroy() {
        hideUninstallPassword()
        super.onDestroy()
    }
}

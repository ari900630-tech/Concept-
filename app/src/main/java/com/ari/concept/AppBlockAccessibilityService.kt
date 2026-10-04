package com.ari.concept

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.Path
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.content.Intent
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityNodeInfo
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.FrameLayout
import android.widget.TextView

class AppBlockAccessibilityService : AccessibilityService() {
    private var windowManager: WindowManager? = null
    private var uninstallView: View? = null
    private var playControlsView: View? = null
    private var homeBlockView: View? = null
    private var blockedAppView: View? = null
    private var homePackage: String? = null
    private var uninstallApproved = false
    private var disableApproved = false
    private var lastPlayPackage = ""
    private var playInstallButton: Button? = null
    private var playUpdateButton: Button? = null
    private var playInstallInProgress = false
    private var playUpdateInProgress = false
    private var lastPlayReopenAt = 0L
    private val chromeGuards = mutableListOf<View>()
    private val mainHandler = Handler(Looper.getMainLooper())
    private val hidePlayRunnable = Runnable { hidePlayControls() }

    override fun onServiceConnected() {
        super.onServiceConnected()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        homePackage = runCatching {
            packageManager.resolveActivity(
                Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME),
                0
            )?.activityInfo?.packageName
        }.getOrNull()
    }

    override fun onAccessibilityEvent(event: android.view.accessibility.AccessibilityEvent?) {
        val pkg = event?.packageName?.toString() ?: return
        val root = event?.source
            ?: windows.firstOrNull { it.root?.packageName?.toString() == pkg }?.root

        // While the Play Store gate is active, prevent leaving Play Store with
        // Home, Recents, notification/system UI, or another app. The four
        // overlay controls remain the only way to navigate away.
        val playGateActive = getSharedPreferences("play_gate", MODE_PRIVATE)
            .getBoolean("active", false)
        if (playGateActive && pkg != "com.android.vending" && pkg != packageName) {
            reopenGatedPlayStore()
            return
        }

        val siteLockActive = getSharedPreferences("chrome_lock", MODE_PRIVATE)
            .getBoolean("enabled", false)
        if (isHomePackage(pkg)) {
            hideBlockedAppBlock()
            if (siteLockActive) {
                showHomeBlock()
                return
            } else {
                hideHomeBlock()
            }
        } else {
            hideHomeBlock()
        }

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

        if (pkg == "com.android.vending") {
            mainHandler.removeCallbacks(hidePlayRunnable)
            showPlayControls()
            if (root != null) {
                updatePlayTargetFromPage(root)
                updatePlayInstallState(root)
            }
        } else if (playControlsView != null) {
            mainHandler.removeCallbacks(hidePlayRunnable)
            mainHandler.postDelayed(hidePlayRunnable, 900)
        }

        if (pkg == "com.android.chrome") {
            enforceChromeLock(root)
        } else {
            clearChromeGuards()
        }

        if (isBlockedPackage(pkg)) {
            ejectBlockedAppAndClearRecent(pkg)
            return
        }
    }

    private val alwaysBlockedPackages = setOf(
        "com.android.vending",
        "com.android.chrome",
        "com.google.android.youtube",
        "com.instagram.android",
        "org.telegram.messenger",
        "com.zhiliaoapp.musically",
        "com.facebook.katana",
        "com.facebook.orca",
        "com.twitter.android",
        "com.snapchat.android",
        "com.google.android.googlequicksearchbox",
        "com.google.android.apps.youtube.music",
        "com.reddit.frontpage",
        "com.discord",
        "tv.twitch.android.app",
        "com.spotify.music",
        "com.pinterest",
        "com.instagram.barcelona",
        "com.linkedin.android",
        "org.mozilla.firefox",
        "com.microsoft.emmx",
        "com.brave.browser",
        "com.sec.android.app.sbrowser"
    )

    private fun isBlockedPackage(pkg: String): Boolean {
        if (pkg.isBlank() || pkg == packageName) return false
        // Play Store is allowed only while the controlled Play gate is active.
        if (pkg == "com.android.vending") {
            return !getSharedPreferences("play_gate", MODE_PRIVATE)
                .getBoolean("active", false)
        }
        if (pkg in alwaysBlockedPackages) return true
        return getSharedPreferences("blocked", MODE_PRIVATE).getBoolean(pkg, false)
    }

    private fun reopenGatedPlayStore() {
        val target = getSharedPreferences("play_gate", MODE_PRIVATE)
            .getString("target_pkg", "")?.trim().orEmpty()
        if (target.isBlank()) return

        val now = android.os.SystemClock.elapsedRealtime()
        if (now - lastPlayReopenAt < 1500L) return
        lastPlayReopenAt = now
        mainHandler.removeCallbacks(hidePlayRunnable)
        mainHandler.post {
            runCatching {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=$target")
                    ).setPackage("com.android.vending")
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                )
            }
        }
    }
    private fun showPlayControls() {
        if (playControlsView != null || windowManager == null) return
        val prefs = getSharedPreferences("play_gate", MODE_PRIVATE)
        if (!prefs.getBoolean("active", false)) return

        val target = prefs.getString("target_pkg", "").orEmpty()
        if (target.isBlank()) return
        lastPlayPackage = target

        val overlay = FrameLayout(this).apply {
            isClickable = false
            isFocusable = false
            setBackgroundColor(Color.TRANSPARENT)
        }

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(10, 8, 10, 8)
            setBackgroundColor(0xF2FFFFFF.toInt())
        }

        playInstallButton = addPlayButton(row, "התקן") {
            if (playInstallInProgress) {
                clickPlayAction(listOf("ביטול", "בטל", "Cancel"), target)
                playInstallInProgress = false
                setPlayInstallButton(false)
            } else {
                playInstallInProgress = true
                setPlayInstallButton(true)
                clickPlayAction(listOf("התקנה", "התקן", "Install", "קבל", "Get", "Download", "הורד"), target)
            }
        }
        playUpdateButton = addPlayButton(row, "עדכן") {
            if (playUpdateInProgress) {
                clickPlayAction(listOf("ביטול", "בטל", "Cancel"), target)
                playUpdateInProgress = false
                setPlayUpdateButton(false)
            } else {
                playUpdateInProgress = true
                setPlayUpdateButton(true)
                clickPlayAction(listOf("עדכון", "עדכן", "Update"), target)
            }
        }
        addPlayButton(row, "הסר") { requestUninstall(target) }
        addPlayButton(row, "חזור") {
            getSharedPreferences("play_gate", MODE_PRIVATE).edit().putBoolean("active", false).apply()
            mainHandler.removeCallbacks(hidePlayRunnable)
            hidePlayControls()
            performGlobalAction(GLOBAL_ACTION_BACK)
        }

        overlay.addView(
            row,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            ).apply {
                bottomMargin = 0
            }
        )

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START

        runCatching {
            windowManager?.addView(overlay, params)
            playControlsView = overlay
        }
    }

    private fun addPlayButton(parent: LinearLayout, text: String, action: () -> Unit): Button {
        val button = Button(this).apply {
            this.text = text
            isAllCaps = false
            setTextColor(Color.WHITE)
            background = GradientDrawable().apply {
                setColor(Color.rgb(25, 103, 210))
                cornerRadius = 32f
            }
            // אפקט לחיצה לבן חל רק בארבעת כפתורי חנות Google Play.
            setOnTouchListener { v, event ->
                val button = v as Button
                when (event.actionMasked) {
                    android.view.MotionEvent.ACTION_DOWN -> {
                        button.background = GradientDrawable().apply {
                            setColor(Color.WHITE)
                            cornerRadius = 32f
                        }
                        button.setTextColor(Color.rgb(25, 103, 210))
                        button.invalidate()
                    }
                    android.view.MotionEvent.ACTION_UP,
                    android.view.MotionEvent.ACTION_CANCEL -> {
                        button.background = GradientDrawable().apply {
                            setColor(Color.rgb(25, 103, 210))
                            cornerRadius = 32f
                        }
                        button.setTextColor(Color.WHITE)
                        button.invalidate()
                    }
                }
                false
            }
            setOnClickListener { action() }
        }
        parent.addView(
            button,
            LinearLayout.LayoutParams(0, 52.dp(), 1f).apply {
                setMargins(5, 0, 5, 0)
            }
        )
        return button
    }

    private fun setPlayInstallButton(inProgress: Boolean) {
        playInstallButton?.let { it.text = if (inProgress) "בטל" else "התקן" }
    }

    private fun setPlayUpdateButton(inProgress: Boolean) {
        playUpdateButton?.let { it.text = if (inProgress) "בטל" else "עדכן" }
    }

    private fun updatePlayInstallState(root: AccessibilityNodeInfo) {
        val text = rootText(root).lowercase()
        val installing = listOf("מתקין", "מוריד", "ממתין", "installing", "downloading", "pending").any { it in text }
        val updating = listOf("מעדכן", "updating", "update").any { it in text }
        if (playInstallInProgress || installing) {
            playInstallInProgress = true
            setPlayInstallButton(true)
        }
        if (playUpdateInProgress || updating) {
            playUpdateInProgress = true
            setPlayUpdateButton(true)
        }
    }

    private fun Int.dp(): Int = (this * resources.displayMetrics.density).toInt()

    private fun updatePlayTargetFromPage(root: AccessibilityNodeInfo) {
        val prefs = getSharedPreferences("play_gate", MODE_PRIVATE)
        if (prefs.getString("target_pkg", "").isNullOrBlank() && lastPlayPackage.isNotBlank()) {
            prefs.edit().putString("target_pkg", lastPlayPackage).apply()
        }
    }

    private fun clickPlayAction(labels: List<String>, target: String) {
        mainHandler.postDelayed({
            val root = getRootInActiveWindow()
                ?: windows.firstOrNull { it.root?.packageName?.toString() == "com.android.vending" }?.root
                ?: return@postDelayed
            if (root.packageName?.toString() != "com.android.vending") return@postDelayed

            val wanted = labels.map { it.trim().lowercase() }.filter { it.isNotEmpty() }
            val candidates = mutableListOf<AccessibilityNodeInfo>()

            fun walk(node: AccessibilityNodeInfo) {
                val text = (node.text?.toString().orEmpty() + " " +
                    node.contentDescription?.toString().orEmpty()).trim().lowercase()
                if (wanted.any { text == it || text.contains(it) }) candidates.add(node)
                for (i in 0 until node.childCount) node.getChild(i)?.let(::walk)
            }
            walk(root)

            fun clickableAncestor(start: AccessibilityNodeInfo): AccessibilityNodeInfo? {
                var current: AccessibilityNodeInfo? = start
                repeat(15) {
                    if (current?.isClickable == true) return current
                    current = current?.parent
                }
                return null
            }

            var actionNode: AccessibilityNodeInfo? = candidates.firstOrNull { it.isClickable }
            if (actionNode == null) {
                for (candidate in candidates) {
                    actionNode = clickableAncestor(candidate)
                    if (actionNode != null) break
                }
            }

            if (actionNode == null) {
                fun findClickableDescendant(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
                    if (node.isClickable) return node
                    for (i in 0 until node.childCount) {
                        node.getChild(i)?.let {
                            val found = findClickableDescendant(it)
                            if (found != null) return found
                        }
                    }
                    return null
                }
                for (candidate in candidates) {
                    actionNode = findClickableDescendant(candidate)
                    if (actionNode != null) break
                }
            }

            val node = actionNode ?: return@postDelayed
            if (node.performAction(AccessibilityNodeInfo.ACTION_CLICK)) return@postDelayed

            val bounds = Rect()
            node.getBoundsInScreen(bounds)
            if (bounds.width() > 0 && bounds.height() > 0) {
                val x = bounds.centerX().toFloat()
                val y = bounds.centerY().toFloat()
                val path = Path().apply {
                    moveTo(x, y)
                    lineTo(x + 2f, y + 2f)
                }
                val gesture = GestureDescription.Builder()
                    .addStroke(GestureDescription.StrokeDescription(path, 0, 120))
                    .build()
                runCatching {
                    dispatchGesture(gesture, object : GestureResultCallback() {}, null)
                }
            }
        }, 180L)
    }

    private fun findNodeByLabels(root: AccessibilityNodeInfo, labels: List<String>): AccessibilityNodeInfo? {
        val wanted = labels.map { it.lowercase() }
        fun walk(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
            val text = (node.text?.toString().orEmpty() + " " + node.contentDescription?.toString().orEmpty()).lowercase()
            if (wanted.any { text == it || text.contains(it) }) return node
            for (i in 0 until node.childCount) node.getChild(i)?.let { child ->
                val found = walk(child)
                if (found != null) return found
            }
            return null
        }
        return walk(root)
    }

    private fun requestUninstall(pkg: String) {
        val savedPassword = getSharedPreferences("concept_security", MODE_PRIVATE)
            .getString("login_password", "").orEmpty()
        if (savedPassword.isBlank()) {
            startActivity(Intent(Intent.ACTION_DELETE, Uri.parse("package:$pkg")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } else {
            showUninstallPassword("נדרשת סיסמה להסרה")
        }
    }

    private fun hidePlayControls() {
        playControlsView?.let { runCatching { windowManager?.removeView(it) } }
        playControlsView = null
        playInstallButton = null
        playUpdateButton = null
        playInstallInProgress = false
        playUpdateInProgress = false
    }

    private fun ejectBlockedAppAndClearRecent(pkg: String) {
        hideBlockedAppBlock()
        performGlobalAction(GLOBAL_ACTION_HOME)
        mainHandler.postDelayed({
            performGlobalAction(GLOBAL_ACTION_RECENTS)
            mainHandler.postDelayed({ removeBlockedTaskFromRecents(pkg) }, 350)
        }, 300)
    }

    private fun removeBlockedTaskFromRecents(pkg: String) {
        val label = runCatching { packageManager.getApplicationInfo(pkg, 0).loadLabel(packageManager).toString() }.getOrDefault(pkg)
        val lowered = label.lowercase()
        var boundsToDismiss: Rect? = null

        windows.forEach { window ->
            val root = window.root ?: return@forEach
            fun walk(node: AccessibilityNodeInfo) {
                if (boundsToDismiss != null) return
                val nodePkg = node.packageName?.toString().orEmpty()
                val text = (node.text?.toString().orEmpty() + " " + node.contentDescription?.toString().orEmpty()).lowercase()
                if (node.isVisibleToUser && (nodePkg == pkg || text.contains(lowered))) {
                    val bounds = Rect()
                    node.getBoundsInScreen(bounds)
                    if (bounds.width() > 140 && bounds.height() > 120) {
                        boundsToDismiss = bounds
                        return
                    }
                }
                for (i in 0 until node.childCount) node.getChild(i)?.let { walk(it) }
            }
            walk(root)
        }

        val bounds = boundsToDismiss
        if (bounds != null) {
            val startX = bounds.centerX().toFloat()
            val startY = (bounds.bottom - 80).coerceAtLeast(bounds.top + 80).toFloat()
            val endY = (bounds.top - 120).toFloat()
            val path = Path().apply { moveTo(startX, startY); lineTo(startX, endY) }
            val gesture = GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(path, 0, 350))
                .build()
            runCatching { dispatchGesture(gesture, object : GestureResultCallback() {}, null) }
        }

        mainHandler.postDelayed({
            performGlobalAction(GLOBAL_ACTION_HOME)
            mainHandler.postDelayed({ showBlockedAppBlock() }, 180)
        }, 520)
    }

    private fun showBlockedAppBlock() {
        if (blockedAppView != null || windowManager == null) return
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(40, 40, 40, 40)
            setBackgroundColor(0xF5FFFFFF.toInt())
            isClickable = true
            isFocusable = true
        }
        val title = TextView(this).apply { text = "חסום"; textSize = 30f; gravity = Gravity.CENTER }
        val message = TextView(this).apply { text = "הגישה לאפליקציה הזו חסומה"; textSize = 18f; gravity = Gravity.CENTER; setPadding(0, 12, 0, 24) }
        val backApp = Button(this).apply {
            text = "חזור לאפליקציה"
            isAllCaps = false
            setOnClickListener {
                hideHomeBlock()
                startActivity(Intent(this@AppBlockAccessibilityService, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                })
            }
        }
        root.addView(title, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        root.addView(message, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        root.addView(backApp, LinearLayout.LayoutParams.MATCH_PARENT, 56.dp())
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.CENTER
        runCatching { windowManager?.addView(root, params); blockedAppView = root }
    }

    private fun hideBlockedAppBlock() {
        blockedAppView?.let { runCatching { windowManager?.removeView(it) } }
        blockedAppView = null
    }

    private fun isHomePackage(pkg: String): Boolean {
        if (pkg == packageName || pkg == "com.android.chrome" || pkg == "com.android.vending") return false
        val home = homePackage ?: runCatching {
            packageManager.resolveActivity(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME), 0)?.activityInfo?.packageName
        }.getOrNull()
        return home != null && pkg == home
    }

    private fun showHomeBlock() {
        if (homeBlockView != null || windowManager == null) return
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(40, 40, 40, 40)
            setBackgroundColor(0xF5FFFFFF.toInt())
            isClickable = true
            isFocusable = true
        }
        val title = TextView(this).apply { text = "חסום"; textSize = 30f; gravity = Gravity.CENTER }
        val message = TextView(this).apply { text = "יציאה מהאתר החסום אינה זמינה"; textSize = 18f; gravity = Gravity.CENTER; setPadding(0, 12, 0, 24) }
        val backSite = Button(this).apply {
            text = "חזור לאתר"
            isAllCaps = false
            setOnClickListener {
                hideHomeBlock()
                val url = getSharedPreferences("chrome_lock", MODE_PRIVATE).getString("allowed_url", null)
                if (!url.isNullOrBlank()) startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    setPackage("com.android.chrome"); addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            }
        }
        val backApp = Button(this).apply {
            text = "חזור לאפליקציה"
            isAllCaps = false
            setOnClickListener {
                hideHomeBlock()
                getSharedPreferences("chrome_lock", MODE_PRIVATE).edit().putBoolean("enabled", false).apply()
                startActivity(Intent(this@AppBlockAccessibilityService, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                })
            }
        }
        root.addView(title, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        root.addView(message, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        root.addView(backSite, LinearLayout.LayoutParams.MATCH_PARENT, 56.dp())
        root.addView(backApp, LinearLayout.LayoutParams.MATCH_PARENT, 56.dp())
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.CENTER
        runCatching { windowManager?.addView(root, params); homeBlockView = root }
    }

    private fun hideHomeBlock() {
        homeBlockView?.let { runCatching { windowManager?.removeView(it) } }
        homeBlockView = null
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
                if (!allowedUrl.isNullOrBlank()) startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(allowedUrl)).apply {
                    setPackage("com.android.chrome"); addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
                return
            }
        }

        clearChromeGuards()
        findNodes(root).forEach { node ->
            val label = ((node.contentDescription ?: "").toString() + " " + (node.text ?: "")).lowercase()
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
            if (!value.isNullOrBlank() && (value.startsWith("http://") || value.startsWith("https://") || value.contains("www.") || value.contains(".co.il"))) {
                result = value
                return
            }
            for (i in 0 until n.childCount) n.getChild(i)?.let { walk(it) }
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
            bounds.width(), bounds.height(),
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
        val uninstall = "הסר התקנה" in text || "הסר את ההתקנה" in text || "uninstall" in text || "remove app" in text
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
        val title = TextView(this).apply { text = titleText; textSize = 22f; gravity = Gravity.CENTER }
        val input = EditText(this).apply {
            hint = "הזן סיסמה"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            gravity = Gravity.CENTER
        }
        val button = Button(this).apply {
            text = "אישור"
            setOnClickListener {
                val savedPassword = getSharedPreferences("concept_security", MODE_PRIVATE).getString("login_password", "") ?: ""
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
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.CENTER
        windowManager?.addView(root, params)
        input.requestFocus()
    }

    private fun hideUninstallPassword() {
        uninstallView?.let { runCatching { windowManager?.removeView(it) } }
        uninstallView = null
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (!getSharedPreferences("play_gate", MODE_PRIVATE).getBoolean("active", false)) return false
        return when (event.keyCode) {
            KeyEvent.KEYCODE_BACK, KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_RIGHT,
            KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_DPAD_CENTER,
            KeyEvent.KEYCODE_ENTER -> true
            else -> false
        }
    }

    override fun onInterrupt() {
        hideUninstallPassword()
        hidePlayControls()
        hideHomeBlock()
        hideBlockedAppBlock()
        clearChromeGuards()
    }

    override fun onDestroy() {
        hideUninstallPassword()
        hidePlayControls()
        hideHomeBlock()
        hideBlockedAppBlock()
        clearChromeGuards()
        super.onDestroy()
    }
}
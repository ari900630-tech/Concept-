package com.ari.concept

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.LruCache
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

data class AppItem(val label: String, val packageName: String, val info: ApplicationInfo)
data class StoreItem(val label: String, val packageName: String)

class MainActivity : ComponentActivity() {
    private val sites = listOf(
        "כיכר השבת" to "https://www.kikar.co.il/",
        "JDN" to "https://www.jdn.co.il/",
        "בחדרי חרדים" to "https://www.bhol.co.il/",
        "המחדש" to "https://hm-news.co.il/",
        "דרשו" to "https://www.dirshu.co.il/",
        "בנק הפועלים" to "https://www.bankhapoalim.co.il/",
        "בנק לאומי" to "https://www.leumi.co.il/",
        "בנק דיסקונט" to "https://www.discountbank.co.il/",
        "מכבי" to "https://www.maccabi4u.co.il/",
        "כללית" to "https://www.clalit.co.il/",
        "מאוחדת" to "https://www.meuhedet.co.il/",
        "לאומית" to "https://www.leumit.co.il/",
        "ביטוח לאומי" to "https://www.btl.gov.il/",
        "דואר ישראל" to "https://israelpost.co.il/",
        "Google" to "https://www.google.com/",
        "Gmail" to "https://mail.google.com/",
        "Google Drive" to "https://drive.google.com/",
        "Google Maps" to "https://maps.google.com/",
        "Google Translate" to "https://translate.google.com/",
        "Wikipedia" to "https://he.wikipedia.org/",
        "ממשל זמין" to "https://www.gov.il/",
        "רשות המסים" to "https://www.gov.il/he/departments/israel_tax_authority/govil-landing-page",
        "רכבת ישראל" to "https://www.rail.co.il/",
        "אגד" to "https://www.egged.co.il/",
        "רמי לוי" to "https://www.rami-levy.co.il/",
        "שופרסל" to "https://www.shufersal.co.il/",
        "ביטוח ישיר" to "https://www.555.co.il/",
        "הראל" to "https://www.harel-group.co.il/",
        "אלטשולר שחם" to "https://www.as-invest.co.il/"
    )

    private val alwaysBlockedApps = listOf(
        StoreItem("TikTok", "com.zhiliaoapp.musically"),
        StoreItem("Instagram", "com.instagram.android"),
        StoreItem("Google", "com.google.android.googlequicksearchbox"),
        StoreItem("Chrome", "com.android.chrome"),
        StoreItem("YouTube", "com.google.android.youtube"),
        StoreItem("Facebook", "com.facebook.katana"),
        StoreItem("Messenger", "com.facebook.orca"),
        StoreItem("X", "com.twitter.android"),
        StoreItem("Snapchat", "com.snapchat.android"),
        StoreItem("Telegram", "org.telegram.messenger")
    )

    private val usefulApps = listOf(
            StoreItem("WhatsApp", "com.whatsapp"),
            StoreItem("Waze", "com.waze"),
            StoreItem("Moovit", "com.tranzmate"),
            StoreItem("PayBox", "com.payboxapp"),
            StoreItem("Dropbox", "com.dropbox.android"),
            StoreItem("Zoom", "us.zoom.videomeetings"),
            StoreItem("Google Drive", "com.google.android.apps.docs"),
            StoreItem("Google Calendar", "com.google.android.calendar"),
            StoreItem("Google Translate", "com.google.android.apps.translate"),
            StoreItem("Google Keep", "com.google.android.keep"),
            StoreItem("Outlook", "com.microsoft.office.outlook"),
            StoreItem("Google Docs", "com.google.android.apps.docs.editors.docs"),
            StoreItem("Google Sheets", "com.google.android.apps.docs.editors.sheets"),
            StoreItem("Microsoft Word", "com.microsoft.office.word"),
            StoreItem("Microsoft Excel", "com.microsoft.office.excel"),
            StoreItem("Microsoft PowerPoint", "com.microsoft.office.powerpoint"),
            StoreItem("Signal", "org.thoughtcrime.securesms"),
            StoreItem("Skype", "com.skype.raider"),
            StoreItem("Microsoft Teams", "com.microsoft.teams"),
            StoreItem("OneDrive", "com.microsoft.skydrive"),
            StoreItem("Adobe Acrobat Reader", "com.adobe.reader"),
            StoreItem("Canva", "com.canva.editor"),
            StoreItem("Pinterest", "com.pinterest"),
            StoreItem("Shazam", "com.shazam.android"),
            StoreItem("Google Photos", "com.google.android.apps.photos"),
            StoreItem("Firefox", "org.mozilla.firefox"),
            StoreItem("Google Authenticator", "com.google.android.apps.authenticator2"),
            StoreItem("Microsoft Authenticator", "com.azure.authenticator"),
            StoreItem("Google Wallet", "com.google.android.apps.walletnfcrel"),
            StoreItem("Amazon Shopping", "com.amazon.mShop.android.shopping"),
            StoreItem("eBay", "com.ebay.mobile"),
            StoreItem("Duolingo", "com.duolingo"),
            StoreItem("Khan Academy", "org.khanacademy.android"),
            StoreItem("Google Classroom", "com.google.android.apps.classroom"),
            StoreItem("Notion", "notion.id"),
            StoreItem("Evernote", "com.evernote"),
            StoreItem("Trello", "com.trello"),
            StoreItem("VLC", "org.videolan.vlc"),
            StoreItem("Spotify", "com.spotify.music"),
            StoreItem("LinkedIn", "com.linkedin.android"),
            StoreItem("Reddit", "com.reddit.frontpage"),
            StoreItem("Discord", "com.discord"),
            StoreItem("Twitch", "tv.twitch.android.app"),
            StoreItem("Pinterest", "com.pinterest"),
            StoreItem("AliExpress", "com.alibaba.aliexpresshd"),
            StoreItem("Temu", "com.einnovation.temu"),
            StoreItem("Booking.com", "com.booking"),
            StoreItem("Airbnb", "com.airbnb.android"),
            StoreItem("Uber", "com.ubercab"),
            StoreItem("Bolt", "ee.mtakso.client"),
            StoreItem("PayPal", "com.paypal.android.p2pmobile"),
            StoreItem("Revolut", "com.revolut.revolut"),
            StoreItem("Shufersal", "il.co.shufersal"),
            StoreItem("Rami Levy", "com.ramilevy"),
            StoreItem("Yad2", "com.yad2"),
            StoreItem("Zap", "com.zap"),
            StoreItem("Mako", "com.mako.news"),
            StoreItem("Ynet", "com.ynetnews"),
            StoreItem("Walla", "com.walla"),
            StoreItem("Calcalist", "com.calcalist"),
            StoreItem("Google News", "com.google.android.apps.magazines"),
            StoreItem("Google Maps", "com.google.android.apps.maps"),
            StoreItem("Google Home", "com.google.android.apps.chromecast.app"),
            StoreItem("Google Meet", "com.google.android.apps.tachyon"),
            StoreItem("Gmail", "com.google.android.gm"),
            StoreItem("YouTube Music", "com.google.android.apps.youtube.music"),
            StoreItem("Files by Google", "com.google.android.apps.nbu.files"),
            StoreItem("Google Lens", "com.google.ar.lens"),
            StoreItem("Microsoft OneNote", "com.microsoft.office.onenote"),
            StoreItem("Microsoft To Do", "com.microsoft.todos"),
            StoreItem("Slack", "com.Slack"),
            StoreItem("Asana", "com.asana.app"),
            StoreItem("Todoist", "com.todoist"),
            StoreItem("Grammarly", "com.grammarly.android.keyboard"),
            StoreItem("1Password", "com.onepassword.android"),
            StoreItem("Bitwarden", "com.x8bit.bitwarden"),
            StoreItem("Duolingo ABC", "com.duolingo.literacy"),
            StoreItem("Coursera", "org.coursera.android"),
            StoreItem("Udemy", "com.udemy.android"),
            StoreItem("Photomath", "com.microblink.photomath"),
            StoreItem("CapCut", "com.lemon.lvoverseas"),
            StoreItem("Lightroom", "com.adobe.lrmobile"),
            StoreItem("Snapseed", "com.niksoftware.snapseed"),
            StoreItem("InShot", "com.camerasideas.instashot"),
            StoreItem("Picsart", "com.picsart.studio"),
            StoreItem("Weather & Radar", "de.wetteronline.wetterapp"),
            StoreItem("AccuWeather", "com.accuweather.android"),
            StoreItem("Google Fit", "com.google.android.apps.fitness"),
            StoreItem("Strava", "com.strava"),
            StoreItem("Fitbit", "com.fitbit.FitbitMobile"),
            StoreItem("MyFitnessPal", "com.myfitnesspal.android"),
            StoreItem("AliExpress", "com.alibaba.aliexpresshd"),
            StoreItem("Amazon Kindle", "com.amazon.kindle"),
            StoreItem("Audible", "com.audible.application"),
            StoreItem("Kindle", "com.amazon.kindle")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE or android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or android.view.View.SYSTEM_UI_FLAG_FULLSCREEN or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        // Render Compose immediately. Protection/device-owner setup is deferred until
        // after the first frame so Android 11 does not show a long blank preview.
        val password = getSharedPreferences("concept_security", MODE_PRIVATE)
            .getString("login_password", null)

        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                var unlocked by rememberSaveable { mutableStateOf(password.isNullOrEmpty()) }
                if (unlocked) {
                    App()
                } else {
                    LoginGate(
                        correctPassword = password.orEmpty(),
                        onUnlocked = { unlocked = true }
                    )
                }
            }
        }

        // Do non-visual protection setup only after the UI has been submitted.
        window.decorView.post {
            runCatching {
                getSharedPreferences("concept_protection", MODE_PRIVATE)
                    .edit().putBoolean("enabled", true).apply()
                val alwaysBlockedPackages = alwaysBlockedApps.map { it.packageName }.toSet()
                getSharedPreferences("blocked", MODE_PRIVATE).edit().apply {
                    alwaysBlockedPackages.forEach { putBoolean(it, true) }
                    apply()
                }
                val dpm = getSystemService(android.app.admin.DevicePolicyManager::class.java)
                val admin = android.content.ComponentName(this, ConceptDeviceAdminReceiver::class.java)
                if (dpm.isDeviceOwnerApp(packageName)) {
                    runCatching { dpm.setLockTaskPackages(admin, arrayOf(packageName)) }
                    runCatching { dpm.setLockTaskFeatures(admin, android.app.admin.DevicePolicyManager.LOCK_TASK_FEATURE_NONE) }
                    runCatching { startLockTask() }
                }
            }
        }
    }

    @Composable
    private fun LoginGate(correctPassword: String, onUnlocked: () -> Unit) {
        var entered by rememberSaveable { mutableStateOf("") }
        var error by rememberSaveable { mutableStateOf(false) }

        Column(
            modifier = Modifier.fillMaxSize().padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) { Text("👑", fontSize = 30.sp); Spacer(Modifier.width(8.dp)); Text("Concept", style = MaterialTheme.typography.headlineLarge) }
            Spacer(Modifier.height(12.dp))
            Text("האפליקציה מוגנת בסיסמה", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(20.dp))
            OutlinedTextField(
                value = entered,
                onValueChange = { entered = it; error = false },
                label = { Text("סיסמה") },
                singleLine = true,
                isError = error,
                textStyle = TextStyle(textAlign = TextAlign.Right, textDirection = androidx.compose.ui.text.style.TextDirection.Rtl),
                modifier = Modifier.fillMaxWidth()
            )
            if (error) {
                Text("סיסמה שגויה", color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    if (entered == correctPassword) onUnlocked() else error = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {                Text("כניסה")
            }
        }
    }

    private fun installedApps(): List<AppItem> {
        return packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { it.packageName != packageName && packageManager.getLaunchIntentForPackage(it.packageName) != null }
            .map { AppItem(packageManager.getApplicationLabel(it).toString(), it.packageName, it) }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
    }

    private fun hasInternet(): Boolean {
        val cm = getSystemService(ConnectivityManager::class.java) ?: return false
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    private fun openPlay(pkg: String) {
        if (!hasInternet()) return
        startActivity(Intent(this, PlayGateActivity::class.java).putExtra("pkg", pkg))
    }

    @Composable
    private fun AppIcon(app: AppItem) {
        androidx.compose.ui.viewinterop.AndroidView(
            factory = {
                android.widget.ImageView(it).apply {
                    setImageDrawable(app.info.loadIcon(packageManager))
                    scaleType = android.widget.ImageView.ScaleType.CENTER_INSIDE
                }
            },
            modifier = Modifier.size(48.dp)
        )
    }

    @Composable
    private fun AppCard(
        app: AppItem,
        onClick: () -> Unit,
        trailing: (@Composable () -> Unit)? = null
    ) {
        androidx.compose.material3.Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clickable { onClick() }
        ) {
            Row(
                Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppIcon(app)
                Spacer(Modifier.width(12.dp))
                Text(app.label, Modifier.weight(1f))
                trailing?.invoke()
            }
        }
    }

    private val imageCache = LruCache<String, Bitmap>(8)

    private fun installedDrawable(pkg: String): android.graphics.drawable.Drawable? =
        runCatching { packageManager.getApplicationInfo(pkg, PackageManager.GET_META_DATA).loadIcon(packageManager) }.getOrNull()

    private suspend fun loadBitmap(url: String, cacheKey: String): Bitmap? = withContext(Dispatchers.IO) {
        imageCache.get(cacheKey)?.let { return@withContext it }
        runCatching {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.connectTimeout = 8000
            connection.readTimeout = 8000
            connection.setRequestProperty("User-Agent", "Mozilla/5.0")
            val options = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.RGB_565
                inSampleSize = 4
            }
            val bitmap = connection.inputStream.use { BitmapFactory.decodeStream(it, null, options) }
            connection.disconnect()
            if (bitmap != null) imageCache.put(cacheKey, bitmap)
            bitmap
        }.getOrNull()
    }

    // Play Store icons are loaded only from installed apps. Remote icon scraping was
    // removed because dozens of simultaneous HTTP/bitmap jobs could exhaust Android 11 memory.

    private suspend fun fetchPlayCatalog(
        queries: List<String>,
        category: String,
        limit: Int = 60
    ): List<StoreItem> = withContext(Dispatchers.IO) {
        val results = mutableListOf<StoreItem>()
        val pattern = Regex(
            """href="/store/apps/details\?id=([^"&]+)"[^>]*>([^<]*)</a>""",
            RegexOption.IGNORE_CASE
        )

        for (query in queries.take(6)) {
            if (results.distinctBy { it.packageName }.size >= limit) break
            runCatching {
                val encoded = java.net.URLEncoder.encode(query, "UTF-8")
                val url = "https://play.google.com/store/search?q=$encoded&c=$category&hl=en&gl=US"
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.connectTimeout = 3000
                connection.readTimeout = 4000
                connection.instanceFollowRedirects = true
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 11)")
                val html = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                connection.disconnect()

                pattern.findAll(html).forEach { match ->
                    val pkg = match.groupValues.getOrNull(1)?.trim().orEmpty()
                    val title = match.groupValues.getOrNull(2)?.trim().orEmpty()
                    if (pkg.isNotBlank()) {
                        results.add(StoreItem(if (title.isBlank()) pkg else title, pkg))
                    }
                }
            }
        }

        results.distinctBy { it.packageName }.take(limit)
    }

    private val appCatalogQueries = listOf(
        "productivity", "tools", "education", "business", "finance",
        "shopping", "travel", "maps", "weather", "health", "fitness",
        "photo", "video", "music", "communication", "news", "books",
        "food", "lifestyle", "utilities", "office", "calendar", "email"
    )

    private val gameCatalogQueries = listOf(
        "action games", "adventure games", "arcade games", "puzzle games",
        "racing games", "sports games", "strategy games", "casual games",
        "card games", "board games", "simulation games", "role playing games",
        "educational games", "word games", "music games", "kids games",
        "offline games", "multiplayer games", "family games", "classic games",
        "football games", "car games", "chess games", "brain games"
    )

    private suspend fun loadSiteIcon(siteUrl: String): Bitmap? {
        val host = Uri.parse(siteUrl).host ?: return null
        return loadBitmap("https://www.google.com/s2/favicons?domain=$host&sz=128", "site:$host")
    }
    @Composable
    private fun PlayStoreCard(item: StoreItem, loadDelayMs: Long = 0L) {
        var bitmap by remember(item.packageName) { mutableStateOf(imageCache.get("play:" + item.packageName)) }
        val localIcon = remember(item.packageName) { installedDrawable(item.packageName) }
        val online = hasInternet()

        val blocked = getSharedPreferences("blocked", MODE_PRIVATE).getBoolean(item.packageName, false)

        Card(modifier = Modifier.padding(4.dp).fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(68.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        bitmap != null -> Image(bitmap!!.asImageBitmap(), item.label, Modifier.size(58.dp))
                        localIcon != null -> androidx.compose.ui.viewinterop.AndroidView(
                            factory = {
                                android.widget.ImageView(it).apply {
                                    setImageDrawable(localIcon)
                                    scaleType = android.widget.ImageView.ScaleType.CENTER_INSIDE
                                }
                            },
                            modifier = Modifier.size(58.dp)
                        )
                        else -> Icon(Icons.Default.SportsEsports, item.label, Modifier.size(42.dp))
                    }
                }
                Text(
                    item.label,
                    maxLines = 2,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.fillMaxWidth().height(32.dp)
                )
                if (online && !blocked) {
                    Button(
                        onClick = { openPlay(item.packageName) },
                        modifier = Modifier.fillMaxWidth().height(34.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 0.dp)
                    ) { Text("התקנה", fontSize = 11.sp) }
                }
            }
        }
    }

    @Composable
    fun App() {
        var ready by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            delay(1000)
            ready = true
        }

        if (!ready) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Box(Modifier.size(112.dp), contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher),
                        contentDescription = "Concept",
                        modifier = Modifier.size(68.dp)                    )
                    CircularProgressIndicator(
                        modifier = Modifier.size(104.dp),
                        strokeWidth = 3.dp
                    )
                }
            }
            return
        }

        var tab by remember { mutableIntStateOf(0) }
        val labels = listOf("משחקים", "אפליקציות", "אתרים", "חסימה", "הגדרות")
        val icons = listOf(
            Icons.Default.SportsEsports,
            Icons.Default.Apps,
            Icons.Default.Language,
            Icons.Default.Block,
            Icons.Default.Settings
        )

        Scaffold(
            bottomBar = {
                NavigationBar {
                    labels.forEachIndexed { i, label ->
                        NavigationBarItem(
                            selected = tab == i,
                            onClick = { tab = i },
                            icon = { Icon(icons[i], contentDescription = label) },
                            label = { Text(label) }
                        )
                    }
                }
            }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                Column(Modifier.fillMaxSize().padding(12.dp)) {
                    when (tab) {
                        0 -> Games()
                        1 -> Apps()
                        2 -> Sites()
                        3 -> BlockApps()
                        4 -> SettingsScreen()
                    }
                }

            }
        }
    }

    @Composable
    fun Apps() {
        val saved = getSharedPreferences("concept_items", MODE_PRIVATE)
            .getStringSet("apps", emptySet())
            .orEmpty()
            .mapNotNull {
                val p = it.split("|", limit = 2)
                if (p.size == 2) StoreItem(p[0], p[1]) else null
            }

        var remoteApps by remember { mutableStateOf<List<StoreItem>>(emptyList()) }
        var catalogLoading by remember { mutableStateOf(false) }
        val apps = (usefulApps + remoteApps + saved).distinctBy { it.packageName }

        var selectedCategory by rememberSaveable { mutableStateOf("הכול") }

        val online = hasInternet()
        LaunchedEffect(online) {
            if (online && remoteApps.isEmpty() && !catalogLoading) {
                catalogLoading = true
                remoteApps = fetchPlayCatalog(appCatalogQueries, "apps", 60)
                catalogLoading = false
            }
        }
        Column(Modifier.fillMaxSize()) {
            if (!online) Text("האפליקציות כרגע אין אינטרנט, נסה שוב במועד מאוחר יותר", modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.error)
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                reverseLayout = true
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory == "חסומות תמיד",
                        onClick = { selectedCategory = "חסומות תמיד" },
                        label = { Text("🚫") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedCategory == "לא חסומות",
                        onClick = { selectedCategory = "לא חסומות" },
                        label = { Text("✅") }
                    )
                }
            }

            val blockedPackages = getSharedPreferences("blocked", MODE_PRIVATE)
                .all.filterValues { it is Boolean && it }.keys
            val visibleApps = when (selectedCategory) {
                "חסומות תמיד" -> alwaysBlockedApps
                "לא חסומות" -> apps.filter {
                    it.packageName !in blockedPackages &&
                    it.packageName !in alwaysBlockedApps.map { item -> item.packageName }
                }
                else -> apps
            }

            if (catalogLoading && remoteApps.isEmpty()) {
                Box(Modifier.fillMaxWidth().padding(12.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 3.dp)
                }
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                itemsIndexed(visibleApps, key = { _, it -> it.packageName }) { index, app ->
                    PlayStoreCard(app, index * 140L)
                }
            }
        }
    }

    @Composable
    fun Games() {
        val baseGames = listOf(
            StoreItem("Roblox", "com.roblox.client"),
            StoreItem("Toca Boca World", "com.tocaboca.tocabocaworld"),
            StoreItem("EA SPORTS FC Mobile", "com.ea.gp.fifamobile"),
            StoreItem("8 Ball Pool", "com.miniclip.eightballpool"),
            StoreItem("Candy Crush Saga", "com.king.candycrushsaga"),
            StoreItem("Pokémon GO", "com.nianticlabs.pokemongo"),
            StoreItem("Township", "com.playrix.township"),
            StoreItem("eFootball", "jp.konami.pesam"),
            StoreItem("Royal Match", "com.dreamgames.royalmatch"),
            StoreItem("Gardenscapes", "com.playrix.gardenscapes"),
            StoreItem("Clash of Clans", "com.supercell.clashofclans"),
            StoreItem("Brawl Stars", "com.supercell.brawlstars"),
            StoreItem("Minecraft", "com.mojang.minecraftpe"),
            StoreItem("Genshin Impact", "com.miHoYo.GenshinImpact"),
            StoreItem("Toon Blast", "net.peakgames.toonblast"),
            StoreItem("Subway Surfers", "com.kiloo.subwaysurf"),
            StoreItem("Block Blast!", "com.block.juggle"),
            StoreItem("Geometry Dash Lite", "com.robtopx.geometryjumplite"),
            StoreItem("Hill Climb Racing", "com.fingersoft.hillclimb"),
            StoreItem("Temple Run 2", "com.imangi.templerun2"),
            StoreItem("Fruit Ninja", "com.halfbrick.fruitninjafree"),
            StoreItem("Jetpack Joyride", "com.halfbrick.jetpackjoyride"),
            StoreItem("Angry Birds 2", "com.rovio.baba"),
            StoreItem("Cut the Rope", "com.zeptolab.ctr.ads"),
            StoreItem("Crossy Road", "com.yodo1.crossyroad"),
            StoreItem("Ludo King", "com.ludo.king"),
            StoreItem("Car Parking Multiplayer", "com.olzhas.carparking.multyplayer"),
            StoreItem("Dream League Soccer", "com.firsttouchgames.dls7"),
            StoreItem("PUBG MOBILE", "com.tencent.ig"),
            StoreItem("Call of Duty Mobile", "com.activision.callofduty.shooter"),
            StoreItem("Clash Royale", "com.supercell.clashroyale"),
            StoreItem("Pokémon TCG Pocket", "jp.pokemon.pokemontcgp"),
            StoreItem("My Talking Tom 2", "com.outfit7.mytalkingtom2"),
            StoreItem("Magic Tiles 3", "com.youmusic.magictiles"),
            StoreItem("Wordscapes", "com.peoplefun.wordcross"),
            StoreItem("Among Us", "com.innersloth.spacemafia"),
            StoreItem("Stumble Guys", "com.kitkagames.fallbuddies"),
            StoreItem("Temple Run", "com.imangi.templerun"),
            StoreItem("Angry Birds Friends", "com.rovio.baba"),
            StoreItem("Angry Birds Dream Blast", "com.rovio.dream"),
            StoreItem("Sonic Dash", "com.sega.sonicdash"),
            StoreItem("Sonic Forces", "com.sega.sonicforces"),
            StoreItem("Mario Kart Tour", "com.nintendo.zaka"),
            StoreItem("Super Mario Run", "com.nintendo.zara"),
            StoreItem("Plants vs Zombies 2", "com.ea.game.pvz2_row"),
            StoreItem("Plants vs Zombies", "com.ea.game.pvzfree_row"),
            StoreItem("Fruit Ninja Classic", "com.halfbrick.fruitninjafree"),
            StoreItem("Jetpack Joyride 2", "com.halfbrick.jetpackjoyride2"),
            StoreItem("Hungry Shark Evolution", "com.fgol.HungrySharkEvolution"),
            StoreItem("Hungry Shark World", "com.fgol.HungrySharkWorld"),
            StoreItem("Shadow Fight 3", "com.nekki.shadowfight3"),
            StoreItem("Shadow Fight 4", "com.nekki.shadowfightarena"),
            StoreItem("Asphalt 9", "com.gameloft.android.ANMP.GloftA9HM"),
            StoreItem("Asphalt 8", "com.gameloft.android.ANMP.GloftA8HM"),
            StoreItem("Real Racing 3", "com.ea.games.r3_row"),
            StoreItem("Need for Speed No Limits", "com.ea.game.nfs14_row"),
            StoreItem("CSR Racing 2", "com.naturalmotion.customstreetracer2"),
            StoreItem("Hill Climb Racing 2", "com.fingersoft.hcr2"),
            StoreItem("Traffic Rider", "com.skgames.trafficrider"),
            StoreItem("Beach Buggy Racing", "com.vectorunit.purple.googleplay"),
            StoreItem("Beach Buggy Racing 2", "com.vectorunit.cobalt.googleplay"),
            StoreItem("World of Tanks Blitz", "com.wargaming.wot.blitz"),
            StoreItem("8 Ball Hero", "com.kingsgroup.ballhero"),
            StoreItem("Chess", "com.chess"),
            StoreItem("Chess.com", "com.chess"),
            StoreItem("Sudoku", "com.andoku"),
            StoreItem("Solitaire", "com.mobilityware.solitaire"),
            StoreItem("Word Search", "com.puzzlegames.wordsearch"),
            StoreItem("2048", "com.androbaby.game2048"),
            StoreItem("Monument Valley", "com.ustwo.monumentvalley"),
            StoreItem("Monument Valley 2", "com.ustwo.monumentvalley2"),
            StoreItem("Alto's Odyssey", "com.noodlecake.altosodyssey"),
            StoreItem("Alto's Adventure", "com.noodlecake.altosadventure"),
            StoreItem("Crossy Road Castle", "com.yodo1.crossyroad"),
            StoreItem("Badland", "com.frogmind.badland"),
            StoreItem("Badland 2", "com.frogmind.badland2"),
            StoreItem("WorldBox", "com.mkarpenko.worldbox"),
            StoreItem("Terraria", "com.and.games505.TerrariaPaid"),
            StoreItem("Stardew Valley", "com.chucklefish.stardewvalley"),
            StoreItem("Bloons TD 6", "com.ninjakiwi.bloonstd6"),
            StoreItem("Kingdom Rush", "com.ironhidegames.android.kingdomrush"),
            StoreItem("Kingdom Rush Frontiers", "com.ironhidegames.android.kingdomrushfrontiers"),
            StoreItem("Fruit Ninja 2", "com.halfbrick.fruitninja2"),
            StoreItem("Ski Safari", "com.DefiantDev.SkiSafari"),
            StoreItem("Pou", "me.pou.app"),
            StoreItem("Pou 2", "me.pou.app2"),
            StoreItem("Talking Tom Gold Run", "com.outfit7.talkingtomgoldrun"),
            StoreItem("Talking Tom Hero Dash", "com.outfit7.herodash"),
            StoreItem("My Talking Angela 2", "com.outfit7.miga"),
            StoreItem("My Talking Tom Friends", "com.outfit7.mytalkingtomfriends"),
            StoreItem("Hungry Hearts Diner", "com.g1playground.hungrhearts"),
            StoreItem("Cooking Fever", "com.nordcurrent.canteenhd"),
            StoreItem("Cooking Madness", "com.zenjoy.cookingmadness"),
            StoreItem("Homescapes", "com.playrix.homescapes"),
            StoreItem("Fishdom", "com.playrix.fishdomdd.gplay"),
            StoreItem("Manor Matters", "com.playrix.manormatters"),
            StoreItem("Matchington Mansion", "com.matchington.matchingtonmansion"),
            StoreItem("Angry Birds Journey", "com.rovio.baba"),
            StoreItem("Bad Piggies", "com.rovio.BadPiggies"),
            StoreItem("Hill Climb Racing", "com.fingersoft.hillclimb"),
            StoreItem("Golf Clash", "com.playdemic.golf.android"),
            StoreItem("8 Ball Pool", "com.miniclip.eightballpool")
        )


        val saved = getSharedPreferences("concept_items", MODE_PRIVATE)
            .getStringSet("games", emptySet())
            .orEmpty()
            .mapNotNull {
                val p = it.split("|", limit = 2)
                if (p.size == 2) StoreItem(p[0], p[1]) else null
            }

        var remoteGames by remember { mutableStateOf<List<StoreItem>>(emptyList()) }
        var catalogLoading by remember { mutableStateOf(false) }
        val games = (baseGames + remoteGames + saved).distinctBy { it.packageName }
        val online = hasInternet()
        LaunchedEffect(online) {
            if (online && remoteGames.isEmpty() && !catalogLoading) {
                catalogLoading = true
                remoteGames = fetchPlayCatalog(gameCatalogQueries, "GAME", 60)
                catalogLoading = false
            }
        }
        Column(Modifier.fillMaxSize()) {
            if (!online) Text("המשחקים כרגע אין אינטרנט, נסה שוב במועד מאוחר יותר", modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.error)
            if (catalogLoading && remoteGames.isEmpty()) {
                Box(Modifier.fillMaxWidth().padding(12.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 3.dp)
                }
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(games, key = { it.packageName }) { game ->
                    PlayStoreCard(game)
                }
            }
        }
    }

    @Composable
    fun Sites() {
        Column(Modifier.fillMaxSize()) {
            Text(
                "אתרים",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                textAlign = TextAlign.Right
            )
            LazyColumn(Modifier.weight(1f)) {
                items(sites, key = { it.first }) { (name, url) ->
                    var bitmap by remember(url) { mutableStateOf(imageCache.get("site:${Uri.parse(url).host}")) }
                    LaunchedEffect(url) {
                        val loaded = loadSiteIcon(url)
                        if (loaded != null) bitmap = loaded
                    }
                    androidx.compose.material3.Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable {
                            openSiteInChrome(url)
                        }
                    ) {
                        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            if (bitmap != null) Image(bitmap!!.asImageBitmap(), contentDescription = name, modifier = Modifier.size(56.dp))
                            else Box(Modifier.size(56.dp), contentAlignment = Alignment.Center) { Icon(Icons.Default.Language, null, modifier = Modifier.size(36.dp)) }
                            Spacer(Modifier.width(12.dp))
                            Text(name, Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }

    private fun openSiteInChrome(url: String) {
        val uri = Uri.parse(url)
        getSharedPreferences("chrome_lock", MODE_PRIVATE).edit()            .putBoolean("enabled", true)
            .putString("allowed_url", url)
            .putString("allowed_host", uri.host ?: "")
            .apply()

        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.android.chrome")
        }
        try {
            startActivity(intent)
        } catch (_: Exception) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }

    @Composable
    fun BlockApps() {
        var loading by remember { mutableStateOf(true) }
        var apps by remember { mutableStateOf<List<AppItem>>(emptyList()) }

        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                installedApps()
            }.also {
                apps = it
                loading = false
            }
        }

        val prefs = getSharedPreferences("blocked", MODE_PRIVATE)
        var blocked by remember {
            mutableStateOf(
                prefs.all.filterValues { it is Boolean && it }.keys.toSet()
            )
        }

        Column(Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                Text(
                    "חסימת אפליקציות",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right
                )
                IconButton(
                    onClick = { },
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Icon(Icons.Default.MoreVert, contentDescription = "אפשרויות")
                }
            }
            if (loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    LoadingDots()
                }
            } else {
                LazyColumn(Modifier.weight(1f)) {
                    items(apps, key = { it.packageName }) { app ->
                        val alwaysBlocked = alwaysBlockedApps.any { it.packageName == app.packageName }
                        val enabled = alwaysBlocked || app.packageName in blocked
                        AppCard(
                            app,
                            onClick = {
                                if (!alwaysBlocked) {
                                    val newValue = !enabled
                                    prefs.edit().putBoolean(app.packageName, newValue).apply()
                                    blocked = if (newValue) blocked + app.packageName else blocked - app.packageName
                                }
                            }
                        ) {
                            Switch(checked = enabled, enabled = !alwaysBlocked, onCheckedChange = null)
                        }
                    }
                }
            }
    }
    }

    @Composable
    private fun LoadingDots() {
        var active by remember { mutableIntStateOf(0) }
        LaunchedEffect(Unit) {
            while (true) {
                delay(350)
                active = (active + 1) % 3
            }
        }
        Row(Modifier.fillMaxWidth().padding(24.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text("טוען אפליקציות", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.width(8.dp))
            repeat(3) { i ->
                Box(Modifier.padding(horizontal = 3.dp).size(10.dp), contentAlignment = Alignment.Center) {
                    Surface(
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = if (i == active) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(9.dp)
                    ) {}
                }
            }
        }
    }

    private fun accessibilityEnabled(): Boolean {
        val value = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES).orEmpty()
        return value.split(':').any { it.contains(packageName, ignoreCase = true) }
    }

    private fun overlayEnabled(): Boolean = Settings.canDrawOverlays(this)

    private fun usageAccessEnabled(): Boolean {
        val appOps = getSystemService(android.app.AppOpsManager::class.java)
        return appOps.checkOpNoThrow(
            android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
            applicationInfo.uid,
            packageName
        ) == android.app.AppOpsManager.MODE_ALLOWED
    }

    private fun deviceAdminEnabled(): Boolean {
        val dpm = getSystemService(DevicePolicyManager::class.java)
        val admin = ComponentName(this, ConceptDeviceAdminReceiver::class.java)
        return dpm.isAdminActive(admin) || dpm.isDeviceOwnerApp(packageName)
    }

    @Composable
    private fun PermissionButton(label: String, enabled: Boolean, onClick: () -> Unit) {
        Button(
            onClick = onClick,
            enabled = !enabled,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (enabled) "✓ $label" else label)
        }
    }

    @Composable
    fun SettingsScreen() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val accessibilityOk = accessibilityEnabled()
            val adminOk = deviceAdminEnabled()
            val overlayOk = overlayEnabled()
            val usageOk = usageAccessEnabled()

            PermissionButton("נגישות", accessibilityOk) {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }

            PermissionButton("אישור שליטה על הטלפון", adminOk) {
                val admin = ComponentName(this@MainActivity, ConceptDeviceAdminReceiver::class.java)
                startActivity(Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                    putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, admin)
                    putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "נדרש אישור שליטה על הטלפון כדי להגן על האפליקציה מפני הסרה.")
                })
            }

            PermissionButton("הצגה מעל אפליקציות", overlayOk) {
                startActivity(
                    Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                )
            }

            PermissionButton("נתוני שימוש", usageOk) {
                startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            }

            var password by remember {
                mutableStateOf(
                    getSharedPreferences("concept_security", MODE_PRIVATE)
                        .getString("login_password", "") ?: ""
                )
            }
            var showPasswordDialog by remember { mutableStateOf(false) }
            var newPassword by remember { mutableStateOf("") }
            var confirmPassword by remember { mutableStateOf("") }
            var passwordError by remember { mutableStateOf("") }
            var showDisableDialog by remember { mutableStateOf(false) }
            var disablePassword by remember { mutableStateOf("") }
            var disablePasswordError by remember { mutableStateOf(false) }

            Button(
                onClick = {
                    newPassword = password
                    confirmPassword = password
                    passwordError = ""
                    showPasswordDialog = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (password.isBlank()) "הגדרת סיסמת כניסה" else "שינוי סיסמת כניסה")
            }

            if (showPasswordDialog) {
                AlertDialog(
                    onDismissRequest = { showPasswordDialog = false },
                    title = { Text("סיסמת כניסה") },
                    text = {
                        Column {
                            Text("בחר סיסמה חופשית. אפשר להשתמש בעברית, אותיות, מספרים וסימנים.")
                            Spacer(Modifier.height(10.dp))
                            OutlinedTextField(
                                value = newPassword,
                                onValueChange = { newPassword = it; passwordError = "" },
                                label = { Text("סיסמה") },
                                singleLine = true,
                                textStyle = TextStyle(textAlign = TextAlign.Right, textDirection = androidx.compose.ui.text.style.TextDirection.Rtl),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it; passwordError = "" },
                                label = { Text("אימות סיסמה") },
                                singleLine = true,
                                textStyle = TextStyle(textAlign = TextAlign.Right, textDirection = androidx.compose.ui.text.style.TextDirection.Rtl),
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (passwordError.isNotBlank()) {
                                Text(passwordError, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            when {
                                newPassword.isBlank() -> passwordError = "יש להזין סיסמה"
                                newPassword != confirmPassword -> passwordError = "הסיסמאות אינן זהות"
                                else -> {
                                    getSharedPreferences("concept_security", MODE_PRIVATE)
                                        .edit().putString("login_password", newPassword).apply()
                                    password = newPassword
                                    showPasswordDialog = false
                                }
                            }
                        }) { Text("שמירה") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showPasswordDialog = false }) { Text("ביטול") }
                    }
                )
            }

            Button(
                onClick = {
                    if (password.isBlank()) {
                        startActivity(Intent("android.settings.SECURITY_SETTINGS"))
                    } else {
                        disablePassword = ""
                        disablePasswordError = false
                        showDisableDialog = true
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("השבתת הגנה")
            }

            if (showDisableDialog) {
                AlertDialog(
                    onDismissRequest = { showDisableDialog = false },
                    title = { Text("נדרשת סיסמה") },
                    text = {
                        Column {
                            Text("יש להזין את הסיסמה לפני השבתת ההגנה.")
                            Spacer(Modifier.height(10.dp))
                            OutlinedTextField(
                                value = disablePassword,
                                onValueChange = {
                                    disablePassword = it
                                    disablePasswordError = false
                                },
                                label = { Text("סיסמה") },
                                singleLine = true,
                                isError = disablePasswordError,
                                textStyle = TextStyle(textAlign = TextAlign.Right, textDirection = androidx.compose.ui.text.style.TextDirection.Rtl),
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (disablePasswordError) {
                                Text("סיסמה שגויה", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            if (disablePassword == password) {
                                showDisableDialog = false
                                startActivity(Intent("android.settings.SECURITY_SETTINGS"))
                            } else {
                                disablePasswordError = true
                            }
                        }) { Text("אישור") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDisableDialog = false }) { Text("ביטול") }
                    }
                )
            }
        }
    }
}

class RestrictedWebActivity : ComponentActivity() {
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)

        val start = Uri.parse(
            intent.getStringExtra("url") ?: "https://www.google.com/"
        )
        val exact = intent.getStringExtra("exact") ?: start.toString()
        val allowedHost = start.host

        val webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.setSupportMultipleWindows(false)
            settings.javaScriptCanOpenWindowsAutomatically = false
            settings.loadsImagesAutomatically = true
            settings.allowFileAccess = false
            settings.allowContentAccess = true

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView,
                    request: WebResourceRequest
                ): Boolean {
                    val host = request.url.host
                    return host == null || host != allowedHost
                }
            }

            loadUrl(exact)
        }

        setContentView(webView)
    }
}
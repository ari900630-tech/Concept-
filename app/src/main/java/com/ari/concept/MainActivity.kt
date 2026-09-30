package com.ari.concept

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Language
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
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

    private val usefulApps = listOf(
        "בנק הפועלים" to "com.ideomobile.il",
        "בנק לאומי" to "com.leumi.leumiwallet",
        "בנק דיסקונט" to "com.discountbank.mobile",
        "מכבי" to "com.maccabi.health",
        "כללית" to "com.clalit.clalit",
        "מאוחדת" to "com.meuhedet",
        "לאומית" to "com.leumit",
        "WhatsApp" to "com.whatsapp",
        "Waze" to "com.waze",
        "Google Maps" to "com.google.android.apps.maps",
        "Moovit" to "com.tranzmate",
        "PayBox" to "com.payboxapp",
        "Dropbox" to "com.dropbox.android",
        "Zoom" to "us.zoom.videomeetings",
        "Google Drive" to "com.google.android.apps.docs",
        "Google Calendar" to "com.google.android.calendar",
        "Google Translate" to "com.google.android.apps.translate",
        "Google Keep" to "com.google.android.keep",
        "Outlook" to "com.microsoft.office.outlook",
        "Google Docs" to "com.google.android.apps.docs.editors.docs",
        "Google Sheets" to "com.google.android.apps.docs.editors.sheets",
        "Microsoft Word" to "com.microsoft.office.word",
        "Microsoft Excel" to "com.microsoft.office.excel",
        "Microsoft PowerPoint" to "com.microsoft.office.powerpoint",
        "Telegram" to "org.telegram.messenger",
        "Signal" to "org.thoughtcrime.securesms",
        "Skype" to "com.skype.raider",
        "Microsoft Teams" to "com.microsoft.teams",
        "OneDrive" to "com.microsoft.skydrive",
        "Adobe Acrobat Reader" to "com.adobe.reader",
        "Canva" to "com.canva.editor",
        "Pinterest" to "com.pinterest",
        "Shazam" to "com.shazam.android",
        "Google Photos" to "com.google.android.apps.photos",
        "Google Chrome" to "com.android.chrome",
        "Firefox" to "org.mozilla.firefox",
        "Google Authenticator" to "com.google.android.apps.authenticator2",
        "Microsoft Authenticator" to "com.azure.authenticator",
        "Google Wallet" to "com.google.android.apps.walletnfcrel",
        "Amazon Shopping" to "com.amazon.mShop.android.shopping",
        "eBay" to "com.ebay.mobile",
        "Duolingo" to "com.duolingo",
        "Khan Academy" to "org.khanacademy.android",
        "Google Classroom" to "com.google.android.apps.classroom",
        "Notion" to "notion.id",
        "Evernote" to "com.evernote",
        "Trello" to "com.trello",
        "VLC" to "org.videolan.vlc",
        "Spotify" to "com.spotify.music"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getSharedPreferences("concept_protection", MODE_PRIVATE).edit().putBoolean("enabled", true).apply()
        val password = getSharedPreferences("concept_security", MODE_PRIVATE)
            .getString("login_password", null)
        val dpm = getSystemService(android.app.admin.DevicePolicyManager::class.java)
        val admin = android.content.ComponentName(this, ConceptDeviceAdminReceiver::class.java)
        if (dpm.isDeviceOwnerApp(packageName)) {
            runCatching { dpm.setLockTaskPackages(admin, arrayOf(packageName)) }
            runCatching { dpm.setLockTaskFeatures(admin, android.app.admin.DevicePolicyManager.LOCK_TASK_FEATURE_NONE) }
            runCatching { startLockTask() }
        }

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
            ) {
                Text("כניסה")
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

    private fun openPlay(pkg: String) {
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

    private val imageCache = LruCache<String, Bitmap>(80)

    private suspend fun loadBitmap(url: String, cacheKey: String): Bitmap? = withContext(Dispatchers.IO) {
        imageCache.get(cacheKey)?.let { return@withContext it }
        runCatching {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.connectTimeout = 8000
            connection.readTimeout = 8000
            connection.setRequestProperty("User-Agent", "Mozilla/5.0")
            val bitmap = connection.inputStream.use { BitmapFactory.decodeStream(it) }
            connection.disconnect()
            if (bitmap != null) imageCache.put(cacheKey, bitmap)
            bitmap
        }.getOrNull()
    }

    private suspend fun loadPlayIcon(pkg: String): Bitmap? = withContext(Dispatchers.IO) {
        val cacheKey = "play:$pkg"
        imageCache.get(cacheKey)?.let { return@withContext it }
        runCatching {
            val page = URL("https://play.google.com/store/apps/details?id=$pkg&hl=en&gl=US").openConnection() as HttpURLConnection
            page.connectTimeout = 12000
            page.readTimeout = 12000
            page.instanceFollowRedirects = true
            page.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 11) AppleWebKit/537.36 Chrome/154 Mobile Safari/537.36")
            page.setRequestProperty("Accept", "text/html,application/xhtml+xml")
            page.setRequestProperty("Accept-Language", "en-US,en;q=0.9")
            val html = page.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            page.disconnect()

            val imageUrl = sequenceOf(
                Regex("""property\\s*=\\s*["']og:image["'][^>]*content\\s*=\\s*["']([^"']+)["']""", RegexOption.IGNORE_CASE),
                Regex("""content\\s*=\\s*["']([^"']+)["'][^>]*property\\s*=\\s*["']og:image["']""", RegexOption.IGNORE_CASE),
                Regex("""<meta[^>]+property=["']og:image["'][^>]+content=["']([^"']+)["']""", RegexOption.IGNORE_CASE),
                Regex("""<meta[^>]+content=["']([^"']+)["'][^>]+property=["']og:image["']""", RegexOption.IGNORE_CASE)
            ).mapNotNull { it.find(html)?.groupValues?.getOrNull(1) }.firstOrNull()

            val cleanedUrl = imageUrl
                ?.replace("&amp;", "&")
                ?.replace("\\/", "/")
                ?.replace("&quot;", "\"")
                ?: return@withContext null

            loadBitmap(cleanedUrl, cacheKey)
        }.getOrNull()
    }

    private suspend fun loadSiteIcon(siteUrl: String): Bitmap? {
        val host = Uri.parse(siteUrl).host ?: return null
        return loadBitmap("https://www.google.com/s2/favicons?domain=$host&sz=128", "site:$host")
    }
    @Composable
    private fun PlayStoreCard(item: StoreItem) {
        var bitmap by remember(item.packageName) { mutableStateOf(imageCache.get("play:${item.packageName}")) }
        LaunchedEffect(item.packageName) {
            val loaded = loadPlayIcon(item.packageName)
            if (loaded != null) bitmap = loaded
        }

        androidx.compose.material3.Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clickable { openPlay(item.packageName) }
        ) {
            Row(
                Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap!!.asImageBitmap(),
                        contentDescription = item.label,
                        modifier = Modifier.size(56.dp)
                    )
                } else {
                    Box(Modifier.size(56.dp), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.SportsEsports, null, modifier = Modifier.size(36.dp))
                    }
                }
                Spacer(Modifier.width(12.dp))
                Text(item.label, Modifier.weight(1f))
                Text("Google Play")
            }
        }
    }

    @Composable
    fun App() {
        var ready by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            delay(550)
            ready = true
        }

        if (!ready) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier.size(96.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher),
                            contentDescription = "Concept",
                            modifier = Modifier.size(64.dp)
                        )
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(92.dp)
                                .align(Alignment.TopCenter),
                            strokeWidth = 3.dp
                        )
                    }
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

                FloatingActionButton(
                    onClick = {
                        startActivity(Intent(this@MainActivity, AIChatActivity::class.java))
                    },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(Icons.Default.Chat, contentDescription = "צ׳אט AI")
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

        val apps = (usefulApps.map { StoreItem(it.first, it.second) } + saved)
            .distinctBy { it.packageName }

        Column(Modifier.fillMaxSize()) {
            Text(
                "אפליקציות",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                textAlign = TextAlign.Right
            )
            LazyColumn(Modifier.weight(1f)) {
                items(apps, key = { it.packageName }) { app ->
                    PlayStoreCard(app)
                }
            }
        }
    }

    @Composable
    fun Games() {
        val baseGames = listOf(
            StoreItem("שחמט", "com.chess"),
            StoreItem("סודוקו", "com.easybrain.sudoku.android"),
            StoreItem("Flow Free", "com.bigduckgames.flow"),
            StoreItem("2048", "com.androbaby.game2048"),
            StoreItem("Solitaire", "com.mobirix.solitaire"),
            StoreItem("Block Puzzle", "com.blockpuzzle.game"),
            StoreItem("Word Search", "com.wordsearch.puzzle"),
            StoreItem("Minesweeper", "com.microsoft.minesweeper"),
            StoreItem("Tetris", "com.n3twork.tetris"),
            StoreItem("ChessKid", "com.chesskid"),
            StoreItem("Nonogram.com", "com.easybrain.nonogram"),
            StoreItem("Wordscapes", "com.peoplefun.wordcross"),
            StoreItem("Jigsaw Puzzle", "com.easybrain.jigsaw.puzzles"),
            StoreItem("Unblock Me", "com.kiragames.unblockmefree"),
            StoreItem("Checkers", "com.litegames.checkers.free"),
            StoreItem("Backgammon", "com.litegames.backgammon.free"),
            StoreItem("Four in a Row", "com.litegames.fourinarow"),
            StoreItem("Reversi", "com.litegames.reversi.free"),
            StoreItem("Word Cookies", "com.bitmango.go.wordcookies"),
            StoreItem("Word Connect", "com.wordgames.wordconnect"),
            StoreItem("Mahjong", "com.bitmango.go.mahjong"),
            StoreItem("Dominoes", "com.iosdomino.domino"),
            StoreItem("Ludo King", "com.ludo.king"),
            StoreItem("Carrom Pool", "com.miniclip.carrom"),
            StoreItem("8 Ball Pool", "com.miniclip.eightballpool"),
            StoreItem("Hill Climb Racing", "com.fingersoft.hillclimb"),
            StoreItem("Subway Surfers", "com.kiloo.subwaysurf"),
            StoreItem("Temple Run 2", "com.imangi.templerun2"),
            StoreItem("Angry Birds 2", "com.rovio.baba"),
            StoreItem("Cut the Rope", "com.zeptolab.ctr.ads"),
            StoreItem("Fruit Ninja", "com.halfbrick.fruitninjafree"),
            StoreItem("Jetpack Joyride", "com.halfbrick.jetpackjoyride"),
            StoreItem("Geometry Dash Lite", "com.robtopx.geometryjumplite"),
            StoreItem("Crossy Road", "com.yodo1.crossyroad"),
            StoreItem("Stack", "com.ketchapp.stack")
        )

        val saved = getSharedPreferences("concept_items", MODE_PRIVATE)
            .getStringSet("games", emptySet())
            .orEmpty()
            .mapNotNull {
                val p = it.split("|", limit = 2)
                if (p.size == 2) StoreItem(p[0], p[1]) else null
            }

        val games = (baseGames + saved).distinctBy { it.packageName }

        Column(Modifier.fillMaxSize()) {
            Text(
                "משחקים",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                textAlign = TextAlign.Right
            )
            LazyColumn(Modifier.weight(1f)) {
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

    override fun onResume() {
        super.onResume()
        getSharedPreferences("chrome_lock", MODE_PRIVATE).edit().putBoolean("enabled", false).apply()
    }

    private fun openSiteInChrome(url: String) {
        val uri = Uri.parse(url)
        getSharedPreferences("chrome_lock", MODE_PRIVATE).edit()
            .putBoolean("enabled", true)
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
            Text(
                "חסימת אפליקציות",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                textAlign = TextAlign.Right
            )
            if (loading) {
                LoadingDots()
            } else {
                LazyColumn(Modifier.weight(1f)) {
                    items(apps, key = { it.packageName }) { app ->
                        val enabled = app.packageName in blocked
                        AppCard(
                            app,
                            onClick = {
                                val newValue = !enabled
                                prefs.edit().putBoolean(app.packageName, newValue).apply()
                                blocked = if (newValue) blocked + app.packageName else blocked - app.packageName
                            }
                        ) {
                            Switch(checked = enabled, onCheckedChange = null)
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

    @Composable
    fun SettingsScreen() {
        Column(Modifier.fillMaxSize()) {
            Text(
                "הגדרות",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                textAlign = TextAlign.Right
            )
            Spacer(Modifier.height(12.dp))

            Button(
                onClick = { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("נגישות")
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    val admin = ComponentName(this@MainActivity, ConceptDeviceAdminReceiver::class.java)
                    startActivity(Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                        putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, admin)
                        putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "נדרש אישור שליטה על הטלפון כדי להגן על האפליקציה מפני הסרה.")
                    })
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("אישור שליטה על הטלפון")
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    startActivity(
                        Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:$packageName")
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("הצגה מעל אפליקציות")
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("נתוני שימוש")
            }

            Spacer(Modifier.height(8.dp))

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
                                singleLine = true
                            )
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it; passwordError = "" },
                                label = { Text("אימות סיסמה") },
                                singleLine = true
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
                                isError = disablePasswordError
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

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

        // Keep Android 11 launch path minimal: draw the UI first.
        val password = runCatching {
            getSharedPreferences("concept_security", MODE_PRIVATE)
                .getString("login_password", null)
        }.getOrNull()

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

        // Protection/device-owner work is deliberately outside the first frame.
        window.decorView.post {
            runCatching {
                getSharedPreferences("concept_protection", MODE_PRIVATE)
                    .edit()
                    .putBoolean("enabled", true)
                    .apply()

                val alwaysBlockedPackages = alwaysBlockedApps.map { it.packageName }.toSet()
                getSharedPreferences("blocked", MODE_PRIVATE).edit().apply {
                    alwaysBlockedPackages.forEach { putBoolean(it, true) }
                    apply()
                }

                val dpm = getSystemService(DevicePolicyManager::class.java)
                val admin = ComponentName(this, ConceptDeviceAdminReceiver::class.java)
                if (dpm.isDeviceOwnerApp(packageName)) {
                    runCatching {
                        dpm.setLockTaskPackages(admin, arrayOf(packageName))
                        dpm.setLockTaskFeatures(
                            admin,
                            DevicePolicyManager.LOCK_TASK_FEATURE_NONE
                        )
                    }
                    // Do not call startLockTask during launch; it can interfere with
                    // the first activity transition on some Android 11 devices.
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

    private val imageCache = LruCache<String, Bitmap>(80)

    private fun installedDrawable(pkg: String): android.graphics.drawable.Drawable? =
        runCatching { packageManager.getApplicationInfo(pkg, PackageManager.GET_META_DATA).loadIcon(packageManager) }.getOrNull()

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
    private fun PlayStoreIcon(packageName: String, label: String) {
        var bitmap by remember(packageName) { mutableStateOf<Bitmap?>(null) }
        LaunchedEffect(packageName) {
            bitmap = withContext(Dispatchers.IO) { loadPlayStoreIconSafe(packageName) }
        }
        if (bitmap != null) {
            androidx.compose.foundation.Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = label,
                modifier = Modifier.size(42.dp)
            )
        } else {
            Icon(Icons.Default.Apps, contentDescription = label, modifier = Modifier.size(42.dp))
        }
    }

    private val safeIconCache = LruCache<String, Bitmap>(32)
    private val safeIconLock = Any()

    private fun loadPlayStoreIconSafe(packageName: String): Bitmap? {
        synchronized(safeIconLock) { safeIconCache.get(packageName)?.let { return it } }
        return runCatching {
            val page = URL("https://play.google.com/store/apps/details?id=$packageName&hl=en")
                .openConnection() as HttpURLConnection
            page.connectTimeout = 3000
            page.readTimeout = 3000
            page.setRequestProperty("User-Agent", "Mozilla/5.0")
            val html = page.inputStream.bufferedReader().use { it.readText() }
            page.disconnect()
            val matches = listOf(
                Regex("""<meta[^>]+property=["']og:image["'][^>]+content=["']([^"']+)["']""", RegexOption.IGNORE_CASE),
                Regex("""<meta[^>]+content=["']([^"']+)["'][^>]+property=["']og:image["']""", RegexOption.IGNORE_CASE)
            )
            val imageUrl = matches.asSequence().mapNotNull { it.find(html)?.groupValues?.getOrNull(1) }
                .firstOrNull()?.replace("&amp;", "&") ?: return@runCatching null
            val image = URL(imageUrl).openConnection() as HttpURLConnection
            image.connectTimeout = 3000
            image.readTimeout = 3000
            image.setRequestProperty("User-Agent", "Mozilla/5.0")
            val bmp = image.inputStream.use { BitmapFactory.decodeStream(it) }
            image.disconnect()
            if (bmp != null) synchronized(safeIconLock) { safeIconCache.put(packageName, bmp) }
            bmp
        }.getOrNull()
    }


    @Composable
    private fun PlayStoreCard(
        item: StoreItem,
        online: Boolean,
        loadDelayMs: Long = 0L
    ) {
        Card(modifier = Modifier.padding(2.dp).fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(5.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PlayStoreIcon(packageName = item.packageName, label = item.label)
                Text(
                    item.label,
                    maxLines = 2,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.fillMaxWidth().height(34.dp)
                )
                Button(
                    onClick = { openPlay(item.packageName) },
                    modifier = Modifier.fillMaxWidth().height(34.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("התקנה", fontSize = 10.sp)
                }
            }
        }
    }

    @Composable
    fun App() {
        // Safe startup screen: no network, PackageManager, bitmap, or large grid
        // is composed until the user explicitly selects a section.
        var tab by rememberSaveable { mutableIntStateOf(-1) }

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
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                when (tab) {
                    -1 -> {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Apps,
                                contentDescription = "Concept",
                                modifier = Modifier.size(72.dp)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text("Concept", style = MaterialTheme.typography.headlineLarge)
                            Spacer(Modifier.height(8.dp))
                            Text("בחר קטגוריה למטה", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                    0 -> Games()
                    1 -> Apps()
                    2 -> Sites()
                    3 -> BlockApps()
                    4 -> SettingsScreen()
                }
            }
        }
    }

    @Composable
    fun Apps() {
        val apps = remember {
            usefulApps.distinctBy { it.packageName }
        }
        StoreGrid(items = apps)
    }

    @Composable
    fun Games() {
        val games = remember {
            listOf(
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
                StoreItem("Among Us", "com.innersloth.spacemafia"),
                StoreItem("Stumble Guys", "com.kitkagames.fallbuddies"),
                StoreItem("Magic Tiles 3", "com.youmusic.magictiles"),
                StoreItem("Wordscapes", "com.peoplefun.wordcross"),
                StoreItem("Monument Valley", "com.ustwo.monumentvalley"),
                StoreItem("Alto's Odyssey", "com.noodlecake.altosodyssey"),
                StoreItem("Terraria", "com.and.games505.TerrariaPaid"),
                StoreItem("Stardew Valley", "com.chucklefish.stardewvalley"),
                StoreItem("Bloons TD 6", "com.ninjakiwi.bloonstd6"),
                StoreItem("Kingdom Rush", "com.ironhidegames.android.kingdomrush"),
                StoreItem("Fruit Ninja 2", "com.halfbrick.fruitninja2"),
                StoreItem("Pou", "me.pou.app"),
                StoreItem("Talking Tom Gold Run", "com.outfit7.talkingtomgoldrun"),
                StoreItem("Cooking Fever", "com.nordcurrent.canteenhd"),
                StoreItem("Homescapes", "com.playrix.homescapes"),
                StoreItem("Fishdom", "com.playrix.fishdomdd.gplay"),
                StoreItem("Bad Piggies", "com.rovio.BadPiggies"),
                StoreItem("Golf Clash", "com.playdemic.golf.android"),
                StoreItem("Sonic Dash", "com.sega.sonicdash"),
                StoreItem("Sonic Forces", "com.sega.sonicforces"),
                StoreItem("Mario Kart Tour", "com.nintendo.zaka"),
                StoreItem("Fire Emblem Heroes", "com.nintendo.zaba"),
                StoreItem("Pokémon Unite", "jp.pokemon.pokemonunite"),
                StoreItem("Pokémon Masters EX", "com.dena.a12026418"),
                StoreItem("Honkai: Star Rail", "com.HoYoverse.hkrpgoversea"),
                StoreItem("Honkai Impact 3rd", "com.miHoYo.bh3global"),
                StoreItem("Tower of Fantasy", "com.levelinfinite.hotta"),
                StoreItem("AFK Arena", "com.lilithgame.hgame.gp"),
                StoreItem("Raid: Shadow Legends", "com.plarium.raidlegends"),
                StoreItem("Summoners War", "com.com2us.smon.normal.freefull.google.global.android.common"),
                StoreItem("Guardian Tales", "com.kakaogames.gdts"),
                StoreItem("Marvel Snap", "com.nvsgames.snap"),
                StoreItem("Hearthstone", "com.blizzard.wtcg.hearthstone"),
                StoreItem("Yu-Gi-Oh! Master Duel", "jp.konami.masterduel"),
                StoreItem("Magic: The Gathering Arena", "com.wizards.mtga"),
                StoreItem("Plants vs Zombies 2", "com.ea.game.pvz2_row"),
                StoreItem("Plants vs Zombies", "com.ea.game.pvzfree_row"),
                StoreItem("Angry Birds Classic", "com.rovio.angrybirds"),
                StoreItem("Angry Birds Friends", "com.rovio.angrybirdsfriends"),
                StoreItem("Where's My Water?", "com.disney.WheresMyWater"),
                StoreItem("My Talking Tom", "com.outfit7.mytalkingtomfree"),
                StoreItem("My Talking Angela 2", "com.outfit7.mytalkingangela2"),
                StoreItem("Talking Tom Hero Dash", "com.outfit7.talkingtomhero_dash"),
                StoreItem("Subway Princess Runner", "com.rsg.runner"),
                StoreItem("Temple Run", "com.imangi.templerun"),
                StoreItem("Minion Rush", "com.gameloft.android.ANMP.GloftDMHM"),
                StoreItem("Sonic Runners Adventure", "com.sega.sonicrunners"),
                StoreItem("Asphalt 9", "com.gameloft.android.ANMP.GloftA9HM"),
                StoreItem("Asphalt 8", "com.gameloft.android.ANMP.GloftA8HM"),
                StoreItem("Real Racing 3", "com.ea.games.r3_row"),
                StoreItem("Need for Speed No Limits", "com.ea.game.nfs14_row"),
                StoreItem("CSR Racing 2", "com.naturalmotion.customstreetracer2"),
                StoreItem("Hill Climb Racing 2", "com.fingersoft.hcr2"),
                StoreItem("Beach Buggy Racing", "com.vectorunit.purple"),
                StoreItem("Beach Buggy Racing 2", "com.vectorunit.cobalt"),
                StoreItem("Traffic Rider", "com.skgames.trafficrider"),
                StoreItem("Traffic Racer", "com.skgames.trafficracer"),
                StoreItem("Shadow Fight 2", "com.nekki.shadowfight"),
                StoreItem("Shadow Fight 3", "com.nekki.shadowfight3"),
                StoreItem("Vector", "com.nekki.vector"),
                StoreItem("Vector 2", "com.nekki.vector2"),
                StoreItem("Real Steel", "com.jumpgames.rs"),
                StoreItem("WWE SuperCard", "com.catdaddy.wweent"),
                StoreItem("NBA 2K Mobile", "com.t2ksports.myteam"),
                StoreItem("Basketball Stars", "com.miniclip.basketballstars"),
                StoreItem("Golf Rival", "com.sports.real.golf.rival"),
                StoreItem("Chess.com", "com.chess"),
                StoreItem("Sudoku", "com.easybrain.sudoku.android"),
                StoreItem("Solitaire", "com.mobirix.solitaire"),
                StoreItem("2048", "com.androbaby.game2048"),
                StoreItem("Word Cookies", "com.bitmango.go.wordcookies"),
                StoreItem("Words of Wonders", "com.fugo.wow"),
                StoreItem("Crossword Jam", "com.playday.crossword"),
                StoreItem("Trivia Crack", "com.etermax.preguntados.lite"),
                StoreItem("Quizlet", "com.quizlet.quizletandroid"),
                StoreItem("Stack", "com.ketchapp.stack"),
                StoreItem("Helix Jump", "com.amanotes.helixjump"),
                StoreItem("Color Switch", "com.colorswitch.switch2"),
                StoreItem("Doodle Jump", "com.lima.doodlejump"),
                StoreItem("Smash Hit", "com.mediocre.smashhit"),
                StoreItem("Geometry Dash", "com.robtopx.geometryjump"),
                StoreItem("Badland", "com.frogmind.badland"),
                StoreItem("BADLAND 2", "com.frogmind.badland2"),
                StoreItem("Soul Knight", "com.ChillyRoom.DungeonShooter"),
                StoreItem("Archero", "com.habby.archero"),
                StoreItem("Vampire Survivors", "com.poncle.vampiresurvivors"),
                StoreItem("20 Minutes Till Dawn", "com.flanne.20minutestilldawn"),
                StoreItem("Survivor.io", "com.dxx.firenow"),
                StoreItem("Last Day on Earth", "zombie.survival.craft.z"),
                StoreItem("Grim Soul", "fantasy.survival.game.rpg"),
                StoreItem("Roblox Studio", "com.roblox.client"),
                StoreItem("Fall Guys", "com.mediatonic.fallguys"),
                StoreItem("Rocket League Sideswipe", "com.Psyonix.RL2D"),
                StoreItem("World of Tanks Blitz", "com.tanksblitz"),
                StoreItem("World of Warships Blitz", "com.wargaming.wows.blitz"),
                StoreItem("Modern Combat 5", "com.gameloft.android.ANMP.GloftM5HM"),
                StoreItem("Critical Ops", "com.criticalforceentertainment.criticalops"),
                StoreItem("Shadowgun Legends", "com.madfingergames.legends"),
                StoreItem("Into the Dead 2", "com.pikpok.dr2.play"),
                StoreItem("Dead Trigger 2", "com.madfingergames.deadtrigger2"),
                StoreItem("Hungry Shark World", "com.fgol.HungrySharkEvolution"),
                StoreItem("Hungry Shark Evolution", "com.fgol.HungrySharkEvolution"),
                StoreItem("Jurassic World Alive", "com.ludia.jw2"),
                StoreItem("Dragon City", "es.socialpoint.dragoncity"),
                StoreItem("Monster Legends", "es.socialpoint.MonsterLegends"),
                StoreItem("My Singing Monsters", "com.bigbluebubble.my singing monsters"),
                StoreItem("Cookie Run Kingdom", "com.devsisters.ck"),
                StoreItem("Gardenscapes", "com.playrix.gardenscapes"),
                StoreItem("Fishdom", "com.playrix.fishdomdd.gplay")
            )
        }
        StoreGrid(items = games)
    }

    @Composable
    private fun StoreGrid(items: List<StoreItem>) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(6.dp)
        ) {
            items(items.chunked(4)) { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    row.forEach { item ->
                        Box(Modifier.weight(1f)) {
                            PlayStoreCard(item = item, online = true)
                        }
                    }
                    repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
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
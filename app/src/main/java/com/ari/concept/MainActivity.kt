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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
        val alwaysBlockedPackages = alwaysBlockedApps.map { it.packageName }.toSet()
        getSharedPreferences("blocked", MODE_PRIVATE).edit().apply {
            alwaysBlockedPackages.forEach { putBoolean(it, true) }
            apply()
        }
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
    private fun PlayStoreCard(item: StoreItem) {
        var bitmap by remember(item.packageName) { mutableStateOf(imageCache.get("play:" + item.packageName)) }
        val localIcon = remember(item.packageName) { installedDrawable(item.packageName) }

        LaunchedEffect(item.packageName) {
            val loaded = loadPlayIcon(item.packageName)
            if (loaded != null) bitmap = loaded
        }

        if (bitmap == null && localIcon == null) return

        Card(
            modifier = Modifier
                .padding(4.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(68.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        bitmap != null -> Image(
                            bitmap!!.asImageBitmap(),
                            contentDescription = item.label,
                            modifier = Modifier.size(58.dp)
                        )
                        localIcon != null -> androidx.compose.ui.viewinterop.AndroidView(
                            factory = {
                                android.widget.ImageView(it).apply {
                                    setImageDrawable(localIcon)
                                    scaleType = android.widget.ImageView.ScaleType.CENTER_INSIDE
                                }
                            },
                            modifier = Modifier.size(58.dp)
                        )
                        else -> Icon(
                            Icons.Default.SportsEsports,
                            contentDescription = item.label,
                            modifier = Modifier.size(42.dp)
                        )
                    }
                }

                Text(
                    item.label,
                    maxLines = 2,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                )

                Button(
                    onClick = { openPlay(item.packageName) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 0.dp)
                ) {
                    Text("התקנה", fontSize = 11.sp)
                }
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
                            label = { if (i > 1) Text(label) }
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

        var selectedCategory by rememberSaveable { mutableStateOf("הכול") }

        Column(Modifier.fillMaxSize()) {
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

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(visibleApps, key = { it.packageName }) { app ->
                    PlayStoreCard(app)
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
            StoreItem("Wordscapes", "com.peoplefun.wordcross")
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
        Column(Modifier.fillMaxSize()) {
            Text(
                "הגדרות",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                textAlign = TextAlign.Right
            )
            Spacer(Modifier.height(12.dp))

            val accessibilityOk = accessibilityEnabled()
            val adminOk = deviceAdminEnabled()
            val overlayOk = overlayEnabled()
            val usageOk = usageAccessEnabled()

            PermissionButton("נגישות", accessibilityOk) {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }

            Spacer(Modifier.height(12.dp))

            PermissionButton("אישור שליטה על הטלפון", adminOk) {
                val admin = ComponentName(this@MainActivity, ConceptDeviceAdminReceiver::class.java)
                startActivity(Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                    putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, admin)
                    putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "נדרש אישור שליטה על הטלפון כדי להגן על האפליקציה מפני הסרה.")
                })
            }

            Spacer(Modifier.height(12.dp))

            PermissionButton("הצגה מעל אפליקציות", overlayOk) {
                startActivity(
                    Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                )
            }

            Spacer(Modifier.height(12.dp))

            PermissionButton("נתוני שימוש", usageOk) {
                startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            }

            Spacer(Modifier.height(12.dp))

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
                            Spacer(Modifier.height(12.dp))
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
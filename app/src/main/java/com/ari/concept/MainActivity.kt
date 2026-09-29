package com.ari.concept

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
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
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

data class AppItem(val label:String,val packageName:String,val info:ApplicationInfo)
data class StoreItem(val label:String,val packageName:String)

class MainActivity: ComponentActivity() {
    private val sites=listOf(
        "כיכר השבת" to "https://www.kikar.co.il/","JDN" to "https://www.jdn.co.il/",
        "בחדרי חרדים" to "https://www.bhol.co.il/","המחדש" to "https://hm-news.co.il/",
        "דרשו" to "https://www.dirshu.co.il/","בנק הפועלים" to "https://www.bankhapoalim.co.il/",
        "בנק לאומי" to "https://www.leumi.co.il/","בנק דיסקונט" to "https://www.discountbank.co.il/",
        "מכבי" to "https://www.maccabi4u.co.il/","כללית" to "https://www.clalit.co.il/",
        "מאוחדת" to "https://www.meuhedet.co.il/","לאומית" to "https://www.leumit.co.il/",
        "ביטוח לאומי" to "https://www.btl.gov.il/","דואר ישראל" to "https://israelpost.co.il/"
    )
    private val usefulApps=listOf(
        "בנק הפועלים" to "com.ideomobile.il","בנק לאומי" to "com.leumi.leumiwallet",
        "בנק דיסקונט" to "com.discountbank.mobile","מכבי" to "com.maccabi.health",
        "כללית" to "com.clalit.clalit","מאוחדת" to "com.meuhedet","לאומית" to "com.leumit",
        "WhatsApp" to "com.whatsapp","Waze" to "com.waze","Google Maps" to "com.google.android.apps.maps","Moovit" to "com.tranzmate","PayBox" to "com.payboxapp","Dropbox" to "com.dropbox.android","Zoom" to "us.zoom.videomeetings","Google Drive" to "com.google.android.apps.docs","Google Calendar" to "com.google.android.calendar","Google Translate" to "com.google.android.apps.translate","Google Keep" to "com.google.android.keep","Outlook" to "com.microsoft.office.outlook"
    )
    override fun onCreate(savedInstanceState:Bundle?){ super.onCreate(savedInstanceState); setContent{ androidx.compose.runtime.CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl){ App() } } }

    private fun installedApps():List<AppItem> =
        packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { it.packageName != packageName && packageManager.getLaunchIntentForPackage(it.packageName)!=null }
            .map { AppItem(packageManager.getApplicationLabel(it).toString(),it.packageName,it) }
            .distinctBy { it.packageName }.sortedBy { it.label.lowercase() }

    private fun openPlay(pkg:String){
        val marketIntent=Intent(Intent.ACTION_VIEW,Uri.parse("market://details?id=$pkg")).apply {
            setPackage("com.android.vending")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            startActivity(marketIntent)
        } catch (_: Exception) {
            val browserIntent=Intent(Intent.ACTION_VIEW,Uri.parse("https://play.google.com/store/apps/details?id=$pkg&hl=he&gl=IL"))
            startActivity(browserIntent)
        }
    }

    @Composable private fun Icon(app:AppItem){
        androidx.compose.ui.viewinterop.AndroidView(factory={ android.widget.ImageView(it).apply {
            setImageDrawable(app.info.loadIcon(packageManager)); scaleType=android.widget.ImageView.ScaleType.CENTER_INSIDE
        }},modifier=Modifier.size(48.dp))
    }
    @Composable private fun Card(app:AppItem,onClick:()->Unit,trailing: (@Composable () -> Unit)? = null){
        Card(Modifier.fillMaxWidth().padding(vertical=4.dp).clickable{onClick()}){
            Row(Modifier.fillMaxWidth().padding(12.dp),verticalAlignment=Alignment.CenterVertically){
                Icon(app); Spacer(Modifier.width(12.dp)); Text(app.label,Modifier.weight(1f)); trailing?.invoke()
            }
        }
    }
    private suspend fun loadPlayIcon(pkg:String):Bitmap? = withContext(Dispatchers.IO) {
        runCatching {
            val page=URL("https://play.google.com/store/apps/details?id=$pkg&hl=en&gl=US").openConnection() as HttpURLConnection
            page.connectTimeout=10000; page.readTimeout=10000; page.setRequestProperty("User-Agent","Mozilla/5.0")
            val html=page.inputStream.bufferedReader().use { it.readText() }; page.disconnect()
            val match=Regex("""<meta[^>]+property=["']og:image["'][^>]+content=["']([^"']+)["']""",RegexOption.IGNORE_CASE).find(html)
                ?: Regex("""<meta[^>]+content=["']([^"']+)["'][^>]+property=["']og:image["']""",RegexOption.IGNORE_CASE).find(html)
            val imageUrl=match?.groupValues?.get(1)?.replace("&amp;","&") ?: return@withContext null
            val img=URL(imageUrl).openConnection() as HttpURLConnection
            img.connectTimeout=10000; img.readTimeout=10000; img.setRequestProperty("User-Agent","Mozilla/5.0")
            val bitmap=img.inputStream.use { BitmapFactory.decodeStream(it) }; img.disconnect(); bitmap
        }.getOrNull()
    }
    @Composable private fun PlayStoreCard(item:StoreItem){
        var bitmap by remember(item.packageName){ mutableStateOf<Bitmap?>(null) }
        LaunchedEffect(item.packageName){ bitmap=loadPlayIcon(item.packageName) }
        Card(Modifier.fillMaxWidth().padding(vertical=4.dp).clickable{openPlay(item.packageName)}){
            Row(Modifier.fillMaxWidth().padding(12.dp),verticalAlignment=Alignment.CenterVertically){
                if(bitmap!=null) Image(bitmap!!.asImageBitmap(),contentDescription=item.label,modifier=Modifier.size(56.dp))
                else Box(Modifier.size(56.dp),contentAlignment=Alignment.Center){ Icon(Icons.Default.SportsEsports,null,modifier=Modifier.size(36.dp)) }
                Spacer(Modifier.width(12.dp)); Text(item.label,Modifier.weight(1f)); Text("Google Play")
            }
        }
    }
    @Composable fun App(){
        var tab by remember{mutableIntStateOf(0)}
        val labels=listOf("משחקים","אפליקציות","אתרים","חסימה","הגדרות")
        val icons=listOf(Icons.Default.SportsEsports,Icons.Default.Apps,Icons.Default.Language,Icons.Default.Block,Icons.Default.Settings)
        Scaffold(bottomBar={ NavigationBar{ labels.forEachIndexed{ i,t -> NavigationBarItem(selected=tab==i,onClick={tab=i},icon={Icon(icons[i],contentDescription=t)},label={Text(t)}) } } }){p->
            Box(Modifier.fillMaxSize().padding(p).padding(12.dp)){ when(tab){0->Games();1->Apps();2->Sites();3->BlockApps();4->SettingsScreen()}; FloatingActionButton(onClick={startActivity(Intent(this@MainActivity,AIChatActivity::class.java))},modifier=Modifier.align(Alignment.TopEnd)){Icon(Icons.Default.Chat,contentDescription="צ׳אט AI")} }
        }
    }
    @Composable fun Apps(){
        val apps=usefulApps.map{StoreItem(it.first,it.second)} + getSharedPreferences("concept_items",MODE_PRIVATE).getStringSet("apps",emptySet())!!.mapNotNull{val p=it.split("|",limit=2);if(p.size==2)StoreItem(p[0],p[1])else null}.distinctBy{it.packageName}
        Text("אפליקציות",style=MaterialTheme.typography.headlineMedium,modifier=Modifier.fillMaxWidth(),textAlign=TextAlign.Right)
        LazyColumn{items(apps,key={it.packageName}){a->PlayStoreCard(a)}}
    }
    @Composable fun Games(){
        val games=listOf(
            StoreItem("שחמט","com.chess"),StoreItem("סודוקו","com.easybrain.sudoku.android"),
            StoreItem("Flow Free","com.bigduckgames.flow"),StoreItem("2048","com.androbaby.game2048"),
            StoreItem("Solitaire","com.mobirix.solitaire"),StoreItem("Block Puzzle","com.blockpuzzle.game"),
            StoreItem("Word Search","com.wordsearch.puzzle"),StoreItem("Minesweeper","com.microsoft.minesweeper"),
            StoreItem("Tetris","com.n3twork.tetris"),StoreItem("ChessKid","com.chesskid"),StoreItem("Nonogram.com","com.easybrain.nonogram"),StoreItem("Wordscapes","com.peoplefun.wordcross"),StoreItem("Jigsaw Puzzle","com.easybrain.jigsaw.puzzles"),StoreItem("Unblock Me","com.kiragames.unblockmefree"),StoreItem("Checkers","com.litegames.checkers.free"),StoreItem("Backgammon","com.litegames.backgammon.free"),StoreItem("Four in a Row","com.litegames.fourinarow.free"),StoreItem("Reversi","com.litegames.reversi.free")
        ) + getSharedPreferences("concept_items",MODE_PRIVATE).getStringSet("games",emptySet())!!.mapNotNull{val p=it.split("|",limit=2);if(p.size==2)StoreItem(p[0],p[1])else null}
        Text("משחקים",style=MaterialTheme.typography.headlineMedium,modifier=Modifier.fillMaxWidth(),textAlign=TextAlign.Right)
        LazyColumn{items(games,key={it.packageName}){g->PlayStoreCard(g)}}
    }
NaN    @Composable fun BlockApps(){
        val apps by produceState(initialValue=emptyList<AppItem>()){ value=installedApps() }
        val prefs=getSharedPreferences("blocked",MODE_PRIVATE)
        var blocked by remember{mutableStateOf(prefs.all.filterValues{it is Boolean && it}.keys.toSet())}
        Text("חסימת אפליקציות",style=MaterialTheme.typography.headlineMedium,modifier=Modifier.fillMaxWidth(),textAlign=TextAlign.Right)
        LazyColumn{items(apps,key={it.packageName}){a->
            val on=a.packageName in blocked
            Card(a,{ val n=!on; prefs.edit().putBoolean(a.packageName,n).apply(); blocked=if(n) blocked+a.packageName else blocked-a.packageName }){ Switch(checked=on,onCheckedChange=null) }
        }}
    }
    @Composable fun SettingsScreen(){
        Text("הגדרות",style=MaterialTheme.typography.headlineMedium,modifier=Modifier.fillMaxWidth(),textAlign=TextAlign.Right); Spacer(Modifier.height(12.dp))
        Button({startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))},Modifier.fillMaxWidth()){Text("נגישות")}
        Spacer(Modifier.height(8.dp)); Button({startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:$packageName")))},Modifier.fillMaxWidth()){Text("הצגה מעל אפליקציות")}
        Spacer(Modifier.height(8.dp)); Button({startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))},Modifier.fillMaxWidth()){Text("נתוני שימוש")}
        Spacer(Modifier.height(8.dp)); Button({startActivity(Intent(Settings.ACTION_SETTINGS))},Modifier.fillMaxWidth()){Text("הגדרות Android")}
    }
}
class RestrictedWebActivity:ComponentActivity(){
    override fun onCreate(b:Bundle?){ super.onCreate(b)
        val start=Uri.parse(intent.getStringExtra("url") ?: "https://www.google.com/")
        val exact=intent.getStringExtra("exact") ?: start.toString(); val allowedHost=start.host
        val w=WebView(this).apply{
            settings.javaScriptEnabled=true; settings.domStorageEnabled=true; settings.setSupportMultipleWindows(false)
            settings.javaScriptCanOpenWindowsAutomatically=false; settings.loadsImagesAutomatically=true
            settings.allowFileAccess=false; settings.allowContentAccess=true
            webViewClient=object:WebViewClient(){ override fun shouldOverrideUrlLoading(view:WebView,request:WebResourceRequest):Boolean{
                val host=request.url.host; return host == null || host != allowedHost
            }}; loadUrl(exact)
        }
        setContentView(w)
    }
}
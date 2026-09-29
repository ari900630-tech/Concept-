package com.ari.concept

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

data class AppItem(val label:String,val packageName:String,val info:ApplicationInfo)
data class StoreItem(val label:String,val packageName:String)

class MainActivity: ComponentActivity() {
    private val sites=listOf(
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
        "דואר ישראל" to "https://israelpost.co.il/"
    )

    private val usefulApps=listOf(
        "בנק הפועלים" to "com.ideomobile.il",
        "בנק לאומי" to "com.leumi.leumiwallet",
        "בנק דיסקונט" to "com.discountbank.mobile",
        "מכבי" to "com.maccabi.health",
        "כללית" to "com.clalit.clalit",
        "מאוחדת" to "com.meuhedet",
        "לאומית" to "com.leumit",
        "WhatsApp" to "com.whatsapp",
        "Waze" to "com.waze",
        "Google Maps" to "com.google.android.apps.maps"
    )

    override fun onCreate(savedInstanceState:Bundle?){ super.onCreate(savedInstanceState); setContent{ App() } }

    private fun installedApps():List<AppItem> =
        packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { it.packageName != packageName && packageManager.getLaunchIntentForPackage(it.packageName)!=null }
            .map { AppItem(packageManager.getApplicationLabel(it).toString(),it.packageName,it) }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }

    private fun installedGames():List<AppItem> = emptyList()

    private fun openPlay(pkg:String){
        val url="https://play.google.com/store/apps/details?id=$pkg"
        startActivity(Intent(this,RestrictedWebActivity::class.java).putExtra("url",url).putExtra("exact",url))
    }

    @Composable private fun Icon(app:AppItem){
        AndroidView(factory={ ImageView(it).apply{ setImageDrawable(app.info.loadIcon(packageManager)); scaleType=ImageView.ScaleType.CENTER_INSIDE } },
            modifier=Modifier.size(48.dp))
    }

    @Composable private fun Card(app:AppItem,onClick:()->Unit,trailing: (@Composable () -> Unit)? = null){
        Card(Modifier.fillMaxWidth().padding(vertical=4.dp).clickable{onClick()}){
            Row(Modifier.fillMaxWidth().padding(12.dp),verticalAlignment=Alignment.CenterVertically){
                Icon(app); Spacer(Modifier.width(12.dp)); Text(app.label,Modifier.weight(1f))
                trailing?.invoke()
            }
        }
    }

    @Composable private fun StoreCard(item:StoreItem,onClick:()->Unit){
        Card(Modifier.fillMaxWidth().padding(vertical=4.dp).clickable{onClick()}){
            Row(Modifier.fillMaxWidth().padding(12.dp),verticalAlignment=Alignment.CenterVertically){
                Text("🎮",style=MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.width(12.dp))
                Text(item.label,Modifier.weight(1f))
                Text("Google Play")
            }
        }
    }

    @Composable fun App(){
        var tab by remember{mutableIntStateOf(0)}
        val labels=listOf("משחקים","אפליקציות","אתרים","חסימה","הגדרות")
        Scaffold(bottomBar={
            NavigationBar{
                labels.forEachIndexed{ i,t -> NavigationBarItem(selected=tab==i,onClick={tab=i},icon={Text(if(i==3)"●" else "•")},label={Text(t)}) }
            }
        }){p-> Column(Modifier.fillMaxSize().padding(p).padding(12.dp)){
            when(tab){0->Games();1->Apps();2->Sites();3->BlockApps();4->SettingsScreen()}
        }}
    }

    @Composable fun Apps(){
        val apps by produceState(initialValue=emptyList<AppItem>()){
            value=usefulApps.mapNotNull { (label,pkg) ->
                runCatching { packageManager.getApplicationInfo(pkg,0) }.getOrNull()?.let { AppItem(label,pkg,it) }
            }
        }
        Text("אפליקציות שימושיות",style=MaterialTheme.typography.headlineMedium)
        Text("שירותים שימושיים וחיוניים בלבד.")
        LazyColumn{items(apps,key={it.packageName}){a->Card(a,{openPlay(a.packageName)})}}
    }

    @Composable fun Games(){
        val games=listOf(
            StoreItem("שחמט", "com.chess"),
            StoreItem("סודוקו", "com.easybrain.sudoku.android"),
            StoreItem("Flow Free", "com.bigduckgames.flow"),
            StoreItem("2048", "com.androbaby.game2048"),
            StoreItem("Solitaire", "com.mobirix.solitaire"),
            StoreItem("Block Puzzle", "com.blockpuzzle.game"),
            StoreItem("Word Search", "com.wordsearch.puzzle"),
            StoreItem("Minesweeper", "com.microsoft.minesweeper"),
            StoreItem("Tetris", "com.n3twork.tetris"),
            StoreItem("Chess Kid", "com.chesskid")
        )
        Text("משחקים",style=MaterialTheme.typography.headlineMedium)
        Text("משחקים משפחתיים ורגועים שנבחרו מראש.")
        LazyColumn{items(games,key={it.packageName}){g->StoreCard(g,{openPlay(g.packageName)})}}
    }

    @Composable fun Sites(){
        Text("אתרים",style=MaterialTheme.typography.headlineMedium)
        Text("אתרים שימושיים שנבחרו מראש.")
        LazyColumn{items(sites){(name,url)->
            Card(AppItem(name,"",applicationInfo),{
                startActivity(Intent(this@MainActivity,RestrictedWebActivity::class.java)
                    .putExtra("url",url).putExtra("exact",url))
            })
        }}
    }

    @Composable fun BlockApps(){
        val apps by produceState(initialValue=emptyList<AppItem>()){ value=installedApps() }
        val prefs=getSharedPreferences("blocked",MODE_PRIVATE)
        var blocked by remember{mutableStateOf(prefs.all.filterValues{it is Boolean && it}.keys.toSet())}
        Text("חסימת אפליקציות",style=MaterialTheme.typography.headlineMedium)
        Text("מוצגות רק אפליקציות שמותקנות בטלפון. לחץ על כל הכרטיס כדי להפעיל או לכבות.")
        LazyColumn{items(apps,key={it.packageName}){a->
            val on=a.packageName in blocked
            Card(a,{
                val n=!on
                prefs.edit().putBoolean(a.packageName,n).apply()
                blocked=if(n) blocked+a.packageName else blocked-a.packageName
            }){ Switch(checked=on,onCheckedChange=null) }
        }}
    }

    @Composable fun SettingsScreen(){
        Text("הגדרות ואישורים",style=MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text("הפעל את ההרשאות הדרושות לחסימת אפליקציות ולהצגה מעל אפליקציות אחרות.")
        Spacer(Modifier.height(12.dp))
        Button({startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))},Modifier.fillMaxWidth()){Text("הרשאת נגישות — חסימת אפליקציות")}
        Spacer(Modifier.height(8.dp))
        Button({startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:$packageName")))},Modifier.fillMaxWidth()){Text("הצגה מעל אפליקציות אחרות")}
        Spacer(Modifier.height(8.dp))
        Button({startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))},Modifier.fillMaxWidth()){Text("גישה לנתוני שימוש")}
        Spacer(Modifier.height(8.dp))
        Button({startActivity(Intent(Settings.ACTION_SETTINGS))},Modifier.fillMaxWidth()){Text("הגדרות Android")}
        Spacer(Modifier.height(12.dp))
        Text("מניעת הסרה מלאה מחייבת ניהול מכשיר / Device Owner; אפליקציה רגילה אינה יכולה להבטיח זאת.")
    }
}

class RestrictedWebActivity:ComponentActivity(){
    override fun onCreate(b:Bundle?){
        super.onCreate(b)
        val start=Uri.parse(intent.getStringExtra("url") ?: "https://www.google.com/")
        val exact=intent.getStringExtra("exact") ?: start.toString()
        val w=WebView(this).apply{
            settings.javaScriptEnabled=true
            settings.domStorageEnabled=true
            settings.setSupportMultipleWindows(false)
            settings.javaScriptCanOpenWindowsAutomatically=false
            webViewClient=object:WebViewClient(){
                override fun shouldOverrideUrlLoading(view:WebView,request:WebResourceRequest):Boolean{
                    return request.url.toString()!=exact
                }
            }
            loadUrl(exact)
        }
        val root=android.widget.LinearLayout(this).apply{orientation=android.widget.LinearLayout.VERTICAL}
        val bar=android.widget.LinearLayout(this)
        bar.addView(android.widget.Button(this).apply{text="חזור";setOnClickListener{if(w.canGoBack())w.goBack()}},android.widget.LinearLayout.LayoutParams(0,56,1f))
        bar.addView(android.widget.Button(this).apply{text="קדימה";setOnClickListener{if(w.canGoForward())w.goForward()}},android.widget.LinearLayout.LayoutParams(0,56,1f))
        root.addView(bar,android.widget.LinearLayout.LayoutParams(-1,56)); root.addView(w,android.widget.LinearLayout.LayoutParams(-1,0,1f))
        setContentView(root)
    }
}

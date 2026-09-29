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
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class AppItem(val label:String,val packageName:String)

class MainActivity: ComponentActivity() {
    private val sites=listOf("Google" to "https://www.google.com/")
    private fun playStore(pkg:String){ startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg"))) }

    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState)
        setContent { App() }
    }

    @Composable
    fun App(){
        var tab by remember { mutableIntStateOf(0) }
        Scaffold(bottomBar={
            NavigationBar {
                listOf("משחקים","אפליקציות","אתרים","הגדרות").forEachIndexed { i,t ->
                    NavigationBarItem(selected=tab==i,onClick={tab=i},icon={},label={Text(t)})
                }
            }
        }) { p ->
            Column(Modifier.fillMaxSize().padding(p).padding(16.dp)) {
                when(tab){
                    0 -> Games()
                    1 -> Apps()
                    2 -> Sites()
                    3 -> SettingsScreen()
                }
            }
        }
    }

    @Composable fun Apps(){
        val pm=packageManager
        val apps=remember {
            pm.getInstalledApplications(PackageManager.GET_META_DATA)
                .filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 || pm.getLaunchIntentForPackage(it.packageName)!=null }
                .map { AppItem(pm.getApplicationLabel(it).toString(),it.packageName) }
                .sortedBy { it.label.lowercase() }
        }
        Text("אפליקציות",style=MaterialTheme.typography.headlineMedium)
        Text("לחיצה פותחת ישירות את דף האפליקציה ב-Google Play.")
        LazyColumn { items(apps){ a ->
            Card(Modifier.fillMaxWidth().padding(vertical=4.dp).clickable { playStore(a.packageName) }){
                Row(Modifier.padding(16.dp)){Text(a.label)}
            }
        }}
    }

    @Composable fun Games(){
        Text("משחקים",style=MaterialTheme.typography.headlineMedium)
        Text("הטעינה נעשית בהדרגה.")
        LazyColumn { items(listOf("Candy Crush Saga" to "com.king.candycrushsaga","Clash of Clans" to "com.supercell.clashofclans")){(name,pkg)->
            Card(Modifier.fillMaxWidth().padding(vertical=6.dp).clickable { playStore(pkg) }){Text(name,Modifier.padding(18.dp))}
        }}
    }

    @Composable fun Sites(){
        Text("אתרים",style=MaterialTheme.typography.headlineMedium)
        LazyColumn { items(sites){(name,url)->
            Card(Modifier.fillMaxWidth().padding(vertical=5.dp).clickable {
                val i=Intent(this@MainActivity,RestrictedWebActivity::class.java).putExtra("url",url)
                startActivity(i)
            }){Text(name,Modifier.padding(18.dp))}
        }}
    }

    @Composable fun SettingsScreen(){
        Text("הגדרות ואישורים",style=MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))
        Text("כדי להפעיל נעילה מערכתית/מניעת הסרה נדרשות הרשאות Android מיוחדות. אפליקציה רגילה אינה יכולה למנוע הסרה ללא Device Owner/ניהול מכשיר.")
        Spacer(Modifier.height(12.dp))
        Button({startActivity(Intent(Settings.ACTION_SETTINGS))}){Text("פתיחת הגדרות Android")}
    }
}

class RestrictedWebActivity: ComponentActivity(){
    override fun onCreate(b:Bundle?){
        super.onCreate(b)
        val allowed=Uri.parse(intent.getStringExtra("url") ?: "https://www.google.com/")
        val w=WebView(this).apply{
            settings.javaScriptEnabled=true
            settings.setSupportMultipleWindows(false)
            webViewClient=object:WebViewClient(){
                override fun shouldOverrideUrlLoading(view:WebView,request:WebResourceRequest):Boolean{
                    val u=request.url
                    return u.host != allowed.host || u.path != allowed.path
                }
            }
            loadUrl(allowed.toString())
        }
        setContentView(w)
    }
}

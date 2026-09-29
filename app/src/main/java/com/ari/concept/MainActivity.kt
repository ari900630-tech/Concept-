package com.ari.concept

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class LinkItem(val name:String,val url:String)
class MainActivity: ComponentActivity() {
 private val sites=listOf(LinkItem("Google","https://www.google.com/"),LinkItem("YouTube","https://www.youtube.com/"))
 private val games=listOf(LinkItem("משחק 1","https://play.google.com/store/games"))
 private fun open(url:String){startActivity(Intent(Intent.ACTION_VIEW).apply{data=android.net.Uri.parse(url)})}
 override fun onCreate(b:Bundle?){super.onCreate(b); setContent { App() }}
 @Composable fun App(){
  var tab by remember{mutableStateOf(0)}
  var settings by remember{mutableStateOf(false)}
  Scaffold(bottomBar={NavigationBar{
   listOf("משחקים","אפליקציות","אתרים").forEachIndexed{ i,t->NavigationBarItem(selected=tab==i,onClick={tab=i},icon={},label={Text(t)})}
   NavigationBarItem(selected=settings,onClick={settings=true},icon={},label={Text("הגדרות")})
  }}){ p->Column(Modifier.padding(p).padding(16.dp)){
   if(settings){SettingsScreen()} else when(tab){0->ListScreen("משחקים",games);1->AppsScreen();2->ListScreen("אתרים",sites)}
  }}
 }
 @Composable fun ListScreen(title:String,list:List<LinkItem>){Text(title,style=MaterialTheme.typography.headlineMedium);Spacer(Modifier.height(12.dp));LazyColumn{items(list){x->Card(Modifier.fillMaxWidth().padding(5.dp),onClick={open(x.url)}){Text(x.name,Modifier.padding(20.dp))}}}}
 @Composable fun AppsScreen(){Text("אפליקציות",style=MaterialTheme.typography.headlineMedium);Text("כל פריט פותח את Google Play ללא חיפוש פנימי.");ListScreen("",listOf(LinkItem("Google Play","https://play.google.com/store/apps/details?id=com.google.android.apps.maps")))}
 @Composable fun SettingsScreen(){Text("הגדרות ואישורים",style=MaterialTheme.typography.headlineMedium);Text("כאן ירוכזו הרשאות נדרשות להפעלת הגבלות מערכת.");Spacer(Modifier.height(12.dp));Button({startActivity(Intent(Settings.ACTION_SETTINGS))}){Text("פתיחת הגדרות Android")}}
}

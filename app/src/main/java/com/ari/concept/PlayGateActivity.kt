package com.ari.concept

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class PlayGateActivity: ComponentActivity(){
 override fun onCreate(b:Bundle?){super.onCreate(b);val pkg=intent.getStringExtra("pkg")? : "";setContent{Gate(pkg)}}
 @Composable fun Gate(pkg:String){
  Column(Modifier.fillMaxSize().padding(20.dp),verticalArrangement=Arrangement.Center){
   Button(onClick={open(pkg)},Modifier.fillMaxWidth()){Text("התקן / עדכן")}
   Spacer(Modifier.height(12.dp))
   Button(onClick={finish()},Modifier.fillMaxWidth()){Text("חזור")}
  }
 }
 private fun open(pkg:String){startActivity(Intent(Intent.ACTION_VIEW,Uri.parse("market://details?id=$pkg")).setPackage("com.android.vending"))}
}
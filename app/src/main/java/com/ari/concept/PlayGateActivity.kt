package com.ari.concept

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

class PlayGateActivity : ComponentActivity() {
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val pkg = intent.getStringExtra("pkg") ?: ""
        setContent { Gate(pkg) }
    }

    @Composable
    private fun Gate(pkg: String) {
        Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.Center) {
            Button(onClick = { open(pkg) }, Modifier.fillMaxWidth().height(58.dp)) { Text("התקן / עדכן") }
            Spacer(Modifier.height(12.dp))
            Button(onClick = { finish() }, Modifier.fillMaxWidth().height(58.dp)) { Text("חזור") }
        }
    }

    private fun open(pkg: String) {
        if (pkg.isBlank()) return
        getSharedPreferences("play_gate", MODE_PRIVATE).edit()
            .putBoolean("active", true)
            .putBoolean("install_requested", false)
            .putString("target_pkg", pkg)
            .apply()
        val market = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg")).setPackage("com.android.vending")
        try {
            startActivity(market)
        } catch (_: Exception) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$pkg&hl=he&gl=IL")))
        }
    }
}

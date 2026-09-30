package com.ari.concept

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap

class PlayGateActivity : ComponentActivity() {
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val pkg = intent.getStringExtra("pkg").orEmpty()
        setContent { Details(pkg) }
    }

    @Composable
    private fun Details(pkg: String) {
        val appInfo = remember(pkg) { runCatching { packageManager.getApplicationInfo(pkg, 0) }.getOrNull() }
        val installed = appInfo != null
        val label = appInfo?.let { packageManager.getApplicationLabel(it).toString() } ?: pkg
        val icon = appInfo?.let { runCatching { packageManager.getApplicationIcon(it) }.getOrNull() }

        Scaffold(
            bottomBar = {
                Column(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(onClick = { openPlay(pkg) }, Modifier.fillMaxWidth().height(52.dp)) { Text("עדכון") }
                    Button(onClick = { openPlay(pkg) }, Modifier.fillMaxWidth().height(52.dp)) { Text("התקנה") }
                    Button(onClick = { uninstall(pkg) }, Modifier.fillMaxWidth().height(52.dp), enabled = installed) { Text("הסרה") }
                    OutlinedButton(onClick = { finish() }, Modifier.fillMaxWidth().height(52.dp)) { Text("חזור") }
                }
            }
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(28.dp))
                if (icon != null) Image(icon.toBitmap().asImageBitmap(), contentDescription = label, modifier = Modifier.size(96.dp))
                Spacer(Modifier.height(18.dp))
                Text(label, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
                Spacer(Modifier.height(8.dp))
                Text("פרטי האפליקציה", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text(if (installed) "האפליקציה מותקנת במכשיר" else "האפליקציה זמינה להתקנה", textAlign = TextAlign.Center)
            }
        }
    }

    private fun openPlay(pkg: String) {
        if (pkg.isBlank()) return
        getSharedPreferences("play_gate", MODE_PRIVATE).edit().putBoolean("active", true).putBoolean("install_requested", true).putString("target_pkg", pkg).apply()
        val market = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg")).setPackage("com.android.vending")
        try { startActivity(market) }
        catch (_: Exception) { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$pkg&hl=he&gl=IL"))) }
    }

    private fun uninstall(pkg: String) {
        if (pkg.isNotBlank()) startActivity(Intent(Intent.ACTION_DELETE, Uri.parse("package:$pkg")))
    }
}

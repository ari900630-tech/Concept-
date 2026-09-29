package com.ari.concept

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

class PlayGateActivity : ComponentActivity() {
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val pkg = intent.getStringExtra("pkg") ?: ""
        setContent { Gate(pkg) }
    }

    @Composable
    private fun Gate(pkg: String) {
        val installed = remember(pkg) {
            runCatching {
                packageManager.getApplicationInfo(pkg, 0)
                true
            }.getOrDefault(false)
        }
        Column(
            Modifier.fillMaxSize().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                if (installed) "האפליקציה מותקנת" else "האפליקציה אינה מותקנת",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(18.dp))
            Button(
                onClick = { open(pkg) },
                Modifier.fillMaxWidth().height(58.dp)
            ) {
                Text(if (installed) "פתח ב-Google Play לעדכון" else "פתח ב-Google Play להתקנה")
            }
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = { open(pkg) },
                Modifier.fillMaxWidth().height(58.dp)
            ) {
                Text(if (installed) "עדכן ב-Google Play" else "התקן ב-Google Play")
            }
            Spacer(Modifier.height(18.dp))
            OutlinedButton(
                onClick = { finish() },
                Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text("חזור")
            }
        }
    }

    private fun open(pkg: String) {
        if (pkg.isBlank()) return
        getSharedPreferences("play_gate", MODE_PRIVATE).edit()
            .putBoolean("active", true)
            .putBoolean("install_requested", true)
            .putString("target_pkg", pkg)
            .apply()
        val market = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("market://details?id=$pkg")
        ).setPackage("com.android.vending")
        try {
            startActivity(market)
        } catch (_: Exception) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$pkg&hl=he&gl=IL")
                )
            )
        }
    }
}

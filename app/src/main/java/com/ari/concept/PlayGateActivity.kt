package com.ari.concept

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity

class PlayGateActivity : ComponentActivity() {
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val pkg = intent.getStringExtra("pkg").orEmpty()
        if (pkg.isBlank()) {
            finish()
            return
        }

        getSharedPreferences("play_gate", MODE_PRIVATE).edit()
            .putBoolean("active", true)
            .putString("target_pkg", pkg)
            .apply()

        openPlay(pkg)
        finish()
    }

    private fun openPlay(pkg: String) {
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

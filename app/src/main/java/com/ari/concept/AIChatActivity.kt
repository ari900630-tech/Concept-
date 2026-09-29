package com.ari.concept

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class AIMessage(val user:Boolean,val text:String)

class AIChatActivity:ComponentActivity(){
    private val appCatalog = mapOf(
        "וואטסאפ" to ("WhatsApp" to "com.whatsapp"), "whatsapp" to ("WhatsApp" to "com.whatsapp"),
        "וויז" to ("Waze" to "com.waze"), "waze" to ("Waze" to "com.waze"),
        "מפות" to ("Google Maps" to "com.google.android.apps.maps"), "maps" to ("Google Maps" to "com.google.android.apps.maps"),
        "כרום" to ("Google Chrome" to "com.android.chrome"), "chrome" to ("Google Chrome" to "com.android.chrome"),
        "ג׳ימייל" to ("Gmail" to "com.google.android.gm"), "gmail" to ("Gmail" to "com.google.android.gm"),
        "דרייב" to ("Google Drive" to "com.google.android.apps.docs"), "drive" to ("Google Drive" to "com.google.android.apps.docs"),
        "תרגום" to ("Google Translate" to "com.google.android.apps.translate"), "translate" to ("Google Translate" to "com.google.android.apps.translate"),
        "לוח שנה" to ("Google Calendar" to "com.google.android.calendar"), "calendar" to ("Google Calendar" to "com.google.android.calendar"),
        "שמור" to ("Google Keep" to "com.google.android.keep"), "keep" to ("Google Keep" to "com.google.android.keep"),
        "אאוטלוק" to ("Outlook" to "com.microsoft.office.outlook"), "outlook" to ("Outlook" to "com.microsoft.office.outlook"),
        "זום" to ("Zoom" to "us.zoom.videomeetings"), "zoom" to ("Zoom" to "us.zoom.videomeetings"),
        "דרופבוקס" to ("Dropbox" to "com.dropbox.android"), "dropbox" to ("Dropbox" to "com.dropbox.android"),
        "מוביט" to ("Moovit" to "com.tranzmate"), "moovit" to ("Moovit" to "com.tranzmate"),
        "פייבוקס" to ("PayBox" to "com.payboxapp"), "paybox" to ("PayBox" to "com.payboxapp")
    )

    private val gameCatalog = mapOf(
        "שחמט" to ("שחמט" to "com.chess"), "סודוקו" to ("סודוקו" to "com.easybrain.sudoku.android"),
        "2048" to ("2048" to "com.androbaby.game2048"), "טטריס" to ("Tetris" to "com.n3twork.tetris"),
        "tetris" to ("Tetris" to "com.n3twork.tetris"), "נונוגרם" to ("Nonogram.com" to "com.easybrain.nonogram"),
        "nonogram" to ("Nonogram.com" to "com.easybrain.nonogram"), "פאזל" to ("Jigsaw Puzzle" to "com.easybrain.jigsaw.puzzles"),
        "דמקה" to ("Checkers" to "com.litegames.checkers.free"), "שש בש" to ("Backgammon" to "com.litegames.backgammon.free"),
        "רברסי" to ("Reversi" to "com.litegames.reversi.free")
    )

    private fun handleAddAppCommand(q:String):String? {
        val normalized=q.trim()
        val appMatch=Regex("""^(?:הוסף|תוסיף)\s+אפליקציה\s+(.+)$""", RegexOption.IGNORE_CASE).find(normalized)
        val gameMatch=Regex("""^(?:הוסף|תוסיף)\s+משחק\s+(.+)$""", RegexOption.IGNORE_CASE).find(normalized)
        val match=appMatch ?: gameMatch ?: return null
        val requested=match.groupValues[1].trim().lowercase()
        val catalog=if (appMatch != null) appCatalog else gameCatalog
        val item=catalog[requested] ?: catalog.entries.firstOrNull { requested.contains(it.key) || it.key.contains(requested) }?.value
            ?: return "הפריט לא נמצא ברשימת הפריטים המאושרים להוספה."
        val prefs=getSharedPreferences("concept_items", MODE_PRIVATE)
        val key=if (appMatch != null) "apps" else "games"
        val current=prefs.getStringSet(key, emptySet()).orEmpty().toMutableSet()
        current.add(item.first + "|" + item.second)
        prefs.edit().putStringSet(key, current).apply()
        val type=if (appMatch != null) "האפליקציות" else "המשחקים"
        return "הוספתי את " + item.first + " לרשימת " + type + " המאושרת. הוא ייפתח דרך Google Play."
    }}
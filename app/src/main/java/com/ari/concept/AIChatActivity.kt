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
        "וואטסאפ" to ("WhatsApp" to "com.whatsapp"),
        "whatsapp" to ("WhatsApp" to "com.whatsapp"),
        "וויז" to ("Waze" to "com.waze"),
        "waze" to ("Waze" to "com.waze"),
        "מפות" to ("Google Maps" to "com.google.android.apps.maps"),
        "כרום" to ("Google Chrome" to "com.android.chrome"),
        "chrome" to ("Google Chrome" to "com.android.chrome"),
        "יוטיוב" to ("YouTube" to "com.google.android.youtube"),
        "youtube" to ("YouTube" to "com.google.android.youtube"),
        "ג׳ימייל" to ("Gmail" to "com.google.android.gm"),
        "gmail" to ("Gmail" to "com.google.android.gm"),
        "טלגרם" to ("Telegram" to "org.telegram.messenger"),
        "telegram" to ("Telegram" to "org.telegram.messenger"),
        "אינסטגרם" to ("Instagram" to "com.instagram.android"),
        "instagram" to ("Instagram" to "com.instagram.android"),
        "טיקטוק" to ("TikTok" to "com.zhiliaoapp.musically"),
        "tiktok" to ("TikTok" to "com.zhiliaoapp.musically")
    )

    private fun handleAddAppCommand(q:String):String? {
        val normalized=q.trim()
        val prefix=Regex("""^(?:הוסף|תוסיף)\s+אפליקציה\s+(.+)$""", RegexOption.IGNORE_CASE)
        val match=prefix.find(normalized) ?: return null
        val requested=match.groupValues[1].trim().lowercase()
        val item=appCatalog[requested] ?: appCatalog.entries.firstOrNull { requested.contains(it.key) || it.key.contains(requested) }?.value
            ?: return "כדי להוסיף את האפליקציה, כתוב את שמה מתוך האפליקציות הנתמכות כרגע: WhatsApp, Waze, Chrome, YouTube, Gmail, Telegram, Instagram או TikTok."
        val prefs=getSharedPreferences("concept_items", MODE_PRIVATE)
        val current=prefs.getStringSet("apps", emptySet()).orEmpty().toMutableSet()
        current.add(item.first + "|" + item.second)
        prefs.edit().putStringSet("apps", current).apply()
        return "הוספתי את " + item.first + " לרשימת האפליקציות בחנות. עכשיו היא תופיע שם ותיפתח דרך Google Play."
    }
    override fun onCreate(b:Bundle?){super.onCreate(b);setContent{AIChat()}}
    private suspend fun askGroq(messages:List<AIMessage>):String=withContext(Dispatchers.IO){
        val key=BuildConfig.GROQ_API_KEY
        if(key.isBlank()) throw IllegalStateException("Missing AI configuration")
        val body=JSONObject().apply{
            put("model","openai/gpt-oss-120b")
            put("messages",JSONArray().apply{
                messages.forEach{m->put(JSONObject().apply{put("role",if(m.user)"user" else "assistant");put("content",m.text)})}
            })
            put("temperature",0.3)
        }.toString()
        val c=(URL("https://api.groq.com/openai/v1/chat/completions").openConnection() as HttpURLConnection).apply{
            requestMethod="POST";connectTimeout=15000;readTimeout=30000
            doOutput=true
            setRequestProperty("Authorization","Bearer $key")
            setRequestProperty("Content-Type","application/json")
        }
        c.outputStream.use{it.write(body.toByteArray(Charsets.UTF_8))}
        val text=(if(c.responseCode in 200..299)c.inputStream else c.errorStream).bufferedReader().use{it.readText()}
        if(c.responseCode !in 200..299) throw IllegalStateException("שרת AI החזיר ${c.responseCode}: ${text.take(300)}")
        JSONObject(text).getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content")
    }
    @Composable private fun AIChat(){
        val scope=rememberCoroutineScope()
        var input by remember{mutableStateOf("")}
        var loading by remember{mutableStateOf(false)}
        var messages by remember{mutableStateOf(listOf(AIMessage(false,"שלום. אפשר לשאול אותי שאלות או לבקש עזרה.")))}
        Column(Modifier.fillMaxSize()){
            Row(Modifier.fillMaxWidth().statusBarsPadding().padding(12.dp),verticalAlignment=Alignment.CenterVertically){
                Button(onClick={finish()}){Text("חזור")}
                Spacer(Modifier.width(8.dp))
                Text("צ׳אט AI",Modifier.weight(1f),textAlign=TextAlign.Right,style=MaterialTheme.typography.headlineMedium)
            }
            HorizontalDivider()
            LazyColumn(Modifier.weight(1f).fillMaxWidth().padding(horizontal=12.dp),contentPadding=PaddingValues(vertical=8.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){
                items(messages){m->Card(Modifier.fillMaxWidth()){Text(m.text,Modifier.fillMaxWidth().padding(12.dp),textAlign=TextAlign.Right)}}
            }
            Surface(tonalElevation=3.dp){
                Row(Modifier.fillMaxWidth().navigationBarsPadding().imePadding().padding(8.dp),verticalAlignment=Alignment.Bottom){
                    OutlinedTextField(value=input,onValueChange={input=it},modifier=Modifier.weight(1f),label={Text("כתוב בקשה")},maxLines=4)
                    Spacer(Modifier.width(8.dp))
                    Button(enabled=input.isNotBlank()&&!loading,onClick={
                        val q=input.trim();input=""
                        val sent=messages+AIMessage(true,q);messages=sent;loading=true
                        scope.launch{
                            try{
                                val commandAnswer=handleAddAppCommand(q)
                                val answer=commandAnswer ?: askGroq(sent)
                                messages=sent+AIMessage(false,answer)
                            } catch(e:Exception){messages=sent+AIMessage(false,e.message ?: "לא הצלחתי להתחבר לשרת ה-AI.")}
                            finally{loading=false}
                        }
                    }){Text(if(loading)"..." else "שלח")}
                }
            }
        }
    }
}
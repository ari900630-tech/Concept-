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
                            try{val answer=askGroq(sent);messages=sent+AIMessage(false,answer)}
                            catch(e:Exception){messages=sent+AIMessage(false,e.message ?: "לא הצלחתי להתחבר לשרת ה-AI.")}
                            finally{loading=false}
                        }
                    }){Text(if(loading)"..." else "שלח")}
                }
            }
        }
    }
}
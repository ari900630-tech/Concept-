package com.ari.concept

import android.app.Activity
import android.os.Bundle
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalLayoutDirection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

data class AIMessage(val user:Boolean,val text:String)

class AIChatActivity:ComponentActivity(){
    private val store by lazy { getSharedPreferences("concept_items",MODE_PRIVATE) }

    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState)
        setContent{
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl){
                AIChat()
            }
        }
    }

    private suspend fun askGemini(apiKey:String,messages:List<AIMessage>):String = withContext(Dispatchers.IO){
        val history=messages.joinToString("\n"){if(it.user)"USER: "+it.text else "ASSISTANT: "+it.text}
        val prompt="You are the AI assistant inside the Concept Android app store. Answer in Hebrew. Help find useful family-friendly apps and games. If the user explicitly asks to add an app, append exactly [ADD_APP]Name|official.package.id. If the user explicitly asks to add a game, append exactly [ADD_GAME]Name|official.package.id. Only append when reasonably confident the package ID is official. Never invent package IDs. Do not add adult, gambling, weapons or other unsuitable content. Conversation:\n"+history
        val body=JSONObject().apply{
            put("contents",JSONArray().put(JSONObject().apply{
                put("role","user")
                put("parts",JSONArray().put(JSONObject().put("text",prompt)))
            }))
        }.toString()
        val conn=URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.8-flash:generateContent").openConnection() as HttpURLConnection
        conn.requestMethod="POST"
        conn.doOutput=true
        conn.connectTimeout=15000
        conn.readTimeout=30000
        conn.setRequestProperty("Content-Type","application/json")
        conn.setRequestProperty("x-goog-api-key",apiKey.trim())
        conn.outputStream.use{it.write(body.toByteArray(Charsets.UTF_8))}
        val code=conn.responseCode
        val stream=if(code in 200..299) conn.inputStream else conn.errorStream
        val raw=stream.bufferedReader().use{it.readText()}
        if(code !in 200..299) throw IllegalStateException("Gemini HTTP $code")
        JSONObject(raw).getJSONArray("candidates").getJSONObject(0)
            .getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text")
    }

    private fun handleAdd(raw:String):String{
        val app=Regex("""\[ADD_APP\]\s*([^|]+)\|\s*([A-Za-z0-9._-]+)""").find(raw)
        val game=Regex("""\[ADD_GAME\]\s*([^|]+)\|\s*([A-Za-z0-9._-]+)""").find(raw)
        val match=app ?: game ?: return raw
        val setName=if(app!=null)"apps" else "games"
        val value=match.groupValues[1].trim()+"|"+match.groupValues[2].trim()
        val values=store.getStringSet(setName,emptySet())!!.toMutableSet()
        values.add(value)
        store.edit().putStringSet(setName,values).apply()
        return raw.replace(match.value,"").trim()
    }

    @Composable
    private fun AIChat(){
        val scope=rememberCoroutineScope()
        var key by remember{mutableStateOf(store.getString("gemini_key","") ?: "")}
        var input by remember{mutableStateOf("")}
        var loading by remember{mutableStateOf(false)}
        var messages by remember{mutableStateOf(listOf(AIMessage(false,"שלום. אפשר לשאול אותי שאלות או לבקש להוסיף אפליקציה או משחק.")))}

        Column(Modifier.fillMaxSize().padding(12.dp)){
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){
                Button(onClick={finish()}){Text("סגור")}
                Spacer(Modifier.width(8.dp))
                Text("צ׳אט AI",Modifier.weight(1f),textAlign=TextAlign.Right,style=MaterialTheme.typography.headlineMedium)
            }
            Spacer(Modifier.height(8.dp))
            if(key.isBlank()){
                Text("הדבק את מפתח Gemini שלך. הוא נשמר מקומית במכשיר ולא בתוך המאגר.",Modifier.fillMaxWidth(),textAlign=TextAlign.Right)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(value=key,onValueChange={key=it},modifier=Modifier.fillMaxWidth(),label={Text("מפתח API")},singleLine=true)
                Spacer(Modifier.height(8.dp))
                Button(onClick={store.edit().putString("gemini_key",key.trim()).apply()},enabled=key.isNotBlank(),modifier=Modifier.fillMaxWidth()){Text("שמור מפתח")}
            }else{
                LazyColumn(Modifier.weight(1f).fillMaxWidth()){
                    items(messages){m->
                        Card(Modifier.fillMaxWidth().padding(vertical=4.dp)){
                            Text(m.text,Modifier.fillMaxWidth().padding(12.dp),textAlign=TextAlign.Right)
                        }
                    }
                }
                Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.Bottom){
                    OutlinedTextField(value=input,onValueChange={input=it},modifier=Modifier.weight(1f),label={Text("כתוב בקשה")})
                    Spacer(Modifier.width(8.dp))
                    Button(enabled=input.isNotBlank()&&!loading,onClick={
                        val q=input.trim()
                        input=""
                        val sent=messages+AIMessage(true,q)
                        messages=sent
                        loading=true
                        scope.launch{
                            runCatching{askGemini(key,sent)}
                                .onSuccess{messages=sent+AIMessage(false,handleAdd(it))}
                                .onFailure{messages=sent+AIMessage(false,"לא הצלחתי להתחבר ל-Gemini. בדוק את המפתח והחיבור לאינטרנט.")}
                            loading=false
                        }
                    }){Text(if(loading)"..." else "שלח")}
                }
            }
        }
    }
}

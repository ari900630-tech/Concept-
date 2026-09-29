package com.ari.concept
import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class AppBlockAccessibilityService:AccessibilityService(){
 override fun onAccessibilityEvent(event:AccessibilityEvent?){
  if(event?.eventType!=AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)return
  val pkg=event.packageName?.toString() ?: return
  if(pkg!=packageName && getSharedPreferences("blocked",MODE_PRIVATE).getBoolean(pkg,false)){
   performGlobalAction(GLOBAL_ACTION_HOME)
  }
 }
 override fun onInterrupt(){}
}
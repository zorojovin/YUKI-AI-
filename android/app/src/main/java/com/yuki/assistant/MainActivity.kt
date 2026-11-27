package com.yuki.assistant

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.RecognitionListener
import android.speech.tts.TextToSpeech
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.app.ActivityCompat
import java.util.Locale

class MainActivity : Activity(), TextToSpeech.OnInitListener {
 private lateinit var web: WebView
 private var tts: TextToSpeech? = null
 private var recognizer: SpeechRecognizer? = null
 override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); tts=TextToSpeech(this,this); web=WebView(this); web.settings.javaScriptEnabled=true; web.settings.domStorageEnabled=true; web.webChromeClient=WebChromeClient(); web.webViewClient=WebViewClient(); web.addJavascriptInterface(YukiBridge(),"YukiNative"); setContentView(web); web.loadUrl("file:///android_asset/index.html") }
 override fun onInit(status:Int) { if(status==TextToSpeech.SUCCESS) tts?.language=Locale("ta","IN") }
 inner class YukiBridge {
  @JavascriptInterface fun speak(text:String) { runOnUiThread { tts?.speak(text.take(4000),TextToSpeech.QUEUE_FLUSH,null,"yuki-voice") } }
  @JavascriptInterface fun stopSpeaking() { runOnUiThread { tts?.stop() } }
  @JavascriptInterface fun listen() { runOnUiThread { startListening() } }
 }
 private fun startListening() {
  if(!SpeechRecognizer.isRecognitionAvailable(this)){sendToPage("onYukiError","Speech recognition unavailable");return}
  if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.RECORD_AUDIO),41);return}
  recognizer?.destroy(); recognizer=SpeechRecognizer.createSpeechRecognizer(this)
  recognizer?.setRecognitionListener(object:RecognitionListener {
   override fun onReadyForSpeech(params:Bundle?){sendToPage("onYukiStatus","Listening...")}
   override fun onResults(results:Bundle?){val text=results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty();sendToPage("onYukiResult",text)}
   override fun onError(error:Int){sendToPage("onYukiError","Speech recognition error: $error")}
   override fun onBeginningOfSpeech(){};override fun onRmsChanged(rmsdB:Float){};override fun onBufferReceived(buffer:ByteArray?){};override fun onEndOfSpeech(){sendToPage("onYukiStatus","Ready")};override fun onPartialResults(partialResults:Bundle?){};override fun onEvent(eventType:Int,params:Bundle?){}
  })
  val intent=Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply{putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ta-IN");putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,"ta-IN")};recognizer?.startListening(intent)
 }
 private fun sendToPage(method:String,value:String){val safe=org.json.JSONObject.quote(value);runOnUiThread{web.evaluateJavascript("window.$method && window.$method($safe)",null)}}
 override fun onRequestPermissionsResult(requestCode:Int,permissions:Array<out String>,grantResults:IntArray){super.onRequestPermissionsResult(requestCode,permissions,grantResults);if(requestCode==41 && grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED) startListening() else if(requestCode==41) sendToPage("onYukiError","Microphone permission was not granted")}
 override fun onDestroy(){recognizer?.destroy();tts?.stop();tts?.shutdown();web.destroy();super.onDestroy()}
}

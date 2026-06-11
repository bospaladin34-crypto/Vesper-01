package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.braidc.BraidBridge

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFF000000)
      ) { innerPadding ->
        TerminalUI(modifier = Modifier.padding(innerPadding))
      }
    }
  }
}

@Composable
fun TerminalUI(modifier: Modifier = Modifier) {
  var terminalOutput by remember { mutableStateOf<String?>(null) }
  var isIgniting by remember { mutableStateOf(false) }

  val neonGreen = Color(0xFF00FF41)
  val neonCyan = Color(0xFF00FFFF)
  val darkBg = Color(0xFF0A0A0A)

  val monoFont = FontFamily.Monospace

  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(500, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulseAlpha"
  )

  Column(modifier = modifier.fillMaxSize().background(Color.Black).padding(16.dp)) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
        Column {
          Text(text = "COM.VESPER.GENESIS", color = neonGreen.copy(alpha = 0.6f), fontFamily = monoFont, fontSize = 10.sp, letterSpacing = 2.sp)
          Text(text = "BraidBridge_v1.0.2", color = neonGreen, fontFamily = monoFont, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 2.dp))
        }
        Box(modifier = Modifier.background(neonGreen).padding(horizontal = 8.dp, vertical = 4.dp)) {
          Text(text = "SYSTEM ACTIVE", color = Color.Black, fontFamily = monoFont, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
      }
      Box(modifier = Modifier.fillMaxWidth().padding(top = 12.dp).height(2.dp).background(neonGreen))
    }

    Column(modifier = Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(24.dp)) {
      Column(modifier = Modifier.fillMaxWidth().border(2.dp, neonGreen.copy(alpha = 0.4f)).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
          Box(modifier = Modifier.size(8.dp).background(neonGreen))
          Spacer(modifier = Modifier.width(8.dp))
          Text(text = "PRIMARY IGNITION CONTROL", color = neonGreen, fontFamily = monoFont, fontSize = 11.sp)
        }
        Box(modifier = Modifier.fillMaxWidth().border(2.dp, neonCyan).padding(4.dp).border(2.dp, neonCyan).clickable { isIgniting = true; try { val bridge = BraidBridge(); terminalOutput = bridge.igniteSilicon() } catch (e: Throwable) { terminalOutput = "ERR: ${e.javaClass.simpleName}: ${e.message}\n> LAMINAR CORE IGNITION FAILED.\n> AWAITING .SO LINKAGE..." } }.padding(vertical = 20.dp), contentAlignment = Alignment.Center) {
          Text(text = "[IGNITE LAMINAR CORE]", color = neonCyan, fontFamily = monoFont, fontSize = 18.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        }
      }

      Column(modifier = Modifier.weight(1f).fillMaxWidth().border(2.dp, neonGreen).background(darkBg)) {
        Row(modifier = Modifier.fillMaxWidth().background(neonGreen).padding(horizontal = 8.dp, vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
          Text(text = "EXECUTION_LOG_STREAM", color = Color.Black, fontFamily = monoFont, fontSize = 10.sp, fontWeight = FontWeight.Bold)
          Text(text = "ID: 0x8842-X", color = Color.Black, fontFamily = monoFont, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        Column(modifier = Modifier.fillMaxSize().padding(12.dp).verticalScroll(rememberScrollState())) {
          AnimatedVisibility(visible = isIgniting, enter = fadeIn(animationSpec = tween(500))) {
            Column {
              Row { Text(text = "09:42:01", color = Color.White.copy(alpha=0.5f), fontFamily = monoFont, fontSize = 14.sp); Spacer(modifier = Modifier.width(8.dp)); Text(text = "> Init: BraidBridge sequence...", color = neonGreen, fontFamily = monoFont, fontSize = 14.sp) }
              Row(modifier = Modifier.padding(top = 4.dp)) { Text(text = "09:42:02", color = Color.White.copy(alpha=0.5f), fontFamily = monoFont, fontSize = 14.sp); Spacer(modifier = Modifier.width(8.dp)); Text(text = "> System.loadLibrary(\"braidc\")", color = neonGreen, fontFamily = monoFont, fontSize = 14.sp) }
              Row(modifier = Modifier.padding(top = 4.dp)) { Text(text = "09:42:02", color = Color.White.copy(alpha=0.5f), fontFamily = monoFont, fontSize = 14.sp); Spacer(modifier = Modifier.width(8.dp)); Text(text = "> EXECUTING: igniteSilicon()...", color = Color.White, fontFamily = monoFont, fontSize = 14.sp) }
              if (terminalOutput != null) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp).background(neonGreen.copy(alpha = 0.1f)).border(1.dp, neonGreen.copy(alpha = 0.3f)).padding(8.dp)) {
                  Text(text = "PAYLOAD RECEIVED:", color = neonGreen.copy(alpha = 0.6f), fontFamily = monoFont, fontSize = 10.sp, modifier = Modifier.padding(bottom = 4.dp))
                  Text(text = "\"$terminalOutput\"", color = neonGreen, fontFamily = monoFont, fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Bold)
                }
              }
            }
          }
          Spacer(modifier = Modifier.weight(1f))
          Row(modifier = Modifier.padding(top = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.width(8.dp).height(16.dp).background(neonGreen).alpha(pulseAlpha))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "WAITING_FOR_COMMAND...", color = neonGreen, fontFamily = monoFont, fontSize = 10.sp)
          }
        }
      }
    }

    Row(modifier = Modifier.fillMaxWidth().padding(top = 24.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
      Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) { Box(modifier = Modifier.width(4.dp).height(12.dp).background(neonCyan)); Spacer(modifier = Modifier.width(4.dp)); Text(text = "CPU_01: 42%", color = neonGreen, fontFamily = monoFont, fontSize = 10.sp) }
        Row(verticalAlignment = Alignment.CenterVertically) { Box(modifier = Modifier.width(4.dp).height(12.dp).background(neonGreen)); Spacer(modifier = Modifier.width(4.dp)); Text(text = "JNI_LINK: OK", color = neonGreen, fontFamily = monoFont, fontSize = 10.sp) }
      }
      Text(text = "GENESIS_CORE_OS // RC_7", color = neonGreen.copy(alpha = 0.5f), fontFamily = monoFont, fontSize = 10.sp)
    }
  }
}

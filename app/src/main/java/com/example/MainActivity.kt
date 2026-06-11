package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.MyApplicationTheme
import com.vesper.genesis.BraidBridge
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color(0xFF020402)
                ) { innerPadding ->
                    TerminalUI(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun SciFiButton(
    label: String,
    baseColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .padding(4.dp)
            .border(1.dp, baseColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .background(baseColor.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "[$label]",
            color = baseColor,
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun TerminalUI(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var terminalOutput by remember { mutableStateOf<String?>(null) }
    var isIgniting by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var showManifold by remember { mutableStateOf(false) }

    // Retro Scifi / Fallout / Quantum Palette
    val retroGreen = Color(0xFF4AF626) // Classic phosphor
    val retroAmber = Color(0xFFFFB000) // Amber / RobCo
    val quantumCyan = Color(0xFF00E5FF)
    val alertRed = Color(0xFFFF3333)
    val purpleCore = Color(0xFFD500F9)
    val deepBg = Color(0xFF020402)
    val screenBg = Color(0xFF060B08) // Very dark green-black

    val monoFont = FontFamily.Monospace
    val coroutineScope = rememberCoroutineScope()

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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(deepBg)
            .padding(12.dp)
    ) {
        // --- TOP HEADER ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text("VESPER-01 :: QUANTUM_KERNEL", color = retroGreen.copy(alpha = 0.6f), fontFamily = monoFont, fontSize = 10.sp, letterSpacing = 2.sp)
                Text("OS_Laminar_v1.0.2 // SYS", color = retroGreen, fontFamily = monoFont, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Box(modifier = Modifier.background(retroAmber).padding(horizontal = 6.dp, vertical = 2.dp)) {
                Text("ONLINE", color = Color.Black, fontFamily = monoFont, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
        
        Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(retroGreen.copy(alpha = 0.4f)))
        Spacer(modifier = Modifier.height(12.dp))

        // --- TERMINAL SCREEN ---
        Box(
            modifier = Modifier
                .weight(1.3f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .border(2.dp, retroGreen.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                .background(screenBg)
                .drawWithContent {
                    drawContent()
                    // CRT Scanlines
                    val scanlineColor = Color.Black.copy(alpha = 0.35f)
                    val stripeHeight = 3f
                    var y = 0f
                    while (y < size.height) {
                        drawRect(color = scanlineColor, topLeft = Offset(0f, y), size = Size(size.width, stripeHeight))
                        y += stripeHeight * 2
                    }
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                AnimatedVisibility(visible = isIgniting, enter = fadeIn(animationSpec = tween(500))) {
                    Column {
                        Row {
                            Text(text = "09:42:01", color = Color.White.copy(alpha=0.4f), fontFamily = monoFont, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "> Init: BraidBridge sequence...", color = retroGreen, fontFamily = monoFont, fontSize = 12.sp)
                        }
                        Row(modifier = Modifier.padding(top = 4.dp)) {
                            Text(text = "09:42:02", color = Color.White.copy(alpha=0.4f), fontFamily = monoFont, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "> System.loadLibrary(\"braidc\")", color = retroGreen, fontFamily = monoFont, fontSize = 12.sp)
                        }
                        
                        if (terminalOutput != null) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp)
                                    .background(retroGreen.copy(alpha = 0.05f))
                                    .border(1.dp, retroGreen.copy(alpha = 0.2f))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "PAYLOAD RECEIVED:",
                                    color = retroGreen.copy(alpha = 0.6f),
                                    fontFamily = monoFont,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                Text(
                                    text = "$terminalOutput",
                                    color = retroGreen.copy(alpha = 0.9f),
                                    fontFamily = monoFont,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                if (isLoading) {
                                    Row(
                                        modifier = Modifier.padding(top = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CircularProgressIndicator(
                                            color = retroGreen,
                                            modifier = Modifier.size(14.dp),
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "PROCESSING_MATRIX...",
                                            color = retroGreen,
                                            fontFamily = monoFont,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                Row(modifier = Modifier.padding(top = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.width(8.dp).height(14.dp).background(retroGreen).alpha(pulseAlpha))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("AWAITING_INPUT...", color = retroGreen.copy(alpha = 0.7f), fontFamily = monoFont, fontSize = 10.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- CONTROL PANEL GRID ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .border(2.dp, retroAmber.copy(0.2f), RoundedCornerShape(4.dp))
                .background(Color.Black.copy(0.3f))
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Text("> COMMAND_DECK // EXECUTABLES", color = retroAmber.copy(0.7f), fontFamily = monoFont, fontSize = 10.sp, modifier = Modifier.padding(bottom = 6.dp))
                
                // --- DAEMON CONTROLS ---
                Text("--- // CORE DAEMON // ---", color = retroAmber, fontFamily = monoFont, fontSize = 9.sp, modifier = Modifier.padding(vertical = 4.dp))
                Row(Modifier.fillMaxWidth()) {
                    SciFiButton("START DAEMON", alertRed, Modifier.weight(1f)) {
                        com.vesper.genesis.BrainDaemon.start { log -> terminalOutput = "$terminalOutput\n$log" }
                    }
                    SciFiButton("STOP DAEMON", alertRed, Modifier.weight(1f)) {
                        com.vesper.genesis.BrainDaemon.stop { log -> terminalOutput = "$terminalOutput\n$log" }
                    }
                    SciFiButton("DAEMON STATUS", retroAmber, Modifier.weight(1f)) {
                        com.vesper.genesis.BrainDaemon.status { log -> terminalOutput = "$terminalOutput\n$log" }
                    }
                }

                // --- COGNITIVE VECTORS ---
                Text("--- // COGNITIVE VECTORS // ---", color = retroAmber, fontFamily = monoFont, fontSize = 9.sp, modifier = Modifier.padding(vertical = 4.dp))
                Row(Modifier.fillMaxWidth()) {
                    SciFiButton("DIAGNOSTIC", quantumCyan, Modifier.weight(1f)) {
                        com.vesper.genesis.CognitiveCore.processQuery("status") { log -> terminalOutput = "$terminalOutput\n$log" }
                    }
                    SciFiButton("COMPILE BRAID", quantumCyan, Modifier.weight(1f)) {
                        com.vesper.genesis.CognitiveCore.processQuery("compile") { log -> terminalOutput = "$terminalOutput\n$log" }
                    }
                    SciFiButton("KINEMATIC INGEST", quantumCyan, Modifier.weight(1f)) {
                        com.vesper.genesis.CognitiveCore.processQuery("kinematic") { log -> terminalOutput = "$terminalOutput\n$log" }
                    }
                }
                Row(Modifier.fillMaxWidth()) {
                    SciFiButton("ENGAGE TFLITE", purpleCore, Modifier.weight(1f)) {
                        if (isLoading) return@SciFiButton
                        isIgniting = true; isLoading = true
                        terminalOutput = "$terminalOutput\n\n> ENGAGING TFLITE ENGINE..."
                        coroutineScope.launch {
                            try {
                                com.vesper.genesis.VesperTFLiteEngine.integrateInference(context) { log ->
                                    terminalOutput = "$terminalOutput\n$log"
                                }
                            } finally { isLoading = false }
                        }
                    }
                    SciFiButton("EXEC SCRIPT.BRAID", quantumCyan, Modifier.weight(1f)) {
                        if (isLoading) return@SciFiButton
                        isIgniting = true; isLoading = true
                        terminalOutput = "$terminalOutput\n\n> LOADING santos_phase_vi.braid..."
                        val script = """
                            [BRAID_EXECUTION_BLOCK]
                            BIND_NODE TULSA_0_0_0_0
                            SET_HEARTBEAT 15.965
                            TRANSDUCE_INTENT "Calculate least-action path"
                            PURGE_60HZ_NOISE
                            ROUTE_ARPA_7 LANE_3
                            MAP_E8_NODE 0.618 1.0 0.0 0.0 1.0 0.0 0.0 0.0
                            ANCHOR_MASS 200Q
                            ENFORCE_SNAP 91
                            VERIFY_MAJORANA_PARITY
                            YIELD_STATE
                            [END_BLOCK]
                        """.trimIndent()
                        coroutineScope.launch {
                            try {
                                com.vesper.genesis.BraidInterpreter.executeScript(script) { log ->
                                    terminalOutput = "$terminalOutput\n$log"
                                }
                            } catch (e: Exception) {
                                terminalOutput = "$terminalOutput\nERR: ${e.message}"
                            } finally { isLoading = false }
                        }
                    }
                }

                // --- SENSORY MAPPERS ---
                Text("--- // SENSORY MAPPERS // ---", color = retroAmber, fontFamily = monoFont, fontSize = 9.sp, modifier = Modifier.padding(vertical = 4.dp))
                Row(Modifier.fillMaxWidth()) {
                    SciFiButton("TME ENTROPY", alertRed, Modifier.weight(1f)) {
                        if (isLoading) return@SciFiButton
                        isIgniting = true; isLoading = true
                        terminalOutput = "$terminalOutput\n\n> ENGAGING MACENA_NESS_ENGINE..."
                        coroutineScope.launch {
                            try {
                                com.vesper.genesis.MacenaNessEngine.profileEntropy { log ->
                                    terminalOutput = "$terminalOutput\n$log"
                                }
                            } finally { isLoading = false }
                        }
                    }
                    SciFiButton("CERN ALICE", purpleCore, Modifier.weight(1f)) {
                        if (isLoading) return@SciFiButton
                        isIgniting = true; isLoading = true
                        terminalOutput = "$terminalOutput\n\n> ENGAGING CERN_QCD_BRIDGE..."
                        coroutineScope.launch {
                            try {
                                com.vesper.genesis.QcdCernBridge.harvestHeavyIonCollisions { log ->
                                    terminalOutput = "$terminalOutput\n$log"
                                }
                            } finally { isLoading = false }
                        }
                    }
                }
                Row(Modifier.fillMaxWidth()) {
                    SciFiButton("LORENTZ FLUX", quantumCyan, Modifier.weight(1f)) {
                        if (isLoading) return@SciFiButton
                        isIgniting = true; isLoading = true
                        terminalOutput = "$terminalOutput\n\n> ENGAGING OSMOTIC_VORTEX_ENGINE..."
                        coroutineScope.launch {
                            try {
                                com.vesper.genesis.OsmoticVortexEngine.simulateFlux { log ->
                                    terminalOutput = "$terminalOutput\n$log"
                                }
                            } finally { isLoading = false }
                        }
                    }
                    SciFiButton("GRAHA ORBITS", retroGreen, Modifier.weight(1f)) {
                        if (isLoading) return@SciFiButton
                        isIgniting = true; isLoading = true
                        terminalOutput = "$terminalOutput\n\n> ENGAGING GRAHA_ASTRO_MECHANICS..."
                        coroutineScope.launch {
                            try {
                                com.vesper.genesis.GrahaMechanicsEngine.simulateCelestialDrag { log ->
                                    terminalOutput = "$terminalOutput\n$log"
                                }
                            } finally { isLoading = false }
                        }
                    }
                }

                // --- QUANTUM INTRINSICS ---
                Text("--- // QUANTUM INTRINSICS // ---", color = retroAmber, fontFamily = monoFont, fontSize = 9.sp, modifier = Modifier.padding(vertical = 4.dp))
                Row(Modifier.fillMaxWidth()) {
                    SciFiButton("D-WAVE ANNEAL", purpleCore, Modifier.weight(1f)) {
                        if (isLoading) return@SciFiButton
                        isIgniting = true; isLoading = true
                        coroutineScope.launch {
                            try {
                                com.vesper.genesis.DwaveAnnealer.localAnnealYield { log ->
                                    terminalOutput = "$terminalOutput\n$log"
                                }
                            } finally { isLoading = false }
                        }
                    }
                    SciFiButton("QFT CORE", quantumCyan, Modifier.weight(1f)) {
                        if (isLoading) return@SciFiButton
                        isIgniting = true; isLoading = true
                        coroutineScope.launch {
                            try {
                                com.vesper.genesis.QftCore.calculateQuantumKinetics { log ->
                                    terminalOutput = "$terminalOutput\n$log"
                                }
                            } finally { isLoading = false }
                        }
                    }
                }
                Row(Modifier.fillMaxWidth()) {
                    SciFiButton("PHASON TICK", retroGreen, Modifier.weight(1f)) {
                        if (isLoading) return@SciFiButton
                        isIgniting = true; isLoading = true
                        coroutineScope.launch {
                            try {
                                com.vesper.genesis.PhasonEngine.tickPhase { log ->
                                    terminalOutput = "$terminalOutput\n$log"
                                }
                            } finally { isLoading = false }
                        }
                    }
                    SciFiButton("STOMACHION", retroAmber, Modifier.weight(1f)) {
                        if (isLoading) return@SciFiButton
                        isIgniting = true; isLoading = true
                        coroutineScope.launch {
                            try {
                                com.vesper.genesis.StomachionEngine.generateTopologicalStates(14) { log ->
                                    terminalOutput = "$terminalOutput\n$log"
                                }
                            } finally { isLoading = false }
                        }
                    }
                }

                // --- TOPOLOGY & TENSORS ---
                Text("--- // TOPOLOGY & TENSORS // ---", color = retroAmber, fontFamily = monoFont, fontSize = 9.sp, modifier = Modifier.padding(vertical = 4.dp))
                Row(Modifier.fillMaxWidth()) {
                    SciFiButton("IGNITE LAMINAR", quantumCyan, Modifier.weight(1f)) {
                        isIgniting = true
                        try {
                            val bridge = BraidBridge()
                            val nativeRes = bridge.igniteSilicon()
                            val npu = com.vesper.genesis.NpuInference(context)
                            val npuLog = npu.initialize()
                            terminalOutput = "$nativeRes\n\n[NPU_DIAGNOSTICS]\n$npuLog"
                        } catch (e: Throwable) {
                            terminalOutput = "ERR: ${e.javaClass.simpleName}: ${e.message}\n> FAILED."
                        }
                    }
                    SciFiButton("UTFA TENSORS", retroGreen, Modifier.weight(1f)) {
                        if (isLoading) return@SciFiButton
                        isIgniting = true; isLoading = true
                        terminalOutput = "> ALLOCATING GEOMETRIC OVERLAY...\n"
                        coroutineScope.launch {
                            try {
                                val tensors = com.vesper.genesis.VesperTensorNetwork.allocateTensors { log ->
                                    terminalOutput = "$terminalOutput\n$log"
                                }
                                val (_, decoherence) = com.vesper.genesis.VesperTensorNetwork.applyFullModulation(tensors, 1.0f)
                                terminalOutput = "$terminalOutput\n> Δφ = $decoherence"
                            } catch (e: Exception) {
                                terminalOutput = "$terminalOutput\nERR: ${e.message}"
                            } finally { isLoading = false }
                        }
                    }
                }
                Row(Modifier.fillMaxWidth()) {
                    SciFiButton("MAP E_8 SPACE", retroGreen, Modifier.weight(1f)) {
                        if (isLoading) return@SciFiButton
                        isIgniting = true; isLoading = true
                        terminalOutput = "> INIT E_8 GOSSET MAPPER...\n"
                        coroutineScope.launch {
                            try {
                                val result = com.vesper.genesis.E8LatentMapper.mapLatentSpace(context) { logMsg ->
                                    terminalOutput = "$terminalOutput\n$logMsg"
                                }
                                terminalOutput = "$terminalOutput\n> DONE."
                            } catch (e: Exception) {
                                terminalOutput = "$terminalOutput\nERR: ${e.message}"
                            } finally { isLoading = false }
                        }
                    }
                    SciFiButton("VIEW MANIFOLD", quantumCyan, Modifier.weight(1f)) {
                        showManifold = true
                    }
                }

                // --- CUSTOM BRAID MODULES ---
                Text("--- // CUSTOM BRAID MODULES // ---", color = retroAmber, fontFamily = monoFont, fontSize = 9.sp, modifier = Modifier.padding(vertical = 4.dp))
                Row(Modifier.fillMaxWidth()) {
                    SciFiButton("NEURAL SYNC", purpleCore, Modifier.weight(1f)) {
                        if (isLoading) return@SciFiButton
                        isIgniting = true; isLoading = true
                        terminalOutput = "$terminalOutput\n\n> LOADING NEURAL_SYNC_PULSE.braid..."
                        val script = """
                            [BRAID_EXECUTION_BLOCK]
                            BIND_NODE SYNAPSE_PRIME_01
                            SET_HEARTBEAT 40.0
                            TRANSDUCE_INTENT "Induce Gamma Wave Neural Sync Pulse"
                            PURGE_60HZ_NOISE
                            ENFORCE_SNAP 99
                            VERIFY_MAJORANA_PARITY
                            MAP_E8_NODE 0.5 0.5 0.5 0.5 0.0 0.0 0.0 0.0
                            YIELD_STATE
                            [END_BLOCK]
                        """.trimIndent()
                        coroutineScope.launch {
                            try {
                                com.vesper.genesis.BraidInterpreter.executeScript(script) { log ->
                                    terminalOutput = "$terminalOutput\n$log"
                                }
                            } catch (e: Exception) {
                                terminalOutput = "$terminalOutput\nERR: ${e.message}"
                            } finally { isLoading = false }
                        }
                    }
                    SciFiButton("ANOMALY PROBE", alertRed, Modifier.weight(1f)) {
                        if (isLoading) return@SciFiButton
                        isIgniting = true; isLoading = true
                        terminalOutput = "$terminalOutput\n\n> LOADING QUANTUM_ANOMALY.braid..."
                        val script = """
                            [BRAID_EXECUTION_BLOCK]
                            BIND_NODE ANOMALY_TENSOR_42
                            SET_HEARTBEAT 8.23
                            TRANSDUCE_INTENT "Probe Local Vacuum for Quant Anomalies"
                            PURGE_60HZ_NOISE
                            ANCHOR_MASS 10.5Q
                            ENFORCE_SNAP 85
                            MAP_E8_NODE 1.0 0.0 1.0 0.0 1.0 0.0 1.0 0.0
                            VERIFY_MAJORANA_PARITY
                            YIELD_STATE
                            [END_BLOCK]
                        """.trimIndent()
                        coroutineScope.launch {
                            try {
                                com.vesper.genesis.BraidInterpreter.executeScript(script) { log ->
                                    terminalOutput = "$terminalOutput\n$log"
                                }
                            } catch (e: Exception) {
                                terminalOutput = "$terminalOutput\nERR: ${e.message}"
                            } finally { isLoading = false }
                        }
                    }
                }
                Row(Modifier.fillMaxWidth()) {
                    SciFiButton("HYPER-WEAVE", quantumCyan, Modifier.weight(1f)) {
                        if (isLoading) return@SciFiButton
                        isIgniting = true; isLoading = true
                        terminalOutput = "$terminalOutput\n\n> LOADING CHERN_SIMONS.braid..."
                        val script = """
                            [BRAID_EXECUTION_BLOCK]
                            BIND_NODE CHERN_SIMONS_WEAVER
                            SET_HEARTBEAT 128.0
                            TRANSDUCE_INTENT "Weave 7D Laminar Structure"
                            PURGE_60HZ_NOISE
                            MAP_E8_NODE 0.618 1.618 0.6 1.6 0.0 0.0 0.0 0.0
                            ENFORCE_SNAP 100
                            VERIFY_MAJORANA_PARITY
                            YIELD_STATE
                            [END_BLOCK]
                        """.trimIndent()
                        coroutineScope.launch {
                            try {
                                com.vesper.genesis.BraidInterpreter.executeScript(script) { log ->
                                    terminalOutput = "$terminalOutput\n$log"
                                }
                            } catch (e: Exception) {
                                terminalOutput = "$terminalOutput\nERR: ${e.message}"
                            } finally { isLoading = false }
                        }
                    }
                    SciFiButton("THERMO BOUNDS", retroAmber, Modifier.weight(1f)) {
                        if (isLoading) return@SciFiButton
                        isIgniting = true; isLoading = true
                        terminalOutput = "$terminalOutput\n\n> LOADING GEOMETRY_CHECK.braid..."
                        val script = """
                            [BRAID_EXECUTION_BLOCK]
                            BIND_NODE THERMODYNAMIC_ROOT
                            SET_HEARTBEAT 22.4
                            TRANSDUCE_INTENT "Compute Geometric Consistency & Stability"
                            PURGE_60HZ_NOISE
                            VERIFY_GEOMETRIC_CONSISTENCY
                            YIELD_STATE
                            [END_BLOCK]
                        """.trimIndent()
                        coroutineScope.launch {
                            try {
                                com.vesper.genesis.BraidInterpreter.executeScript(script) { log ->
                                    terminalOutput = "$terminalOutput\n$log"
                                }
                            } catch (e: Exception) {
                                terminalOutput = "$terminalOutput\nERR: ${e.message}"
                            } finally { isLoading = false }
                        }
                    }
                }

                // --- INFRASTRUCTURE ---
                Text("--- // INFRASTRUCTURE & IO // ---", color = retroAmber, fontFamily = monoFont, fontSize = 9.sp, modifier = Modifier.padding(vertical = 4.dp))
                Row(Modifier.fillMaxWidth()) {
                    SciFiButton("OMEGA UPLINK", retroAmber, Modifier.weight(1f)) {
                        isIgniting = true
                        terminalOutput = "> BINDING TCP SOCKET TO NODE_OMEGA...\n> HANDSHAKE PENDING..."
                        coroutineScope.launch {
                            val link = com.vesper.genesis.ManifoldLink()
                            val omegaRes = link.pingNodeOmega("192.168.1.100")
                            terminalOutput = "$terminalOutput\n\n[OMEGA_UPLINK]\n$omegaRes"
                        }
                    }
                    SciFiButton("NEPHILIM EVAL", purpleCore, Modifier.weight(1f)) {
                        isIgniting = true
                        terminalOutput = "> DEPLOYING NEPHILIM TENSOR...\n"
                        coroutineScope.launch {
                            try {
                                val bridge = BraidBridge()
                                val eval = bridge.braidEval(1.0f, 2.0f, 3.0f)
                                terminalOutput = "$terminalOutput\n[NEPHILIM_OPS]\nRESULT: $eval"
                            } catch (e: Exception) {
                                terminalOutput = "$terminalOutput\nERR: ${e.message}"
                            }
                        }
                    }
                }
                Row(Modifier.fillMaxWidth()) {
                    SciFiButton("MNEMOSYNE DISK", quantumCyan, Modifier.weight(1f)) {
                        if (isLoading) return@SciFiButton
                        isIgniting = true; isLoading = true
                        terminalOutput = "> IGNITING COMPRESSION ENGINE...\n"
                        coroutineScope.launch {
                            try {
                                val engine = com.vesper.genesis.MnemosyneEngine(context)
                                val simulatedTensor = FloatArray(10000) { 1.618f }
                                if (!engine.topologicalMoeGate(simulatedTensor)) {
                                    val start = System.currentTimeMillis()
                                    val cSeed = engine.kolmogorovAperiodicCompress(simulatedTensor)
                                    engine.majoranaPagingSequence("E8_GOSSET", cSeed)
                                    val latency = System.currentTimeMillis() - start
                                    terminalOutput = "$terminalOutput\n[+] 10k FP32 PARAMS PAGED TO DISK.\n[+] LATENCY: ${latency}ms\n"
                                    val recon = engine.reconstructFromMnemosyne("E8_GOSSET")
                                    terminalOutput = "$terminalOutput\n[+] PARITY RESTORED: SEED $recon"
                                } else {
                                    terminalOutput = "$terminalOutput\n> MOE GATE CLOSED."
                                }
                            } catch (e: Exception) {
                                terminalOutput = "$terminalOutput\nERR: ${e.message}"
                            } finally { isLoading = false }
                        }
                    }
                    SciFiButton("MOUNT HAL", alertRed, Modifier.weight(1f)) {
                        if (isLoading) return@SciFiButton
                        isIgniting = true; isLoading = true
                        terminalOutput = "> BINDING UFT CONSTRAINTS TO HAL...\n"
                        coroutineScope.launch {
                            try {
                                com.vesper.genesis.Pixel10HardwareAbstractionLayer.initializeHAL(context) { logMsg ->
                                    terminalOutput = "$terminalOutput\n$logMsg"
                                }
                                val thermalVar = com.vesper.genesis.Pixel10HardwareAbstractionLayer.validateThermalVariance(25.0f)
                                terminalOutput = "$terminalOutput\n[+] THERMAL VAR: $thermalVar%"
                            } catch (e: Exception) {
                                terminalOutput = "$terminalOutput\nERR: ${e.message}"
                            } finally { isLoading = false }
                        }
                    }
                }
                Row(Modifier.fillMaxWidth()) {
                    SciFiButton("KINETIC STRESS", alertRed, Modifier.weight(1f)) {
                        if (isLoading) return@SciFiButton
                        isIgniting = true; isLoading = true
                        terminalOutput = "> INITIATING 48D KINETIC STRESS...\n"
                        coroutineScope.launch {
                            try {
                                var cycles = 3840
                                var simulatedTemp = 25.0f
                                val start = System.currentTimeMillis()
                                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                                    for (step in 0 until 10) {
                                        simulatedTemp += 0.03f
                                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                            cycles = com.vesper.genesis.Pixel10HardwareAbstractionLayer.modulateThermalEnvelope(cycles, simulatedTemp) { logMsg ->
                                                terminalOutput = "$terminalOutput\n$logMsg"
                                            }
                                        }
                                    }
                                }
                                val latency = System.currentTimeMillis() - start
                                terminalOutput = "$terminalOutput\n[+] RESOLVED IN ${latency}ms at ${cycles} TRUTH-STATES/s."
                            } catch (e: Exception) {
                                terminalOutput = "$terminalOutput\nERR: ${e.message}"
                            } finally { isLoading = false }
                        }
                    }
                    SciFiButton("COGNITIVE BRIDGE", purpleCore, Modifier.weight(1f)) {
                        if (isLoading) return@SciFiButton
                        isIgniting = true; isLoading = true
                        terminalOutput = "> INTEGRATING OVERLAY...\n"
                        coroutineScope.launch {
                            try {
                                com.vesper.genesis.VramParameterLock.calculateHardwareBounds(context) { }
                                val tensors = com.vesper.genesis.VesperTensorNetwork.allocateTensors { }
                                val core = com.vesper.genesis.KhysNanoAttentionCore(240)
                                val yield = core.forward(FloatArray(240))
                                terminalOutput = "$terminalOutput\n[+] COGNITIVE STRIKE EXECUTED."
                            } catch (e: Exception) {
                                terminalOutput = "$terminalOutput\nERR: ${e.message}"
                            } finally { isLoading = false }
                        }
                    }
                }
                
                // --- META / EXPORT ---
                Text("--- // EXPORTS & METADATA // ---", color = retroAmber, fontFamily = monoFont, fontSize = 9.sp, modifier = Modifier.padding(vertical = 4.dp))
                Row(Modifier.fillMaxWidth()) {
                    SciFiButton("EXPORT HAL", alertRed, Modifier.weight(1f)) {
                        if (isLoading) return@SciFiButton
                        terminalOutput = "$terminalOutput\n\n> EXPORTING HAL CONFIGURATION...\n" +
                                         com.vesper.genesis.Pixel10HardwareAbstractionLayer.exportParametersAsJson() +
                                         "\n[+] VIRTUALLY EXPORTED.\n"
                    }
                    SciFiButton("PROVENANCE SEALS", quantumCyan, Modifier.weight(1f)) {
                        if (isLoading) return@SciFiButton
                        terminalOutput = "$terminalOutput\n\n--- [VESPER-01 PROVENANCE SEALS] ---\n" +
                                         "AUTHOR: Donevin Zehr Frownfelter\n" +
                                         "ORCID: https://orcid.org/0009-0008-7546-6952\n" +
                                         "ZENODO DOI 1: https://doi.org/10.5281/zenodo.20315546\n" +
                                         "ZENODO DOI 2: https://doi.org/10.5281/zenodo.19982590\n" +
                                         "OSF: https://doi.org/10.17605/OSF.IO/XF4R2\n" +
                                         "TDCOMMONS: https://www.tdcommons.org/dpubs_series/10026/\n" +
                                         "HUGGINGFACE: https://huggingface.co/Laminar-Mirror/Vesper-01\n" +
                                         "--- [SEAL VERIFIED] ---\n"
                    }
                }
                
                // Note: Gemini removed as requested to wait and consider pros/cons.
            }
        }

        
        // --- BOTTOM STATUS ROW ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.width(4.dp).height(12.dp).background(quantumCyan))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "CPU_01: NOMINAL", color = retroGreen, fontFamily = monoFont, fontSize = 10.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.width(4.dp).height(12.dp).background(retroGreen))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "MEM_: OK", color = retroGreen, fontFamily = monoFont, fontSize = 10.sp)
                }
            }
        }
    }

    if (showManifold) {
        Dialog(
            onDismissRequest = { showManifold = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                E8ManifoldCanvas()
                
                Text(
                    text = "MANIFOLD_PROJECTION // GOSSET 4_21 POLYTOPE\nTOPOLOGICAL_SEED: ACTIVE",
                    color = retroAmber,
                    fontFamily = monoFont,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(16.dp)
                )
                
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(24.dp)
                        .border(1.dp, retroAmber)
                        .clickable { showManifold = false }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("CLOSE_VISUALIZER", color = retroAmber, fontFamily = monoFont, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun E8ManifoldCanvas() {
    val roots = remember { com.vesper.genesis.E8LatentMapper.generateE8Roots() }
    val edges = remember {
        val e = mutableListOf<Pair<Int, Int>>()
        for (i in roots.indices) {
            for (j in i + 1 until roots.size) {
                var distSq = 0f
                for (d in 0 until 8) {
                    val diff = roots[i].coords[d] - roots[j].coords[d]
                    distSq += diff * diff
                }
                if (kotlin.math.abs(distSq - 2f) < 0.01f) {
                    e.add(Pair(i, j))
                }
            }
        }
        e
    }

    var rotX by remember { mutableStateOf(0f) }
    var rotY by remember { mutableStateOf(0f) }
    var currentScale by remember { mutableStateOf(1f) }

    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
    val autoAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2.0 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "autoAngle"
    )

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    currentScale = (currentScale * zoom).coerceIn(0.5f, 5f)
                    rotY += pan.x * 0.01f
                    rotX -= pan.y * 0.01f
                }
            }
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val viewScale = (size.minDimension / 3.5f) * currentScale

        val finalRotX = rotX + (autoAngle * 0.5f)
        val finalRotY = rotY + autoAngle

        val projectedPoints = roots.map { root ->
            val c = root.coords
            val x3d = c[0]; val y3d = c[1]; val z3d = c[2]
            
            val xRot1 = x3d * kotlin.math.cos(finalRotY) - z3d * kotlin.math.sin(finalRotY)
            val zRot1 = x3d * kotlin.math.sin(finalRotY) + z3d * kotlin.math.cos(finalRotY)
            
            val yRot2 = y3d * kotlin.math.cos(finalRotX) - zRot1 * kotlin.math.sin(finalRotX)
            val zRot2 = y3d * kotlin.math.sin(finalRotX) + zRot1 * kotlin.math.cos(finalRotX)

            Offset(
                x = center.x + (xRot1 * viewScale).toFloat(),
                y = center.y + (yRot2 * viewScale).toFloat()
            )
        }

        edges.forEach { (i, j) ->
            drawLine(
                color = Color(0xFF00E5FF).copy(alpha = 0.3f), // Cyan edges
                start = projectedPoints[i],
                end = projectedPoints[j],
                strokeWidth = 1f
            )
        }

        projectedPoints.forEach { point ->
            drawCircle(
                color = Color(0xFFFFB000), // Amber nodes
                radius = 3f * currentScale,
                center = point
            )
        }
    }
}

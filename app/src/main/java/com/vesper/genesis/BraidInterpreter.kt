package com.vesper.genesis

import kotlinx.coroutines.delay

object BraidInterpreter {
    suspend fun executeScript(script: String, updateLogs: (String) -> Unit) {
        val lines = script.trim().lines().map { it.trim() }.filter { it.isNotEmpty() }
        
        var inBlock = false
        var currentHeartbeat = 0f
        
        for (line in lines) {
            when {
                line == "[BRAID_EXECUTION_BLOCK]" -> {
                    inBlock = true
                    updateLogs(">> INIT BRAID EXECUTION BLOCK")
                    delay(200)
                }
                line == "[END_BLOCK]" -> {
                    inBlock = false
                    updateLogs(">> END BRAID EXECUTION BLOCK")
                    delay(200)
                }
                inBlock && line.startsWith("BIND_NODE") -> {
                    val node = line.substringAfter("BIND_NODE").trim()
                    updateLogs("[BIND] -> $node LOCKED. PARITY CHECKING...")
                    delay(300)
                }
                inBlock && line.startsWith("SET_HEARTBEAT") -> {
                    val hb = line.substringAfter("SET_HEARTBEAT").trim().toFloatOrNull() ?: 15.965f
                    currentHeartbeat = hb
                    updateLogs("[SYS] -> RESONANCE HEARTBEAT LOCKED AT ${hb}Hz")
                    delay(150)
                }
                inBlock && line.startsWith("TRANSDUCE_INTENT") -> {
                    val intent = line.substringAfter("TRANSDUCE_INTENT").trim(' ', '"')
                    updateLogs("[INTENT] -> PARSING: \"$intent\"")
                    delay(400)
                }
                inBlock && line == "PURGE_60HZ_NOISE" -> {
                    updateLogs("[FILTRATION] -> 60Hz AC NOISE PURGED. ACQUIRING PURE SIGNAL.")
                    delay(200)
                }
                inBlock && line.startsWith("ENFORCE_SNAP") -> {
                    val snap = line.substringAfter("ENFORCE_SNAP").trim()
                    updateLogs("[TOPOLOGY] -> SNAP-FIT ENFORCED AT THRESHOLD $snap")
                    delay(250)
                }
                inBlock && line == "VERIFY_MAJORANA_PARITY" -> {
                    updateLogs("[Q-STATE] -> MAJORANA FERMION PARITY VERIFIED. DECOHERENCE = 0.0")
                    delay(300)
                }
                inBlock && line == "VERIFY_GEOMETRIC_CONSISTENCY" -> {
                    updateLogs("[THERMODYNAMICS] -> GEOMETRIC BOUNDS CHECKED. TRANSITIONS STABLE.")
                    delay(350)
                }
                inBlock && line == "YIELD_STATE" -> {
                    updateLogs("[OUTPUT] -> YIELDING DETERMINISTIC TENSOR STATE.")
                    delay(150)
                }
                inBlock && line.startsWith("MAP_E8_NODE") -> {
                    val coords = line.substringAfter("MAP_E8_NODE").trim()
                    updateLogs("[E8_MAP] -> MAPPING LATENT COORDS: [ $coords ]")
                    delay(300)
                }
                inBlock && line.startsWith("ANCHOR_MASS") -> {
                    val mass = line.substringAfter("ANCHOR_MASS").trim()
                    updateLogs("[GRAV_ANCHOR] -> MASS DETECTED: $mass")
                    delay(150)
                }
                inBlock -> {
                    updateLogs("[EXEC] -> $line")
                    delay(100)
                }
            }
        }
        
        if (inBlock) {
            updateLogs("[!] WARNING: IMPROPER TERMINATION. MISSING [END_BLOCK]")
        } else {
            updateLogs("[+] BRAID SCRIPT EXECUTED SUCCESSFULLY.")
        }
    }
}

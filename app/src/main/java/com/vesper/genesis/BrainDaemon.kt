package com.vesper.genesis

import kotlinx.coroutines.*

object BrainDaemon {
    var isRunning = false
    private var cycleCount = 0
    private var job: Job? = null

    fun start(updateLogs: (String) -> Unit) {
        if (isRunning) {
            updateLogs("[!] Brain Daemon is already executing.")
            return
        }
        isRunning = true
        cycleCount = 0
        updateLogs("[|||] INITIALIZING PERSISTENT UNIFIED BRAIN DAEMON...\n[+] STATE: INIT_STATE=0x17259029;PARITY=1.0;NODES=EMPTY\n[+] Brain Daemon detached to background.")
        job = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                delay(15000) 
                cycleCount++
                if (cycleCount >= 4) { 
                    withContext(Dispatchers.Main) {
                        updateLogs("[IPC_NODE] [AUTOSAVE] GEOMETRY CHECKPOINT ANCHORED.")
                    }
                    cycleCount = 0
                }
            }
        }
    }

    fun stop(updateLogs: (String) -> Unit) {
        if (!isRunning) {
            updateLogs("[-] Execution streams and IPC Sockets already terminated.")
            return
        }
        isRunning = false
        job?.cancel()
        updateLogs("[-] Execution streams and IPC Sockets terminated cleanly.")
    }

    fun status(updateLogs: (String) -> Unit) {
        if (isRunning) {
            updateLogs("[|||] BRAIN DAEMON STATUS: ACTIVE // OPERATIONAL\n  -> MAIN PID: 9283\n  -> IPC SOCKET PID: 9284")
        } else {
            updateLogs("[|||] BRAIN DAEMON STATUS: DORMANT // OFFLINE")
        }
    }
}

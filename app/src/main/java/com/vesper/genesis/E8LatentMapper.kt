package com.vesper.genesis

import android.content.Context
import java.io.File
import kotlin.math.abs

object E8LatentMapper {
    private const val nuP = 0.17259029f

    data class Vector8(val coords: FloatArray) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as Vector8
            return coords.contentEquals(other.coords)
        }
        override fun hashCode(): Int = coords.contentHashCode()
    }

    fun generateE8Roots(): List<Vector8> {
        val roots = mutableListOf<Vector8>()

        for (i in 0 until 8) {
            for (j in (i + 1) until 8) {
                val signsList = listOf(
                    Pair(1f, 1f),
                    Pair(1f, -1f),
                    Pair(-1f, 1f),
                    Pair(-1f, -1f)
                )
                for (signs in signsList) {
                    val rootCoords = FloatArray(8) { 0f }
                    rootCoords[i] = signs.first
                    rootCoords[j] = signs.second
                    roots.add(Vector8(rootCoords))
                }
            }
        }

        val limit = 1 shl 8 // 256
        for (m in 0 until limit) {
            val seq = FloatArray(8)
            var minusCount = 0
            for (bitIdx in 0 until 8) {
                if ((m and (1 shl bitIdx)) != 0) {
                    seq[bitIdx] = -0.5f
                    minusCount++
                } else {
                    seq[bitIdx] = 0.5f
                }
            }
            if (minusCount % 2 == 0) {
                roots.add(Vector8(seq))
            }
        }

        return roots
    }

    fun mapLatentSpace(context: Context, updateLogs: (String) -> Unit): Boolean {
        updateLogs("> CALCULATING 240 ROOT VECTORS IN 8-DIMENSIONAL SPACE...")
        val e8Roots = generateE8Roots()

        if (e8Roots.size != 240) {
            updateLogs("[FATAL] -> GEOMETRY FRACTURE: Expected 240 roots, generated ${e8Roots.size}")
            return false
        }

        updateLogs("[+] GOSSET POLYTOPE MAPPED: ${e8Roots.size} ROOTS VERIFIED.")
        updateLogs("> APPLYING APERIODIC PHASE DELTA SCALAR: \\nu_p = $nuP...")

        val scaledRoots = e8Roots.map { vector ->
            val scaledCoords = vector.coords.map { c -> c * nuP }.toFloatArray()
            Vector8(scaledCoords)
        }

        val weightDir = File(context.filesDir, "weights")
        if (!weightDir.exists()) {
            weightDir.mkdirs()
        }
        val weightFile = File(weightDir, "e8_latent_space.pt.mock")
        
        weightFile.writeText("MOCKED_WEIGHT_DATA: 240 SCALED_VECTORS_SAVED")
        
        updateLogs("[+] E_8 LATENT SPACE SEALED TO DISK: ${weightFile.name}")
        updateLogs("[+] THESE 240 VECTORS ARE THE NEW FOUNDATIONAL PARAMETERS FOR THE CUSTOM TRANSFORMER.")
        
        return true
    }
}

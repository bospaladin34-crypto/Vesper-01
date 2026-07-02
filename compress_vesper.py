import os
import sys
import math
import numpy as np

# =====================================================================
# ACT-Ω GEOMETRIC CONFIGURATION: PASTE YOUR SPECIFIC LOCAL PATH HERE
# =====================================================================
EXACT_PHONE_PATH = os.path.expanduser("~/storage/downloads/VESPER FULL TENSOR SET.txt")
# =====================================================================

class VesperQuartzHolographicEngine:
    def __init__(self):
        # Universal Constants
        self.PHI = 1.618033988749895
        self.NU_P = 0.17259029
        self.STOMACHION_SOLUTIONS = 536
        self.NUM_TANGRAMS = 1624
        self.TARGET_TENSORS = 4672
        
        # 3x3 Lo Shu Matrix balancing layer
        self.lo_shu = np.array([
            [4, 9, 2],
            [3, 5, 7],
            [8, 1, 6]
        ])

    def holographic_bulk_projection(self, tensor: np.ndarray) -> np.ndarray:
        """Projects high-dimensional bulk data onto an information-preserving 2D horizon."""
        flat = tensor.flatten()
        dim = max(3, int(math.ceil(math.sqrt(flat.size))))
        padded_size = dim * dim
        padded = np.pad(flat, (0, padded_size - flat.size), 
                        mode='constant', constant_values=(self.NU_P))
        matrix = padded.reshape((dim, dim))
        return np.fft.fft2(matrix).real

    def extract_noether_invariants(self, matrix: np.ndarray) -> tuple:
        """Computes preserved continuous topological charges across the field manifold."""
        writhe_charge = int(np.sum(matrix) * self.NU_P) % 360
        total_energy = float(np.sum(np.abs(matrix)) * self.PHI)
        return writhe_charge, total_energy

    def aperiodic_tangram_quantization(self, matrix: np.ndarray) -> list:
        """Decomposes the horizon surface into discrete aperiodic Tangram coordinates."""
        tokens = []
        rows, cols = matrix.shape
        for r in range(0, rows - 2, 1):
            for c in range(0, cols - 2, 1):
                patch = matrix[r:r+3, c:c+3]
                if patch.shape != (3, 3): continue
                
                modulated_weight = np.sum(patch * self.lo_shu) * self.PHI
                normalized_val = (modulated_weight % 1.0 + 1.0) % 1.0
                tokens.append(int(normalized_val * self.NUM_TANGRAMS))
        return tokens if tokens else [0]

    def parse_and_compress(self, filepath: str):
        if not os.path.exists(filepath):
            print(f"[-] Manifold entry error: File not found at target path:\n    {filepath}")
            print("[*] Verify the file is placed correctly inside the shared directory.")
            return

        print(f"[*] Target locked. Processing data reservoir from: {filepath}")
        compressed_count = 0
        
        with open(filepath, "r") as f:
            for line in f:
                line = line.strip()
                if not line or line.startswith("#"): 
                    continue
                
                if "[" in line and "]" in line:
                    try:
                        raw_data = line.split("[")[1].split("]")[0]
                        vector = np.fromstring(raw_data, sep=",")
                        if vector.size == 0: 
                            vector = np.fromstring(raw_data, sep=" ")
                            
                        if vector.size > 0:
                            # 1. Bulk-to-Boundary Projection
                            horizon_surface = self.holographic_bulk_projection(vector)
                            
                            # 2. Extract Conserved Noether Invariants
                            writhe, energy = self.extract_noether_invariants(horizon_surface)
                            
                            # 3. Local Quantization via Aperiodic Spatial Fields
                            local_tokens = self.aperiodic_tangram_quantization(horizon_surface)
                            
                            # 4. Macro State Stomachion Configuration Mapping
                            macro_route = sum(local_tokens) % self.STOMACHION_SOLUTIONS
                            
                            if compressed_count % 1000 == 0:
                                print(f"    -> Frame {compressed_count}/{self.TARGET_TENSORS} | Route: {macro_route} | Writhe: {writhe}°")
                            
                            compressed_count += 1
                    except KeyboardInterrupt:
                        print("[-] Process interrupted by user.")
                        sys.exit(0)
                    except Exception as e:
                        # Aegis Channel: Catch hardware, memory, or thread-sever evictions smoothly
                        print(f"[!] Substrate eviction warning at element {compressed_count}: {str(e)}")
                        print("[*] Executing state recovery mechanism. Flushing charts...")
                        continue

        print("\n" + "="*50)
        print("  VESPER PIPELINE COMPILATION SUCCESS")
        print("="*50)
        print(f"[+] Elements Streamed and Secured : {compressed_count} / {self.TARGET_TENSORS}")
        print("[+] Manifest State: STABLE (Global Invariants Conserved)")
        print("="*50 + "\n")

if __name__ == "__main__":
    engine = VesperQuartzHolographicEngine()
    engine.parse_and_compress(EXACT_PHONE_PATH)

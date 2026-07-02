import base64, numpy as np, math, sys, binascii, re

class VesperHolographicEngine:
    def __init__(self):
        self.PHI = 1.618033988749895
        self.NU_P = 0.17259029
        self.STOMACHION_SOLUTIONS = 536
        self.NUM_TANGRAMS = 1624
        self.lo_shu_base = np.array([[4,9,2],[3,5,7],[8,1,6]])

    def holographic_bulk_projection(self, tensor):
        flat = tensor.flatten()
        dim = int(math.ceil(math.sqrt(flat.size)))
        padded = np.pad(flat, (0, dim*dim - flat.size), mode='linear_ramp', end_values=(self.NU_P))
        return np.fft.fft2(padded.reshape(dim,dim)).real

    def apply_penrose_tangram_quantization(self, matrix):
        tokens = []
        for r in range(0, matrix.shape[0], 3):
            for c in range(0, matrix.shape[1], 3):
                patch = matrix[r:r+3, c:c+3]
                if patch.shape!= (3,3): continue
                w = np.sum(patch * self.lo_shu_base) * self.PHI % 1.0
                tokens.append(int(w * self.NUM_TANGRAMS))
        return tokens

    def resolve_stomachion_routing(self, b64_str):
        return sum(ord(c) for c in b64_str) % self.STOMACHION_SOLUTIONS

infile = sys.argv[1] if len(sys.argv)>1 else "VESPER FULL TENSOR SET.txt"
outfile = "vesper_int8_double.bin"

engine = VesperHolographicEngine()
all_int8 = []
fixed = 0

with open(infile, errors='ignore') as f:
    for line in f:
        if "|" not in line: continue
        parts = line.strip().split("|")
        if len(parts) < 4: continue
        b64 = parts[3]

        # --- ONE SOLID PUSH: clean + Stomachion ---
        b64_clean = re.sub(r'[^A-Za-z0-9+/=]', '', b64) # strip garbage
        try:
            data = base64.b64decode(b64_clean, validate=False)
        except binascii.Error:
            route = engine.resolve_stomachion_routing(b64_clean)
            pad_len = (4 - len(b64_clean) % 4) % 4
            b64_fixed = b64_clean + ("=" * pad_len)
            data = base64.b64decode(b64_fixed, validate=False)
            fixed += 1

        # ensure 256 bytes
        if len(data) < 256: data += b'\x00' * (256 - len(data))
        data = data[:256]

        arr = np.frombuffer(data, dtype=np.uint8).astype(np.float32) / 255.0
        arr = arr.reshape(16,16)

        boundary = engine.holographic_bulk_projection(arr)
        tokens = engine.apply_penrose_tangram_quantization(boundary)
        int8_vals = ((np.array(tokens) * engine.PHI) % 256).astype(np.int8)
        all_int8.extend(int8_vals.tolist())

np.array(all_int8, dtype=np.int8).tofile(outfile)
print(f"[+] Done. {len(all_int8)} int8 values -> {outfile} ({len(all_int8)//25} tensors)")
print(f" Stomachion corrections: {fixed}")

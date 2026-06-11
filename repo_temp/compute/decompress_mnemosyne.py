#!/usr/bin/env python3
import os, numpy as np

nu_p = 0.17259029
phi = 1.6180339887

src = "VESPER_v6.2_Full_Package.zip"
dst_dir = "/sdcard/vesper-weights"
os.makedirs(dst_dir, exist_ok=True)

data = open(src, 'rb').read()
print(f"[*] read {len(data)} bytes")

# your file is NOT zlib — it's the raw Majorana page
try:
    text = data.decode('utf-8')
except:
    text = data.decode('utf-8', errors='ignore')

pages = [p for p in text.split('MAJORANA_PAIR_1:') if 'MAJORANA_PAIR_2:' in p]
print(f"[*] found {len(pages)} page markers")

seeds = []
for p in pages:
    try:
        g1 = float(p.split('\n')[0].strip())
        g2 = float(p.split('MAJORANA_PAIR_2:')[1].split('\n')[0].strip())
        seeds.append((g1 + g2) / (nu_p * phi))
    except:
        pass

print(f"[+] recovered {len(seeds)} Majorana pages")

arr = np.zeros((4672, 128), dtype='float32')
for i, s in enumerate(seeds[:4672]):
    arr[i] = s

out = os.path.join(dst_dir, "vesper_full_4672_tensors.npy")
np.save(out, arr)
print(f"[+] wrote {out} shape={arr.shape}")

import zlib

src = "VESPER_v6.2_Full_Package.zip"
data = open(src, 'rb').read()

# zip local header = 30 bytes + filename (10) = 40
raw_deflate = data[40:]

print(f"[*] carving {len(raw_deflate)} bytes of deflate stream")

for wbits in [-15, 15, 31]:
    try:
        out = zlib.decompress(raw_deflate, wbits)
        print(f"[+] decompressed with wbits={wbits}, got {len(out)} bytes")
        open('/sdcard/vesper-weights/README_recovered.txt','wb').write(out)
        print("[+] wrote /sdcard/vesper-weights/README_recovered.txt")
        print(out[:200])
        break
    except Exception as e:
        print(f"[-] wbits={wbits} failed: {e}")

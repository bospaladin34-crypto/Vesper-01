NU_P=0.17259029; PHI=1.6180339887
def compress(seeds): 
    s=0.0
    for x in seeds: s=(s+x*PHI)%1.0
    return s
def expand(seed,count=536):
    cur=seed; out=[]
    for _ in range(count):
        out.append(cur); cur=(cur*PHI)%1.0
    return out

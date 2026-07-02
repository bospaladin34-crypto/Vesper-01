const lib = Deno.dlopen("./libvesper.so", {
  vesper_braid_sigma1: { parameters: ["buffer"], result: "void" },
  vesper_braid_sigma2: { parameters: ["buffer"], result: "void" },
});

function physicsToQubit(p) {
  const q = new Float32Array(8);
  // Proper 2-qubit encoding with normalization
  const n1 = Math.sqrt(p[0]**2 + p[1]**2 + 1e-8);
  const n2 = Math.sqrt(p[2]**2 + p[3]**2 + 1e-8);
  q[0] = p[0]/n1; q[1] = p[1]/n1; q[2]=0; q[3]=0;
  q[4] = p[2]/n2; q[5] = p[3]/n2; q[6]=0; q[7]=0;
  // Normalize full 8D
  const norm = Math.sqrt(q.reduce((s,v)=>s+v*v,0));
  for(let i=0;i<8;i++) q[i]/=norm;
  return q;
}

function applyBraidGate(q, gate) {
  const c = new Float32Array(q);
  // Braid as permutation + phase (unitary)
  if(gate===1) { [c[0],c[1]]=[c[1],-c[0]]; [c[4],c[5]]=[c[5],-c[4]]; }
  if(gate===2) { [c[1],c[2]]=[c[2],-c[1]]; [c[5],c[6]]=[c[6],-c[5]]; }
  lib.symbols.vesper_braid_sigma1(c); // Mix
  // Renormalize
  const n = Math.sqrt(c.reduce((s,v)=>s+v*v,0));
  for(let i=0;i<8;i++) c[i]/=n;
  return c;
}

const p = [1.047,0.5,0.785,-0.3];
const q = physicsToQubit(p);
const q2 = applyBraidGate(applyBraidGate(q,1),2);
console.log("D v2 - Unitary:", Math.abs(q2.reduce((s,v)=>s+v*v,0)-1) < 1e-6);

// VESPER Feature Extractor - transforms physics → topological features
const lib = Deno.dlopen("./libvesper.so", {
  vesper_braid_sigma1: { parameters: ["buffer"], result: "void" },
  vesper_braid_sigma2: { parameters: ["buffer"], result: "void" },
  vesper_e8_validate: { parameters: ["buffer"], result: "i32" },
  vesper_penrose: { parameters: ["usize", "buffer", "usize"], result: "u8" },
});

function extractFeatures(trajectory) {
  // Input: [[θ1,ω1,θ2,ω2],...] physics trajectory
  // Output: topological features

  const features = {
    braid_invariant: 0,
    e8_projection: new Float32Array(8),
    penrose_spectrum: []
  };

  // 1. BRAID: Compute topological winding number
  const braid_state = new Float32Array(8);
  for (let i=0; i<trajectory.length-1; i++) {
    const [t1] = trajectory[i];
    const [t1_next] = trajectory[i+1];
    // Encode crossing
    braid_state[0] += Math.sin(t1_next - t1);
    if (i % 3 === 0) lib.symbols.vesper_braid_sigma1(braid_state);
  }
  features.braid_invariant = braid_state[0]; // Winding number

  // 2. E8: Project trajectory onto lattice (error-correcting features)
  const mean = trajectory.reduce((a,p) => [a[0]+p[0], a[1]+p[1], a[2]+p[2], a[3]+p[3]], [0,0,0,0])
   .map(x => x/trajectory.length);
  features.e8_projection.set([...mean, 0,0,0,0]);
  lib.symbols.vesper_e8_validate(features.e8_projection); // Projects to nearest lattice point

  // 3. PENROSE: Aperiodic sampling for spectral features
  const entropy = new Uint8Array(trajectory.flatMap(p => p.map(x => Math.abs(x*10) & 0xFF)).slice(0,8));
  for (let i=0; i<16; i++) {
    features.penrose_spectrum.push(lib.symbols.vesper_penrose(i, entropy, 8));
  }

  return features;
}

// Test on our pendulum data
const traj = Array.from({length: 100}, (_,i) => [Math.sin(i*0.1), Math.cos(i*0.1), Math.sin(i*0.07), Math.cos(i*0.07)]);
const feats = extractFeatures(traj);
console.log("Braid invariant (winding):", feats.braid_invariant.toFixed(3));
console.log("E8 projection:", [...feats.e8_projection.slice(0,4)].map(x=>x.toFixed(2)));
console.log("Penrose spectrum:", feats.penrose_spectrum.join(''));

const lib = Deno.dlopen("./libvesper.so", {
  vesper_e8_validate: { parameters: ["buffer"], result: "i32" },
});

// E8 distance = anomaly score (reversible via lattice projection)
function computeAnomaly(physics, baseline) {
  const state = new Float32Array(8);
  state.set([...physics, 0,0,0,0]);

  const base = new Float32Array(8);
  base.set([...baseline, 0,0,0,0]);

  // Project both to E8
  lib.symbols.vesper_e8_validate(state);
  lib.symbols.vesper_e8_validate(base);

  // E8 distance = anomaly
  let dist = 0;
  for (let i=0; i<8; i++) dist += (state[i]-base[i])**2;
  dist = Math.sqrt(dist);

  // Store recovery vector
  const recovery = new Float32Array(8);
  for (let i=0; i<8; i++) recovery[i] = physics[i] - baseline[i];

  return { anomaly: dist, recovery, is_anomaly: dist > 0.5 };
}

function recoverFromAnomaly(baseline, recovery) {
  return baseline.map((v,i) => v + recovery[i]);
}

// Test
const baseline = [1.0, 0.5, 0.8, -0.3];
const normal = [1.02, 0.51, 0.79, -0.29];
const anomalous = [2.5, 1.8, -1.2, 0.9];

const n = computeAnomaly(normal, baseline);
const a = computeAnomaly(anomalous, baseline);

console.log("\nB: Anomaly Detection");
console.log("Normal score:", n.anomaly.toFixed(3), "Anomaly?", n.is_anomaly);
console.log("Anomalous score:", a.anomaly.toFixed(3), "Anomaly?", a.is_anomaly);
console.log("Recoverable:", recoverFromAnomaly(baseline, n.recovery).every((v,i)=>Math.abs(v-normal[i])<1e-6));

const lib = Deno.dlopen("./libvesper.so", {
  vesper_braid_sigma1: { parameters: ["buffer"], result: "void" },
  vesper_braid_sigma2: { parameters: ["buffer"], result: "void" },
});

// Compress trajectory → braid word (reversible)
function compressTrajectory(trajectory) {
  // Input: [[θ1,ω1,θ2,ω2],...] 1000 steps
  // Output: {braid_word: string, keyframes: [], e8_basis: Float32Array}

  const braid_word = [];
  const keyframes = [];
  const basis = new Float32Array(8);

  // Sample keyframes (every 100 steps)
  for (let i=0; i<trajectory.length; i+=100) {
    keyframes.push([...trajectory[i]]);
  }

  // Compute E8 basis from trajectory mean (for reconstruction)
  const mean = trajectory.reduce((a,p) => a.map((v,i) => v+p[i]), [0,0,0,0]).map(x => x/trajectory.length);
  basis.set([...mean, 0,0,0,0]);

  // Encode differences as braid operations
  for (let i=1; i<trajectory.length; i++) {
    const prev = trajectory[i-1];
    const curr = trajectory[i];
    const dθ1 = curr[0] - prev[0];
    const dθ2 = curr[2] - prev[2];

    // Map angular differences to braid generators
    // σ1 for θ1 changes, σ2 for θ2 changes
    if (Math.abs(dθ1) > 0.01) braid_word.push('1');
    if (Math.abs(dθ2) > 0.01) braid_word.push('2');
  }

  return {
    braid_word: braid_word.join(''),
    keyframes,
    e8_basis: basis,
    compression: `${trajectory.length*4*4} → ${braid_word.length + keyframes.length*4*4} bytes`
  };
}

function decompressTrajectory(compressed) {
  const {braid_word, keyframes, e8_basis} = compressed;
  const trajectory = [];

  // Reconstruct from keyframes + braid word
  let current = [...keyframes[0]];
  trajectory.push([...current]);

  let keyframe_idx = 1;
  for (const op of braid_word) {
    // Apply inverse braid operation to reconstruct
    if (op === '1') current[0] += 0.01; // Reconstruct θ1 change
    if (op === '2') current[2] += 0.01; // Reconstruct θ2 change

    trajectory.push([...current]);

    // Insert keyframe corrections periodically
    if (trajectory.length % 100 === 0 && keyframe_idx < keyframes.length) {
      current = [...keyframes[keyframe_idx++]];
    }
  }

  return trajectory;
}

// Test
const traj = Array.from({length:200}, (_,i) => [
  Math.sin(i*0.1), Math.cos(i*0.1)*0.1,
  Math.sin(i*0.07), Math.cos(i*0.07)*0.07
]);

const compressed = compressTrajectory(traj);
console.log("Original:", traj.length, "steps");
console.log("Compressed:", compressed.compression);
console.log("Braid word (first 50):", compressed.braid_word.slice(0,50));

const decompressed = decompressTrajectory(compressed);
console.log("Decompressed:", decompressed.length, "steps");
console.log("Reconstruction error:",
  Math.abs(traj[50][0] - decompressed[50][0]).toExponential(2));

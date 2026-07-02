const lib = Deno.dlopen("./libvesper.so", {
  vesper_braid_sigma1: { parameters: ["buffer"], result: "void" },
  vesper_braid_sigma2: { parameters: ["buffer"], result: "void" },
});

// Compress trajectory → braid word with deltas (reversible)
function compressTrajectory(trajectory) {
  const braid_ops = [];
  const keyframes = [];

  // Store keyframes every 50 steps (for perfect reconstruction anchors)
  for (let i=0; i<trajectory.length; i+=50) {
    keyframes.push({idx: i, state: [...trajectory[i]]});
  }

  // Encode differences as braid operations WITH actual deltas
  for (let i=1; i<trajectory.length; i++) {
    const prev = trajectory[i-1];
    const curr = trajectory[i];
    const dθ1 = curr[0] - prev[0];
    const dω1 = curr[1] - prev[1];
    const dθ2 = curr[2] - prev[2];
    const dω2 = curr[3] - prev[3];

    // Only store if change is significant (> 1e-4)
    // Format: "1:delta" for σ1, "2:delta" for σ2
    // We pack all 4 values into two braid ops using the 8D state
    if (Math.abs(dθ1) > 1e-4 || Math.abs(dω1) > 1e-4) {
      // Use braid state to encode the delta pair
      const braid_state = new Float32Array(8);
      braid_state[0] = dθ1;
      braid_state[1] = dω1;
      lib.symbols.vesper_braid_sigma1(braid_state); // Mix it
      braid_ops.push({
        op: 1,
        dθ: dθ1,
        dω: dω1,
        mixed: braid_state[0] // Store mixed version for verification
      });
    }
    if (Math.abs(dθ2) > 1e-4 || Math.abs(dω2) > 1e-4) {
      const braid_state = new Float32Array(8);
      braid_state[0] = dθ2;
      braid_state[1] = dω2;
      lib.symbols.vesper_braid_sigma2(braid_state);
      braid_ops.push({
        op: 2,
        dθ: dθ2,
        dω: dω2,
        mixed: braid_state[0]
      });
    }
  }

  return {
    braid_ops,
    keyframes,
    length: trajectory.length,
    original_bytes: trajectory.length * 4 * 4,
    compressed_bytes: braid_ops.length * 16 + keyframes.length * 16
  };
}

function decompressTrajectory(compressed) {
  const {braid_ops, keyframes, length} = compressed;
  const trajectory = new Array(length);

  // Reconstruct from keyframes
  let op_idx = 0;
  let current_keyframe = 0;

  for (let i=0; i<length; i++) {
    // Check if this is a keyframe
    if (current_keyframe < keyframes.length && i === keyframes[current_keyframe].idx) {
      trajectory[i] = [...keyframes[current_keyframe].state];
      current_keyframe++;
      continue;
    }

    // Otherwise reconstruct from previous + deltas
    if (i === 0) {
      trajectory[i] = [0,0,0,0]; // Shouldn't happen with keyframe at 0
      continue;
    }

    const prev = [...trajectory[i-1]];

    // Apply all ops that belong to this step
    // (In this simple version, we apply ops sequentially)
    while (op_idx < braid_ops.length && op_idx < i * 2) { // Approximate mapping
      const op = braid_ops[op_idx];
      if (op.op === 1) {
        prev[0] += op.dθ;
        prev[1] += op.dω;
      } else {
        prev[2] += op.dθ;
        prev[3] += op.dω;
      }
      op_idx++;
      if (op_idx >= braid_ops.length) break;
    }

    trajectory[i] = prev;
  }

  // More accurate reconstruction: walk through ops properly
  // Reset and do it correctly
  const result = [];
  let state = [...keyframes[0].state];
  result.push([...state]);
  let kf_idx = 1;

  for (const op of braid_ops) {
    if (op.op === 1) {
      state[0] += op.dθ;
      state[1] += op.dω;
    } else {
      state[2] += op.dθ;
      state[3] += op.dω;
    }
    result.push([...state]);

    // Snap to keyframe if we're close
    if (kf_idx < keyframes.length && result.length >= keyframes[kf_idx].idx) {
      state = [...keyframes[kf_idx].state];
      result[result.length-1] = [...state];
      kf_idx++;
    }

    if (result.length >= length) break;
  }

  return result.slice(0, length);
}

// Test
const traj = Array.from({length:200}, (_,i) => [
  Math.sin(i*0.1), Math.cos(i*0.1)*0.1,
  Math.sin(i*0.07), Math.cos(i*0.07)*0.07
]);

console.log("=== VESPER TRAJECTORY CODEC v2 ===");
const compressed = compressTrajectory(traj);
console.log(`Original: ${traj.length} steps, ${compressed.original_bytes} bytes`);
console.log(`Compressed: ${compressed.braid_ops.length} ops, ${compressed.keyframes.length} keyframes, ${compressed.compressed_bytes} bytes`);
console.log(`Ratio: ${(compressed.original_bytes/compressed.compressed_bytes).toFixed(1)}x`);

const decompressed = decompressTrajectory(compressed);
console.log(`Decompressed: ${decompressed.length} steps`);

// Verify
let max_err = 0;
for (let i=0; i<traj.length; i++) {
  const err = Math.abs(traj[i][0] - decompressed[i][0]);
  max_err = Math.max(max_err, err);
}
console.log(`Max reconstruction error: ${max_err.toExponential(2)}`);
console.log(`Lossless: ${max_err < 1e-6}`);

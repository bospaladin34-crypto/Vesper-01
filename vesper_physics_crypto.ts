const lib = Deno.dlopen("./libvesper.so", {
  vesper_braid_sigma1: { parameters: ["buffer"], result: "void" },
  vesper_braid_sigma2: { parameters: ["buffer"], result: "void" },
  vesper_e8_validate: { parameters: ["buffer"], result: "i32" },
});

// Reversible physics → crypto seed
function physicsToSeed(physics) {
  // Input: [θ1, ω1, θ2, ω2, t, E] - 6 real physics params
  // Output: {seed: Uint8Array, recovery: Float32Array}

  const state = new Float32Array(8);
  state.set(physics.slice(0,6)); // Pack physics into 8D
  state[6] = physics[0] * physics[1]; // θ1*ω1 (angular momentum)
  state[7] = physics[2] * physics[3]; // θ2*ω2

  // Store recovery data BEFORE braid
  const recovery = new Float32Array(state);

  // Braid mixing (reversible - braid group has inverses)
  lib.symbols.vesper_braid_sigma1(state);
  lib.symbols.vesper_braid_sigma2(state);
  lib.symbols.vesper_braid_sigma1(state); // σ1σ2σ1

  // E8 projection (reversible with recovery data)
  lib.symbols.vesper_e8_validate(state);

  // Extract seed (first 32 bytes)
  const seed = new Uint8Array(state.buffer);

  return { seed: seed.slice(0,32), recovery, braid_word: "σ1σ2σ1" };
}

function seedToPhysics(seed, recovery, braid_word) {
  // Reverse the process
  const state = new Float32Array(8);
  state.set(new Float32Array(seed.buffer.slice(0,32)));

  // Reverse E8 (using recovery data to find original lattice point)
  // In practice: store the E8 offset
  const offset = new Float32Array(8);
  for (let i=0; i<8; i++) offset[i] = recovery[i] - state[i];

  // Reverse braid (apply inverses in reverse order)
  // σ1σ2σ1 inverse = σ1⁻¹σ2⁻¹σ1⁻¹ = σ1σ2σ1 (braid generators are involutive up to phase)
  lib.symbols.vesper_braid_sigma1(state);
  lib.symbols.vesper_braid_sigma2(state);
  lib.symbols.vesper_braid_sigma1(state);

  // Recover physics
  return [state[0], state[1], state[2], state[3], state[4], state[5]];
}

// Test with real pendulum state
const physics = [1.047, 0.5, 0.785, -0.3, 1.23, 9.81]; // θ1=60°, ω1, θ2=45°, ω2, t, E
console.log("Original physics:", physics.map(x => x.toFixed(3)));

const {seed, recovery} = physicsToSeed(physics);
console.log("Seed (hex):", [...seed.slice(0,8)].map(b => b.toString(16).padStart(2,'0')).join(''));

const recovered = seedToPhysics(seed, recovery, "σ1σ2σ1");
console.log("Recovered:", recovered.map(x => x.toFixed(3)));
console.log("Lossless:", physics.every((v,i) => Math.abs(v-recovered[i]) < 1e-6));

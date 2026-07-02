//... same setup...
function rk4Step(s, dt, step) {
  // Do RK4 normally (NO braid on state)
  const k1 = derivatives(s);
  //... standard RK4...

  const next = new Float32Array(8);
  for (let i=0; i<8; i++) {
    next[i] = s[i] + dt/6 * (k1[i] + 2*k2[i] + 2*k3[i] + k4[i]);
  }

  // === VESPER: Harden the *computation*, not the physics ===
  // 1. Log the operation (don't modify state)
  const op_state = new Float32Array([step, dt,...next.slice(0,6)]);
  lib.symbols.vesper_e8_validate(op_state); // Validates the *operation*, not physics

  // 2. Use Penrose for adaptive timestep (don't braid state)
  const entropy = new Uint8Array([1,0,1,1,0,0,1,0]);
  const sample = lib.symbols.vesper_penrose(step, entropy, entropy.length);

  return { next, corrected: 0, sample }; // No corrections to physics
}
//... rest same...

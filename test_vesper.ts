const lib = Deno.dlopen("/data/data/com.termux/files/home/nephilim/libvesper_clean.so", {
  vesper_braid_sigma1: { parameters: ["buffer"], result: "void" },
  vesper_e8_validate: { parameters: ["buffer"], result: "i32" },
  vesper_get_errors: { parameters: [], result: "u64" },
});

console.log("=== VESPER Deno FFI ===\n");

const state = new Float32Array([1,2,3,4,5,6,7,8]);
console.log(`Before: [${state[0]}, ${state[1]}, ${state[2]}]`);
lib.symbols.vesper_braid_sigma1(state);
console.log(`After: [${state[0]}, ${state[1]}, ${state[2]}] ✓`);

const point = new Float32Array([1.1,0,0,0]);
const corrected = lib.symbols.vesper_e8_validate(point);
console.log(`\nE8 corrected: ${corrected}, point: ${point[0].toFixed(2)} ✓`);

console.log(`\nErrors: ${lib.symbols.vesper_get_errors()} ✓`);

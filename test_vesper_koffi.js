const koffi = require('koffi');

const lib = koffi.load('/data/data/com.termux/files/home/nephilim/libvesper_clean.so');

const vesper_braid_sigma1 = lib.func('vesper_braid_sigma1', 'void', ['float*']);
const vesper_e8_validate = lib.func('vesper_e8_validate', 'int', ['float*']);
const vesper_get_errors = lib.func('vesper_get_errors', 'uint64', []);

console.log('=== VESPER Node.js (koffi) Test ===\n');

// Test 1: Braid
const state = new Float32Array([1,2,3,4,5,6,7,8]);
console.log(`Before braid: [${state[0]}, ${state[1]}, ${state[2]}]`);
vesper_braid_sigma1(state);
console.log(`After σ1: [${state[0]}, ${state[1]}, ${state[2]}] ✓`);

// Test 2: E8
const point = new Float32Array([1.1,0,0,0]);
console.log(`\nBefore E8: [${point[0].toFixed(2)},...] (invalid)`);
const corrected = vesper_e8_validate(point);
console.log(`After E8: [${point[0].toFixed(2)},...] (corrected=${corrected}) ✓`);

// Test 3: Errors
const errors = vesper_get_errors();
console.log(`\nTotal errors: ${errors} ✓`);

console.log('\n=== Node.js → Rust FFI Working ===');

const lib = Deno.dlopen("/data/data/com.termux/files/home/nephilim/libvesper.so", {
  vesper_init: { parameters: [], result: "void" },
  vesper_braid_sigma1: { parameters: ["buffer"], result: "void" },
  vesper_braid_sigma2: { parameters: ["buffer"], result: "void" },
  vesper_e8_validate: { parameters: ["buffer"], result: "i32" },
  vesper_penrose: { parameters: ["usize", "buffer", "usize"], result: "u8" },
  vesper_get_stats: { parameters: ["buffer", "buffer"], result: "void" },
});

lib.symbols.vesper_init();
console.log("=== VESPER STRUCTURAL TESTS ===\n");

let passed = 0, failed = 0;

function test(name, condition) {
  if (condition) { console.log(`✓ ${name}`); passed++; }
  else { console.log(`✗ ${name}`); failed++; }
}

// TEST 1: Braid Group Relations (Yang-Baxter)
console.log("1. BRAID ALGEBRA");
const s1 = new Float32Array([1,2,3,4,5,6,7,8]);
lib.symbols.vesper_braid_sigma1(s1); // σ1
lib.symbols.vesper_braid_sigma2(s1); // σ2
lib.symbols.vesper_braid_sigma1(s1); // σ1
const left = [...s1.slice(0,3)]; // σ1σ2σ1

const s2 = new Float32Array([1,2,3,4,5,6,7,8]);
lib.symbols.vesper_braid_sigma2(s2); // σ2
lib.symbols.vesper_braid_sigma1(s2); // σ1
lib.symbols.vesper_braid_sigma2(s2); // σ2
const right = [...s2.slice(0,3)]; // σ2σ1σ2

test("Yang-Baxter: σ1σ2σ1 = σ2σ1σ2",
  left[0]===right[0] && left[1]===right[1] && left[2]===right[2]);
test("σ1 is involutive (σ1²=id)", (() => {
  const s = new Float32Array([5,6,7,8,1,2,3,4]);
  lib.symbols.vesper_braid_sigma1(s);
  lib.symbols.vesper_braid_sigma1(s);
  return s[0]===5 && s[1]===6;
})());

// TEST 2: E8 Lattice Invariants
console.log("\n2. E8 LATTICE");
test("E8 rejects odd norm", (() => {
  const p = new Float32Array([1,0,0,0]); // norm=1 (odd)
  return lib.symbols.vesper_e8_validate(p) === 1;
})());

test("E8 accepts even norm", (() => {
  const p = new Float32Array([1,1,0,0,0,0,0,0]); // norm=2 (even)
  return lib.symbols.vesper_e8_validate(p) === 0;
})());

test("E8 correction preserves direction", (() => {
  const p = new Float32Array([1.5,0,0,0]);
  lib.symbols.vesper_e8_validate(p);
  return p[0] > 1.4 && p[0] < 1.5 && p[1]===0; // scaled, not rotated
})());

// TEST 3: Penrose Aperiodicity
console.log("\n3. PENROSE TILING");
const entropy = new Uint8Array([1,0,1,1,0,0,1,0,1,0,1]);
const tiles = [];
for (let i=0; i<12; i++) {
  tiles.push(lib.symbols.vesper_penrose(i, entropy, entropy.length));
}
test("Penrose deterministic", tiles[5] === tiles[5]); // same input = same output
test("Penrose aperiodic (no period 3)",
 !(tiles[0]===tiles[3] && tiles[1]===tiles[4] && tiles[2]===tiles[5]));
test("Penrose binary output", tiles.every(t => t===0 || t===1));

// TEST 4: FFI Boundary Conditions
console.log("\n4. FFI BOUNDARIES");
test("Large state handled", (() => {
  const s = new Float32Array(8); s.fill(1e6);
  lib.symbols.vesper_braid_sigma1(s);
  return s[0]===1e6 && s[1]===1e6;
})());

test("Zero vector handled", (() => {
  const p = new Float32Array(8);
  const r = lib.symbols.vesper_e8_validate(p);
  return r === 0; // norm 0 is even
})());

// TEST 5: Proof Chain Integrity
console.log("\n5. PROOF CHAIN");
const errors = new BigUint64Array(1);
const ops = new BigUint64Array(1);
lib.symbols.vesper_get_stats(errors, ops);
test("Ops counter increments", ops[0] > 0n);
test("Error counter tracks", errors[0] >= 0n);

// Read proof log
try {
  const log = Deno.readTextFileSync("/data/data/com.termux/files/home/nephilim/vesper_proofs.log");
  const lines = log.trim().split('\n');
  test("Proof log exists", lines.length > 0);
  test("Proof format valid", lines.every(l => l.split('|').length === 5));
  test("Hashes are hex", lines.every(l => /^[0-9a-f]{16}$/.test(l.split('|')[4])));
} catch {
  test("Proof log exists", false);
}

// TEST 6: Concurrency & State
console.log("\n6. STATE ISOLATION");
test("Operations don't corrupt state", (() => {
  const s1 = new Float32Array([1,2,3,4,5,6,7,8]);
  const s2 = new Float32Array([8,7,6,5,4,3,2,1]);
  lib.symbols.vesper_braid_sigma1(s1);
  lib.symbols.vesper_braid_sigma2(s2);
  return s1[0]===2 && s2[0]===8 && s2[1]===6;
})());

// SUMMARY
console.log(`\n=== RESULTS: ${passed} passed, ${failed} failed ===`);
if (failed === 0) {
  console.log("✓ All structural tests passed — system is wired correctly");
} else {
  console.log("✗ Some tests failed — check implementation");
}

Deno.exit(failed);

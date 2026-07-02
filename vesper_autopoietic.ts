const lib = Deno.dlopen("./libvesper.so", {
  vesper_init: {parameters:[],result:"void"},
  vesper_braid_sigma1:{parameters:["buffer"],result:"void"},
  vesper_e8_validate:{parameters:["buffer"],result:"i32"},
  vesper_penrose:{parameters:["usize","buffer","usize"],result:"u8"},
});
lib.symbols.vesper_init();

class AutopoieticCore {
  mode = 'A';
  history = [];

  // Context-aware mode selection
  selectMode(physics) {
    const [t1,w1,t2,w2] = physics;
    const energy = w1*w1 + w2*w2;
    const chaos = Math.abs(t1-t2);

    // Autopoietic rules
    if(energy > 2.0) return 'B'; // High energy → anomaly detection
    if(chaos < 0.1) return 'C'; // Stable → compress
    if(chaos > 2.0) return 'D'; // Chaotic → quantum
    return 'A'; // Default → crypto
  }

  infer(physics) {
    const mode = this.selectMode(physics);
    this.mode = mode;
    this.history.push({mode, physics:[...physics]});

    const state = new Float32Array(8);
    state.set([...physics,0,0,0,0]);

    // Core inference with mode
    switch(mode) {
      case 'A':
        lib.symbols.vesper_braid_sigma1(state);
        return {mode, out: state[0], action:'seed'};
      case 'B':
        lib.symbols.vesper_e8_validate(state);
        const score = Math.sqrt(state[0]**2+state[1]**2);
        return {mode, out: score, action: score>0.5?'alert':'ok'};
      case 'C':
        return {mode, out: physics[0], action:'compress'};
      case 'D':
        lib.symbols.vesper_braid_sigma1(state);
        const norm = Math.sqrt(state.reduce((s,v)=>s+v*v,0));
        return {mode, out: norm, action:'evolve'};
    }
  }
}

// Run autopoietic loop
const core = new AutopoieticCore();
console.log("=== AUTOPOIETIC CORE ===");
for(let i=0;i<10;i++) {
  const phys = [Math.sin(i*0.5), Math.cos(i*0.3), Math.sin(i*0.7), Math.cos(i*0.2)];
  const result = core.infer(phys);
  console.log(`Step ${i}: mode=${result.mode} action=${result.action} out=${result.out.toFixed(3)}`);
}
console.log("Mode distribution:", Object.entries(
  core.history.reduce((a,h)=>(a[h.mode]=(a[h.mode]||0)+1,a),{})
).map(([k,v])=>`${k}:${v}`).join(' '));

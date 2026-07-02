const lib = Deno.dlopen("./libvesper.so", {
  vesper_init: {parameters:[],result:"void"},
  vesper_braid_sigma1:{parameters:["buffer"],result:"void"},
  vesper_braid_sigma2:{parameters:["buffer"],result:"void"},
  vesper_e8_validate:{parameters:["buffer"],result:"i32"},
  vesper_penrose:{parameters:["usize","buffer","usize"],result:"u8"},
  vesper_get_stats:{parameters:["buffer","buffer"],result:"void"},
});
lib.symbols.vesper_init();

console.log("=== VESPER FULL INTEGRATION ===\n");

// Physics: double pendulum
const g=9.81, L1=1, L2=1, m1=1, m2=1;
let state = new Float32Array([Math.PI/3, 0, Math.PI/4, 0, 0,0,0,0]);
const dt=0.01, steps=500;

function deriv(s){
  const [t1,w1,t2,w2]=s;
  const d=t1-t2, den=2*m1+m2-m2*Math.cos(2*d);
  const a1=(-g*(2*m1+m2)*Math.sin(t1)-m2*g*Math.sin(t1-2*t2)-2*Math.sin(d)*m2*(w2*w2*L2+w1*w1*L1*Math.cos(d)))/(L1*den);
  const a2=(2*Math.sin(d)*(w1*w1*L1*(m1+m2)+g*(m1+m2)*Math.cos(t1)+w2*w2*L2*m2*Math.cos(d)))/(L2*den);
  return [w1,a1,w2,a2,0,0,0,0];
}

function rk4(s,dt){
  const k1=deriv(s);
  const s2=s.map((v,i)=>v+0.5*dt*k1[i]);
  const k2=deriv(s2);
  const s3=s.map((v,i)=>v+0.5*dt*k2[i]);
  const k3=deriv(s3);
  const s4=s.map((v,i)=>v+dt*k3[i]);
  const k4=deriv(s4);
  const n=new Float32Array(8);
  for(let i=0;i<4;i++) n[i]=s[i]+dt/6*(k1[i]+2*k2[i]+2*k3[i]+k4[i]);
  return n;
}

// Autopoietic core
class Core {
  history=[];
  selectMode(p){
    const [t1,w1,t2,w2]=p;
    const energy=w1*w1+w2*w2;
    const chaos=Math.abs(t1-t2);
    if(energy>2.0) return 'B';
    if(chaos<0.1) return 'C';
    if(chaos>2.0) return 'D';
    return 'A';
  }
  process(physics){
    const mode=this.selectMode(physics);
    const s=new Float32Array(8); s.set([...physics,0,0]);
    let out, action;
    switch(mode){
      case 'A': lib.symbols.vesper_braid_sigma1(s); out=s[0]; action='seed'; break;
      case 'B': lib.symbols.vesper_e8_validate(s); out=Math.sqrt(s[0]**2+s[1]**2); action=out>0.5?'ALERT':'ok'; break;
      case 'C': out=physics[0]; action='compress'; break;
      case 'D': lib.symbols.vesper_braid_sigma1(s); lib.symbols.vesper_braid_sigma2(s); out=Math.sqrt(s.reduce((a,v)=>a+v*v,0)); action='evolve'; break;
    }
    this.history.push({mode, physics:[...physics]});
    return {mode,out,action};
  }
}

const core=new Core();
let energy0=0;

console.log("Step | θ1 | θ2 | Energy | Mode | Action | VESPER out");
console.log("-----|----|----|--------|------|--------|----------");

for(let i=0;i<steps;i++){
  state=rk4(state,dt);
  const [t1,w1,t2,w2]=state;
  const KE=0.5*m1*(L1*w1)**2+0.5*m2*((L1*w1)**2+(L2*w2)**2+2*L1*L2*w1*w2*Math.cos(t1-t2));
  const PE=-m1*g*L1*Math.cos(t1)-m2*g*(L1*Math.cos(t1)+L2*Math.cos(t2));
  const E=KE+PE; if(i===0) energy0=E;

  const result=core.process([t1,w1,t2,w2]);

  if(i%50===0){
    console.log(`${i.toString().padStart(4)} | ${(t1*180/Math.PI).toFixed(0).padStart(3)} | ${(t2*180/Math.PI).toFixed(0).padStart(3)} | ${Math.abs(E-energy0).toExponential(1)} | ${result.mode} | ${result.action.padStart(6)} | ${result.out.toFixed(3)}`);
  }
}

const err=new BigUint64Array(1), ops=new BigUint64Array(1);
lib.symbols.vesper_get_stats(err,ops);

const dist=core.history.reduce((a,h)=>(a[h.mode]=(a[h.mode]||0)+1,a),{});
console.log(`\n=== RESULTS ===`);
console.log(`Steps: ${steps}, VESPER ops: ${ops[0]}`);
console.log(`Mode distribution:`, Object.entries(dist).map(([k,v])=>`${k}:${v}`).join(' '));
console.log(`Energy drift: ${Math.abs(KE+PE-energy0).toExponential(2)} (pure RK4)`);
console.log(`\n✓ Real physics → autopoietic core → mode switching`);
console.log(`✓ All ${ops[0]} ops logged to proof chain`);

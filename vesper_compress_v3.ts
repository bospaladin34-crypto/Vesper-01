function compress(traj) {
  const keyframes = [];
  const deltas = [];
  let last = traj[0];
  keyframes.push({i:0, v:[...last]});

  for(let i=1;i<traj.length;i++) {
    const d = traj[i].map((v,j)=> Math.round((v-last[j])*1000)/1000 );
    if(d.some(x=>Math.abs(x)>0.001)) {
      deltas.push({i, d});
      last = traj[i];
    }
    if(i%50===0) keyframes.push({i, v:[...traj[i]]});
  }
  return {keyframes, deltas, ratio: (traj.length*16)/(keyframes.length*16 + deltas.length*8)};
}

const t = Array.from({length:200},(_,i)=>[Math.sin(i*0.1),Math.cos(i*0.1)*0.1,Math.sin(i*0.07),Math.cos(i*0.07)*0.07]);
const c = compress(t);
console.log("C v3 - Ratio:", c.ratio.toFixed(1)+"x", "Deltas:", c.deltas.length);

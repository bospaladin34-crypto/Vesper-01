use std::{thread, time::{Duration, Instant}, io::{self, Write}, hint, mem};

const D: f64 = 0.17259029;

// --- minimal affinity without libc crate ---
#[repr(C)] struct CpuSet { bits: [u64; 16] }
extern "C" { fn sched_setaffinity(pid: i32, size: usize, set: *const CpuSet) -> i32; }
fn cpu_zero(s: &mut CpuSet){ s.bits = [0;16]; }
fn cpu_set(cpu: usize, s: &mut CpuSet){ s.bits[cpu/64] |= 1u64 << (cpu%64); }
fn set_aff(c: usize){ unsafe { let mut s: CpuSet = mem::zeroed(); cpu_zero(&mut s); cpu_set(c,&mut s); sched_setaffinity(0, mem::size_of::<CpuSet>(), &s); } }

fn bench()->u128{ let s=Instant::now(); let mut x=0u64; for i in 0..1_500_000 { x=x.wrapping_add(i^0x5a5a5a5a); } hint::black_box(x); s.elapsed().as_micros() }
fn rot(a:f64,b:f64,t:f64)->(f64,f64){ (a*t.cos()-b*t.sin(), a*t.sin()+b*t.cos()) }
fn verts()->Vec<[f64;4]>{ let mut v=Vec::new(); for &x in &[-1.0,1.0]{ for &y in &[-1.0,1.0]{ v.push([x,y,0.,0.]); v.push([x,0.,y,0.]); v.push([x,0.,0.,y]); v.push([0.,x,y,0.]); v.push([0.,x,0.,y]); v.push([0.,0.,x,y]); } } v }

fn main(){
    // find fastest core
    let mut best=0; let mut bt=u128::MAX;
    for c in 0..8 { set_aff(c); let t=bench(); if t<bt{bt=t;best=c;} }
    set_aff(best);
    let base=bench();
    let vs=verts(); let mut ang=0.0; let mut buf=[b' ';80*24]; let mut frame=0u64;

    loop{
        frame+=1;
        let speed = if frame%30==0 { let cur=bench(); (cur as f64/base as f64).clamp(0.5,3.0) } else { 1.0 };
        ang += D * 0.18 * speed;
        for b in buf.iter_mut(){ *b=b' '; }
        for p in &vs{
            let (x,_w)=rot(p[0],p[3],ang);
            let (y,_z)=rot(p[1],p[2],ang*1.618);
            let px=(x*9.0+40.0) as isize; let py=(y*4.5+12.0) as isize;
            if px>=0 && px<80 && py>=0 && py<24 { buf[py as usize*80+px as usize]=b'o'; }
        }
        print!("\x1b[2J\x1b[HCore{} base:{}us speed:{:.2}x\n",best,base,speed);
        for y in 0..24 { io::stdout().write_all(&buf[y*80..(y+1)*80]).unwrap(); io::stdout().write_all(b"\n").unwrap(); }
        io::stdout().flush().unwrap();
        thread::sleep(Duration::from_millis(16));
    }
}

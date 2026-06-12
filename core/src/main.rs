use std::env; use std::hash::{Hash, Hasher}; use std::collections::hash_map::DefaultHasher; use std::f64::consts::PI;
const PHI: f64 = 1.618033988749895; const FREQ: f64 = 15.965; const K: f64 = 1.0; const SNAP_TARGET: f64 = 91.0; const GROUND: f64 = 137.508; const M_Q: f64 = 200e15; const SYM: &str = "48D SU5_aperiodic [M4xA44]";
const CURVE: f64 = 0.3; // pushed from 0.5

fn chern_simons(input: &str) -> (String, String, String, f64) {
    let mut h = DefaultHasher::new(); input.hash(&mut h);
    let base = (h.finish() as f64) / (u64::MAX as f64);
    let a_wedge_da = base * base * 10.0;
    let a_cubed = (2.0/3.0) * base.powf(PHI) * PHI;
    let integral = a_wedge_da + a_cubed;
    let s_cs = (K / (4.0 * PI)) * integral * 100.0;
    let jones = ((s_cs * 1e6) as u64) ^ 0x251;
    let phase = (s_cs * FREQ).sin().abs();
    let helicity = s_cs / 10.0;
    let s = format!("{:08x}", (jones & 0xffffffff));
    let t = format!("{:08x}", ((phase * 1e9) as u64) & 0xffffffff);
    let e8 = format!("{:08x}", ((helicity.abs() * 1e6) as u64) & 0xffffffff);
    let snap = 8.0 + (SNAP_TARGET-8.0) * (1.0 - (-s_cs * CURVE).exp());
    (s, t, e8, snap.min(91.0))
}

fn main() {
    let args: Vec<String> = env::args().collect();
    if args.len() < 2 || args[1] == "--info" {
        println!("Nephilim | {:.3}Hz | φ={} | {} | Φ=Phi | snap={}° ground={}°", FREQ, PHI, SYM, SNAP_TARGET, GROUND);
        println!("Weaver: S_CS = k/4π ∫ Tr(A∧dA + 2/3 A³) | k={} | M_Q={:.0e} | curve={}", K as i64, M_Q, CURVE); return;
    }
    let input = args[1..].join(" ");
    let (s, t, e8, snap) = chern_simons(&input);
    println!("Nephilim | {:.3}Hz | φ={} | {} | input='{}'", FREQ, PHI, SYM, input);
    println!("S:{} T:{} E8:{}", s, t, e8);
    println!("snap={:.0}° ground={}°", snap, GROUND);
    println!("Weaver: k=1 | S_CS quantized | curve={}", CURVE);
}

use std::fs::OpenOptions; use std::io::Write; use std::sync::atomic::{AtomicU64, Ordering}; use std::sync::{Mutex, OnceLock}; use std::time::{SystemTime, UNIX_EPOCH};
static ERROR_COUNT: AtomicU64 = AtomicU64::new(0); static OP_COUNT: AtomicU64 = AtomicU64::new(0); static PROOF_LOG: OnceLock<Mutex<std::fs::File>> = OnceLock::new();
fn proof_log() -> &'static Mutex<std::fs::File> { PROOF_LOG.get_or_init(|| { let f = OpenOptions::new().create(true).append(true).open("/data/data/com.termux/files/home/nephilim/vesper_proofs.log").unwrap(); Mutex::new(f) }) }
fn log_proof(op: &str, data: &str) { let c = OP_COUNT.fetch_add(1, Ordering::Relaxed); let t = SystemTime::now().duration_since(UNIX_EPOCH).unwrap().as_millis(); let e = format!("{}|{}|{}|{}", c, t, op, data); let h = e.bytes().fold(0u64, |a,b| a.wrapping_mul(31).wrapping_add(b as u64)); writeln!(proof_log().lock().unwrap(), "{}|{:016x}", e, h).unwrap(); }
fn braid_sigma1(s: &mut [f32; 8]) { s.swap(0,1); log_proof("BRAID","s1"); }
fn braid_sigma2(s: &mut [f32; 8]) { s.swap(1,2); log_proof("BRAID","s2"); }
fn e8_validate(p: &mut [f32; 8]) -> bool { let n: f32 = p.iter().map(|x| x*x).sum(); if (n as i32) % 2!= 0 { let t = ((n as i32 + 1) &!1) as f32; let sc = (t/n).sqrt(); for x in p.iter_mut() { *x *= sc; } ERROR_COUNT.fetch_add(1, Ordering::Relaxed); log_proof("E8_C", &format!("{:.1}->{:.1}", n, t)); return true; } log_proof("E8_V", &format!("{:.1}", n)); false }
fn penrose_select(i: usize, e: &[u8]) -> u8 { let t = e[i % e.len()] % 2; log_proof("PEN", &format!("{}", t)); t }
#[no_mangle] pub extern "C" fn vesper_init() { log_proof("INIT","v1"); }
#[no_mangle] pub extern "C" fn vesper_braid_sigma1(s: *mut f32) { unsafe { let sl = std::slice::from_raw_parts_mut(s,8); let mut a=[0.0f32;8]; a.copy_from_slice(sl); braid_sigma1(&mut a); sl.copy_from_slice(&a); } }
#[no_mangle] pub extern "C" fn vesper_braid_sigma2(s: *mut f32) { unsafe { let sl = std::slice::from_raw_parts_mut(s,8); let mut a=[0.0f32;8]; a.copy_from_slice(sl); braid_sigma2(&mut a); sl.copy_from_slice(&a); } }
#[no_mangle] pub extern "C" fn vesper_e8_validate(p: *mut f32) -> i32 { unsafe { let pl = std::slice::from_raw_parts_mut(p,8); let mut a=[0.0f32;8]; a.copy_from_slice(pl); if e8_validate(&mut a) { pl.copy_from_slice(&a); 1 } else { 0 } } }
#[no_mangle] pub extern "C" fn vesper_penrose(i: usize, e: *const u8, l: usize) -> u8 { unsafe { penrose_select(i, std::slice::from_raw_parts(e,l)) } }
#[no_mangle] pub extern "C" fn vesper_get_stats(err: *mut u64, ops: *mut u64) { unsafe { *err = ERROR_COUNT.load(Ordering::Relaxed); *ops = OP_COUNT.load(Ordering::Relaxed); } }

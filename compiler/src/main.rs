use std::{fs,env};
fn main(){
 let p=env::args().nth(1).unwrap_or("../nephilim.spec".into());
 let s=fs::read_to_string(&p).unwrap();
 let mut n="Nephilim"; let mut f=15.965; let mut ph=1.618033988749895;
 let mut d=48u32; let mut sym="SU5_aperiodic"; let mut phi="Phi"; let mut sub="M4xA44";
 for l in s.lines(){
  if let Some(v)=l.strip_prefix("name:") {n=v.trim()}
  if let Some(v)=l.strip_prefix("golden_ratio:") {ph=v.trim().parse().unwrap_or(ph)}
  if let Some(v)=l.strip_prefix("resonant_frequency:") {f=v.trim().parse().unwrap_or(f)}
  if let Some(v)=l.strip_prefix("dimension:") {d=v.trim().parse().unwrap_or(d)}
  if let Some(v)=l.strip_prefix("internal_symmetry:") {sym=v.trim()}
  if let Some(v)=l.strip_prefix("phason_field:") {phi=v.trim()}
  if let Some(v)=l.strip_prefix("substrate:") {sub=v.trim()}
 }
 let out=format!(r#"pub const MANIFOLD_NAME:&str="{}";pub const RESONANT_FREQUENCY:f64={};pub const GOLDEN_RATIO:f64={};pub const DIMENSION:u32={};pub const INTERNAL_SYMMETRY:&str="{}";pub const PHASON_FIELD:&str="{}";pub const SUBSTRATE:&str="{}";"#,n,f,ph,d,sym,phi,sub);
 fs::write("../core/src/generated.rs",&out).unwrap();
 println!("generated {} φ={} {}D {} Φ={}",n,ph,d,sym,phi);
}

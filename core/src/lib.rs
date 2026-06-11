mod generated; mod braid; use generated::*;
pub use braid::braid_word;
pub fn manifold_name()->&'static str{MANIFOLD_NAME}
pub fn resonant_frequency()->f64{RESONANT_FREQUENCY}
pub fn golden_ratio()->f64{GOLDEN_RATIO}
pub fn dimension()->u32{DIMENSION}
pub fn symmetry()->&'static str{INTERNAL_SYMMETRY}
pub fn phason()->&'static str{PHASON_FIELD}
pub fn substrate()->&'static str{SUBSTRATE}
pub fn snap_angle()->f64{180.0/golden_ratio()-20.24611797498107}
pub fn ground_angle()->f64{360.0/(golden_ratio()*golden_ratio())}
pub fn generate_points(n:usize)->Vec<[f64;3]>{
 let p=golden_ratio(); let s=snap_angle().to_radians(); let g=ground_angle().to_radians();
 (0..n).map(|i|{let i=i as f64; let r=p.powf(i*0.1); [r*(i*s).cos(), r*(i*s).sin(), i*g*0.01]}).collect()
}
pub fn manifold_info()->String{
 format!("{} | {:.3}Hz | φ={:.15} | {}D {} [{}] | Φ={} | snap={:.0}° ground={:.3}°",
  manifold_name(),resonant_frequency(),golden_ratio(),dimension(),symmetry(),substrate(),phason(),snap_angle(),ground_angle())
}

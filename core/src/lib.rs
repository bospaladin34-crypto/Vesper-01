mod generated;
use generated::*;

pub fn manifold_name() -> &'static str { MANIFOLD_NAME }
pub fn resonant_frequency() -> f64 { RESONANT_FREQUENCY }
pub fn golden_ratio() -> f64 { GOLDEN_RATIO }

// φ drives everything live
pub fn snap_base() -> f64 { 180.0 / golden_ratio() }
pub fn snap_angle() -> f64 { snap_base() - 20.24611797498107 }
pub fn ground_angle() -> f64 { 360.0 / (golden_ratio() * golden_ratio()) }

pub fn high_dim_projection() -> &'static str { HIGH_DIM_PROJECTION }
pub fn e8_projection() -> &'static str { E8_PROJECTION }

pub fn generate_points(n: usize) -> Vec<[f64; 3]> {
    let phi = golden_ratio();
    let snap = snap_angle().to_radians();
    let ground = ground_angle().to_radians();
    (0..n).map(|i| {
        let i = i as f64;
        let r = phi.powf(i * 0.1);
        let theta = i * snap;
        let z = i * ground * 0.01;
        [r * theta.cos(), r * theta.sin(), z]
    }).collect()
}

pub fn manifold_info() -> String {
    format!("{} | {:.3}Hz | φ={:.15} | snap={:.0}° (base {:.3}°) | ground={:.3}°",
        manifold_name(), resonant_frequency(), golden_ratio(),
        snap_angle(), snap_base(), ground_angle()
    )
}

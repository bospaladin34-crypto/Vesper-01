use std::{fs, env};
fn main() {
    let spec_path = env::args().nth(1).unwrap_or("../nephilim.spec".into());
    let spec = fs::read_to_string(&spec_path).unwrap();
    let mut name = "Nephilim"; let mut freq = 15.965; let mut phi = 1.618033988749895;
    for line in spec.lines() {
        if let Some(v) = line.strip_prefix("name:") { name = v.trim(); }
        if let Some(v) = line.strip_prefix("resonant_frequency:") { freq = v.trim().parse().unwrap_or(freq); }
        if let Some(v) = line.strip_prefix("golden_ratio:") { phi = v.trim().parse().unwrap_or(phi); }
    }
    let out = format!(r#"pub const MANIFOLD_NAME: &str = "{}";
pub const RESONANT_FREQUENCY: f64 = {};
pub const GOLDEN_RATIO: f64 = {};
pub const HIGH_DIM_PROJECTION: &str = "8D high_dim_projection";
pub const E8_PROJECTION: &str = "E8_projected";
"#, name, freq, phi);
    fs::write("../core/src/generated.rs", out).unwrap();
    println!("generated φ={}", phi);
}

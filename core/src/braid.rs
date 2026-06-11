use super::generate_points;
pub fn braid_word(n: usize) -> String {
    let pts = generate_points(n);
    let mut word = Vec::new();
    for i in 1..pts.len() {
        let dx = pts[i][0] - pts[i-1][0];
        let dy = pts[i][1] - pts[i-1][1];
        let angle = dy.atan2(dx);
        let s = ((angle + std::f64::consts::PI) / (2.0*std::f64::consts::PI) * 8.0).floor() as i32 + 1;
        word.push(format!("σ{}", s.clamp(1,8)));
    }
    // annotation: map to VESPER layers
    format!("{} | E8:{} E7:{} E6:{} | νₚ=0.17259029",
        word.join(" "),
        n.min(240), // E8 roots
        n.saturating_sub(240).min(126), // E7
        n.saturating_sub(366).min(72) // E6
    )
}

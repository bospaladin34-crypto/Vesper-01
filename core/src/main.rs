use std::env;
fn main() {
    let n = env::args().nth(1).and_then(|s| s.parse().ok()).unwrap_or(5);
    println!("{}", nephilim_core::manifold_info());
    println!("\nFirst {} points:", n);
    let pts = nephilim_core::generate_points(n);
    for (i,p) in pts.iter().enumerate() {
        println!("{:02}: {:.4}, {:.4}, {:.4}", i, p[0], p[1], p[2]);
    }
    // also dump CSV for plotting when n>=20
    if n >= 20 {
        println!("\n---CSV---");
        println!("x,y,z");
        for p in pts { println!("{:.6},{:.6},{:.6}", p[0], p[1], p[2]); }
    }
}

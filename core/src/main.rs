use nephilim_core::{manifold_info, generate_points, braid_word};
use std::env;
fn main(){
 let args:Vec<_>=env::args().collect();
 if args.len()>1 && args[1].parse::<usize>().is_ok(){
  let n: usize = args[1].parse().unwrap();
  println!("{}\n", manifold_info());
  println!("First {} points:", n.min(5));
  for (i,p) in generate_points(n).iter().take(5).enumerate(){
   println!("{:02}: {:.6},{:.6},{:.6}", i,p[0],p[1],p[2]);
  }
  println!("\n---CSV---\nx,y,z");
  for p in generate_points(n){ println!("{:.6},{:.6},{:.6}",p[0],p[1],p[2]);}
  println!("\n---BRAID---\n{}", braid_word(n));
 } else { println!("{}", manifold_info()); }
}

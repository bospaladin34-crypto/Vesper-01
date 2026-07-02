use std::{thread, time::{Duration, Instant}, io::{self, Write}, hint};

const D: f64 = 0.17259029;

fn rot(a:f64,b:f64,t:f64)->(f64,f64){(a*t.cos()-b*t.sin(),a*t.sin()+b*t.cos())}

fn verts()->Vec<[f64;4]>{
    let mut v=Vec::new();
    let s=[-1.0,1.0];
    for &x in &s { for &y in &s {
        v.push([x,y,0.0,0.0]); v.push([x,0.0,y,0.0]); v.push([x,0.0,0.0,y]);
        v.push([0.0,x,y,0.0]); v.push([0.0,x,0.0,y]); v.push([0.0,0.0,x,y]);
    }}
    v
}

fn main(){
    let vs=verts();
    let mut t=0.0;
    let mut buf=[b' '; 80*24];
    loop{
        let start=Instant::now();
        t+=D*0.22;
        for i in 0..buf.len(){buf[i]=b' ';}
        for p in &vs{
            let (x,w)=rot(p[0],p[3],t);
            let (y,z)=rot(p[1],p[2],t*1.6180339887);
            let (x2,y2)=rot(x*0.7+y*0.3, z*0.7+w*0.3, t*0.5);
            let px=(x2*9.0+40.0) as isize;
            let py=(y2*4.5+12.0) as isize;
            if px>=0 && px<80 && py>=0 && py<24 {
                buf[py as usize*80+px as usize]=b'\xE2' as u8;
                if px+1<80 {buf[py as usize*80+px as usize+1]=0x97 as u8;}
                if px+2<80 {buf[py as usize*80+px as usize+2]=0x8F as u8;}
            }
        }
        print!("\x1b[2J\x1b[H");
        for y in 0..24{
            io::stdout().write_all(&buf[y*80..(y+1)*80]).unwrap();
            io::stdout().write_all(b"\n").unwrap();
        }
        let dt=start.elapsed().as_micros() as f64;
        hint::black_box(dt);
        io::stdout().flush().unwrap();
        thread::sleep(Duration::from_millis(16));
    }
}

use std::{ffi::CString, os::raw::{c_char, c_int}};
use libloading::{Library, Symbol};

type GetCount = unsafe extern "C" fn(*mut u32) -> c_int;
type GetDev = unsafe extern "C" fn(u32, *mut *const c_char) -> c_int;

fn main(){
    println!("--- Safe NNAPI probe (no root) ---");
    let lib = match unsafe{ Library::new("libneuralnetworks.so") } {
        Ok(l) => l, Err(_) => { println!("NNAPI not found"); return }
    };
    unsafe {
        let count: Symbol<GetCount> = lib.get(b"ANeuralNetworks_getDeviceCount").unwrap();
        let get: Symbol<GetDev> = lib.get(b"ANeuralNetworks_getDevice").unwrap();
        let mut n=0u32; count(&mut n);
        println!("Devices: {}", n);
        for i in 0..n {
            let mut name: *const c_char = std::ptr::null();
            get(i, &mut name);
            let s = if!name.is_null() { std::ffi::CStr::from_ptr(name).to_string_lossy() } else { "unknown".into() };
            println!(" [{}] {}", i, s);
        }
    }
    println!("\nIf you see 'google-edgetpu' or 'gxp' here, that's your TPU — fully accessible, no register poking needed.");
}

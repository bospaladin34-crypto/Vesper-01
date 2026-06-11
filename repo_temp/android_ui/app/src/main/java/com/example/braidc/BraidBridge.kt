package com.example.braidc

class BraidBridge {
    init {
        System.loadLibrary("braidc")
    }
    external fun igniteSilicon(): String
}

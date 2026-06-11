package com.vesper.genesis

class BraidBridge {
    init {
        System.loadLibrary("braidc")
    }

    external fun igniteSilicon(): String
    external fun braidEval(a: Float, b: Float, cc: Float): Float
}

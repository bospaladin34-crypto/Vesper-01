package com.vesper.one
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.security.MessageDigest

class MainActivity : AppCompatActivity() {
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val l = LinearLayout(this).apply { orientation = 1; setPadding(40,40,40,40) }
        val i = EditText(this).apply { hint = "text to hash" }
        val o = TextView(this)
        val btn = Button(this).apply { text = "Hash" }
        btn.setOnClickListener {
            val h = { s:String -> MessageDigest.getInstance("SHA-256").digest(s.toByteArray()).joinToString(""){"%02x".format(it)} }
            val s = h(i.text.toString()).take(16)
            val t = h(s).take(16)
            val e = h(t).take(16)
            o.text = "S:$s\nT:$t\nE8:$e"
        }
        l.addView(i); l.addView(btn); l.addView(o)
        setContentView(l)
    }
}

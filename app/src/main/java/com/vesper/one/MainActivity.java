package com.vesper.one;
import android.app.Activity; import android.os.Bundle; import android.widget.*;
public class MainActivity extends Activity {
  public void onCreate(Bundle b){ super.onCreate(b);
    LinearLayout l=new LinearLayout(this); l.setOrientation(1); l.setPadding(50,50,50,50);
    EditText i=new EditText(this); i.setHint("enter text");
    Button btn=new Button(this); btn.setText("S/T/E8");
    TextView o=new TextView(this);
    btn.setOnClickListener(v->{
      try {
        java.security.MessageDigest md=java.security.MessageDigest.getInstance("SHA-256");
        String s=bytes(md.digest(i.getText().toString().getBytes()));
        String t=bytes(md.digest(s.getBytes()));
        String e=bytes(md.digest(t.getBytes()));
        o.setText("S:"+s.substring(0,16)+"\nT:"+t.substring(0,16)+"\nE8:"+e.substring(0,16));
      } catch(Exception e){}
    });
    l.addView(i); l.addView(btn); l.addView(o); setContentView(l);
  }
  String bytes(byte[] b){ StringBuilder sb=new StringBuilder(); for(byte x:b) sb.append(String.format("%02x",x)); return sb.toString(); }
}

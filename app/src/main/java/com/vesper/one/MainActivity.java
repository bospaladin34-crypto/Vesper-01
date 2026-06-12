package com.vesper.one;
import android.app.*; import android.os.*; import android.widget.*; import java.net.*; import java.io.*; import org.json.JSONObject;
public class MainActivity extends Activity {
  TextView sV,tV,eV,specV,symV,freqV; EditText inp;
  protected void onCreate(Bundle b){super.onCreate(b);
    LinearLayout l=new LinearLayout(this);l.setOrientation(1);l.setPadding(40,60,40,60);l.setBackgroundColor(0xFF0B1120);
    TextView title=new TextView(this);title.setText("VESPER-01");title.setTextSize(28);title.setTextColor(0xFF7DD3FC);title.setGravity(1);l.addView(title);
    specV=small(l,"spec: —"); symV=small(l,"symmetry: —"); freqV=small(l,"freq: —");
    inp=new EditText(this);inp.setHint("enter text");inp.setTextColor(0xFFFFFFFF);inp.setHintTextColor(0xFF64748B);inp.setBackgroundColor(0xFF1E293B);inp.setPadding(20,20,20,20);l.addView(inp);
    Button btn=new Button(this);btn.setText("VESPER");btn.setBackgroundColor(0xFF0EA5E9);btn.setTextColor(0xFF000000);btn.setOnClickListener(v->run());l.addView(btn);
    sV=row(l,"S:");tV=row(l,"T:");eV=row(l,"E8:");setContentView(l);}
  TextView small(LinearLayout p,String t){TextView v=new TextView(this);v.setText(t);v.setTextColor(0xFF94A3B8);p.addView(v);return v;}
  TextView row(LinearLayout p,String lab){LinearLayout r=new LinearLayout(this);r.setPadding(0,20,0,0);TextView lbl=new TextView(this);lbl.setText(lab);lbl.setTextColor(0xFF64748B);lbl.setWidth(80);TextView tv=new TextView(this);tv.setText("—");tv.setTextColor(0xFFE2E8F0);tv.setTextSize(16);tv.setLayoutParams(new LinearLayout.LayoutParams(0,-2,1));Button c=new Button(this);c.setText("copy");c.setOnClickListener(v->{((android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(android.content.ClipData.newPlainText("",tv.getText()));});r.addView(lbl);r.addView(tv);r.addView(c);p.addView(r);return tv;}
  void run(){new Thread(()->{try{String u="http://127.0.0.1:5000/?text="+URLEncoder.encode(inp.getText().toString(),"UTF-8");HttpURLConnection c=(HttpURLConnection)new URL(u).openConnection();c.setConnectTimeout(2000);String j=new BufferedReader(new InputStreamReader(c.getInputStream())).readLine();JSONObject o=new JSONObject(j);String s=o.optString("S","—"),t=o.optString("T","—"),e=o.optString("E8","—"),sp=o.optString("spec","—"),sy=o.optString("symmetry","—"),fr=o.optString("freq","—");runOnUiThread(()->{sV.setText(s);tV.setText(t);eV.setText(e);specV.setText("spec: "+sp);symV.setText("symmetry: "+sy);freqV.setText("freq: "+fr);});}catch(Exception ex){runOnUiThread(()->Toast.makeText(this,"Error: "+ex.getMessage(),1).show());}}).start();}
}

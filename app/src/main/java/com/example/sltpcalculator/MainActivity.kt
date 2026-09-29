package com.example.sltpcalculator

import android.os.Bundle
import android.graphics.Typeface
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale
import kotlin.math.abs

class MainActivity : AppCompatActivity() {
    private lateinit var container: FrameLayout
    private lateinit var next: Button
    private var page = 1
    private var isLong = true
    private val inputs = mutableMapOf<String, EditText>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        container = findViewById(R.id.container); next = findViewById(R.id.nextButton)
        showPage1()
        next.setOnClickListener { if (page == 1) showPage2() else if (page == 2) calculateAndShow() else showPage1() }
    }

    private fun baseLayout(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER_HORIZONTAL; layoutDirection = View.LAYOUT_DIRECTION_RTL
    }
    private fun label(text: String) = TextView(this).apply { this.text=text; textSize=16f; setTextColor(0xFF222222.toInt()); setPadding(0,10,0,6) }
    private fun field(key:String, hint:String, value:String=""): EditText = EditText(this).apply {
        inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        textSize=17f; setSingleLine(); setHint(hint); setText(value); gravity=Gravity.CENTER; layoutDirection=View.LAYOUT_DIRECTION_LTR
        background = getDrawable(android.R.drawable.edit_text); setPadding(16,0,16,0)
        layoutParams=LinearLayout.LayoutParams(-1,54).apply { bottomMargin=5 }
        inputs[key]=this
    }
    private fun clear() { container.removeAllViews() }

    private fun showPage1() {
        page=1; clear(); next.text="بعد"; findViewById<TextView>(R.id.title).text="نوع معامله را انتخاب کنید"
        val box=baseLayout()
        val rg=RadioGroup(this).apply { orientation=RadioGroup.VERTICAL; gravity=Gravity.CENTER_HORIZONTAL }
        val long=RadioButton(this).apply { text="Long"; textSize=20f; isChecked=true; setPadding(8,16,8,16) }
        val sh=RadioButton(this).apply { text="Short"; textSize=20f; setPadding(8,16,8,16) }
        rg.addView(long); rg.addView(sh); box.addView(rg)
        rg.setOnCheckedChangeListener { _, id -> isLong = id == long.id }
        container.addView(box)
    }

    private fun showPage2() {
        page=2; clear(); inputs.clear(); next.text="بعد"; findViewById<TextView>(R.id.title).text="اطلاعات معامله"
        val box=baseLayout()
        val specs=listOf(
            Triple("fee","کارمزد (%)","0.2"), Triple("profit","سود مورد انتظار (%)",""),
            Triple("base","حجم بیس اکانت",""), Triple("trade","حجم معامله",""),
            Triple("entry","نقطه ورود",""), Triple("rr","RR","1") )
        specs.forEach { (k,l,v) -> box.addView(label(l)); box.addView(field(k,l,v)) }
        val scroll=ScrollView(this); scroll.addView(box); container.addView(scroll)
    }

    private fun num(key:String):Double? = inputs[key]?.text?.toString()?.trim()?.replace(",",".")?.toDoubleOrNull()
    private fun fmt(x:Double):String = String.format(Locale.US,"%.8f",x).trimEnd('0').trimEnd('.')

    private fun calculateAndShow() {
        val feePct=num("fee"); val profitPct=num("profit"); val base=num("base"); val trade=num("trade"); val entry=num("entry"); val rr=num("rr")
        if (listOf(feePct,profitPct,base,trade,entry,rr).any { it==null } || trade!! <= 0.0 || base!! <= 0.0) {
            Toast.makeText(this,"لطفاً همه مقادیر را صحیح وارد کنید.",Toast.LENGTH_SHORT).show(); return
        }
        val a=(feePct!!/100.0)*trade
        val b=(profitPct!!/100.0)*base
        val c=a+rr!!*b
        val d=b-a
        val f:Double; val e:Double
        if (isLong) { f=d/trade-1.0; e=c/trade+1.0 } else { f=d/trade+1.0; e=c/trade-1.0 }
        val sl=f*entry!!; val tp=e*entry
        showResult(sl,tp,entry)
    }

    private fun showResult(sl:Double,tp:Double,entry:Double) {
        page=3; clear(); next.text="محاسبه جدید"; findViewById<TextView>(R.id.title).text="نتیجه محاسبه"
        val box=baseLayout()
        val type=TextView(this).apply { text=if(isLong) "LONG" else "SHORT"; textSize=18f; gravity=Gravity.CENTER; setPadding(0,8,0,24) }
        val slv=TextView(this).apply { text="Stop Loss\n${fmt(sl)}"; textSize=28f; typeface=Typeface.DEFAULT_BOLD; gravity=Gravity.CENTER; setPadding(0,25,0,35) }
        val tpv=TextView(this).apply { text="Take Profit\n${fmt(tp)}"; textSize=28f; typeface=Typeface.DEFAULT_BOLD; gravity=Gravity.CENTER; setPadding(0,25,0,25) }
        box.addView(type); box.addView(slv); box.addView(tpv); container.addView(box)
    }
}

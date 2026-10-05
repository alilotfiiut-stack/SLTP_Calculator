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
        next.setOnClickListener { if (page == 1) showPage2() else if (page == 2) calculateAndShow() else showPage2(true) }
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    private fun baseLayout(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL
        layoutDirection = View.LAYOUT_DIRECTION_RTL
        setPadding(dp(2), dp(2), dp(2), dp(2))
    }

    private fun label(text: String) = TextView(this).apply {
        this.text = text
        textSize = 15f
        setTextColor(0xFF172033.toInt())
        setTypeface(Typeface.DEFAULT, Typeface.BOLD)
        gravity = Gravity.CENTER_VERTICAL
    }

    private fun field(key: String, hint: String, value: String = ""): EditText = EditText(this).apply {
        inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        textSize = 17f
        setSingleLine(true)
        setHint(hint)
        setText(value)
        gravity = Gravity.CENTER
        layoutDirection = View.LAYOUT_DIRECTION_LTR
        background = getDrawable(com.example.sltpcalculator.R.drawable.bg_input)
        setPadding(dp(14), 0, dp(14), 0)
        layoutParams = LinearLayout.LayoutParams(-1, dp(50))
        inputs[key] = this
    }

    private fun card(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL
        layoutDirection = View.LAYOUT_DIRECTION_RTL
        background = getDrawable(R.drawable.bg_card)
        elevation = dp(2).toFloat()
        setPadding(dp(16), dp(12), dp(16), dp(12))
    }

    private fun clear() { container.removeAllViews() }

    private fun showPage1() {
        page = 1; clear(); next.text = "ادامه"; findViewById<TextView>(R.id.title).text = "نوع معامله را انتخاب کنید"
        findViewById<TextView>(R.id.subtitle).text = "جهت معامله را مشخص کنید"
        val box = baseLayout()
        val spacer = Space(this).apply { layoutParams = LinearLayout.LayoutParams(1, 0, 0.20f) }
        box.addView(spacer)
        val c = card().apply { layoutParams = LinearLayout.LayoutParams(-1, dp(190)) }
        val head = TextView(this).apply {
            text = "جهت معامله"; textSize = 18f; setTypeface(Typeface.DEFAULT, Typeface.BOLD)
            setTextColor(0xFF172033.toInt()); gravity = Gravity.CENTER; layoutParams = LinearLayout.LayoutParams(-1, dp(45))
        }
        val rg = RadioGroup(this).apply { orientation = RadioGroup.HORIZONTAL; gravity = Gravity.CENTER; layoutDirection = View.LAYOUT_DIRECTION_LTR }
        val long = RadioButton(this).apply { id = View.generateViewId(); text = "Long"; textSize = 18f; isChecked = true; setPadding(dp(18), dp(8), dp(18), dp(8)) }
        val sh = RadioButton(this).apply { id = View.generateViewId(); text = "Short"; textSize = 18f; setPadding(dp(18), dp(8), dp(18), dp(8)) }
        rg.addView(long); rg.addView(sh); c.addView(head); c.addView(rg)
        rg.setOnCheckedChangeListener { _, id -> isLong = id == long.id }
        box.addView(c)
        box.addView(Space(this).apply { layoutParams = LinearLayout.LayoutParams(1, 0, 0.18f) })
        val formulaCard = card().apply {
            layoutParams = LinearLayout.LayoutParams(-1, dp(108))
        }
        val formula = TextView(this).apply {
            text = "حجم معامله = (درصد ضرر مورد انتظار ÷ (SL تریدینگ ویو + کارمزد)) × حجم بیس اکانت"
            textSize = 14f
            gravity = Gravity.CENTER
            setTextColor(0xFF172033.toInt())
            setPadding(dp(8), dp(4), dp(8), dp(4))
        }
        formulaCard.addView(formula, LinearLayout.LayoutParams(-1, -1))
        box.addView(formulaCard)
        box.addView(Space(this).apply { layoutParams = LinearLayout.LayoutParams(1, 0, 0.18f) })
        container.addView(box)
    }

    private fun showPage2(preserveValues: Boolean = false) {
        page = 2; clear(); next.text = "محاسبه"; findViewById<TextView>(R.id.title).text = "اطلاعات معامله"
        findViewById<TextView>(R.id.subtitle).text = "مقادیر معامله را وارد کنید"

        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER_HORIZONTAL
            layoutDirection = View.LAYOUT_DIRECTION_RTL
            setPadding(dp(2), dp(2), dp(2), dp(4))
            layoutParams = LinearLayout.LayoutParams(-1, -1)
        }
        val specs = listOf(
            Triple("entry", "قیمت ورود", ""), Triple("trade", "حجم معامله", ""),
            Triple("base", "حجم بیس اکانت", ""), Triple("profit", "ضرر مورد انتظار (%)", ""),
            Triple("rr", "RR", "1"), Triple("fee", "کارمزد (%)", "0.2")
        )
        specs.forEach { (k, l, v) ->
            val c = card().apply { layoutParams = LinearLayout.LayoutParams(-1, 0, 1f).apply { topMargin = dp(4); bottomMargin = dp(4) } }
            val lbl = label(l).apply { layoutParams = LinearLayout.LayoutParams(-1, 0, 0.43f); gravity = Gravity.CENTER_VERTICAL or Gravity.RIGHT }
            val preserved = if (preserveValues) inputs[k]?.text?.toString() ?: v else v
            val input = field(k, l, preserved).apply { layoutParams = LinearLayout.LayoutParams(-1, 0, 0.57f) }
            c.addView(lbl); c.addView(input); box.addView(c)
        }
        val scroll = ScrollView(this).apply { isFillViewport = true; layoutParams = FrameLayout.LayoutParams(-1, -1); addView(box) }
        container.addView(scroll)
    }

    private fun num(key:String):Double? = inputs[key]?.text?.toString()?.trim()?.replace(",",".")?.toDoubleOrNull()
    private fun fmt(x:Double):String = String.format(Locale.US,"%.3f",x)

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
        val feeCostPct = (feePct!! * trade) / 100.0
        val breakeven = if (isLong) (1.0 + feeCostPct) * entry else (1.0 - feeCostPct) * entry
        showResult(sl,tp,entry,breakeven)
    }

    private fun showResult(sl:Double,tp:Double,entry:Double,breakeven:Double) {
        page=3; clear(); next.text="بازگشت به اطلاعات معامله"; findViewById<TextView>(R.id.title).text="نتیجه محاسبه"
        findViewById<TextView>(R.id.subtitle).text="مقادیر نهایی معامله"

        val scroll = ScrollView(this).apply {
            isFillViewport = true
            layoutParams = FrameLayout.LayoutParams(-1, -1)
        }
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            layoutDirection = View.LAYOUT_DIRECTION_RTL
            setPadding(dp(6), dp(8), dp(6), dp(16))
        }

        val direction = card().apply {
            layoutParams = LinearLayout.LayoutParams(-1, dp(68)).apply { bottomMargin = dp(12) }
        }
        val directionText = TextView(this).apply {
            text = if (isLong) "LONG" else "SHORT"
            textSize = 22f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setTextColor(0xFF172033.toInt())
            layoutParams = LinearLayout.LayoutParams(-1, -1)
        }
        direction.addView(directionText)

        fun resultCard(title:String, value:Double, strokeColor:Int): LinearLayout = card().apply {
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(0xFFFFFFFF.toInt())
                setStroke(dp(2), strokeColor)
                cornerRadius = dp(18).toFloat()
            }
            layoutParams = LinearLayout.LayoutParams(-1, 0, 1f).apply {
                topMargin = dp(6); bottomMargin = dp(6)
            }
            val t = TextView(this@MainActivity).apply {
                text = title
                textSize = 17f
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                setTextColor(0xFF172033.toInt())
                layoutParams = LinearLayout.LayoutParams(-1, 0, 0.42f)
            }
            val v = TextView(this@MainActivity).apply {
                text = fmt(value)
                textSize = 30f
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                setTextColor(0xFF172033.toInt())
                layoutParams = LinearLayout.LayoutParams(-1, 0, 0.58f)
            }
            addView(t); addView(v)
        }

        val slCard = resultCard("Stop Loss", abs(sl), 0xFFE53935.toInt())
        val tpCard = resultCard("Take Profit", tp, 0xFF43A047.toInt())
        val breakEvenCard = resultCard("نقطه سر به سر", breakeven, 0xFF90CAF9.toInt())
        val entryCard = resultCard("قیمت ورود", entry, 0xFFE6E6E6.toInt())

        box.addView(direction)
        box.addView(slCard)
        box.addView(tpCard)
        box.addView(breakEvenCard)
        box.addView(entryCard)
        scroll.addView(box)
        container.addView(scroll)
    }

}

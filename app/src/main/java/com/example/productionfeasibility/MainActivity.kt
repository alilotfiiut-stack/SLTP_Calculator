package com.example.productionfeasibility

import android.os.Bundle
import android.graphics.Typeface
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.ceil

class MainActivity : AppCompatActivity() {
    private lateinit var container: FrameLayout
    private lateinit var next: Button
    private lateinit var title: TextView
    private lateinit var subtitle: TextView
    private lateinit var settingsButton: TextView
    private var page = 1
    private val fields = mutableMapOf<String, EditText>()
    private val demandValues = mutableMapOf<String, String>()
    private val prefs by lazy { getSharedPreferences("production_settings", MODE_PRIVATE) }

    private data class Setting(val key: String, val label: String, val unit: String)
    private val settings = listOf(
        Setting("syrupPercent", "درصد مصرف شربت در ۱ کیلوگرم محصول", "درصد"),
        Setting("productCapacity", "ظرفیت ساخت محصول در یک شیفت", "کیلوگرم"),
        Setting("syrupCapacity", "ظرفیت ساخت شربت در ۲۴ ساعت", "کیلوگرم"),
        Setting("pack1", "ظرفیت بسته‌بندی خط ۱ لیتری در یک شیفت", "کارتن"),
        Setting("pack4", "ظرفیت بسته‌بندی خط ۴ لیتری در یک شیفت", "کارتن"),
        Setting("pack20", "ظرفیت بسته‌بندی خط ۲۰ لیتری در یک شیفت", "سطل")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        container = findViewById(R.id.container)
        next = findViewById(R.id.nextButton)
        title = findViewById(R.id.title)
        subtitle = findViewById(R.id.subtitle)
        settingsButton = findViewById(R.id.settingsButton)
        settingsButton.setOnClickListener { showSettings() }
        next.setOnClickListener {
            when (page) {
                1 -> calculateAndShow()
                2 -> saveSettings()
                3 -> showPage1()
            }
        }
        showPage1()
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun base() = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        layoutDirection = View.LAYOUT_DIRECTION_RTL
        gravity = Gravity.CENTER_HORIZONTAL
        setPadding(dp(2), dp(2), dp(2), dp(8))
    }
    private fun card(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        layoutDirection = View.LAYOUT_DIRECTION_RTL
        background = getDrawable(R.drawable.bg_card)
        elevation = dp(2).toFloat()
        setPadding(dp(16), dp(12), dp(16), dp(12))
    }
    private fun label(s: String) = TextView(this).apply {
        text = s; textSize = 15f; setTextColor(0xFF172033.toInt())
        setTypeface(Typeface.DEFAULT, Typeface.BOLD); gravity = Gravity.CENTER_VERTICAL
    }
    private fun input(key: String, hint: String, value: String = ""): EditText = EditText(this).apply {
        inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        textSize = 17f; setSingleLine(true); setHint(hint); setText(value)
        gravity = Gravity.CENTER; layoutDirection = View.LAYOUT_DIRECTION_LTR
        background = getDrawable(R.drawable.bg_input); setPadding(dp(14), 0, dp(14), 0)
        layoutParams = LinearLayout.LayoutParams(-1, dp(50)); fields[key] = this
    }
    private fun pageHeader(t: String, st: String, button: String) {
        title.text = t; subtitle.text = st; next.text = button
    }
    private fun clear() { container.removeAllViews(); fields.clear() }
    private fun addInputCard(parent: LinearLayout, key: String, text: String, unit: String, value: String = "") {
        val c = card().apply { layoutParams = LinearLayout.LayoutParams(-1, dp(112)).apply { topMargin=dp(5); bottomMargin=dp(5) } }
        c.addView(label(text), LinearLayout.LayoutParams(-1, 0, 1f))
        c.addView(input(key, unit, value), LinearLayout.LayoutParams(-1, 0, 1.15f))
        parent.addView(c)
    }

    private fun showPage1() {
        page = 1; clear()
        settingsButton.visibility = View.VISIBLE
        pageHeader("نیاز روزانه تولید", "مقادیر مورد نیاز روزانه را وارد کنید", "محاسبه و مشاهده نتایج")
        val box = base()
        addInputCard(box, "need1", "مقدار مورد نیاز محصول ۱ لیتری", "تعداد کارتن", demandValues["need1"] ?: "")
        addInputCard(box, "need4", "مقدار مورد نیاز محصول ۴ لیتری", "تعداد کارتن", demandValues["need4"] ?: "")
        addInputCard(box, "need20", "مقدار مورد نیاز محصول ۲۰ لیتری", "تعداد سطل", demandValues["need20"] ?: "")
        val scroll = ScrollView(this).apply { isFillViewport=true; addView(box) }
        container.addView(scroll)
    }

    private fun showSettings() {
        listOf("need1", "need4", "need20").forEach { demandValues[it] = fields[it]?.text?.toString() ?: "" }
        page = 2; clear()
        settingsButton.visibility = View.INVISIBLE
        pageHeader("تنظیمات", "ظرفیت‌ها و پارامترهای تولید را وارد کنید", "ذخیره تنظیمات")
        val box = base()
        settings.forEach { s ->
            addInputCard(box, s.key, s.label, s.unit, prefs.getString(s.key, "") ?: "")
        }
        val note = TextView(this).apply {
            text = "یک شیفت کاری ۸ ساعت در نظر گرفته شده است"
            textSize=13f; setTextColor(0xFF687386.toInt()); gravity=Gravity.CENTER
            setPadding(0, dp(12), 0, dp(12))
        }
        box.addView(note)
        container.addView(ScrollView(this).apply { isFillViewport=true; addView(box) })
    }

    private fun saveSettings() {
        val values = settings.associate { it.key to (fields[it.key]?.text?.toString()?.trim() ?: "") }
        if (values.values.any { it.isNotEmpty() && parse(it) == null }) {
            toast("لطفاً مقادیر تنظیمات را به‌صورت عددی وارد کنید."); return
        }
        if (values.values.any { it.isEmpty() || (parse(it) ?: 0.0) <= 0.0 }) {
            toast("لطفاً همه ظرفیت‌ها و درصد مصرف شربت را با عددی بزرگ‌تر از صفر تکمیل کنید."); return
        }
        prefs.edit().apply { values.forEach { (k,v) -> putString(k,v) } }.apply()
        toast("تنظیمات ذخیره شد.")
        showPage1()
    }

    private fun parse(raw: String?): Double? {
        if (raw == null) return null
        val normalized = raw.trim().map { ch ->
            when (ch) {
                in '۰'..'۹' -> '0' + (ch - '۰')
                in '٠'..'٩' -> '0' + (ch - '٠')
                '٫', ',' -> '.'
                else -> ch
            }
        }.joinToString("")
        return normalized.toDoubleOrNull()
    }
    private fun read(key: String) = parse(fields[key]?.text?.toString())
    private fun fmt(x: Double): String {
        val symbols = DecimalFormatSymbols(Locale.US)
        val df = DecimalFormat("#,##0.##", symbols)
        return df.format(x)
    }
    private fun toast(s: String) = Toast.makeText(this, s, Toast.LENGTH_LONG).show()

    private fun calculateAndShow() {
        val n1=read("need1"); val n4=read("need4"); val n20=read("need20")
        if (listOf(n1,n4,n20).any { it == null } || listOf(n1!!,n4!!,n20!!).any { it < 0 }) {
            toast("لطفاً هر سه مقدار نیاز روزانه را با عدد صفر یا بزرگ‌تر وارد کنید."); return
        }
        val cfg = settings.associate { it.key to parse(prefs.getString(it.key, null)) }
        if (cfg.values.any { it == null || it!! <= 0.0 }) {
            toast("ابتدا از طریق چرخ‌دنده، تمام تنظیمات و ظرفیت‌ها را تکمیل و ذخیره کنید."); return
        }
        val need1 = n1 ?: 0.0
        val need4 = n4 ?: 0.0
        val need20 = n20 ?: 0.0
        val a = need1*12.0 + need4*16.0 + need20*20.0
        val productKg = a*0.9
        val syrupKg = productKg * cfg.getValue("syrupPercent")!! / 100.0
        val productShifts = if (productKg == 0.0) 0 else ceil(productKg / cfg.getValue("productCapacity")!!).toInt()
        val syrupCap = cfg.getValue("syrupCapacity")!!
        val syrupResult = if (syrupKg <= syrupCap) "ساخت شربت امکان‌پذیر است" else "ساخت شربت امکان‌پذیر نمی‌باشد"
        val productResult = when (productShifts) {
            0 -> "نیازی به ساخت محصول نیست"
            1 -> "در صورت تأمین شربت، ساخت در یک شیفت کاری امکان‌پذیر است"
            2 -> "در صورت تأمین شربت، ساخت در دو شیفت کاری امکان‌پذیر است"
            3 -> "در صورت تأمین شربت، ساخت در سه شیفت کاری امکان‌پذیر است"
            else -> "ساخت امکان‌پذیر نمی‌باشد"
        }
        val p1 = if (need1 == 0.0) 0 else ceil(need1 / cfg.getValue("pack1")!!).toInt()
        val p4 = if (need4 == 0.0) 0 else ceil(need4 / cfg.getValue("pack4")!!).toInt()
        val p20 = if (need20 == 0.0) 0 else ceil(need20 / cfg.getValue("pack20")!!).toInt()
        showResults(productKg, productResult, syrupKg, syrupResult,
            listOf(PackResult("محصول ۱ لیتری", p1), PackResult("محصول ۴ لیتری", p4), PackResult("محصول ۲۰ لیتری", p20)))
    }
    private data class PackResult(val name: String, val lines: Int)
    private fun packingText(n: Int): String = when (n) {
        0 -> "نیازی به بسته‌بندی نیست"
        1 -> "یک خط در شیفت اول"
        2 -> "دو خط در شیفت اول"
        3 -> "دو خط در شیفت اول + یک خط در شیفت دوم"
        4 -> "دو خط در شیفت اول و دوم"
        5 -> "دو خط در شیفت اول و دوم + یک خط در شیفت سوم"
        6 -> "دو خط در شیفت اول، دوم و سوم"
        else -> "با منابع فعلی امکان‌پذیر نیست"
    }

    private fun resultCard(heading: String, firstLabel: String, firstValue: String, result: String): LinearLayout {
        val c=card().apply { layoutParams=LinearLayout.LayoutParams(-1,-2).apply { topMargin=dp(6); bottomMargin=dp(6) } }
        c.addView(TextView(this).apply {
            text=heading; textSize=17f; typeface=Typeface.DEFAULT_BOLD; setTextColor(0xFF172033.toInt())
            gravity=Gravity.CENTER; setPadding(0,dp(4),0,dp(10))
        })
        val row=LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL; layoutDirection=View.LAYOUT_DIRECTION_RTL }
        fun cell(s:String, weight:Float, bold:Boolean=false)=TextView(this).apply {
            text=s; textSize=14f; setTextColor(0xFF172033.toInt()); gravity=Gravity.CENTER
            if(bold) typeface=Typeface.DEFAULT_BOLD
            setPadding(dp(4),dp(10),dp(4),dp(10))
            layoutParams=LinearLayout.LayoutParams(0,-2,weight)
        }
        row.addView(cell(firstLabel,1f,true)); row.addView(cell(firstValue,1f,true))
        c.addView(row)
        c.addView(TextView(this).apply {
            text=result; textSize=14f; setTextColor(if(result.contains("امکان‌پذیر نمی")) 0xFFC62828.toInt() else 0xFF2457D6.toInt())
            typeface=Typeface.DEFAULT_BOLD; gravity=Gravity.CENTER; setPadding(dp(4),dp(8),dp(4),dp(4))
        })
        return c
    }

    private fun showResults(productKg:Double, productResult:String, syrupKg:Double, syrupResult:String, packs:List<PackResult>) {
        page=3; clear(); settingsButton.visibility=View.INVISIBLE
        pageHeader("نتایج امکان‌سنجی تولید", "خلاصه محاسبات بر اساس نیاز روزانه و ظرفیت‌های ثبت‌شده", "بازگشت به نیاز روزانه")
        val box=base()
        box.addView(resultCard("امکان‌سنجی ساخت شربت", "شربت مورد نیاز (Kg)", fmt(syrupKg), syrupResult))
        box.addView(resultCard("امکان‌سنجی ساخت محصول", "محصول مورد نیاز (Kg)", fmt(productKg), productResult))
        val packCard=card().apply { layoutParams=LinearLayout.LayoutParams(-1,-2).apply { topMargin=dp(6); bottomMargin=dp(6) } }
        packCard.addView(TextView(this).apply {
            text="امکان‌سنجی بسته‌بندی محصول"; textSize=17f; typeface=Typeface.DEFAULT_BOLD
            setTextColor(0xFF172033.toInt()); gravity=Gravity.CENTER; setPadding(0,dp(4),0,dp(10))
        })
        val header=LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL; layoutDirection=View.LAYOUT_DIRECTION_RTL }
        listOf("نوع محصول","تعداد خطوط","تخصیص خطوط و شیفت").forEachIndexed { i,s ->
            header.addView(TextView(this).apply {
                text=s; textSize=12f; typeface=Typeface.DEFAULT_BOLD; setTextColor(0xFF687386.toInt())
                gravity=Gravity.CENTER; setPadding(dp(2),dp(8),dp(2),dp(8))
                layoutParams=LinearLayout.LayoutParams(0,-2, if(i==2) 1.6f else 1f)
            })
        }
        packCard.addView(header)
        packs.forEach { p ->
            val row=LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL; layoutDirection=View.LAYOUT_DIRECTION_RTL }
            val vals=listOf(p.name, if(p.lines==0) "۰" else p.lines.toString(), packingText(p.lines))
            vals.forEachIndexed { i,s ->
                row.addView(TextView(this).apply {
                    text=s; textSize=12f; setTextColor(if(i==2 && p.lines>=7) 0xFFC62828.toInt() else 0xFF172033.toInt())
                    gravity=Gravity.CENTER; setPadding(dp(3),dp(10),dp(3),dp(10))
                    layoutParams=LinearLayout.LayoutParams(0,-2, if(i==2) 1.6f else 1f)
                })
            }
            packCard.addView(row)
            val line=View(this).apply { setBackgroundColor(0xFFE9EDF4.toInt()); layoutParams=LinearLayout.LayoutParams(-1,dp(1)) }
            packCard.addView(line)
        }
        box.addView(packCard)
        container.addView(ScrollView(this).apply { isFillViewport=true; addView(box) })
    }
}
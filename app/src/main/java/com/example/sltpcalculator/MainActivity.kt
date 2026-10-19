package com.example.sltpcalculator

import android.content.Context
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
    private var page = 0
    private var isLong = true
    private val inputs = mutableMapOf<String, EditText>()

    companion object {
        private const val PREFS_NAME = "sltp_calculator_settings"
        private const val KEY_FEE = "fee_percent"
        private const val DEFAULT_FEE = 0.2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        container = findViewById(R.id.container)
        next = findViewById(R.id.nextButton)
        showHome()
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
        background = getDrawable(R.drawable.bg_input)
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

    private fun clear() {
        container.removeAllViews()
    }

    private fun setHeader(titleText: String, subtitleText: String) {
        findViewById<TextView>(R.id.title).text = titleText
        findViewById<TextView>(R.id.subtitle).text = subtitleText
    }

    private fun showHome() {
        page = 0
        clear()
        next.visibility = View.GONE
        setHeader("نوع محاسبه را انتخاب کنید", "")

        val box = baseLayout()
        box.setPadding(dp(2), dp(2), dp(2), dp(2))

        // Move the title area slightly lower and keep the two choices centered as a group.
        box.addView(Space(this).apply {
            layoutParams = LinearLayout.LayoutParams(1, 0, 0.22f)
        })

        fun homeOption(title: String, onClick: () -> Unit): LinearLayout {
            return card().apply {
                isClickable = true
                isFocusable = true
                gravity = Gravity.CENTER
                background = getDrawable(R.drawable.bg_home_card)
                elevation = dp(5).toFloat()
                setPadding(dp(12), 0, dp(12), 0)
                layoutParams = LinearLayout.LayoutParams(-1, dp(112)).apply {
                    topMargin = dp(9)
                    bottomMargin = dp(9)
                }
                setOnClickListener { onClick() }

                addView(TextView(this@MainActivity).apply {
                    text = title
                    textSize = 19f
                    typeface = Typeface.DEFAULT_BOLD
                    gravity = Gravity.CENTER
                    textAlignment = View.TEXT_ALIGNMENT_CENTER
                    setTextColor(0xFF172033.toInt())
                    layoutParams = LinearLayout.LayoutParams(-1, -1)
                })
            }
        }

        box.addView(homeOption("محاسبه RR اسمی") {
            showNominalInfo()
        })
        box.addView(homeOption("محاسبه RR واقعی") {
            showActualRrInfo()
        })
        box.addView(homeOption("محاسبه SL و TP") {
            showTradeInfo()
        })

        box.addView(Space(this).apply {
            layoutParams = LinearLayout.LayoutParams(1, 0, 0.22f)
        })

        val settingsButton = Button(this).apply {
            text = "⚙ تنظیمات"
            textSize = 16f
            textStyleSafe()
            setTextColor(0xFF172033.toInt())
            background = getDrawable(R.drawable.bg_card)
            isAllCaps = false
            layoutParams = LinearLayout.LayoutParams(-1, dp(54)).apply {
                topMargin = dp(6)
            }
            setOnClickListener { showSettings() }
        }
        box.addView(settingsButton)
        container.addView(box)
    }

    private fun TextView.textStyleSafe() {
        setTypeface(Typeface.DEFAULT, Typeface.BOLD)
    }

    private fun showTradeInfo(preserveValues: Boolean = false) {
        page = 1
        clear()
        next.visibility = View.VISIBLE
        next.text = "محاسبه"
        setHeader("اطلاعات معامله", "مقادیر معامله را وارد کنید")

        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            layoutDirection = View.LAYOUT_DIRECTION_RTL
            setPadding(dp(0), dp(0), dp(0), dp(2))
            layoutParams = LinearLayout.LayoutParams(-1, -1)
        }

        // جهت معامله در ابتدای فرم قرار می‌گیرد تا تمام کادرها در همان صفحه دیده شوند.
        val directionCard = card().apply {
            layoutParams = LinearLayout.LayoutParams(-1, dp(72)).apply {
                topMargin = dp(2)
                bottomMargin = dp(5)
            }
        }
        directionCard.addView(label("جهت معامله").apply {
            layoutParams = LinearLayout.LayoutParams(-1, 0, 0.42f)
            gravity = Gravity.CENTER_VERTICAL or Gravity.RIGHT
        })

        val rg = RadioGroup(this).apply {
            orientation = RadioGroup.HORIZONTAL
            gravity = Gravity.CENTER
            layoutDirection = View.LAYOUT_DIRECTION_LTR
            layoutParams = LinearLayout.LayoutParams(-1, 0, 0.58f)
        }
        val long = RadioButton(this).apply {
            id = View.generateViewId()
            text = "Long"
            textSize = 17f
            isChecked = isLong
            setPadding(dp(14), dp(2), dp(14), dp(2))
        }
        val sh = RadioButton(this).apply {
            id = View.generateViewId()
            text = "Short"
            textSize = 17f
            isChecked = !isLong
            setPadding(dp(14), dp(2), dp(14), dp(2))
        }
        rg.addView(long)
        rg.addView(sh)
        rg.setOnCheckedChangeListener { _, id -> isLong = id == long.id }
        directionCard.addView(rg)
        box.addView(directionCard)

        val specs = listOf(
            Triple("entry", "قیمت ورود", ""),
            Triple("trade", "حجم معامله", ""),
            Triple("loss", "ضرر مورد انتظار (تتر)", ""),
            Triple("profit", "سود مورد انتظار (تتر)", "")
        )

        specs.forEach { (k, l, v) ->
            val c = card().apply {
                layoutParams = LinearLayout.LayoutParams(-1, dp(72)).apply {
                    topMargin = dp(3)
                    bottomMargin = dp(5)
                }
            }
            val lbl = label(l).apply {
                layoutParams = LinearLayout.LayoutParams(-1, 0, 0.43f)
                gravity = Gravity.CENTER_VERTICAL or Gravity.RIGHT
            }
            val preserved = if (preserveValues) inputs[k]?.text?.toString() ?: v else v
            val input = field(k, l, preserved).apply {
                layoutParams = LinearLayout.LayoutParams(-1, 0, 0.57f)
            }
            c.addView(lbl)
            c.addView(input)
            box.addView(c)
        }

        val scroll = ScrollView(this).apply {
            isFillViewport = true
            layoutParams = FrameLayout.LayoutParams(-1, -1)
            addView(box)
        }
        container.addView(scroll)
    }

    private fun showSettings() {
        page = 4
        clear()
        next.visibility = View.VISIBLE
        next.text = "ذخیره و بازگشت"
        setHeader("تنظیمات", "کارمزد را یک‌بار وارد کنید")

        val box = baseLayout().apply {
            setPadding(dp(2), dp(8), dp(2), dp(8))
        }
        box.addView(Space(this).apply {
            layoutParams = LinearLayout.LayoutParams(1, 0, 0.28f)
        })

        val c = card().apply {
            layoutParams = LinearLayout.LayoutParams(-1, dp(150))
        }
        c.addView(label("کارمزد معامله (%)").apply {
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(-1, 0, 0.38f)
        })
        val feeInput = field("settings_fee", "مثلاً 0.2", formatStoredFee()).apply {
            layoutParams = LinearLayout.LayoutParams(-1, 0, 0.62f)
        }
        c.addView(feeInput)
        box.addView(c)

        box.addView(TextView(this).apply {
            text = "این مقدار در گوشی ذخیره می‌شود و تا زمان تغییر دستی ثابت می‌ماند."
            textSize = 13f
            gravity = Gravity.CENTER
            setTextColor(0xFF687386.toInt())
            layoutParams = LinearLayout.LayoutParams(-1, dp(80)).apply {
                topMargin = dp(8)
            }
        })

        box.addView(Space(this).apply {
            layoutParams = LinearLayout.LayoutParams(1, 0, 0.35f)
        })
        container.addView(box)
    }

    private fun getStoredFee(): Double =
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_FEE, DEFAULT_FEE.toString())
            ?.replace(",", ".")
            ?.toDoubleOrNull()
            ?: DEFAULT_FEE

    private fun formatStoredFee(): String =
        String.format(Locale.US, "%.3f", getStoredFee()).trimEnd('0').trimEnd('.')

    private fun saveSettingsAndReturnHome() {
        val fee = num("settings_fee")
        if (fee == null || fee < 0.0) {
            Toast.makeText(this, "لطفاً کارمزد را صحیح وارد کنید.", Toast.LENGTH_SHORT).show()
            return
        }
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_FEE, fee.toString())
            .apply()
        Toast.makeText(this, "کارمزد ذخیره شد.", Toast.LENGTH_SHORT).show()
        showHome()
    }

    private fun num(key: String): Double? =
        inputs[key]?.text?.toString()?.trim()?.replace(",", ".")?.toDoubleOrNull()

    private fun fmt(x: Double): String = String.format(Locale.US, "%.3f", x)

    // Five decimal places for prices shown on the SL/TP results page.
    private fun fmtPrice(x: Double): String = String.format(Locale.US, "%.5f", x)

    private fun calculateTradeAndShow() {
        val feePct = getStoredFee()
        val expectedProfit = num("profit")
        val expectedLoss = num("loss")
        val trade = num("trade")
        val entry = num("entry")

        if (listOf(expectedProfit, expectedLoss, trade, entry).any { it == null } ||
            trade!! <= 0.0 || entry!! <= 0.0 || expectedProfit!! < 0.0 || expectedLoss!! < 0.0) {
            Toast.makeText(this, "لطفاً همه مقادیر را صحیح وارد کنید.", Toast.LENGTH_SHORT).show()
            return
        }

        // Keep the existing fee amount and break-even calculations unchanged.
        val feeAmount = (feePct / 100.0) * trade
        val a = feeAmount + expectedProfit
        val b = expectedLoss - feeAmount
        val c = a / trade
        val d = b / trade
        val h: Double
        val k: Double
        if (isLong) {
            h = c + 1.0
            k = 1.0 - d
        } else {
            h = 1.0 - c
            k = 1.0 + d
        }
        val sl = k * entry
        val tp = h * entry

        val aBreakEven = feePct / 100.0
        val bBreakEven = if (isLong) 1.0 + aBreakEven else 1.0 - aBreakEven
        val breakeven = entry * bBreakEven
        showTradeResult(sl, tp, entry, breakeven, feeAmount)
    }

    private fun showTradeResult(sl: Double, tp: Double, entry: Double, breakeven: Double, feeAmount: Double) {
        page = 2
        clear()
        next.visibility = View.VISIBLE
        next.text = "بازگشت به اطلاعات معامله"
        setHeader("نتیجه محاسبه", "مقادیر نهایی معامله")

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

        fun resultCard(title: String, value: Double, strokeColor: Int): LinearLayout = card().apply {
            background = android.graphics.drawable.GradientDrawable().apply {
                val fillColor = when (strokeColor) {
                    0xFFE53935.toInt() -> 0xFFFFEBEE.toInt()
                    0xFF43A047.toInt() -> 0xFFE8F5E9.toInt()
                    0xFF42A5F5.toInt() -> 0xFFE3F2FD.toInt()
                    else -> 0xFFF1F3F6.toInt()
                }
                setColor(fillColor)
                setStroke(dp(2), strokeColor)
                cornerRadius = dp(18).toFloat()
            }
            layoutParams = LinearLayout.LayoutParams(-1, 0, 1f).apply {
                topMargin = dp(6)
                bottomMargin = dp(6)
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
                text = fmtPrice(value)
                textSize = 30f
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                setTextColor(0xFF172033.toInt())
                layoutParams = LinearLayout.LayoutParams(-1, 0, 0.58f)
            }
            addView(t)
            addView(v)
        }

        box.addView(direction)
        box.addView(resultCard("Stop Loss", abs(sl), 0xFFE53935.toInt()))
        box.addView(resultCard("Take Profit", tp, 0xFF43A047.toInt()))
        box.addView(resultCard("نقطه سر به سر", breakeven, 0xFF42A5F5.toInt()))
        box.addView(resultCard("قیمت ورود", entry, 0xFFE6E6E6.toInt()))
        box.addView(resultCard("مقدار کارمزد (تتر)", feeAmount, 0xFFE6E6E6.toInt()))
        scroll.addView(box)
        container.addView(scroll)
    }

    private fun showActualRrInfo(preserveValues: Boolean = false) {
        page = 7
        clear()
        next.visibility = View.VISIBLE
        next.text = "محاسبه"
        setHeader("محاسبه RR واقعی", "مقادیر را وارد کنید")

        val box = baseLayout().apply {
            setPadding(dp(2), dp(8), dp(2), dp(8))
        }
        box.addView(Space(this).apply {
            layoutParams = LinearLayout.LayoutParams(1, 0, 0.24f)
        })

        val slCard = card().apply {
            layoutParams = LinearLayout.LayoutParams(-1, dp(120)).apply {
                topMargin = dp(6)
                bottomMargin = dp(6)
            }
        }
        slCard.addView(label("SL (%)").apply {
            layoutParams = LinearLayout.LayoutParams(-1, 0, 0.43f)
            gravity = Gravity.CENTER_VERTICAL or Gravity.RIGHT
        })
        slCard.addView(field("actual_sl", "مثلاً 0.5", if (preserveValues) inputs["actual_sl"]?.text?.toString() ?: "" else "").apply {
            layoutParams = LinearLayout.LayoutParams(-1, 0, 0.57f)
        })
        box.addView(slCard)

        val rrCard = card().apply {
            layoutParams = LinearLayout.LayoutParams(-1, dp(120)).apply {
                topMargin = dp(6)
                bottomMargin = dp(6)
            }
        }
        rrCard.addView(label("TradingView RR Base").apply {
            layoutParams = LinearLayout.LayoutParams(-1, 0, 0.43f)
            gravity = Gravity.CENTER_VERTICAL or Gravity.RIGHT
        })
        rrCard.addView(field("tv_rr", "مثلاً 2", if (preserveValues) inputs["tv_rr"]?.text?.toString() ?: "2" else "2").apply {
            layoutParams = LinearLayout.LayoutParams(-1, 0, 0.57f)
        })
        box.addView(rrCard)

        val stepCard = card().apply {
            layoutParams = LinearLayout.LayoutParams(-1, dp(120)).apply {
                topMargin = dp(6)
                bottomMargin = dp(6)
            }
        }
        stepCard.addView(label("پله تغییر RR").apply {
            layoutParams = LinearLayout.LayoutParams(-1, 0, 0.43f)
            gravity = Gravity.CENTER_VERTICAL or Gravity.RIGHT
        })
        stepCard.addView(field("actual_step", "مثلاً 0.5", if (preserveValues) inputs["actual_step"]?.text?.toString() ?: "0.5" else "0.5").apply {
            layoutParams = LinearLayout.LayoutParams(-1, 0, 0.57f)
        })
        box.addView(stepCard)

        box.addView(Space(this).apply {
            layoutParams = LinearLayout.LayoutParams(1, 0, 0.20f)
        })
        container.addView(box)
    }

    private fun calculateActualRrAndShow() {
        val sl = num("actual_sl")
        val tvRr = num("tv_rr")
        val step = num("actual_step")
        val feeFraction = getStoredFee()
        if (sl == null || tvRr == null || step == null || sl <= 0.0 || tvRr < 0.0 || step < 0.0) {
            Toast.makeText(this, "لطفاً SL، TradingView RR Base و پله تغییر را صحیح وارد کنید.", Toast.LENGTH_SHORT).show()
            return
        }
        val values = (0..4).map { i ->
            val rr = tvRr + i * step
            val a = rr * sl
            val b = a - feeFraction
            val c = sl + feeFraction
            val actual = if (c == 0.0) 0.0 else b / c
            Triple(i + 1, rr, actual)
        }
        showActualRrResult(values)
    }

    private fun showActualRrResult(rows: List<Triple<Int, Double, Double>>) {
        page = 8
        clear()
        next.visibility = View.VISIBLE
        next.text = "بازگشت به اطلاعات"
        setHeader("نتیجه محاسبه", "RR واقعی")
        val scroll = ScrollView(this).apply { layoutParams = FrameLayout.LayoutParams(-1, -1) }
        val box = baseLayout().apply { setPadding(dp(6), dp(8), dp(6), dp(16)) }
        box.addView(makeRrTable("RR واقعی", "TradingView RR", rows.map { Triple(it.first, it.second, it.third) }))
        scroll.addView(box)
        container.addView(scroll)
    }

    private fun showNominalInfo(preserveValues: Boolean = false) {
        page = 5
        clear()
        next.visibility = View.VISIBLE
        next.text = "محاسبه"
        setHeader("اطلاعات معامله", "مقادیر معامله را وارد کنید")

        val box = baseLayout().apply {
            setPadding(dp(2), dp(8), dp(2), dp(8))
        }
        box.addView(Space(this).apply {
            layoutParams = LinearLayout.LayoutParams(1, 0, 0.24f)
        })

        val slCard = card().apply {
            layoutParams = LinearLayout.LayoutParams(-1, dp(120)).apply {
                topMargin = dp(6)
                bottomMargin = dp(6)
            }
        }
        slCard.addView(label("SL (%)").apply {
            layoutParams = LinearLayout.LayoutParams(-1, 0, 0.43f)
            gravity = Gravity.CENTER_VERTICAL or Gravity.RIGHT
        })
        slCard.addView(field("nominal_sl", "مثلاً 0.5", if (preserveValues) inputs["nominal_sl"]?.text?.toString() ?: "" else "").apply {
            layoutParams = LinearLayout.LayoutParams(-1, 0, 0.57f)
        })
        box.addView(slCard)

        val rrCard = card().apply {
            layoutParams = LinearLayout.LayoutParams(-1, dp(120)).apply {
                topMargin = dp(6)
                bottomMargin = dp(6)
            }
        }
        rrCard.addView(label("Actual RR Base").apply {
            layoutParams = LinearLayout.LayoutParams(-1, 0, 0.43f)
            gravity = Gravity.CENTER_VERTICAL or Gravity.RIGHT
        })
        rrCard.addView(field("nominal_rr", "مثلاً 1", if (preserveValues) inputs["nominal_rr"]?.text?.toString() ?: "1" else "1").apply {
            layoutParams = LinearLayout.LayoutParams(-1, 0, 0.57f)
        })
        box.addView(rrCard)

        val stepCard = card().apply {
            layoutParams = LinearLayout.LayoutParams(-1, dp(120)).apply {
                topMargin = dp(6)
                bottomMargin = dp(6)
            }
        }
        stepCard.addView(label("پله تغییر RR").apply {
            layoutParams = LinearLayout.LayoutParams(-1, 0, 0.43f)
            gravity = Gravity.CENTER_VERTICAL or Gravity.RIGHT
        })
        stepCard.addView(field("nominal_step", "مثلاً 0.5", if (preserveValues) inputs["nominal_step"]?.text?.toString() ?: "0.5" else "0.5").apply {
            layoutParams = LinearLayout.LayoutParams(-1, 0, 0.57f)
        })
        box.addView(stepCard)

        box.addView(Space(this).apply {
            layoutParams = LinearLayout.LayoutParams(1, 0, 0.20f)
        })
        container.addView(box)
    }

    private fun calculateNominalAndShow() {
        val sl = num("nominal_sl")
        val realRr = num("nominal_rr")
        val step = num("nominal_step")
        val fee = getStoredFee()

        if (sl == null || realRr == null || step == null || sl <= 0.0 || realRr < 0.0 || step < 0.0) {
            Toast.makeText(this, "لطفاً SL، Actual RR Base و پله تغییر را صحیح وارد کنید.", Toast.LENGTH_SHORT).show()
            return
        }
        val values = (0..4).map { i ->
            val rr = realRr + i * step
            val a = 1.0 + rr
            val b = fee * a
            val c = b / sl
            val nominalRr = rr + c
            Triple(i + 1, rr, nominalRr)
        }
        showNominalResult(values)
    }

    private fun showNominalResult(rows: List<Triple<Int, Double, Double>>) {
        page = 6
        clear()
        next.visibility = View.VISIBLE
        next.text = "بازگشت به اطلاعات معامله"
        setHeader("نتیجه محاسبه", "RR اسمی")
        val scroll = ScrollView(this).apply { layoutParams = FrameLayout.LayoutParams(-1, -1) }
        val box = baseLayout().apply { setPadding(dp(6), dp(8), dp(6), dp(16)) }
        box.addView(makeRrTable("RR اسمی", "Actual RR Base", rows))
        scroll.addView(box)
        container.addView(scroll)
    }

    private fun makeRrTable(title: String, baseTitle: String, rows: List<Triple<Int, Double, Double>>): LinearLayout {
        val outer = card().apply {
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(8) }
        }
        outer.addView(label(title).apply {
            gravity = Gravity.CENTER
            textSize = 20f
            setPadding(0, dp(8), 0, dp(12))
        })
        val table = TableLayout(this).apply { isStretchAllColumns = true }
        val header = TableRow(this)
        listOf("ردیف", baseTitle, title).forEach { txt ->
            header.addView(tableCell(txt, true))
        }
        table.addView(header)
        rows.forEach { (index, baseValue, resultValue) ->
            val row = TableRow(this)
            row.addView(tableCell(index.toString(), false))
            row.addView(tableCell(fmt(baseValue), false))
            row.addView(tableCell(fmt(resultValue), false))
            table.addView(row)
        }
        outer.addView(table)
        return outer
    }

    private fun tableCell(value: String, header: Boolean): TextView = TextView(this).apply {
        text = value
        textSize = if (header) 13f else 15f
        gravity = Gravity.CENTER
        setTextColor(0xFF172033.toInt())
        if (header) setTypeface(Typeface.DEFAULT, Typeface.BOLD)
        setPadding(dp(5), dp(12), dp(5), dp(12))
        background = android.graphics.drawable.GradientDrawable().apply {
            setColor(if (header) 0xFFE3F2FD.toInt() else 0xFFF7F8FA.toInt())
            setStroke(dp(1), 0xFFD4D9E2.toInt())
        }
    }

    override fun onBackPressed() {
        when (page) {
            0 -> super.onBackPressed()
            1, 4, 5, 7 -> showHome()
            2 -> showTradeInfo(true)
            6 -> showNominalInfo(true)
            8 -> showActualRrInfo(true)
            else -> showHome()
        }
    }

    override fun onResume() {
        super.onResume()
        if (!::next.isInitialized) return
        next.setOnClickListener {
            when (page) {
                1 -> calculateTradeAndShow()
                2 -> showTradeInfo(true)
                4 -> saveSettingsAndReturnHome()
                5 -> calculateNominalAndShow()
                6 -> showNominalInfo(true)
                7 -> calculateActualRrAndShow()
                8 -> showActualRrInfo(true)
                else -> showHome()
            }
        }
    }
}

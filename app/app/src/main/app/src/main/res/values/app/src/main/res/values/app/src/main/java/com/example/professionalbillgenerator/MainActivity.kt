package com.example.professionalbillgenerator

import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

data class BillItem(
    var name: String = "",
    var qty: Double = 1.0,
    var rate: Double = 0.0
)

class MainActivity : AppCompatActivity() {

    private lateinit var company: EditText
    private lateinit var customer: EditText
    private lateinit var mobile: EditText
    private lateinit var invoiceNo: EditText
    private lateinit var discount: EditText
    private lateinit var gst: EditText
    private lateinit var itemsBox: LinearLayout
    private lateinit var totalText: TextView

    private var lastPdf: File? = null

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createScreen()
    }

    private fun createScreen() {

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(245, 247, 250))
        }

        val title = TextView(this).apply {
            text = "  PROFESSIONAL BILL GENERATOR"
            textSize = 19f
            gravity = Gravity.CENTER_VERTICAL
            setTextColor(Color.WHITE)
            setTypeface(typeface, Typeface.BOLD)
            setBackgroundColor(Color.rgb(15, 23, 42))
        }

        root.addView(title, LinearLayout.LayoutParams(-1, dp(60)))

        val scroll = ScrollView(this)

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(24))
        }

        scroll.addView(content)

        fun heading(text: String) {
            val t = TextView(this).apply {
                this.text = text
                textSize = 17f
                setTypeface(typeface, Typeface.BOLD)
                setTextColor(Color.rgb(31, 75, 153))
                setPadding(0, dp(14), 0, dp(8))
            }

            content.addView(t)
        }

        fun field(hint: String, value: String = ""): EditText {
            return EditText(this).apply {
                this.hint = hint
                setText(value)
                setSingleLine(true)
                setPadding(dp(12), 0, dp(12), 0)
            }
        }

        fun twoFields(a: EditText, b: EditText) {

            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }

            row.addView(
                a,
                LinearLayout.LayoutParams(0, dp(55), 1f)
            )

            row.addView(
                b,
                LinearLayout.LayoutParams(0, dp(55), 1f)
            )

            content.addView(row)
        }

        heading("Business Details")

        company = field(
            "Business / Company Name",
            "Your Business"
        )

        content.addView(
            company,
            LinearLayout.LayoutParams(-1, dp(55))
        )

        heading("Customer Details")

        customer = field("Customer Name")
        mobile = field("Mobile Number")

        twoFields(customer, mobile)

        heading("Invoice Details")

        invoiceNo = field(
            "Invoice Number",
            "INV-" + SimpleDateFormat(
                "yyyyMMdd-HHmm",
                Locale.getDefault()
            ).format(Date())
        )

        val date = field(
            "Date",
            SimpleDateFormat(
                "dd/MM/yyyy",
                Locale.getDefault()
            ).format(Date())
        )

        twoFields(invoiceNo, date)

        heading("Items")

        itemsBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        content.addView(itemsBox)

        val addItem = Button(this).apply {
            text = "+ ADD ITEM"

            setOnClickListener {
                addItemRow()
            }
        }

        content.addView(addItem)

        heading("GST & Discount")

        discount = field("Discount %", "0")
        gst = field("GST %", "0")

        twoFields(discount, gst)

        totalText = TextView(this).apply {
            textSize = 21f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(Color.rgb(15, 23, 42))
            gravity = Gravity.END
            setPadding(0, dp(20), 0, dp(20))
        }

        content.addView(totalText)

        val pdfButton = Button(this).apply {
            text = "GENERATE PDF"

            setOnClickListener {
                generatePdf()
            }
        }

        content.addView(
            pdfButton,
            LinearLayout.LayoutParams(-1, dp(55))
        )

        val shareButton = Button(this).apply {
            text = "SHARE LAST PDF"

            setOnClickListener {
                shareLastPdf()
            }
        }

        content.addView(
            shareButton,
            LinearLayout.LayoutParams(-1, dp(55))
        )

        val footer = TextView(this).apply {
            text = "Offline • GST / Non-GST • PDF Invoice"
            gravity = Gravity.CENTER
            setPadding(0, dp(16), 0, 0)
        }

        content.addView(footer)

        root.addView(
            scroll,
            LinearLayout.LayoutParams(-1, 0, 1f)
        )

        setContentView(root)

        addItemRow()
        addItemRow()

        recalculate()
    }

    private fun addItemRow() {

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                dp(10),
                dp(8),
                dp(10),
                dp(8)
            )

            setBackgroundColor(Color.WHITE)
        }

        val name = EditText(this).apply {
            hint = "Item / Service Description"
            setSingleLine(true)
        }

        val qty = EditText(this).apply {
            hint = "Qty"
            setText("1")
            setSingleLine(true)
            inputType =
                android.text.InputType.TYPE_CLASS_NUMBER or
                android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        }

        val rate = EditText(this).apply {
            hint = "Rate"
            setText("0")
            setSingleLine(true)
            inputType =
                android.text.InputType.TYPE_CLASS_NUMBER or
                android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        }

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        row.addView(
            qty,
            LinearLayout.LayoutParams(0, dp(52), 1f)
        )

        row.addView(
            rate,
            LinearLayout.LayoutParams(0, dp(52), 1.5f)
        )

        card.addView(
            name,
            LinearLayout.LayoutParams(-1, dp(52))
        )

        card.addView(row)

        val remove = Button(this).apply {
            text = "REMOVE"

            setOnClickListener {

                if (itemsBox.childCount > 1) {
                    itemsBox.removeView(card)
                    recalculate()
                }
            }
        }

        card.addView(remove)

        itemsBox.addView(
            card,
            LinearLayout.LayoutParams(-1, -2)
        )

        val watcher =
            object : android.text.TextWatcher {

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {}

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                    recalculate()
                }

                override fun afterTextChanged(
                    s: android.text.Editable?
                ) {}
            }

        qty.addTextChangedListener(watcher)
        rate.addTextChangedListener(watcher)
        discount.addTextChangedListener(watcher)
        gst.addTextChangedListener(watcher)

        recalculate()
    }

    private fun recalculate() {

        if (!::totalText.isInitialized) return

        var subtotal = 0.0

        for (i in 0 until itemsBox.childCount) {

            val card =
                itemsBox.getChildAt(i) as LinearLayout

            val row =
                card.getChildAt(1) as LinearLayout

            val qty =
                row.getChildAt(0) as EditText

            val rate =
                row.getChildAt(1) as EditText

            val q =
                qty.text.toString()
                    .toDoubleOrNull() ?: 0.0

            val r =
                rate.text.toString()
                    .toDoubleOrNull() ?: 0.0

            subtotal += q * r
        }

        val discountPercent =
            discount.text.toString()
                .toDoubleOrNull() ?: 0.0

        val gstPercent =
            gst.text.toString()
                .toDoubleOrNull() ?: 0.0

        val discountAmount =
            subtotal * discountPercent / 100

        val taxable =
            subtotal - discountAmount

        val gstAmount =
            taxable * gstPercent / 100

        val total =
            taxable + gstAmount

        totalText.text =
            "Subtotal: ₹%.2f\n".format(subtotal) +
            "Discount: ₹%.2f\n".format(discountAmount) +
            "GST: ₹%.2f\n".format(gstAmount) +
            "GRAND TOTAL: ₹%.2f".format(total)
    }

    private fun generatePdf() {

        val document = PdfDocument()

        val pageInfo =
            PdfDocument.PageInfo
                .Builder(595, 842, 1)
                .create()

        val page =
            document.startPage(pageInfo)

        val canvas = page.canvas

        val paint =
            Paint(Paint.ANTI_ALIAS_FLAG)

        paint.color =
            Color.rgb(15, 23, 42)

        paint.textSize = 22f
        paint.typeface =
            Typeface.DEFAULT_BOLD

        canvas.drawText(
            company.text.toString()
                .ifBlank { "Your Business" },
            40f,
            55f,
            paint
        )

        paint.textSize = 11f
        paint.typeface =
            Typeface.DEFAULT

        canvas.drawText(
            "PROFESSIONAL INVOICE",
            40f,
            78f,
            paint
        )

        canvas.drawText(
            "Invoice: ${invoiceNo.text}",
            390f,
            55f,
            paint
        )

        canvas.drawText(
            "Customer: ${customer.text}",
            40f,
            110f,
            paint
        )

        canvas.drawText(
            "Mobile: ${mobile.text}",
            40f,
            128f,
            paint
        )

        paint.typeface =
            Typeface.DEFAULT_BOLD

        canvas.drawText(
            "Description",
            50f,
            170f,
            paint
        )

        canvas.drawText(
            "Qty",
            340f,
            170f,
            paint
        )

        canvas.drawText(
            "Rate",
            400f,
            170f,
            paint
        )

        canvas.drawText(
            "Amount",
            475f,
            170f,
            paint
        )

        var y = 200f

        for (i in 0 until itemsBox.childCount) {

            val card =
                itemsBox.getChildAt(i)
                    as LinearLayout

            val name =
                card.getChildAt(0)
                    as EditText

            val row =
                card.getChildAt(1)
                    as LinearLayout

            val qty =
                row.getChildAt(0)
                    as EditText

            val rate =
                row.getChildAt(1)
                    as EditText

            val q =
                qty.text.toString()
                    .toDoubleOrNull() ?: 0.0

            val r =
                rate.text.toString()
                    .toDoubleOrNull() ?: 0.0

            canvas.drawText(
                name.text.toString()
                    .take(30),
                50f,
                y,
                paint
            )

            canvas.drawText(
                "%.2f".format(q),
                340f,
                y,
                paint
            )

            canvas.drawText(
                "₹%.2f".format(r),
                400f,
                y,
                paint
            )

            canvas.drawText(
                "₹%.2f".format(q * r),
                475f,
                y,
                paint
            )

            y += 25f
        }

        val subtotal =
            calculateSubtotal()

        val discountPercent =
            discount.text.toString()
                .toDoubleOrNull() ?: 0.0

        val gstPercent =
            gst.text.toString()
                .toDoubleOrNull() ?: 0.0

        val discountAmount =
            subtotal * discountPercent / 100

        val taxable =
            subtotal - discountAmount

        val gstAmount =
            taxable * gstPercent / 100

        val total =
            taxable + gstAmount

        paint.typeface =
            Typeface.DEFAULT_BOLD

        canvas.drawText(
            "Subtotal: ₹%.2f".format(subtotal),
            390f,
            y + 30,
            paint
        )

        canvas.drawText(
            "Discount: ₹%.2f".format(discountAmount),
            390f,
            y + 55,
            paint
        )

        canvas.drawText(
            "GST: ₹%.2f".format(gstAmount),
            390f,
            y + 80,
            paint
        )

        paint.textSize = 16f

        canvas.drawText(
            "GRAND TOTAL: ₹%.2f".format(total),
            350f,
            y + 120,
            paint
        )

        paint.textSize = 10f
        paint.typeface =
            Typeface.DEFAULT

        canvas.drawText(
            "Thank you for your business.",
            40f,
            790f,
            paint
        )

        document.finishPage(page)

        val folder =
            getExternalFilesDir(
                Environment.DIRECTORY_DOCUMENTS
            )

        val file =
            File(
                folder,
                "Bill_${invoiceNo.text}.pdf"
            )

        FileOutputStream(file).use {
            document.writeTo(it)
        }

        document.close()

        lastPdf = file

        Toast.makeText(
            this,
            "PDF Generated Successfully",
            Toast.LENGTH_LONG
        ).show()

        shareFile(file)
    }

    private fun calculateSubtotal(): Double {

        var total = 0.0

        for (i in 0 until itemsBox.childCount) {

            val card =
                itemsBox.getChildAt(i)
                    as LinearLayout

            val row =
                card.getChildAt(1)
                    as LinearLayout

            val qty =
                row.getChildAt(0)
                    as EditText

            val rate =
                row.getChildAt(1)
                    as EditText

            val q =
                qty.text.toString()
                    .toDoubleOrNull() ?: 0.0

            val r =
                rate.text.toString()
                    .toDoubleOrNull() ?: 0.0

            total += q * r
        }

        return total
    }

    private fun shareLastPdf() {

        if (lastPdf == null) {

            Toast.makeText(
                this,
                "First generate a PDF",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        shareFile(lastPdf!!)
    }

    private fun shareFile(file: File) {

        val uri =
            Uri.fromFile(file)

        val intent =
            Intent(Intent.ACTION_SEND).apply {

                type = "application/pdf"

                putExtra(
                    Intent.EXTRA_STREAM,
                    uri
                )
            }

        startActivity(
            Intent.createChooser(
                intent,
                "Share Bill"
            )
        )
    }
}

package com.example.professionalbillgenerator;

import android.content.Intent;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    EditText businessName, businessPhone, businessAddress;
    EditText invoiceNumber, invoiceDate;
    EditText customerName, mobileNumber;
    EditText itemName, quantity, rate, gst, discount;

    TextView subtotalAmount, gstAmount, totalAmount;
    Button generateBill;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        businessName = findViewById(R.id.businessName);
        businessPhone = findViewById(R.id.businessPhone);
        businessAddress = findViewById(R.id.businessAddress);

        invoiceNumber = findViewById(R.id.invoiceNumber);
        invoiceDate = findViewById(R.id.invoiceDate);

        customerName = findViewById(R.id.customerName);
        mobileNumber = findViewById(R.id.mobileNumber);

        itemName = findViewById(R.id.itemName);
        quantity = findViewById(R.id.quantity);
        rate = findViewById(R.id.rate);

        gst = findViewById(R.id.gst);
        discount = findViewById(R.id.discount);

        subtotalAmount = findViewById(R.id.subtotalAmount);
        gstAmount = findViewById(R.id.gstAmount);
        totalAmount = findViewById(R.id.totalAmount);

        generateBill = findViewById(R.id.generateBill);

        invoiceDate.setText(
                new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        .format(new Date())
        );

        generateBill.setOnClickListener(v -> generatePdf());
    }

    private double getNumber(EditText field) {
        String value = field.getText().toString().trim();

        if (value.isEmpty()) {
            return 0;
        }

        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return 0;
        }
    }

    private void generatePdf() {

        double qty = getNumber(quantity);
        double price = getNumber(rate);
        double gstPercent = getNumber(gst);
        double discountValue = getNumber(discount);

        double subtotal = qty * price;
        double gstValue = subtotal * gstPercent / 100;
        double grandTotal = subtotal + gstValue - discountValue;

        if (grandTotal < 0) {
            grandTotal = 0;
        }

        subtotalAmount.setText(
                String.format(Locale.getDefault(),
                        "Subtotal: ₹%.2f", subtotal)
        );

        gstAmount.setText(
                String.format(Locale.getDefault(),
                        "GST: ₹%.2f", gstValue)
        );

        totalAmount.setText(
                String.format(Locale.getDefault(),
                        "Grand Total: ₹%.2f", grandTotal)
        );

        createPdf(subtotal, gstValue, grandTotal);
    }

    private void createPdf(double subtotal,
                           double gstValue,
                           double grandTotal) {

        PdfDocument document = new PdfDocument();

        PdfDocument.PageInfo pageInfo =
                new PdfDocument.PageInfo.Builder(595, 842, 1).create();

        PdfDocument.Page page = document.startPage(pageInfo);

        Paint paint = new Paint();
        paint.setColor(android.graphics.Color.BLACK);

        int y = 50;

        paint.setTextSize(24);
        paint.setFakeBoldText(true);
        page.getCanvas().drawText(
                "PROFESSIONAL INVOICE", 170, y, paint);

        paint.setFakeBoldText(false);
        paint.setTextSize(14);

        y += 40;

        page.getCanvas().drawText(
                "Business: " + businessName.getText().toString(),
                40, y, paint);

        y += 22;

        page.getCanvas().drawText(
                "Phone: " + businessPhone.getText().toString(),
                40, y, paint);

        y += 22;

        page.getCanvas().drawText(
                "Address: " + businessAddress.getText().toString(),
                40, y, paint);

        y += 35;

        page.getCanvas().drawText(
                "Invoice No: " + invoiceNumber.getText().toString(),
                40, y, paint);

        y += 22;

        page.getCanvas().drawText(
                "Date: " + invoiceDate.getText().toString(),
                40, y, paint);

        y += 35;

        page.getCanvas().drawText(
                "Customer: " + customerName.getText().toString(),
                40, y, paint);

        y += 22;

        page.getCanvas().drawText(
                "Mobile: " + mobileNumber.getText().toString(),
                40, y, paint);

        y += 40;

        paint.setFakeBoldText(true);
        page.getCanvas().drawText("ITEM", 40, y, paint);
        page.getCanvas().drawText("QTY", 300, y, paint);
        page.getCanvas().drawText("RATE", 380, y, paint);
        page.getCanvas().drawText("AMOUNT", 470, y, paint);

        paint.setFakeBoldText(false);

        y += 30;

        page.getCanvas().drawText(
                itemName.getText().toString(),
                40, y, paint);

        page.getCanvas().drawText(
                String.format(Locale.getDefault(), "%.2f", getNumber(quantity)),
                300, y, paint);

        page.getCanvas().drawText(
                String.format(Locale.getDefault(), "₹%.2f", getNumber(rate)),
                380, y, paint);

        page.getCanvas().drawText(
                String.format(Locale.getDefault(), "₹%.2f", subtotal),
                470, y, paint);

        y += 60;

        page.getCanvas().drawText(
                String.format(Locale.getDefault(),
                        "Subtotal: ₹%.2f", subtotal),
                380, y, paint);

        y += 25;

        page.getCanvas().drawText(
                String.format(Locale.getDefault(),
                        "GST: ₹%.2f", gstValue),
                380, y, paint);

        y += 25;

        page.getCanvas().drawText(
                String.format(Locale.getDefault(),
                        "Discount: ₹%.2f", getNumber(discount)),
                380, y, paint);

        y += 35;

        paint.setFakeBoldText(true);

        page.getCanvas().drawText(
                String.format(Locale.getDefault(),
                        "GRAND TOTAL: ₹%.2f", grandTotal),
                350, y, paint);

        paint.setFakeBoldText(false);

        y += 60;

        page.getCanvas().drawText(
                "Thank you for your business!",
                190, y, paint);

        document.finishPage(page);

        try {

            File folder = new File(
                    getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                    "ProfessionalBills"
            );

            if (!folder.exists()) {
                folder.mkdirs();
            }

            String fileName =
                    "Invoice_" +
                    System.currentTimeMillis() +
                    ".pdf";

            File file = new File(folder, fileName);

            FileOutputStream outputStream =
                    new FileOutputStream(file);

            document.writeTo(outputStream);
            outputStream.close();
            document.close();

            Toast.makeText(
                    this,
                    "PDF created successfully",
                    Toast.LENGTH_LONG
            ).show();

            sharePdf(file);

        } catch (Exception e) {

            document.close();

            Toast.makeText(
                    this,
                    "PDF Error: " + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void sharePdf(File file) {

        Uri uri = FileProvider.getUriForFile(
                this,
                getPackageName() + ".provider",
                file
        );

        Intent intent = new Intent(Intent.ACTION_SEND);

        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(
                Intent.createChooser(
                        intent,
                        "Share Invoice"
                )
        );
    }
}

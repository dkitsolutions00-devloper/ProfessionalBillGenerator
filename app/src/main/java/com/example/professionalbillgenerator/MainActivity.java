package com.example.professionalbillgenerator;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    EditText quantity, rate, gst, discount;
    TextView subtotalAmount, gstAmount, totalAmount;
    Button generateBill;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        quantity = findViewById(R.id.quantity);
        rate = findViewById(R.id.rate);
        gst = findViewById(R.id.gst);
        discount = findViewById(R.id.discount);

        subtotalAmount = findViewById(R.id.subtotalAmount);
        gstAmount = findViewById(R.id.gstAmount);
        totalAmount = findViewById(R.id.totalAmount);

        generateBill = findViewById(R.id.generateBill);

        generateBill.setOnClickListener(v -> calculateBill());
    }

    private void calculateBill() {

        double qty = getNumber(quantity);
        double price = getNumber(rate);
        double gstPercent = getNumber(gst);
        double discountValue = getNumber(discount);

        double subtotal = qty * price;
        double tax = subtotal * gstPercent / 100;
        double total = subtotal + tax - discountValue;

        if (total < 0) {
            total = 0;
        }

        subtotalAmount.setText(
                String.format("Subtotal: ₹%.2f", subtotal)
        );

        gstAmount.setText(
                String.format("GST: ₹%.2f", tax)
        );

        totalAmount.setText(
                String.format("Grand Total: ₹%.2f", total)
        );
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
}

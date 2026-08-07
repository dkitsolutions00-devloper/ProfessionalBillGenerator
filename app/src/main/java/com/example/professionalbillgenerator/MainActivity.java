package com.example.professionalbillgenerator;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    EditText quantity, rate, gst, discount;
    TextView totalAmount;
    Button generateBill;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        quantity = findViewById(R.id.quantity);
        rate = findViewById(R.id.rate);
        gst = findViewById(R.id.gst);
        discount = findViewById(R.id.discount);
        totalAmount = findViewById(R.id.totalAmount);
        generateBill = findViewById(R.id.generateBill);

        generateBill.setOnClickListener(v -> calculateBill());
    }

    private void calculateBill() {

        double qty = getNumber(quantity);
        double price = getNumber(rate);
        double gstPercent = getNumber(gst);
        double discountAmount = getNumber(discount);

        double subtotal = qty * price;
        double gstAmount = subtotal * gstPercent / 100;
        double total = subtotal + gstAmount - discountAmount;

        if (total < 0) {
            total = 0;
        }

        totalAmount.setText(
                String.format("Total: ₹%.2f", total)
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

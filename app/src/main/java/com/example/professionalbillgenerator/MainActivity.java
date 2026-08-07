package com.example.professionalbillgenerator;

import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {


    EditText businessName,businessPhone,businessAddress;
    EditText customerName,mobileNumber;
    EditText invoiceNumber,invoiceDate;
    EditText itemName,quantity,rate,gst,discount;

    TextView subtotalAmount,gstAmount,totalAmount;

    Button generateBill;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);


        businessName=findViewById(R.id.businessName);
        businessPhone=findViewById(R.id.businessPhone);
        businessAddress=findViewById(R.id.businessAddress);

        customerName=findViewById(R.id.customerName);
        mobileNumber=findViewById(R.id.mobileNumber);

        invoiceNumber=findViewById(R.id.invoiceNumber);
        invoiceDate=findViewById(R.id.invoiceDate);

        itemName=findViewById(R.id.itemName);
        quantity=findViewById(R.id.quantity);
        rate=findViewById(R.id.rate);

        gst=findViewById(R.id.gst);
        discount=findViewById(R.id.discount);


        subtotalAmount=findViewById(R.id.subtotalAmount);
        gstAmount=findViewById(R.id.gstAmount);
        totalAmount=findViewById(R.id.totalAmount);

        generateBill=findViewById(R.id.generateBill);



        invoiceDate.setText(
                new SimpleDateFormat(
                "dd-MM-yyyy",
                Locale.getDefault()
                ).format(new Date())
        );


        generateBill.setOnClickListener(v -> createBill());

    }



    double number(EditText e){

        try{

            return Double.parseDouble(
                    e.getText().toString()
            );

        }catch(Exception ex){

            return 0;

        }

    }




    void createBill(){


        double qty=number(quantity);

        double price=number(rate);

        double sub=qty*price;

        double gstValue=sub*number(gst)/100;

        double total=sub+gstValue-number(discount);



        subtotalAmount.setText(
                "Subtotal: ₹"+sub
        );


        gstAmount.setText(
                "GST: ₹"+gstValue
        );


        totalAmount.setText(
                "Grand Total: ₹"+total
        );


        createPDF(total,sub,gstValue);

    }




    void createPDF(double total,double sub,double gstValue){


        PdfDocument pdf=new PdfDocument();


        PdfDocument.PageInfo info=
                new PdfDocument.PageInfo.Builder(
                        595,842,1
                ).create();


        PdfDocument.Page page=
                pdf.startPage(info);


        Paint paint=new Paint();

        paint.setTextSize(18);


        int y=60;


        page.getCanvas().drawText(
                "PROFESSIONAL INVOICE",
                160,y,paint
        );


        y+=40;


        page.getCanvas().drawText(
                "Business: "+businessName.getText(),
                40,y,paint
        );


        y+=30;


        page.getCanvas().drawText(
                "Customer: "+customerName.getText(),
                40,y,paint
        );


        y+=30;


        page.getCanvas().drawText(
                "Item: "+itemName.getText(),
                40,y,paint
        );


        y+=30;


        page.getCanvas().drawText(
                "Subtotal: ₹"+sub,
                40,y,paint
        );


        y+=30;


        page.getCanvas().drawText(
                "GST: ₹"+gstValue,
                40,y,paint
        );


        y+=30;


        page.getCanvas().drawText(
                "TOTAL: ₹"+total,
                40,y,paint
        );



        pdf.finishPage(page);



        try{


            File folder=new File(
                    getExternalFilesDir(
                    Environment.DIRECTORY_DOCUMENTS),
                    "Bills"
            );


            if(!folder.exists())
                folder.mkdirs();



            File file=new File(
                    folder,
                    "Invoice.pdf"
            );


            FileOutputStream out=
                    new FileOutputStream(file);


            pdf.writeTo(out);


            out.close();

            pdf.close();



            Toast.makeText(
                    this,
                    "PDF Created",
                    Toast.LENGTH_LONG
            ).show();


            sharePDF(file);



        }catch(Exception e){

            Toast.makeText(
                    this,
                    e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();

        }


    }



    void sharePDF(File file){


        Uri uri= FileProvider.getUriForFile(
                this,
                getPackageName()+".provider",
                file
        );


        Intent intent=new Intent(
                Intent.ACTION_SEND
        );


        intent.setType(
                "application/pdf"
        );


        intent.putExtra(
                Intent.EXTRA_STREAM,
                uri
        );


        intent.addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION
        );


        startActivity(
                Intent.createChooser(
                        intent,
                        "Share Bill"
                )
        );

    }

}

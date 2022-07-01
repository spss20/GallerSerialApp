package com.ssoftwares.newgaller;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.ssoftwares.newgaller.views.MainActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

public class TestLayout extends AppCompatActivity {

    EditText editText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_layout);

        editText = findViewById(R.id.mac_address);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Toast.makeText(TestLayout.this, "", Toast.LENGTH_SHORT).show();
        }
        Button button = findViewById(R.id.submit_command);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              makeFile();
            }
        });
    }

    private void makeFile(){
        File newFile = new File(getExternalCacheDir() , "Galler");
        if (!newFile.exists()) {
            boolean abcd = newFile.mkdir();
            if (!abcd){
                Toast.makeText(this, "Cannot make Dir", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
                File file = new File(getExternalCacheDir() , "Galler/surya.pdf");
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed ot create file", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            File file = new File(getExternalCacheDir() , "Galler/surya.pdf");
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed ot create file", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void cookPdf() throws FileNotFoundException, DocumentException {


        PdfPTable table = new PdfPTable(new float[]{5,3});
        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
        table.getDefaultCell().setFixedHeight(50);
        table.setTotalWidth(PageSize.A4.getWidth());
        table.setWidthPercentage(100);
        table.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell("Command Name");
        table.addCell("Time");
        table.setHeaderRows(1);
        PdfPCell[] cells = table.getRow(0).getCells();
        cells[0].setBackgroundColor(BaseColor.GRAY);
        cells[1].setBackgroundColor(BaseColor.GRAY);

        for (int i = 0; i<20 ; i++){
            table.addCell("Command Surya Executed");
            table.addCell("28th Wednesday 2020");
        }

        File newFile = new File(Environment.getExternalStorageDirectory() + "/" + "surya.pdf");
        Document document = new Document();
        PdfWriter.getInstance(document , new FileOutputStream(newFile));
        document.open();
        document.setPageSize(PageSize.A4);
        document.addCreationDate();
        document.addAuthor("Sikarwar Softwares");
        document.addCreator("Surya Pratap");

        try {
            // get input stream
            InputStream ims = getAssets().open("logo.jpg");
            Bitmap bmp = BitmapFactory.decodeStream(ims);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
            Image image = Image.getInstance(stream.toByteArray());
            document.add(image);
        } catch (IOException e) {
            e.printStackTrace();
        }


        Font f = new Font(Font.FontFamily.TIMES_ROMAN , 20 , Font.UNDERLINE , BaseColor.BLUE);
        Font f1 = new Font(Font.FontFamily.TIMES_ROMAN , 16 , Font.NORMAL , BaseColor.BLACK);
        document.add(new Paragraph("Mac Address: AC AF 22 33 44 05" , f));
        String details = "IP Version: IPV4 \nIP Address: 190.168.0.1\n" +
                "DNS NEtwork 1: 192.168.40.52\n" +
                "DNS Network 2: 192.158.6.70\n" +
                "Subnet Mask: 255.255.255.255" ;
        Paragraph detailPara = new Paragraph(details , f1);
        detailPara.setPaddingTop(10);
        document.add(detailPara);

        table.setPaddingTop(50);
        document.add(table);
        document.close();


        Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri pdfUri = FileProvider.getUriForFile(this , getApplicationContext().getPackageName()
                + ".provider" , newFile);
        intent.setDataAndType(pdfUri , "application/pdf");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }
}

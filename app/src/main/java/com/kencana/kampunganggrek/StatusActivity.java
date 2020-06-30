package com.kencana.kampunganggrek;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.snackbar.Snackbar;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.kencana.kampunganggrek.Util.AppController;
import com.kencana.kampunganggrek.Util.DownloadTask;
import com.kencana.kampunganggrek.Util.ServerAPI;
import com.kosalgeek.android.photoutil.GalleryPhoto;
import com.kosalgeek.android.photoutil.ImageBase64;
import com.kosalgeek.android.photoutil.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.kencana.kampunganggrek.Util.ServerAPI.DOWNLOAD_NOTA;

public class StatusActivity extends AppCompatActivity {

    TextView status, tanggal, nota, username, total, totalBeli, totalOngkir;
    ImageView imgStatus, gambar;
    Button btnsimpan, btncetak, btngallery;
    ProgressDialog pd;
    DecimalFormat decimalFormat;

    GalleryPhoto mGalery;
    String encode_image = null;
    private final int TAG_GALLERY = 2222;
    String selected_photo = null;

    String date;

    public static final int progress_bar_type = 0;
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        status = (TextView) findViewById(R.id.text_status);
        tanggal = (TextView) findViewById(R.id.tanggal);
        nota = (TextView) findViewById(R.id.no_nota);
        username = (TextView) findViewById(R.id.user);

        totalBeli = (TextView) findViewById(R.id.total_beli);
        totalOngkir = (TextView) findViewById(R.id.total_ongkir);
        total = (TextView) findViewById(R.id.total_biaya);
        imgStatus = (ImageView) findViewById(R.id.img_status);
        gambar = (ImageView) findViewById(R.id.inp_gambar);
        btngallery = (Button) findViewById(R.id.btn_gallery);
        btncetak = (Button) findViewById(R.id.btn_cetak);
        btnsimpan = (Button) findViewById(R.id.btn_simpan);
        date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        decimalFormat = new DecimalFormat("#,##0.00");
        pd = new ProgressDialog(StatusActivity.this);
        mGalery = new GalleryPhoto(getApplicationContext());
        loadJson();

        btngallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                startActivityForResult(mGalery.openGalleryIntent(),TAG_GALLERY);
                ActivityCompat.requestPermissions(
                        StatusActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        TAG_GALLERY
                );
            }
        });
        btncetak.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String url = DOWNLOAD_NOTA+nota.getText().toString();
                new DownloadTask(StatusActivity.this, url, nota.getText().toString());
            }
        });
        btnsimpan.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                if (selected_photo != null) {
                    simpanData();
                }
                else {
                    Toast.makeText(getApplicationContext(),
                            "Upload bukti pembayaran terlebih dahulu",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }



    private void loadJson() {
        pd.setMessage("Loading");
        pd.setCancelable(false);
        pd.show();
        StringRequest sendData = new StringRequest(Request.Method.POST, ServerAPI.URL_DASHBOARD_JUAL, new Response.Listener<String>() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onResponse(String response) {
                pd.cancel();
                Log.d("JSON RESPONSE", "onResponse: "+response);
                try {
                    JSONObject data = new JSONObject(response);
                    username.setText(data.getString("username"));
                    tanggal.setText(data.getString("tanggal"));
                    nota.setText(data.getString("no_nota"));
                    totalBeli.setText("Rp. " + decimalFormat.format(data.getInt("pembelian")));
                    totalOngkir.setText("Rp. " + decimalFormat.format(data.getInt("ongkir")));
                    total.setText("Rp. " + decimalFormat.format(data.getInt("total_biaya")));
                    if (data.getString("status").equalsIgnoreCase("Sudah dibayar")) {
                        status.setText("Sudah Dibayar");

                        status.setTextColor(R.color.colorPrimary);

                        imgStatus.setImageResource(R.drawable.berhasil);

                        btngallery.setVisibility(View.GONE);

                        btnsimpan.setVisibility(View.GONE);

                        findViewById(R.id.rekening).setVisibility(View.GONE);
                    } else if (data.getString("status").equalsIgnoreCase("Belum dibayar")) {
                        status.setText("BelumDibayar");
                        status.setTextColor(Color.RED);

                        imgStatus.setImageResource(R.drawable.gagal);

                        btncetak.setVisibility(View.GONE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pd.cancel();
                Log.d("volley", "error : " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                Intent intent = getIntent();
                String no_nota = intent.getStringExtra("no_nota");
                map.put("no_nota", no_nota );
                return map;
            }
        };

        AppController.getInstance().addToRequestQueue(sendData, "json_obj_req");
    }

    private void simpanData() {
        pd.setMessage("Mengirim Data");
        pd.setCancelable(false);
        pd.show();

        try {
            Bitmap bitmap = ImageLoader.init().from(selected_photo)
                    .requestSize(1024,1024).getBitmap();
            encode_image = ImageBase64.encode(bitmap);

            Log.d("ENCODER", encode_image);
            StringRequest sendData = new StringRequest(Request.Method.POST, ServerAPI.URL_UPLOAD_GAMBAR,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response)
                        {
                            pd.cancel();
                            Log.d("TAG", "onResponse: "+response);
                            try {
                                JSONObject res = new JSONObject(response);
                                Toast.makeText(StatusActivity.this, "pesan : " +
                                        res.getString("message"), Toast.LENGTH_SHORT).show();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            Intent intent = new Intent(getApplicationContext(), StatusActivity.class);
                            intent.putExtra("no_nota", nota.getText().toString());
                            startActivity(intent);
                            finish();
                        }
                    },
                    new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    pd.cancel();
                    Toast.makeText(StatusActivity.this,
                            "pesan : Gagal Kirim Data", Toast.LENGTH_SHORT).show();
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> map = new HashMap<>();
                    map.put("no_nota", nota.getText().toString());
                    map.put("gambar", encode_image);
                    return map;
                }
            };


            AppController.getInstance().addToRequestQueue(sendData);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == TAG_GALLERY){
            if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //gallery intent
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, TAG_GALLERY);
            }
            else {
                Toast.makeText(this, "Tidak ada perizinan untuk mengakses gambar", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == TAG_GALLERY) {
                Uri uri_path = data.getData();
                mGalery.setPhotoUri(uri_path);
                String path = mGalery.getPath();

                selected_photo = path;
                try {
                    Bitmap bitmap;
//                    bitmap = ImageLoader.init().from(path).requestSize(512, 512).getBitmap();
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri_path);
                    gambar.setImageBitmap(bitmap);

                    Snackbar.make(findViewById(android.R.id.content), "Success Loader Image", Snackbar.LENGTH_SHORT).show();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();

                    Snackbar.make(findViewById(android.R.id.content), "Something Wrong", Snackbar.LENGTH_SHORT).show();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }


}

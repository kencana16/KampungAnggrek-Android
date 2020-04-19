package com.kencana.kampunganggrek;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.kencana.kampunganggrek.Util.ServerAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.kencana.kampunganggrek.LoginActivity.TAG_USERNAME;

public class MainActivity extends AppCompatActivity {

    Toast toast;
    String username;
    SharedPreferences sharedpreferences;

    androidx.appcompat.widget.Toolbar toolbar;
    ViewFlipper v_flipper;
    int images[] = {R.drawable.banner, R.drawable.banner1, R.drawable.banner2, R.drawable.banner3,  R.drawable.banner4, R.drawable.banner5};
    ProgressDialog pd;
    ArrayList<Produk> mItems = new ArrayList<>();
    private ProdukAdapter produkadapter;
    RecyclerView mRecyclerview;

    LinearLayout bottomSheetLayout;
    RelativeLayout colapseBottomSheet;
    BottomSheetBehavior bottomSheetBehavior;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedpreferences = getSharedPreferences(LoginActivity.my_shared_preferences, Context.MODE_PRIVATE);
        username = getIntent().getStringExtra(TAG_USERNAME);
        toast = Toast.makeText(getApplicationContext(), null,Toast.LENGTH_SHORT);

        pd = new ProgressDialog(MainActivity.this);

        toolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //membuat slideshow dengan viewfliper
        v_flipper = findViewById(R.id.v_flipper);
        for (int i =0; i<images.length; i++) fliverImages(images[i]);
        for (int image: images) fliverImages(image);

        mRecyclerview = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerview.setHasFixedSize(true);

        //mengambil data dari API
        loadJson();

        //set recycler view 2 kolom
        GridLayoutManager layoutManager=new GridLayoutManager(this,2);
        mRecyclerview.setLayoutManager(layoutManager);
        produkadapter = new ProdukAdapter(mItems); //memanggil adapter
        mRecyclerview.setAdapter(produkadapter);

        // get the bottom sheet view
        bottomSheetLayout = findViewById(R.id.bs_ll);
        colapseBottomSheet = findViewById(R.id.bs_colapse);
        // init the bottom sheet behavior
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout);
        //ketika bottomsheet di klik maka akan expand
        colapseBottomSheet.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                } else {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });
    }

    //fungsi utnuk slide show
    public  void  fliverImages(int images){
        ImageView imageView = new ImageView(this);
        imageView.setBackgroundResource(images);
        v_flipper.addView(imageView);
        v_flipper.setFlipInterval(5000);
        v_flipper.setAutoStart(true);
        v_flipper.setOutAnimation(this,android.R.anim.slide_out_right);
        v_flipper.setInAnimation(this,android.R.anim.slide_in_left);
    }

    //fungsi ambil data dari API
    private void loadJson(){
        pd.setMessage("Mengambil Data");
        pd.setCancelable(false);
        pd.show();
        JsonArrayRequest reqData = new JsonArrayRequest(Request.Method.POST, ServerAPI.URL_DATA,null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        pd.cancel();
                        Log.d("volley","response : " + response.toString());
                        for(int i = 0 ; i < response.length(); i++)
                        {
                            try {
                                JSONObject data = response.getJSONObject(i);
                                Produk md = new Produk();
                                md.setKode(data.getString("kd_barang"));
                                md.setNama(data.getString("nm_barang"));
                                md.setSatuan(data.getString("satuan"));
                                md.setDeskripsi(data.getString("deskripsi"));
                                md.setHarga(data.getInt("harga"));
                                md.setHarga_beli(data.getInt("harga_beli"));
                                md.setStok(data.getInt("stok"));
                                md.setStok_min(data.getInt("stok_min"));
                                md.setImg(data.getString("gambar"));
                                mItems.add(md);
                                ProdukAdapter.listBarangfull.add(md); //masukkan ke arraylist listBarangfull yang ada di ProdukAdapter
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        produkadapter.notifyDataSetChanged();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        pd.cancel();
                        Log.d("volley", "error : " + error.getMessage());
                    }
                });

        com.kencana.kampunganggrek.Util.AppController.getInstance().addToRequestQueue(reqData);
    }

    //fungsi untuk tombol menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_call_center:
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:085155057752"));
                startActivity(intent);
                return true;
            case R.id.action_sms_center:
                String number = "085155057752";
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", number,null)));
                return true;
            case R.id.action_maps:
                Uri gmmIntentUri = Uri.parse("geo:-7.079667,110.329499?q=-7.079667,110.329499");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);
                }
                return true;
            case R.id.action_profile:
                Toast.makeText(this,"anda menekan profile", Toast.LENGTH_LONG).show();
                return true;
            case R.id.action_logout:
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(LoginActivity.session_status, false);
                editor.putString(TAG_USERNAME, null);
                editor.commit();
                finish();
                startActivity(new Intent(MainActivity.this,LoginActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //tombol search di action bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);

        androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                produkadapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                produkadapter.getFilter().filter(newText);
                return false;
            }
        });

        return true;
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent event){
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            //menutup bottom sheet ketika tekan di luar bottomsheet
            if (bottomSheetBehavior.getState()==BottomSheetBehavior.STATE_EXPANDED) {

                Rect outRect = new Rect();
                bottomSheetLayout.getGlobalVisibleRect(outRect);

                if(!outRect.contains((int)event.getRawX(), (int)event.getRawY()))
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        }
        return super.dispatchTouchEvent(event);
    }
}

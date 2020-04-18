package com.kencana.kampunganggrek;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toolbar;
import android.widget.ViewFlipper;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.kencana.kampunganggrek.Util.ServerAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    androidx.appcompat.widget.Toolbar toolbar;
    ViewFlipper v_flipper;
    int images[] = {R.drawable.banner, R.drawable.banner1, R.drawable.banner2, R.drawable.banner3,  R.drawable.banner4, R.drawable.banner5};
    ProgressDialog pd;
    ArrayList<Produk> mItems = new ArrayList<>();
    private ProdukAdapter produkadapter;
    RecyclerView.Adapter mAdapter;
    RecyclerView mRecyclerview;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pd = new ProgressDialog(MainActivity.this);

        toolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //membuat slideshow dengan viewfliper
        v_flipper = findViewById(R.id.v_flipper);
        for (int i =0; i<images.length; i++) fliverImages(images[i]);
        for (int image: images) fliverImages(image);

        mRecyclerview = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerview.setHasFixedSize(true);

        loadJson();

        GridLayoutManager layoutManager=new GridLayoutManager(this,2);
        mRecyclerview.setLayoutManager(layoutManager);
        produkadapter = new ProdukAdapter(mItems,this);
        mRecyclerview.setAdapter(produkadapter);

    }

    public  void  fliverImages(int images){
        ImageView imageView = new ImageView(this);
        imageView.setBackgroundResource(images);
        v_flipper.addView(imageView);
        v_flipper.setFlipInterval(5000);
        v_flipper.setAutoStart(true);
        v_flipper.setOutAnimation(this,android.R.anim.slide_out_right);
        v_flipper.setInAnimation(this,android.R.anim.slide_in_left);
    }

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
                                Produk md = new Produk(
                                        data.getString("kd_barang"),
                                        data.getString("nm_barang"),
                                        data.getString("satuan"),
                                        data.getString("deskripsi"),
                                        data.getString("harga"),
                                        data.getString("harga_beli"),
                                        data.getString("stok"),
                                        data.getString("stok_min"),
                                        data.getString("gambar")
                                );
                                mItems.add(md);
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
}

package com.kencana.kampunganggrek;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.JsonParser;
import com.kencana.kampunganggrek.Util.AppController;
import com.kencana.kampunganggrek.Util.ServerAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.kencana.kampunganggrek.LoginActivity.TAG_USERNAME;

public class HistoryActivity extends AppCompatActivity implements HistoryAdapter.ItemClickListener {
    SharedPreferences sharedpreferences;
    String kd_konsumen;
    ProgressDialog pd;
    public static ArrayList<Pembelian> listPembelian = new ArrayList<>();
    private RecyclerView historyRecycler;
    private HistoryAdapter historyAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        sharedpreferences = getSharedPreferences(LoginActivity.my_shared_preferences, Context.MODE_PRIVATE);
        kd_konsumen = sharedpreferences.getString("kode_konsumen", null);
        pd = new ProgressDialog(HistoryActivity.this);

        historyRecycler = (RecyclerView) findViewById(R.id.rv_pembelian);
        historyRecycler.setHasFixedSize(true);
        loadJson();
        historyRecycler.setLayoutManager(new LinearLayoutManager(this));
        historyAdapter = new HistoryAdapter(listPembelian,HistoryActivity.this);
        historyAdapter.setClickListener(this);
        historyRecycler.setAdapter(historyAdapter);

    }

    private void loadJson() {

        pd.setMessage("Loading");
        pd.setCancelable(false);
        pd.show();
        StringRequest sendData = new StringRequest(Request.Method.POST, ServerAPI.URL_HISTORY, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                pd.cancel();
                try {
                    JSONArray array = new JSONArray(response);
                    for(int i = 0 ; i < array.length(); i++)
                    {
                        try {
                            JSONObject data = array.getJSONObject(i);
                            String no_nota = data.getString("no_nota");
                            String due_date = data.getString("due_date");
                            String status = data.getString("status");
                            double total_biaya = data.getDouble("total_biaya");
                            Pembelian md = new Pembelian(no_nota, due_date, status, total_biaya);
                            listPembelian.add(md);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    historyAdapter.notifyDataSetChanged();
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
                map.put("kd_kons", kd_konsumen );
                return map;
            }
        };

        AppController.getInstance().addToRequestQueue(sendData, "json_obj_req");
    }

    public void onClick(View view, int position) {
        final Pembelian pembelian = listPembelian.get(position);
        switch (view.getId()) {
            default:
                Intent intent = new Intent(HistoryActivity.this, StatusActivity.class);
                intent.putExtra("no_nota", pembelian.getNo_nota());
                startActivity(intent);
                break;
        }
    }
}

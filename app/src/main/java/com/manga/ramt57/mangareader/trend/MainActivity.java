package com.manga.ramt57.mangareader.trend;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.manga.ramt57.mangareader.trend.adapter.RecyclerAdapter;
import com.manga.ramt57.mangareader.trend.pojomodels.Mangabasemodel;
import com.manga.ramt57.mangareader.trend.pojomodels.Mangalist;
import com.manga.ramt57.mangareader.trend.pojomodels.SearchPojo;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    String url = "https://www.mangaeden.com/api/list/0/";
    ArrayList<Mangalist> mangalist = new ArrayList<>();
    static ArrayList<Mangalist> listmanga = new ArrayList<>();
    ProgressDialog gress;
    RecyclerAdapter adapter;
    MaterialSearchView searchView;
    RelativeLayout previous,next;
    ArrayList<Mangalist> category=new ArrayList<>();
    SharedPreferences categoryPrefrences;
    SharedPreferences.Editor editor;
   static ArrayList<SearchPojo> searchList=new ArrayList<>();
    int page=1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        searchvolleyrequest();
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        gress = new ProgressDialog(this);
        gress.setIndeterminate(true);
        volleyrequest(0);
        gress.setMessage("Fetching Manga..");
        gress.show();
        categoryPrefrences=getSharedPreferences("CATEGORY",MODE_PRIVATE);
        editor=categoryPrefrences.edit();
        final List<String> g = new ArrayList<String>();
        adapter = new RecyclerAdapter();
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));
        recyclerView.setAdapter(adapter);
        searchView = (MaterialSearchView) findViewById(R.id.search_view);
        previous=(RelativeLayout)findViewById(R.id.previous);
        next=(RelativeLayout)findViewById(R.id.next);
        previous.setVisibility(View.GONE);
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gress.show();
                if(page<=34&&page>0){
                    volleyrequest(--page);
                }
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gress.show();
                if(page<34&&page>=0){
                    volleyrequest(++page);
                }
            }
        });
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

    }

    private void sortlist(List<String> g) {
        String arrays[] = new String[g.size()];
                    arrays = g.toArray(arrays);
                    searchView.setSuggestions(arrays);
//        for (SearchPojo pojo:searchList){
//            if(pojo.getTitle()!=null){
//                if(g.contains(pojo.getTitle())){
//                    Log.d("sap",pojo.getTitle());
//                }
//            }
//        }
    }

    private void volleyrequest(final int  i) {
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url+"?p="+i, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        if(i==0){

                            if(previous.getVisibility()==View.VISIBLE){
                                previous.setVisibility(View.GONE);
                            }
                        }
                        if(i==34){
                            if(next.getVisibility()==View.VISIBLE){
                                next.setVisibility(View.GONE);
                            }
                        }else{
                            if(next.getVisibility()==View.GONE){
                                next.setVisibility(View.VISIBLE);
                            }

                        }
                        if(i==0){
                            if(previous.getVisibility()==View.VISIBLE){
                                previous.setVisibility(View.GONE);
                            }

                        }else{
                            if(previous.getVisibility()==View.GONE){
                                previous.setVisibility(View.VISIBLE);
                            }

                        }
                        Gson gson = new Gson();
                        Mangabasemodel people;
                        people = gson.fromJson(response.toString(), Mangabasemodel.class);
                        mangalist = people.getManga();
                        listmanga=people.getManga();
                        showcategoryData();
                        gress.dismiss();
                        
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        View view=findViewById(R.id.cordinate);
                        Log.d("TAG",error.toString());
                        Snackbar.make(view,"Check internet connection or refresh",Snackbar.LENGTH_LONG).setAction("RETRY", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                volleyrequest(0);
                            }
                        }).show();
                        gress.dismiss();
                    }
                });
        MySingleton.getInstance(this).addToRequestQueue(jsObjRequest);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MaterialSearchView.REQUEST_VOICE && resultCode == RESULT_OK) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches != null && matches.size() > 0) {
                String searchWrd = matches.get(0);
                if (!TextUtils.isEmpty(searchWrd)) {
                    searchView.setQuery(searchWrd, false);
                }
            }

            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if (searchView.isSearchOpen()) {
            searchView.closeSearch();
        } else {
            super.onBackPressed();
        }
    }


    private void searchvolleyrequest() {
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        InputStream inputStream=new ByteArrayInputStream(response.toString().getBytes());
                        try {
                            Reader streamReader=new InputStreamReader(inputStream,"UTF-8");
                            JsonReader reader = new JsonReader(streamReader);
                            reader.beginObject();
                            while (reader.hasNext()) {
                                String name = reader.nextName();
                                if (name.equals("manga")) {
                                    reader.beginArray();
                                    while (reader.hasNext()) {
                                        SearchPojo pojo=new SearchPojo();
                                        reader.beginObject();
                                        while (reader.hasNext()) {
                                            String title = reader.nextName();
                                            if (title.equals("t")) {
                                                String arc=reader.nextString();
                                                pojo.setTitle(arc);
                                            }else if(title.equals("i")){
                                                String id=reader.nextString();
                                                pojo.setId(id);
                                            }else if(title.equals("im")&&reader.peek()!= JsonToken.NULL) {
                                                String img=reader.nextString();
                                                pojo.setImg(img);
                                            } else {
                                                reader.skipValue();
                                            }
                                        }
                                        reader.endObject();
                                        if(pojo.getTitle()!=null){
                                            searchList.add(pojo);
                                        }
                                    }
                                    reader.endArray();
                                }else {
                                    reader.skipValue(); // avoid some unhandle events
                                }
                            }
                            reader.endObject();
                            reader.close();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        Log.d("TAG1",error.toString());
                        Toast.makeText(MainActivity.this, "error " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        gress.dismiss();
                    }
                });
        MySingleton.getInstance(this).addToRequestQueue(jsObjRequest);
    }
    public void showcategoryData(){
        String cat=categoryPrefrences.getString("CAT","All");
        if(cat.equals("All")){
            adapter.setData(listmanga);
            adapter.notifyDataSetChanged();
        }else{
            ArrayList<Mangalist> news=new ArrayList<>();
            for (Mangalist list:mangalist){
                for(String s:list.getC()){
                    if(s.equals(cat)){
                        news.add(list);
                    }
                }
            }
            adapter.setData(news);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}

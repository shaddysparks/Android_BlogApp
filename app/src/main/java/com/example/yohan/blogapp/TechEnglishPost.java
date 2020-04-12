package com.example.yohan.blogapp;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TechEnglishPost extends AppCompatActivity implements RecentPostAdapter.onItemClicked {

    private Toolbar mToolbar;
    private RecyclerView TechEnglishrecyclerView;
    private LinearLayoutManager linearLayoutManager;
    private ArrayList<RecentModel> list;
    private RecentPostAdapter recentPostAdapter;
    // ProgressDialog progressDialog;
    ProgressDialog progressDialog1;
    Dialog MyDialog1;


    private String TechEnglishBaseURL = "https://readhub.lk/wp-json/wp/v2/";
    public static final String RENDER_CONTENT = "RENDER";
    public  static final String link = "link";
    private String url;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    ValueEventListener valueEventListener;
    OkHttpClient okHttpClient;
    sharedPref sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPreferences = new sharedPref(this);
        if (sharedPreferences.loadNightModeState() == true){
            setTheme(R.style.darkTheme);
        }else
            setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tech_english_post);

        mToolbar = findViewById(R.id.TechEnglishPost_app_bar);
        setSupportActionBar(mToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("ReadHub - Tech News");
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        TechEnglishrecyclerView = findViewById(R.id.TechEnglish_recycleview);
        progressDialog1 = new ProgressDialog(TechEnglishPost.this);
        progressDialog1.setTitle("Tech English Posts");
        progressDialog1.setMessage("Loading");
        progressDialog1.show();

        mAuth = FirebaseAuth.getInstance();

        String userId = mAuth.getUid();

        mDatabase = FirebaseDatabase.getInstance().getReference("Articles").child(userId).child("Tech Eng Articles");
        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(120, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .build();


        linearLayoutManager = new LinearLayoutManager(TechEnglishPost.this,LinearLayoutManager.VERTICAL,false);
        TechEnglishrecyclerView.setLayoutManager(linearLayoutManager);
        list = new ArrayList<RecentModel>();

        if(haveNetwork(getApplicationContext())){

            mDatabase.removeValue();
            new GetTechEnglishJson().execute();

            list.clear();
            InitListner();
            mDatabase.addValueEventListener(valueEventListener);


        }else {
            new GetTechEnglishJson().execute();

            list.clear();
            InitListner();
            mDatabase.addValueEventListener(valueEventListener);
            progressDialog1.dismiss();
            mDatabase.keepSynced(true);
        }

    }

    public class GetTechEnglishJson extends AsyncTask<Void,Void,Void> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
//            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(TechEnglishBaseURL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            RetrofitArrayAPI retrofitArrayAPI = retrofit. create(RetrofitArrayAPI.class);

            Call<List<WPPost>> call = retrofitArrayAPI.getEngTechNewsPost();


            call.enqueue(new Callback<List<WPPost>>() {
                @Override
                public void onResponse(Call<List<WPPost>> call, Response<List<WPPost>> response) {

                    for (int i =0;i<response.body().size(); i++){

                        String temdetails = response.body().get(i).getDate();
                        String titile = response.body().get(i).getTitle().getRendered().toString();
                        titile = titile.replace("&#8211;","");
                        titile = titile.replace("&#x200d;","");
                        titile = titile.replace("&#8230;","");
                        titile = titile.replace("&amp;","");
                        titile = titile.replace("&#8220;","");
                        titile = titile.replace("&#8221;","");
                        String render = response.body().get(i).getContent().getRendered();
                        /// render = render.replace("--aspect-ratio","aspect-ratio");

                        // String profileUrl = response.body().get(i).getLinks().getAuthor().get(0).getHref();


                        Model model = new Model( titile,
                                temdetails,
                                response.body().get(i).getEmbedded().getWpFeaturedmedia().get(0).getSourceUrl(),render,RecentModel.IMAGE_TYPE,response.body().get(i).getEmbedded().getAuthor().get(0).getName(),response.body().get(i).getLink());

                        mDatabase.push().setValue(model).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){


                                    progressDialog1.dismiss();
                                }else {

                                    progressDialog1.dismiss();

                                }

                            }
                        });
                    }


                }

                @Override
                public void onFailure(Call<List<WPPost>> call, Throwable t) {

                }
            });

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
//            progressDialog.dismiss();
        }
    }

    public class GetTechEnglishJson1 extends AsyncTask<Void,Void,Void> {

        ProgressDialog progressDialog;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
//            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(TechEnglishBaseURL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            RetrofitArrayAPI retrofitArrayAPI = retrofit. create(RetrofitArrayAPI.class);

            Call<List<WPPost>> call = retrofitArrayAPI.getEngTechNewsPost();


            call.enqueue(new Callback<List<WPPost>>() {
                @Override
                public void onResponse(Call<List<WPPost>> call, Response<List<WPPost>> response) {

                    progressDialog1.dismiss();
                    for (int i =0;i<response.body().size(); i++){

                        String temdetails = response.body().get(i).getDate();
                        String titile = response.body().get(i).getTitle().getRendered().toString();
                        titile = titile.replace("&#8211;","");
                        titile = titile.replace("&#x200d;","");
                        titile = titile.replace("&#8230;","");
                        titile = titile.replace("&amp;","");
                        titile = titile.replace("&#8220;","");
                        titile = titile.replace("&#8221;","");
                        String render = response.body().get(i).getContent().getRendered();
                        /// render = render.replace("--aspect-ratio","aspect-ratio");

                        // String profileUrl = response.body().get(i).getLinks().getAuthor().get(0).getHref();


                        list.add(new RecentModel( titile,
                                temdetails,
                                response.body().get(i).getEmbedded().getWpFeaturedmedia().get(0).getSourceUrl(),render,RecentModel.IMAGE_TYPE,response.body().get(i).getEmbedded().getAuthor().get(0).getName(),response.body().get(i).getLink()));

                    }

                    recentPostAdapter.notifyDataSetChanged();
                }

                @Override
                public void onFailure(Call<List<WPPost>> call, Throwable t) {

                }
            });

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
//            progressDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mDatabase != null){
            mDatabase.removeEventListener(valueEventListener);
            valueEventListener = null;
            //FirebaseDatabase.getInstance().goOffline();
        }
    }

    private void InitListner(){
      valueEventListener =new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

              list.clear();
              for (DataSnapshot data : dataSnapshot.getChildren()){

                  RecentModel model = data.getValue(RecentModel.class);
                  list.add(model);
              }

              recentPostAdapter = new RecentPostAdapter(list,getApplicationContext());
              TechEnglishrecyclerView.setAdapter(recentPostAdapter);
              recentPostAdapter.SetOnItemClickListener(TechEnglishPost.this);

              // progressDialog1.dismiss();

          }

          @Override
          public void onCancelled(@NonNull DatabaseError databaseError) {

              recentPostAdapter = new RecentPostAdapter(list,getApplicationContext());
              new GetTechEnglishJson1().execute();
              TechEnglishrecyclerView.setAdapter(recentPostAdapter);
              recentPostAdapter.SetOnItemClickListener(TechEnglishPost.this);
          }

      };
    }

    @Override
    public void OnItemClick(int index) {
        Intent i = new Intent(this,RecentPostView.class);
        RecentModel model = list.get(index);
        i.putExtra(RENDER_CONTENT,model.render);
        i.putExtra(link,model.link);
        startActivity(i);

    }


    private void updateUI(){
        Intent startIntent = new Intent(TechEnglishPost.this,Login.class);
        startActivity(startIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId() == R.id.main_logout){
            openDialog();
        }
        return true;
    }

    public void openDialog(){

        logoutDialog logoutdialog = new logoutDialog();
        logoutdialog.show(getSupportFragmentManager(),"Logoutdialog");

    }

    public void connectionDialog1(){
        MyDialog1 = new Dialog(TechEnglishPost.this);
        MyDialog1.setContentView(R.layout.customconnectiondialog);
        MyDialog1.setTitle("Error");
        MyDialog1.show();
    }

    private boolean haveNetwork(Context context){
        boolean have_WIFI = false;
        boolean have_MobileData = false;

        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        // NetworkInfo [] networkInfos = connectivityManager.getAllNetworkInfo();
        if (connectivityManager != null)
        {
            NetworkInfo[] info = connectivityManager.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED)
                    {
                        return true;
                    }
        }
        return false;
    }
}

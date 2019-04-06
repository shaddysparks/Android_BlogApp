package com.example.yohan.blogapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GitPosts extends AppCompatActivity implements RecentPostAdapter.onItemClicked {

    private Toolbar mToolbar;
    private RecyclerView GitrecyclerView;
    private LinearLayoutManager linearLayoutManager;
    private ArrayList<RecentModel> list;
    private RecentPostAdapter recentPostAdapter;

    private String GitBaseURL = "https://readhublk.com/wp-json/wp/v2/";
    public static final String RENDER_CONTENT = "RENDER";
    public  static final String title = "render";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_git_posts);

        mToolbar = findViewById(R.id.GitPost_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("ReadHub - Git ");

        GitrecyclerView = findViewById(R.id.Git_recycleview);

        linearLayoutManager = new LinearLayoutManager(GitPosts.this,LinearLayoutManager.VERTICAL,false);
        GitrecyclerView.setLayoutManager(linearLayoutManager);

        list = new ArrayList<>();

        recentPostAdapter = new RecentPostAdapter(list,this);

        new GetGitJson().execute();
        GitrecyclerView.setAdapter(recentPostAdapter);

        recentPostAdapter.SetOnItemClickListener(GitPosts.this);
    }

    public class GetGitJson extends AsyncTask<Void,Void,Void> {

        ProgressDialog progressDialog;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(GitPosts.this);
            progressDialog.setTitle("Git Post");
            progressDialog.setMessage("Loading");
            progressDialog.show();



        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(GitBaseURL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            RetrofitArrayAPI retrofitArrayAPI = retrofit. create(RetrofitArrayAPI.class);

            Call<List<WPJavaPost>> call = retrofitArrayAPI.getGitPost();


            call.enqueue(new Callback<List<WPJavaPost>>() {
                @Override
                public void onResponse(Call<List<WPJavaPost>> call, Response<List<WPJavaPost>> response) {
                    Toast.makeText(GitPosts.this,"done",Toast.LENGTH_LONG).show();


                    for (int i =0;i<response.body().size(); i++){

                        String temdetails = response.body().get(i).getDate();
                        String titile = response.body().get(i).getTitle().getRendered().toString();
                        titile = titile.replace("&#8211;","");
                        String render = response.body().get(i).getContent().getRendered();
                        /// render = render.replace("--aspect-ratio","aspect-ratio");

                        // String profileUrl = response.body().get(i).getLinks().getAuthor().get(0).getHref();

                        list.add(new RecentModel( titile,
                                temdetails,
                                response.body().get(i).getBetterFeaturedImage().getMediaDetails().getSizes().getThumbnail().getSourceUrl(),render,RecentModel.IMAGE_TYPE));

                    }

                    recentPostAdapter.notifyDataSetChanged();
                }

                @Override
                public void onFailure(Call<List<WPJavaPost>> call, Throwable t) {

                }
            });

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();
        }
    }


    @Override
    public void OnItemClick(int index) {
        Intent i = new Intent(this,RecentPostView.class);
        RecentModel model = list.get(index);
        i.putExtra(RENDER_CONTENT,model.render);
        // i.putExtra(title,model.title);
        startActivity(i);
    }
}
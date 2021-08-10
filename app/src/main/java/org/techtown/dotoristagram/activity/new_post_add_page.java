package org.techtown.dotoristagram.activity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.techtown.dotoristagram.R;
import org.techtown.dotoristagram.retrofit.RetrofitAPI;
import org.techtown.dotoristagram.util.imageEditItem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import static org.techtown.dotoristagram.activity.image_show_activity.imageList;

public class new_post_add_page extends AppCompatActivity {


    //레트로핏 객체
    Gson gson  = new GsonBuilder().setLenient().create();
    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("http://54.248.192.133")
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build();


    EditText contents;

    //상단 액션 바에 메뉴 생성
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.complete_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //uri 경로 해당 이미지의 실경로를 받아오기
    public String getPathFromUri(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToNext();
        String path = cursor.getString(cursor.getColumnIndex("_data"));
        return path;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        //상단 완료 눌렀을때
        if (id == R.id.action_complete) {
            //게시물 추가시 글자와 사진이 서버에 올라간다.


            RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);
            ArrayList<MultipartBody.Part> bodyList = new ArrayList<>();

            for(int i=0; i<imageList.size(); i++){
                File file = new File(getPathFromUri(imageList.get(i).getImage_uri()));  //uri를 경로 형태로 변환해줘서 파일객체로 만듦
                RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
                MultipartBody.Part body = MultipartBody.Part.createFormData("uploaded_file[]", file.getName(), requestFile);
                bodyList.add(body); //리스트에 body 추가해주기
            }

            Call<String> comment = retrofitAPI.posting(bodyList,login.user_id,contents.getText().toString());   //사진파일들, 아이디, 게시글내용 보내주기
            comment.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    Log.d("프사 결과값",response.body());

                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {

                }
            });


            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_post_add_page);

        ImageView imagePreview = findViewById(R.id.imagePreview);
        imagePreview.setImageURI(imageList.get(0).getImage_uri());






        ActionBar actionBar = getSupportActionBar();  //제목줄 객체 얻어오기
        actionBar.setTitle("새 게시물 추가");  //액션바 제목설정
        actionBar.setDisplayHomeAsUpEnabled(true);   //업버튼 <- 만들기
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#dadafc")));//연보라색으로 만들기


        contents = findViewById(R.id.contents); //내용 받아오기


    }
}

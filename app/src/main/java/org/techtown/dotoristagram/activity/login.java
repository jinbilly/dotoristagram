package org.techtown.dotoristagram.activity;

import android.content.Intent;
import android.icu.text.LocaleDisplayNames;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.techtown.dotoristagram.R;
import org.techtown.dotoristagram.retrofit.RetrofitAPI;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class login extends AppCompatActivity {

    public static String user_id;  //유저의 id를 static으로 만들어 어디서든 쓸 수 있게 만들기

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);


        EditText id = findViewById(R.id.id);
        EditText pw = findViewById(R.id.pw);
        Button login = findViewById(R.id.login);
        Button register = findViewById(R.id.register);


        Gson gson  = new GsonBuilder().setLenient().create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://54.248.192.133")
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();


        //로그인 버튼을 눌렀을때
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);
                Call<String> comment = retrofitAPI.loginAction(id.getText().toString(), pw.getText().toString());   //아이디 비밀번호 받아서 보내주기

                comment.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        String answer = response.body();
                        Log.d("성공시", answer);

                        if(answer.equals("로그인 성공")){
                            user_id = id.getText().toString(); //로그인 하게 되면 id넣기
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                            finish();
                        }else {
                            Toast.makeText(getApplicationContext(),"아이디나 비밀번호가 잘못되었습니다.",Toast.LENGTH_LONG).show();
                        }

                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        Log.d("실패", "낑");


                    }
                });

            }
        });

        //회원가입 버튼을 눌렀을때
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), register.class);
                startActivity(intent);
                finish();
            }
        });






    }
}

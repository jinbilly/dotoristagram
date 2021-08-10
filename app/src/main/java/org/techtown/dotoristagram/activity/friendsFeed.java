package org.techtown.dotoristagram.activity;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import org.techtown.dotoristagram.R;

public class friendsFeed extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //상태바 숨기기 코드(setcontentview 보다 먼저 선언해줘야함)
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //getWindow().setStatusBarColor(Color.YELLOW);

        setContentView(R.layout.friends_feed);

        ActionBar actionBar = getSupportActionBar();  //제목줄 객체 얻어오기
        actionBar.setTitle("유저 닉네임");  //액션바 제목설정
        actionBar.setDisplayHomeAsUpEnabled(true);   //업버튼 <- 만들기
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#dadafc")));//연보라색으로 만들기


    }
}

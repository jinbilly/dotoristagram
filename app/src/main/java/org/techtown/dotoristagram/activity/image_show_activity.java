package org.techtown.dotoristagram.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.techtown.dotoristagram.R;
import org.techtown.dotoristagram.fragment.Frag1;
import org.techtown.dotoristagram.util.OnItemClickListener;
import org.techtown.dotoristagram.util.imageEditItem;
import org.techtown.dotoristagram.util.image_show_adapter;

import java.util.ArrayList;

import static org.techtown.dotoristagram.activity.image_edit_activity.where;

public class image_show_activity extends AppCompatActivity {

    //리사이클러 뷰 관련 변수선언
    public static ArrayList<imageEditItem> imageList;

    //리사이클러 뷰 관련 변수선언
    public static ArrayList<Uri> image_uri;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private image_show_adapter image_show_adapter;

    private int positions;//리사이클러뷰 포지션

    //수정된 사진의 uri값
    public static Uri edit_uri = null;

    //상단 액션 바에 메뉴 생성
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.complete_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        //상단 완료 눌렀을때
        if (id == R.id.action_complete) {

            //새 게시물 추가 페이지에 들어가게 되며, 인텐트로 그 액티비티가 켜진다
            Intent intent = new Intent(getApplicationContext(), new_post_add_page.class); //수정 할 수 있는 페이지 열기
            startActivity(intent);
            return true;
        }
            return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(edit_uri!=null){
            Log.d("edit_uri2", "" + edit_uri);
            imageList.set(positions,new imageEditItem(edit_uri));    // 클릭한 리사이클러뷰 항목을 변경해주기
            image_show_adapter.notifyDataSetChanged();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_show_activity);


        ActionBar actionBar = getSupportActionBar();  //제목줄 객체 얻어오기
        actionBar.setTitle("사진 편집하기");  //액션바 제목설정
        actionBar.setDisplayHomeAsUpEnabled(true);   //업버튼 <- 만들기
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#dadafc")));//연보라색으로 만들기

        if(imageList == null){
            imageList = new ArrayList<>();
        }

        //리사이클러뷰 만들기
        recyclerView = findViewById(R.id.recyclerView_imageShow);
        //가로로 만들기
        linearLayoutManager = new LinearLayoutManager(getApplicationContext(), linearLayoutManager.HORIZONTAL, false);
/*
        //reverse시켜서 출력하기
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);//뒤에서부터 보여주기
        recyclerView.setLayoutManager(linearLayoutManager); //layoutmanager 등록
*/

        for (Uri uri: image_uri){
            //이미지 추가해주기
            imageEditItem itemClass = new imageEditItem(uri);
            imageList.add(itemClass);

            Log.d("이미지리스트", "" + imageList.get(0).getImage_uri());
        }



        //어뎁터생성
        image_show_adapter = new image_show_adapter(imageList);
        recyclerView.setAdapter(image_show_adapter);
        recyclerView.setLayoutManager(linearLayoutManager);

        //image_show_adapter.notifyDataSetChanged();


        image_show_adapter.setOnItemClicklistener(new OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerView.ViewHolder holder, View view, int position) {
                edit_uri=null;
                positions = position;
                Intent intent = new Intent(getApplicationContext(), image_edit_activity.class); //수정 할 수 있는 페이지 열기
                intent.putExtra("Uri", imageList.get(position).getImage_uri().toString());   //날짜 text값 넘겨주기
                where = "게시물";
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(RecyclerView.ViewHolder holder, View view, int position) {

            }
        });












    }
}
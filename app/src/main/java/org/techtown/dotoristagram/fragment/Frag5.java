package org.techtown.dotoristagram.fragment;

import android.Manifest;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import org.techtown.dotoristagram.R;
import org.techtown.dotoristagram.activity.MainActivity;
import org.techtown.dotoristagram.activity.image_show_activity;
import org.techtown.dotoristagram.activity.image_edit_activity;
import org.techtown.dotoristagram.activity.login;
import org.techtown.dotoristagram.activity.profile_edit;
import org.techtown.dotoristagram.activity.test;
import org.techtown.dotoristagram.retrofit.RetrofitAPI;
import org.techtown.dotoristagram.util.MediaScanner;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import static android.app.Activity.RESULT_OK;
import static android.os.Environment.DIRECTORY_PICTURES;
import static android.os.Environment.getExternalStorageState;
import static org.techtown.dotoristagram.activity.image_edit_activity.where;

public class Frag5 extends Fragment {


    private View view;
    private boolean permission = false;//권한 체크 여부 확인

    //갤러리 접근 관련 변수
    private final int GET_GALLERY_IMAGE = 200;

    //camera 관련 변수 선언
    private static final int REQUEST_IMAGE_CAPTURE = 672, pick_from_Multi_album=11;
    private String imageFilePath;   //이미지 파일 경로
    private Uri photoUri;

    //사진 저장 시 갤러리 폴더에 바로 반영사항을 업데이트 시켜주려면 필요함(미디어 스캐닝)
    private MediaScanner mMediaScanner;

    //갤러리에서 사진 가져왔을때 uri경로 변수
    Uri selectedImageUri;

    String imagePath1;

    ImageView imageView;
    TextView userId;
    TextView test;

    Button btn_post;//게시물 추가 버튼

    //레트로핏 객체
    Gson gson  = new GsonBuilder().setLenient().create();
    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("http://54.248.192.133")
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag5, container, false);
        userId = view.findViewById(R.id.userId);    //프사 위에 보이는 아이디 부분
        test = view.findViewById(R.id.textView11); //테스트 위해서 만들었음, 지우자
        imageView = view.findViewById(R.id.imageView);


        userId.setText(login.user_id);   //id 설정해주기기
        //Log.d("왜안나옴 아이디?",MainActivity.user_id);

        btn_post = view.findViewById(R.id.btn_post);


        btn_post.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {


                Intent intent = new Intent(getActivity(),test.class);
                startActivity(intent);

                /*
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
                intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, pick_from_Multi_album);

                */

            }
        });


        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*
                final Dialog dialog = new Dialog(getActivity());
                // 아래 코드를 꼭 적어준다
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.show();*/


                checkPermissions(); //권한을 물어보기




            }
        });

        //사진 저장 후 미디어 스캐닝을 돌려줘야 갤러리에 반영됨
        mMediaScanner = MediaScanner.getInstance(getActivity());


        return view;
    }




    public void showDialog() {

        final List<String> ListItems = new ArrayList<>();
        ListItems.add("앨범에서 사진 선택");
        ListItems.add("카메라로 사진 찍기");

        final String[] items = ListItems.toArray(new String[ListItems.size()]);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setItems(items, (dialog, pos) -> {
            String selectedText = items[pos];
            if (selectedText.equals("앨범에서 사진 선택")) {
                // "리스트 다이얼로그 메뉴-1번" 클릭 시 동작할 코드를 작성하면 됩니다.
                if (!permission)
                    return;

                Intent intent = new Intent(Intent.ACTION_PICK);     //암시적 인텐트 사용(사진첩 접근)
                intent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(Intent.createChooser(intent, "사진첩을 고르세요"), GET_GALLERY_IMAGE);



            } else if (selectedText.equals("카메라로 사진 찍기")) {
                // "리스트 다이얼로그 메뉴-2번" 클릭 시 동작할 코드를 작성하면 됩니다.
                if (!permission)
                    return;

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);    //암시적 인텐트 사용(카메라 사용)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){//안드11부터는 정책상 기본캠만 사용가능
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();  //파일 입출력을 사용해 임시 저장(내부 저장소에)
                    } catch (IOException e) {
                    }
                    if (photoFile != null) {
                        photoUri = FileProvider.getUriForFile(getActivity(), getActivity().getPackageName(), photoFile);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                       startActivityForResult(intent,REQUEST_IMAGE_CAPTURE);
                    }
                }else{//안드 10 이하부터는 여러 카메라 사용가능
                    if (intent.resolveActivity(getActivity().getPackageManager()) != null) {//카메라 어플이 하나라도 있는지 확인(?)
                        File photoFile = null;
                        try {
                            photoFile = createImageFile();  //파일 입출력을 사용해 임시 저장(내부 저장소에)
                        } catch (IOException e) {

                        }

                        if (photoFile != null) {
                            photoUri = FileProvider.getUriForFile(getActivity(), getActivity().getPackageName(), photoFile);
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                            startActivityForResult(Intent.createChooser(intent, "카메라를 고르세요"), REQUEST_IMAGE_CAPTURE);//암시적 인텐트(카메라 사용 여러개 고르기)
                        }

                    }
                }
            }
        });

        builder.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("온리쥼","프래그먼트 온리줌");
        Log.d("유저 아이디",login.user_id);

        RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);
        Call<String> comment = retrofitAPI.inquiryUserData(login.user_id);   //아이디 보내주기
        comment.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                Log.d("프사 결과값",response.body());
                if(!"사진없음".equals(response.body())){
                    Glide.with(getActivity()).load(response.body()).into(imageView);//glide써서 uri넣어주고 바로 적용
                    Log.d("사진있음","사진있음");

                }else{
                    Log.d("사진없음","없음");
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //일반 갤러리 이미지 받아오기
        if (requestCode == GET_GALLERY_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {

            selectedImageUri = data.getData();          // uri 형태의 변수에 경로 데이터 가져와서 저장
            //Toast.makeText(getActivity(),data.toString(),Toast.LENGTH_LONG).show();
            //test.setText(selectedImageUri.toString());
            //Glide.with(getActivity()).load(selectedImageUri).into(imageView);//glide써서 uri넣어주고 바로 적용

            Intent intent = new Intent(getActivity(), image_edit_activity.class);
            intent.putExtra("Uri",selectedImageUri.toString());  //uri를 string으로 intent 사용하여 넘겨주기
            where = "프로필";
            startActivity(intent);



        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {   //촬영된 이미지 비트맵형식으로 받아오기, 미리보기 이미지


            Bitmap bitmap = BitmapFactory.decodeFile(imageFilePath);

            ExifInterface exif = null;
            try {
                exif = new ExifInterface(imageFilePath);
            } catch (Exception e) {
                e.printStackTrace();
            }

            int exifOrientation;
            int exifDegree;

            if (exif != null) {
                exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                exifDegree = exifOrientationToDegree(exifOrientation);
            } else {
                exifDegree = 0;
            }

            Log.d("uri",selectedImageUri.toString());

            Intent intent = new Intent(getActivity(), image_edit_activity.class);
            intent.putExtra("Uri",selectedImageUri.toString());  //uri를 string으로 intent 사용하여 넘겨주기
            startActivity(intent);


            //Intent intent = new Intent(getActivity(), profile_edit.class);
            //startActivity(intent);


            //imageView.setImageBitmap(rotate(bitmap, exifDegree));//이미지뷰 사진으로 셋팅 해주기
            //imageView.setVisibility(View.VISIBLE);

        }



        //게시물 작성으로 받아온 사진들
        if(requestCode == pick_from_Multi_album){
            if(data != null){
                if (data.getClipData() ==null){
                    Toast.makeText(getActivity(),"다중선택이 불가한 기기입니다.", Toast.LENGTH_LONG).show();
                }else {
                    ClipData clipData = data.getClipData();

                    Log.i("clipData", String.valueOf(clipData.getItemCount()));

                    if(clipData.getItemCount() > 9){    //갤러리에서 사진을 9장 넘게 골랐을때 불가 메세지 띄우기
                        Toast.makeText(getActivity(),"사진은 9장까지 선택 가능합니다.", Toast.LENGTH_LONG).show();
                    }else if (clipData.getItemCount() ==1){ //사진을 하나만 골랐을때


                        imagePath1 = getPath(getActivity(),clipData.getItemAt(0).getUri());

                        /*
                        File file = new File(imagePath1);
                        RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);
                        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
                        MultipartBody.Part body = MultipartBody.Part.createFormData("uploaded_file", file.getName(), requestFile);

                        Call<String> resultCall = retrofitAPI.posting(body,login.user_id);

                        resultCall.enqueue(new Callback<String>() {
                            @Override
                            public void onResponse(Call<String> call, Response<String> response) {
                                Log.d("결과1",response.body());
                            }

                            @Override
                            public void onFailure(Call<String> call, Throwable t) {
                                Log.d("결과2",t.getMessage());
                            }
                        });
                        */

                    }else if(clipData.getItemCount() > 1&& clipData.getItemCount() < 9){//갤러리에서 사진을 2개 이상 9개 이하로 사진 골랐을때
                        image_show_activity.image_uri = new ArrayList<>();
                        //image_show_activity.image_uri = new ArrayList<>();//리스트 객체 만들어주기

                        for(int i = 0; i<clipData.getItemCount(); i++){
                            Log.i("사진들",String.valueOf(clipData.getItemAt(i).getUri()));
                            image_show_activity.image_uri.add(clipData.getItemAt(i).getUri());//uri 값들을 넣어주기
                        }

                        ///////여기닷
                        Intent intent = new Intent(getActivity(), image_show_activity.class);
                        startActivity(intent);

                    }
                }

            }
        }

    }



    //사진이 회전되어 있는반큼 정방향으로 회전 시켜주기
    private int exifOrientationToDegree(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }


    /*
    //비트맵 방향을 제대로된 방향으로 돌린다.
    public Bitmap getOrientationBitmap(Uri uri, Bitmap bm) {
        try {
            ExifInterface exif = new ExifInterface(uri.getPath());
            int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            int exifDegree = exifOrientationToDegree(exifOrientation);
            bm = rotate(bm, exifDegree);
            return bm;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
     */

    private Bitmap rotate(Bitmap bitmap, float degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }


    //파일의 실제 경로를 가져오기 위한 메소드(갤러리에서 받아온 파일 경로는 실제 경로가 아니라 mediastore의 가상 파일 경로 이기에 실제 주소로 convert해줘야함)
    public String getPath(Context cxt, Uri uri) {
        Cursor cursor = cxt.getContentResolver().query(uri, null, null, null, null);
        cursor.moveToNext();
        String path = cursor.getString(cursor.getColumnIndex("_data"));
        cursor.close();

        return path;
    }


    //이미지 파일을 년,월,일, 시간의 형식으로 생성
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());  //현재 년,월,일 시간이 pattern형태로 timestamp변수에 저장됨
        String imageFileName = "DOTORISTAGRAM_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        imageFilePath = image.getAbsolutePath();  //이미지 경로 절대경로로 설정해주기

        selectedImageUri = Uri.fromFile(new File(imageFilePath));//uri 형태로 저장도 해주기




        return image;
    }





    public void checkPermissions() {


        //권한 허용, 거부 액션들들
        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() { //권한 허용했때 액션
                permission = true;
                showDialog();       //다이얼로그 띄우기
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                Toast.makeText(getActivity(), "권한이 거부됨", Toast.LENGTH_SHORT).show();
                //권한 거부면 버튼 숨겨짐
                permission = false;
            }

        };



        //권한 체크 부분
        TedPermission.with(getActivity())
                .setPermissionListener(permissionListener)
                .setRationaleMessage("카메라 권한이 필요합니다.")
                .setDeniedMessage("거부하셨습니다.")
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .check();

    }
}

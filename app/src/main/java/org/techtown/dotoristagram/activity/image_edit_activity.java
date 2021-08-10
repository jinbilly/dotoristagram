package org.techtown.dotoristagram.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.zomato.photofilters.FilterPack;
import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.imageprocessors.SubFilter;
import com.zomato.photofilters.imageprocessors.subfilters.BrightnessSubFilter;
import com.zomato.photofilters.imageprocessors.subfilters.ContrastSubFilter;
import com.zomato.photofilters.imageprocessors.subfilters.SaturationSubfilter;
import com.zomato.photofilters.utils.ThumbnailItem;
import com.zomato.photofilters.utils.ThumbnailsManager;

import org.techtown.dotoristagram.R;
import org.techtown.dotoristagram.fragment.EditImageFragment;
import org.techtown.dotoristagram.fragment.FiltersListFragment;
import org.techtown.dotoristagram.retrofit.RetrofitAPI;
import org.techtown.dotoristagram.util.BitmapUtils;
import org.techtown.dotoristagram.util.ThumbnailsAdapter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.xml.transform.Result;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import static org.techtown.dotoristagram.activity.image_show_activity.edit_uri;

public class image_edit_activity extends AppCompatActivity implements FiltersListFragment.FiltersListFragmentListener, EditImageFragment.EditImageFragmentListener, ThumbnailsAdapter.ThumbnailsAdapterListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    public static String where = null;//어디서 넘어 왔는지 체크하여 값에 따라 다른 행동을 하게 됨

    public static Bitmap IMAGE_NAME;

    Bitmap final_image_bitmap;  //마지막 비트맵은 저장을 위해 변수에 넣어주기

    String filePath;    //지워줄 파일 경로 저장

    Uri selectedImageUri;

    public static final int SELECT_GALLERY_IMAGE = 101;

    @BindView(R.id.image_preview)
    ImageView imagePreview;

    @BindView(R.id.tabs)
    TabLayout tabLayout;

    @BindView(R.id.viewpager)
    ViewPager viewPager;

    @BindView(R.id.coordinator_layout)
    CoordinatorLayout coordinatorLayout;

    Bitmap originalImage;
    // to backup image with filter applied
    Bitmap filteredImage;

    // the final image after applying
    // brightness, saturation, contrast
    Bitmap finalImage;

    FiltersListFragment filtersListFragment;
    EditImageFragment editImageFragment;

    // modified image values
    int brightnessFinal = 0;


    float saturationFinal = 1.0f;
    float contrastFinal = 1.0f;

    // load native image filters library
    static {
        System.loadLibrary("NativeImageProcessor");
    }
    //레트로핏
    Retrofit retrofit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_edit_activity);
        ButterKnife.bind(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("필터");


        /*레트로핏 객체 생성*/
        Gson gson  = new GsonBuilder().setLenient().create();
        retrofit = new Retrofit.Builder()
                .baseUrl("http://54.248.192.133")
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();


        //최초 이미지 페이지 오면 넘겨주는 부분
        Uri imageUri = Uri.parse(getIntent().getExtras().getString("Uri"));
        Log.d("uri_image_edi에서 uri 형태 과연!!!!imageUri.getPath(): ",imageUri.getPath());
        Log.d("uri_image_edi에서 uri 형태 과연!!!!imageUri.toString(): ",imageUri.toString());


        //Bitmap bitmap = BitmapUtils.getBitmapFromGallery(this, imageUri, 800, 800);

        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
        } catch (IOException e) {
            e.printStackTrace();
        }


        loadImage(bitmap);

        IMAGE_NAME = bitmap;    //비트맵을 넣어주기 , 이유: 화단부분에 프레그먼트 필터가 받아서 그려주기 위해


        // clear bitmap memory
/*
            originalImage.recycle();
            finalImage.recycle();
            finalImage.recycle();
*/


        originalImage = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        filteredImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);
        finalImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);
        //imagePreview.setImageBitmap(originalImage);
        Glide.with(getApplicationContext()).load(originalImage).into(imagePreview);//glide써서 uri넣어주고 바로 적용

        setupViewPager(viewPager, originalImage);
        tabLayout.setupWithViewPager(viewPager);


    }


    //뷰페이져 세팅 해주기
    private void setupViewPager(ViewPager viewPager, Bitmap bitmap) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        // adding filter list fragment
        filtersListFragment = new FiltersListFragment();
        filtersListFragment.setListener(this);

        // adding edit image fragment
        editImageFragment = new EditImageFragment();
        editImageFragment.setListener(this);

        adapter.addFragment(filtersListFragment, getString(R.string.tab_filters));
        adapter.addFragment(editImageFragment, getString(R.string.tab_edit));

        viewPager.setAdapter(adapter);


    }


    //하단 미리보기 필터 선택했을때
    @Override
    public void onFilterSelected(Filter filter) {

        // 새로운 필터를 선택 했을때 수정 하는 값들을 다 초기화 시켜주기
        resetControls();

        // applying the selected filter
        filteredImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);


        //상단 미리보기 필터 적용된 이미지 결과물 출력
        // preview filtered image
        //imagePreview.setImageBitmap(filter.processFilter(filteredImage));

        finalImage = filteredImage.copy(Bitmap.Config.ARGB_8888, true);


        final_image_bitmap =filter.processFilter(filteredImage);    //마지막 이미지 비트맵으로 넣어주기




        //최종 이미지를 uri로 바꿔서 저장해주기
        selectedImageUri = getImageUri(getApplicationContext(), final_image_bitmap);

        // getPathFromUri(selectedImageUri);

        Glide.with(getApplicationContext()).load(final_image_bitmap).into(imagePreview);//glide써서 필터링 된 이미지 바로 적용하기


        //이미지가 저장 안되게 즉석으로 지워주기
        File file = new File(getPathFromUri(selectedImageUri));
        if (file.exists()) {
            file.delete();
        }
    }


    //uri 경로 해당 이미지의 실경로를 받아오기
    public String getPathFromUri(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToNext();
        String path = cursor.getString(cursor.getColumnIndex("_data"));
        return path;
    }

    //bitmap to uri 메소드
    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

        //title 중복 안되게끔 하기 위해
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HHmmss", Locale.getDefault());
        Date curDate = new Date(System.currentTimeMillis());
        String filename = formatter.format(curDate);

        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, filename, null);
        return Uri.parse(path);
    }

    //밝기 변화 주는 메소드
    @Override
    public void onBrightnessChanged(final int brightness) {
        brightnessFinal = brightness;
        Filter myFilter = new Filter();
        myFilter.addSubFilter(new BrightnessSubFilter(brightness));

        //imagePreview.setImageBitmap(myFilter.processFilter(finalImage.copy(Bitmap.Config.ARGB_8888, true)));

        //최종 이미지를 uri로 바꿔서 저장해주기
        selectedImageUri = getImageUri(getApplicationContext(), myFilter.processFilter(finalImage.copy(Bitmap.Config.ARGB_8888, true)));

        Glide.with(getApplicationContext()).load(myFilter.processFilter(finalImage.copy(Bitmap.Config.ARGB_8888, true))).into(imagePreview);//glide써서 uri넣어주고 바로 적용

        //이미지가 저장 안되게 즉석으로 지워주기
        File file = new File(getPathFromUri(selectedImageUri));
        if (file.exists()) {
            file.delete();
        }
    }


    //대비 변화 주는 메소드
    @Override
    public void onSaturationChanged(final float saturation) {
        saturationFinal = saturation;
        Filter myFilter = new Filter();
        myFilter.addSubFilter(new SaturationSubfilter(saturation));
        //imagePreview.setImageBitmap(myFilter.processFilter(finalImage.copy(Bitmap.Config.ARGB_8888, true)));

        //최종 이미지를 uri로 바꿔서 저장해주기
        selectedImageUri = getImageUri(getApplicationContext(), myFilter.processFilter(finalImage.copy(Bitmap.Config.ARGB_8888, true)));

        Glide.with(getApplicationContext()).load(myFilter.processFilter(finalImage.copy(Bitmap.Config.ARGB_8888, true))).into(imagePreview);//glide써서 uri넣어주고 바로 적용

        //이미지가 저장 안되게 즉석으로 지워주기
        File file = new File(getPathFromUri(selectedImageUri));
        if (file.exists()) {
            file.delete();
        }
    }


    //채도 변화주는 메소드
    @Override
    public void onContrastChanged(final float contrast) {
        contrastFinal = contrast;
        Filter myFilter = new Filter();
        myFilter.addSubFilter(new ContrastSubFilter(contrast));
        //imagePreview.setImageBitmap(myFilter.processFilter(finalImage.copy(Bitmap.Config.ARGB_8888, true)));

        //최종 이미지를 uri로 바꿔서 변수에 저장해주기
        selectedImageUri = getImageUri(getApplicationContext(), myFilter.processFilter(finalImage.copy(Bitmap.Config.ARGB_8888, true)));

        Glide.with(getApplicationContext()).load(myFilter.processFilter(finalImage.copy(Bitmap.Config.ARGB_8888, true))).into(imagePreview);//glide써서 uri넣어주고 바로 적용

        //이미지가 저장 안되게 즉석으로 지워주기
        File file = new File(getPathFromUri(selectedImageUri));
        if (file.exists()) {
            file.delete();
        }
    }

    @Override
    public void onEditStarted() {

    }

    @Override
    public void onEditCompleted() {
        // once the editing is done i.e seekbar is drag is completed,
        // apply the values on to filtered image
        final Bitmap bitmap = filteredImage.copy(Bitmap.Config.ARGB_8888, true);

        Filter myFilter = new Filter();
        myFilter.addSubFilter(new BrightnessSubFilter(brightnessFinal));
        myFilter.addSubFilter(new ContrastSubFilter(contrastFinal));
        myFilter.addSubFilter(new SaturationSubfilter(saturationFinal));
        finalImage = myFilter.processFilter(bitmap);
    }

    /**
     * Resets image edit controls to normal when new filter
     * is selected
     */
    private void resetControls() {
        if (editImageFragment != null) {
            editImageFragment.resetControls();
        }
        brightnessFinal = 0;
        saturationFinal = 1.0f;
        contrastFinal = 1.0f;
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    // load the default image from assets on app launch
    //이미지 로드 해주는 부분인거 같아 보임
    private void loadImage(Bitmap originalImage) {

        //originalImage = BitmapUtils.getBitmapFromAssets(this, IMAGE_NAME, 300, 300);
        filteredImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);
        finalImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);
        imagePreview.setImageBitmap(originalImage);


        //Glide.with(getApplicationContext()).load(R.drawable.home).into(imagePreview);//glide써서 uri넣어주고 바로 적용
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(where.equals("게시물")){
            getMenuInflater().inflate(R.menu.menu_image_edit, menu);
        }else{
            getMenuInflater().inflate(R.menu.menu_main, menu);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //상단 사진변경 눌렀을때
        if (id == R.id.action_open) {
            openImageFromGallery();
            return true;
        }
        //상단 저장 버튼 눌렀을때
        if (id == R.id.action_save) {
            saveImageToGallery(getImageUri(getApplicationContext(),final_image_bitmap));
            return true;
        }

        //상단 취소 버튼 눌렀을때
        if (id == R.id.action_cancel) {
            finish();
            return true;
        }

        //상단 수정완료 버튼 눌렀을때
        if (id == R.id.action_edit_finish) {
            edit_finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //수정완료 메소드
    public void edit_finish(){
        edit_uri = getImageUri(getApplicationContext(),final_image_bitmap);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == SELECT_GALLERY_IMAGE) {
            Bitmap bitmap = BitmapUtils.getBitmapFromGallery(this, data.getData(), 800, 800);

            // clear bitmap memory
/*
            originalImage.recycle();
            finalImage.recycle();
            finalImage.recycle();
*/

            originalImage = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            filteredImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);
            finalImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);
            //imagePreview.setImageBitmap(originalImage);


            Glide.with(getApplicationContext()).load(originalImage).into(imagePreview);//glide써서 uri넣어주고 바로 적용

            bitmap.recycle();

            // render selected image thumbnails
            filtersListFragment.prepareThumbnail(originalImage);

        }
    }


    private void openImageFromGallery() {
        Dexter.withActivity(this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            Intent intent = new Intent(Intent.ACTION_PICK);
                            intent.setType("image/*");
                            startActivityForResult(intent, SELECT_GALLERY_IMAGE);
                        } else {
                            Toast.makeText(getApplicationContext(), "권한이 없습니다!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }





     //저장하기 버튼 눌렀을때
    private void saveImageToGallery(Uri img_url) {

        //파일 생성
        //img_url은 이미지의 경로
        RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);

        File file = new File(getPathFromUri(img_url));  //uri를 경로 형태로 변환해줘서 파일객체로 만듦


        //
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("uploaded_file", file.getName(), requestFile);

        Call<String> resultCall = retrofitAPI.changeOnlyProfileImage(body,login.user_id);

        resultCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                Log.d("결과1",response.body());
                finish();
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.d("결과2",t.getMessage());
            }
        });





        /*
        Dexter.withActivity(this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            final String path = BitmapUtils.insertImage(getContentResolver(), finalImage, System.currentTimeMillis() + "_profile.jpg", null);
                            if (!TextUtils.isEmpty(path)) {
                                Snackbar snackbar = Snackbar
                                        .make(coordinatorLayout, "Image saved to gallery!", Snackbar.LENGTH_LONG)
                                        .setAction("OPEN", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                openImage(path);
                                            }
                                        });

                                snackbar.show();
                            } else {
                                Snackbar snackbar = Snackbar
                                        .make(coordinatorLayout, "Unable to save image!", Snackbar.LENGTH_LONG);

                                snackbar.show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Permissions are not granted!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

                    }

                }).check();
*/
    }





    // opening image in default image viewer app
    private void openImage(String path) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(path), "image/*");
        startActivity(intent);
    }
}
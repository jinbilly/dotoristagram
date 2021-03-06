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

    public static String where = null;//????????? ?????? ????????? ???????????? ?????? ?????? ?????? ????????? ?????? ???

    public static Bitmap IMAGE_NAME;

    Bitmap final_image_bitmap;  //????????? ???????????? ????????? ?????? ????????? ????????????

    String filePath;    //????????? ?????? ?????? ??????

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
    //????????????
    Retrofit retrofit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_edit_activity);
        ButterKnife.bind(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("??????");


        /*???????????? ?????? ??????*/
        Gson gson  = new GsonBuilder().setLenient().create();
        retrofit = new Retrofit.Builder()
                .baseUrl("http://54.248.192.133")
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();


        //?????? ????????? ????????? ?????? ???????????? ??????
        Uri imageUri = Uri.parse(getIntent().getExtras().getString("Uri"));
        Log.d("uri_image_edi?????? uri ?????? ??????!!!!imageUri.getPath(): ",imageUri.getPath());
        Log.d("uri_image_edi?????? uri ?????? ??????!!!!imageUri.toString(): ",imageUri.toString());


        //Bitmap bitmap = BitmapUtils.getBitmapFromGallery(this, imageUri, 800, 800);

        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
        } catch (IOException e) {
            e.printStackTrace();
        }


        loadImage(bitmap);

        IMAGE_NAME = bitmap;    //???????????? ???????????? , ??????: ??????????????? ??????????????? ????????? ????????? ???????????? ??????


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
        Glide.with(getApplicationContext()).load(originalImage).into(imagePreview);//glide?????? uri???????????? ?????? ??????

        setupViewPager(viewPager, originalImage);
        tabLayout.setupWithViewPager(viewPager);


    }


    //???????????? ?????? ?????????
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


    //?????? ???????????? ?????? ???????????????
    @Override
    public void onFilterSelected(Filter filter) {

        // ????????? ????????? ?????? ????????? ?????? ?????? ????????? ??? ????????? ????????????
        resetControls();

        // applying the selected filter
        filteredImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);


        //?????? ???????????? ?????? ????????? ????????? ????????? ??????
        // preview filtered image
        //imagePreview.setImageBitmap(filter.processFilter(filteredImage));

        finalImage = filteredImage.copy(Bitmap.Config.ARGB_8888, true);


        final_image_bitmap =filter.processFilter(filteredImage);    //????????? ????????? ??????????????? ????????????




        //?????? ???????????? uri??? ????????? ???????????????
        selectedImageUri = getImageUri(getApplicationContext(), final_image_bitmap);

        // getPathFromUri(selectedImageUri);

        Glide.with(getApplicationContext()).load(final_image_bitmap).into(imagePreview);//glide?????? ????????? ??? ????????? ?????? ????????????


        //???????????? ?????? ????????? ???????????? ????????????
        File file = new File(getPathFromUri(selectedImageUri));
        if (file.exists()) {
            file.delete();
        }
    }


    //uri ?????? ?????? ???????????? ???????????? ????????????
    public String getPathFromUri(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToNext();
        String path = cursor.getString(cursor.getColumnIndex("_data"));
        return path;
    }

    //bitmap to uri ?????????
    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

        //title ?????? ???????????? ?????? ??????
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HHmmss", Locale.getDefault());
        Date curDate = new Date(System.currentTimeMillis());
        String filename = formatter.format(curDate);

        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, filename, null);
        return Uri.parse(path);
    }

    //?????? ?????? ?????? ?????????
    @Override
    public void onBrightnessChanged(final int brightness) {
        brightnessFinal = brightness;
        Filter myFilter = new Filter();
        myFilter.addSubFilter(new BrightnessSubFilter(brightness));

        //imagePreview.setImageBitmap(myFilter.processFilter(finalImage.copy(Bitmap.Config.ARGB_8888, true)));

        //?????? ???????????? uri??? ????????? ???????????????
        selectedImageUri = getImageUri(getApplicationContext(), myFilter.processFilter(finalImage.copy(Bitmap.Config.ARGB_8888, true)));

        Glide.with(getApplicationContext()).load(myFilter.processFilter(finalImage.copy(Bitmap.Config.ARGB_8888, true))).into(imagePreview);//glide?????? uri???????????? ?????? ??????

        //???????????? ?????? ????????? ???????????? ????????????
        File file = new File(getPathFromUri(selectedImageUri));
        if (file.exists()) {
            file.delete();
        }
    }


    //?????? ?????? ?????? ?????????
    @Override
    public void onSaturationChanged(final float saturation) {
        saturationFinal = saturation;
        Filter myFilter = new Filter();
        myFilter.addSubFilter(new SaturationSubfilter(saturation));
        //imagePreview.setImageBitmap(myFilter.processFilter(finalImage.copy(Bitmap.Config.ARGB_8888, true)));

        //?????? ???????????? uri??? ????????? ???????????????
        selectedImageUri = getImageUri(getApplicationContext(), myFilter.processFilter(finalImage.copy(Bitmap.Config.ARGB_8888, true)));

        Glide.with(getApplicationContext()).load(myFilter.processFilter(finalImage.copy(Bitmap.Config.ARGB_8888, true))).into(imagePreview);//glide?????? uri???????????? ?????? ??????

        //???????????? ?????? ????????? ???????????? ????????????
        File file = new File(getPathFromUri(selectedImageUri));
        if (file.exists()) {
            file.delete();
        }
    }


    //?????? ???????????? ?????????
    @Override
    public void onContrastChanged(final float contrast) {
        contrastFinal = contrast;
        Filter myFilter = new Filter();
        myFilter.addSubFilter(new ContrastSubFilter(contrast));
        //imagePreview.setImageBitmap(myFilter.processFilter(finalImage.copy(Bitmap.Config.ARGB_8888, true)));

        //?????? ???????????? uri??? ????????? ????????? ???????????????
        selectedImageUri = getImageUri(getApplicationContext(), myFilter.processFilter(finalImage.copy(Bitmap.Config.ARGB_8888, true)));

        Glide.with(getApplicationContext()).load(myFilter.processFilter(finalImage.copy(Bitmap.Config.ARGB_8888, true))).into(imagePreview);//glide?????? uri???????????? ?????? ??????

        //???????????? ?????? ????????? ???????????? ????????????
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
    //????????? ?????? ????????? ???????????? ?????? ??????
    private void loadImage(Bitmap originalImage) {

        //originalImage = BitmapUtils.getBitmapFromAssets(this, IMAGE_NAME, 300, 300);
        filteredImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);
        finalImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);
        imagePreview.setImageBitmap(originalImage);


        //Glide.with(getApplicationContext()).load(R.drawable.home).into(imagePreview);//glide?????? uri???????????? ?????? ??????
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(where.equals("?????????")){
            getMenuInflater().inflate(R.menu.menu_image_edit, menu);
        }else{
            getMenuInflater().inflate(R.menu.menu_main, menu);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //?????? ???????????? ????????????
        if (id == R.id.action_open) {
            openImageFromGallery();
            return true;
        }
        //?????? ?????? ?????? ????????????
        if (id == R.id.action_save) {
            saveImageToGallery(getImageUri(getApplicationContext(),final_image_bitmap));
            return true;
        }

        //?????? ?????? ?????? ????????????
        if (id == R.id.action_cancel) {
            finish();
            return true;
        }

        //?????? ???????????? ?????? ????????????
        if (id == R.id.action_edit_finish) {
            edit_finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //???????????? ?????????
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


            Glide.with(getApplicationContext()).load(originalImage).into(imagePreview);//glide?????? uri???????????? ?????? ??????

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
                            Toast.makeText(getApplicationContext(), "????????? ????????????!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }





     //???????????? ?????? ????????????
    private void saveImageToGallery(Uri img_url) {

        //?????? ??????
        //img_url??? ???????????? ??????
        RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);

        File file = new File(getPathFromUri(img_url));  //uri??? ?????? ????????? ??????????????? ??????????????? ??????


        //
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("uploaded_file", file.getName(), requestFile);

        Call<String> resultCall = retrofitAPI.changeOnlyProfileImage(body,login.user_id);

        resultCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                Log.d("??????1",response.body());
                finish();
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.d("??????2",t.getMessage());
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
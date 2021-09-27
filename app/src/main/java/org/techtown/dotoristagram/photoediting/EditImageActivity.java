package org.techtown.dotoristagram.photoediting;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.os.Environment.DIRECTORY_PICTURES;
import static org.techtown.dotoristagram.activity.image_show_activity.edit_uri;
import static org.techtown.dotoristagram.photoediting.FileSaveHelper.isSdkHigherThan28;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.ChangeBounds;
import androidx.transition.TransitionManager;

import org.techtown.dotoristagram.fragment.Frag1;
import org.techtown.dotoristagram.photoediting.base.BaseActivity;
import org.techtown.dotoristagram.photoediting.filters.FilterListener;
import org.techtown.dotoristagram.photoediting.filters.FilterViewAdapter;
import org.techtown.dotoristagram.photoediting.tools.EditingToolsAdapter;
import org.techtown.dotoristagram.photoediting.tools.ToolType;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.techtown.dotoristagram.R;
import org.techtown.dotoristagram.util.MediaScanner;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ja.burhanrashid52.photoeditor.OnPhotoEditorListener;
import ja.burhanrashid52.photoeditor.PhotoEditor;
import ja.burhanrashid52.photoeditor.PhotoEditorView;
import ja.burhanrashid52.photoeditor.PhotoFilter;
import ja.burhanrashid52.photoeditor.SaveSettings;
import ja.burhanrashid52.photoeditor.TextStyleBuilder;
import ja.burhanrashid52.photoeditor.ViewType;
import ja.burhanrashid52.photoeditor.shape.ShapeBuilder;
import ja.burhanrashid52.photoeditor.shape.ShapeType;

public class EditImageActivity extends BaseActivity implements OnPhotoEditorListener,
        View.OnClickListener,
        PropertiesBSFragment.Properties,
        ShapeBSFragment.Properties,
        EmojiBSFragment.EmojiListener,
        StickerBSFragment.StickerListener, EditingToolsAdapter.OnItemSelected, FilterListener {

    private static final String TAG = EditImageActivity.class.getSimpleName();
    public static final String FILE_PROVIDER_AUTHORITY = "com.burhanrashid52.photoeditor.fileprovider";
    private static final int CAMERA_REQUEST = 52;
    private static final int PICK_REQUEST = 53;
    public static final String ACTION_NEXTGEN_EDIT = "action_nextgen_edit";
    public static final String PINCH_TEXT_SCALABLE_INTENT_KEY = "PINCH_TEXT_SCALABLE";

    PhotoEditor mPhotoEditor;

    PhotoEditorView mPhotoEditorView;
    //사진 저장 시 갤러리 폴더에 바로 반영사항을 업데이트 시켜주려면 필요함(미디어 스캐닝)
    private MediaScanner mMediaScanner;

    Bitmap final_image_bitmap;  //마지막 비트맵은 저장을 위해 변수에 넣어주기

    //camera 관련 변수 선언
    private static final int REQUEST_IMAGE_CAPTURE = 672;
    private String imageFilePath;   //이미지 파일 경로
    private Uri photoUri;



    private PropertiesBSFragment mPropertiesBSFragment;
    private ShapeBSFragment mShapeBSFragment;
    private ShapeBuilder mShapeBuilder;
    private EmojiBSFragment mEmojiBSFragment;
    private StickerBSFragment mStickerBSFragment;
    private TextView mTxtCurrentTool;
    private Typeface mWonderFont;
    private RecyclerView mRvTools, mRvFilters;
    private final EditingToolsAdapter mEditingToolsAdapter = new EditingToolsAdapter(this);
    private final FilterViewAdapter mFilterViewAdapter = new FilterViewAdapter(this);
    private ConstraintLayout mRootView;
    private final ConstraintSet mConstraintSet = new ConstraintSet();
    private boolean mIsFilterVisible;

    @Nullable
    @VisibleForTesting
    Uri mSaveImageUri;

    private FileSaveHelper mSaveFileHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //상태바 숨기기 코드(setcontentview 보다 먼저 선언해줘야함)
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_edit_image);

        ActionBar actionBar = getSupportActionBar();  //제목줄 객체 얻어오기
        actionBar.setTitle("사진 편집");  //액션바 제목설정
        actionBar.setDisplayHomeAsUpEnabled(true);   //업버튼 <- 만들기
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#dadafc")));//연보라색으로 만들기
        //makeFullScreen();

        initViews();


        //최초 이미지 페이지 오면 넘겨주는 부분
        Uri imageUri = Uri.parse(getIntent().getExtras().getString("Uri"));


        handleIntentImage(mPhotoEditorView.getSource());    //딱히 별거 없다 아직은..


        mWonderFont = Typeface.createFromAsset(getAssets(), "beyond_wonderland.ttf");

        mPropertiesBSFragment = new PropertiesBSFragment();
        mEmojiBSFragment = new EmojiBSFragment();
        mStickerBSFragment = new StickerBSFragment();
        mShapeBSFragment = new ShapeBSFragment();
        mStickerBSFragment.setStickerListener(this);
        mEmojiBSFragment.setEmojiListener(this);
        mPropertiesBSFragment.setPropertiesChangeListener(this);
        mShapeBSFragment.setPropertiesChangeListener(this);

        LinearLayoutManager llmTools = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mRvTools.setLayoutManager(llmTools);
        mRvTools.setAdapter(mEditingToolsAdapter);

        LinearLayoutManager llmFilters = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mRvFilters.setLayoutManager(llmFilters);
        mRvFilters.setAdapter(mFilterViewAdapter);

        // NOTE(lucianocheng): Used to set integration testing parameters to PhotoEditor
        boolean pinchTextScalable = getIntent().getBooleanExtra(PINCH_TEXT_SCALABLE_INTENT_KEY, true);

        //Typeface mTextRobotoTf = ResourcesCompat.getFont(this, R.font.roboto_medium);
        //Typeface mEmojiTypeFace = Typeface.createFromAsset(getAssets(), "emojione-android.ttf");

        mPhotoEditor = new PhotoEditor.Builder(this, mPhotoEditorView)
                .setPinchTextScalable(pinchTextScalable) // set flag to make text scalable when pinch
                //.setDefaultTextTypeface(mTextRobotoTf)
                //.setDefaultEmojiTypeface(mEmojiTypeFace)
                .build(); // build photo editor sdk

        mPhotoEditor.setOnPhotoEditorListener(this);



        //이미지를 동적으로 변경해주기
        //mPhotoEditorView.getSource().setImageResource(R.drawable.paris_tower);
        Glide.with(this).load(imageUri).centerCrop().into(mPhotoEditorView.getSource());


        mSaveFileHelper = new FileSaveHelper(this);
    }

    private void handleIntentImage(ImageView source) {
        Intent intent = getIntent();
        if (intent != null) {
            // NOTE(lucianocheng): Using "yoda conditions" here to guard against
            //                     a null Action in the Intent.
            if (Intent.ACTION_EDIT.equals(intent.getAction()) ||
                    ACTION_NEXTGEN_EDIT.equals(intent.getAction())) {
                try {


                    Uri uri = intent.getData();
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    source.setImageBitmap(bitmap);
                    Toast.makeText(this, "1-1번상황", Toast.LENGTH_SHORT).show();


                } catch (IOException e) {
                    e.printStackTrace();
                }
                Toast.makeText(this, "1번상황", Toast.LENGTH_SHORT).show();
            } else {
                String intentType = intent.getType();
                if (intentType != null && intentType.startsWith("image/")) {
                    Uri imageUri = intent.getData();
                    Toast.makeText(this, "2-1번상황", Toast.LENGTH_SHORT).show();
                    if (imageUri != null) {
                        source.setImageURI(imageUri);
                        Toast.makeText(this, "2-2번상황", Toast.LENGTH_SHORT).show();
                    }
                }
                Toast.makeText(this, "2-3번상황", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initViews() {
        ImageView imgUndo;
        ImageView imgRedo;
        ImageView imgCamera;
        ImageView imgGallery;
        ImageView imgSave;
        ImageView imgClose;
        ImageView imgShare;

        mPhotoEditorView = findViewById(R.id.photoEditorView);
        mTxtCurrentTool = findViewById(R.id.txtCurrentTool);
        mRvTools = findViewById(R.id.rvConstraintTools);
        mRvFilters = findViewById(R.id.rvFilterView);
        mRootView = findViewById(R.id.rootView);

        imgUndo = findViewById(R.id.imgUndo);
        imgUndo.setOnClickListener(this);

        imgRedo = findViewById(R.id.imgRedo);
        imgRedo.setOnClickListener(this);

        imgCamera = findViewById(R.id.imgCamera);
        imgCamera.setOnClickListener(this);

        imgGallery = findViewById(R.id.imgGallery);
        imgGallery.setOnClickListener(this);

        imgSave = findViewById(R.id.imgSave);
        imgSave.setOnClickListener(this);

        imgClose = findViewById(R.id.imgClose);
        imgClose.setOnClickListener(this);

        imgShare = findViewById(R.id.imgShare);
        imgShare.setOnClickListener(this);

    }

    @Override
    public void onEditTextChangeListener(final View rootView, String text, int colorCode) {
        TextEditorDialogFragment textEditorDialogFragment =
                TextEditorDialogFragment.show(this, text, colorCode);
        textEditorDialogFragment.setOnTextEditorListener((inputText, newColorCode) -> {
            final TextStyleBuilder styleBuilder = new TextStyleBuilder();
            styleBuilder.withTextColor(newColorCode);

            mPhotoEditor.editText(rootView, inputText, styleBuilder);
            mTxtCurrentTool.setText(R.string.label_text);
        });
    }

    @Override
    public void onAddViewListener(ViewType viewType, int numberOfAddedViews) {
        Log.d(TAG, "onAddViewListener() called with: viewType = [" + viewType + "], numberOfAddedViews = [" + numberOfAddedViews + "]");
    }

    @Override
    public void onRemoveViewListener(ViewType viewType, int numberOfAddedViews) {
        Log.d(TAG, "onRemoveViewListener() called with: viewType = [" + viewType + "], numberOfAddedViews = [" + numberOfAddedViews + "]");
    }

    @Override
    public void onStartViewChangeListener(ViewType viewType) {
        Log.d(TAG, "onStartViewChangeListener() called with: viewType = [" + viewType + "]");
    }

    @Override
    public void onStopViewChangeListener(ViewType viewType) {
        Log.d(TAG, "onStopViewChangeListener() called with: viewType = [" + viewType + "]");
    }

    @Override
    public void onTouchSourceImage(MotionEvent event) {
        Log.d(TAG, "onTouchView() called with: event = [" + event + "]");
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.imgUndo:
                mPhotoEditor.undo();
                break;

            case R.id.imgRedo:
                mPhotoEditor.redo();
                break;

            case R.id.imgSave:
                saveImage();
                break;

            case R.id.imgClose:
                onBackPressed();
                break;
            case R.id.imgShare:
                shareImage();
                break;

            case R.id.imgCamera:    //카메라 선택 했을때
                /*
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
                */

                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);    //암시적 인텐트 사용(카메라 사용)
                File photoFile = null;
                try {
                    photoFile = createImageFile();  //파일 입출력을 사용해 임시 저장(내부 저장소에)
                } catch (IOException e) {
                }
                if (photoFile != null) {
                    photoUri = FileProvider.getUriForFile(getApplicationContext(), getPackageName(), photoFile);
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                    startActivityForResult(cameraIntent,CAMERA_REQUEST);
                }

                break;

            case R.id.imgGallery://갤러리 선택 했을때
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_PICK);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_REQUEST);
                break;
        }
    }

    //이미지 파일을 년,월,일, 시간의 형식으로 생성
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());  //현재 년,월,일 시간이 pattern형태로 timestamp변수에 저장됨
        String imageFileName = "Dotoristagram_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        imageFilePath = image.getAbsolutePath();  //이미지 경로 절대경로로 설정해주기
        return image;
    }

    private void shareImage() {
        if (mSaveImageUri == null) {
            showSnackbar(getString(R.string.msg_save_image_to_share));
            return;
        }

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_STREAM, buildFileProviderUri(mSaveImageUri));
        startActivity(Intent.createChooser(intent, getString(R.string.msg_share_image)));
    }

    private Uri buildFileProviderUri(@NonNull Uri uri) {
        return FileProvider.getUriForFile(this,
                FILE_PROVIDER_AUTHORITY,
                new File(uri.getPath()));
    }


    private void saveImage() {
        final String fileName = System.currentTimeMillis() + ".png";
        final boolean hasStoragePermission =
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED;
        if (hasStoragePermission || isSdkHigherThan28()) {
            showLoading("Saving...");
            mSaveFileHelper.createFile(fileName, (fileCreated, filePath, error, uri) -> {
                if (fileCreated) {
                    SaveSettings saveSettings = new SaveSettings.Builder()
                            .setClearViewsEnabled(true)
                            .setTransparencyEnabled(true)
                            .build();

                    mPhotoEditor.saveAsFile(filePath, saveSettings, new PhotoEditor.OnSaveListener() {
                        @Override
                        public void onSuccess(@NonNull String imagePath) {
                            mSaveFileHelper.notifyThatFileIsNowPubliclyAvailable(getContentResolver());
                            hideLoading();
                            showSnackbar("Image Saved Successfully");
                            mSaveImageUri = uri;
                            mPhotoEditorView.getSource().setImageURI(mSaveImageUri);
                        }

                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            hideLoading();
                            showSnackbar("Failed to save Image");
                        }
                    });

                } else {
                    hideLoading();
                    showSnackbar(error);
                }
            });
        } else {
            requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {

                case CAMERA_REQUEST: //카메라로 사진을 촬영한 뒤 이미지 받아오기
                    /*
                    //mPhotoEditor.clearAllViews();
                    Bitmap photo = (Bitmap) data.getExtras().get("data");

                    //mPhotoEditorView.getSource().setImageBitmap(photo);
                    Glide.with(this).load(photo).centerCrop().into(mPhotoEditorView.getSource());

                    */

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

                    //새로 추가된 부분
                    // 갤러리에 Dotoristagram 이름으로 폴더 생성 후 그안에 촬영한 사진 저장(새로 추가된 부분)
                    String result = "";
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HHmmss", Locale.getDefault());
                    Date curDate = new Date(System.currentTimeMillis());
                    String filename = formatter.format(curDate);


                    String strFolderName = Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES) + File.separator + "Dotoristagram" + File.separator;
                    File file = new File(strFolderName);
                    if (!file.exists())
                        file.mkdirs();


                    File f = new File(strFolderName + "/" + filename + ".png");
                    result = f.getPath();

                    FileOutputStream fOut = null;

                    try {
                        fOut = new FileOutputStream(f);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        result = "Save Error fOut";
                        System.out.println("파일이 안만들어져!");
                    }

                    // 비트맵 사진 폴더 경로에 저장
                    rotate(bitmap, exifDegree).compress(Bitmap.CompressFormat.PNG, 70, fOut);

                    try {
                        fOut.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    Glide.with(getApplicationContext()).load(imageFilePath).centerCrop().into(mPhotoEditorView.getSource());


                    break;

                case PICK_REQUEST:  //파일로써 갤러리 접근하여 사진 가져오기
                    try {
                        mPhotoEditor.clearAllViews();
                        Uri uri = data.getData();
                        Bitmap bitmap1 = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                        //mPhotoEditorView.getSource().setImageBitmap(bitmap);
                        Glide.with(this).load(bitmap1).centerCrop().into(mPhotoEditorView.getSource());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }

    private Bitmap rotate(Bitmap bitmap, float degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
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


    @Override
    public void onColorChanged(int colorCode) {
        mPhotoEditor.setShape(mShapeBuilder.withShapeColor(colorCode));
        mTxtCurrentTool.setText(R.string.label_brush);
    }

    @Override
    public void onOpacityChanged(int opacity) {
        mPhotoEditor.setShape(mShapeBuilder.withShapeOpacity(opacity));
        mTxtCurrentTool.setText(R.string.label_brush);
    }

    @Override
    public void onShapeSizeChanged(int shapeSize) {
        mPhotoEditor.setShape(mShapeBuilder.withShapeSize(shapeSize));
        mTxtCurrentTool.setText(R.string.label_brush);
    }

    @Override
    public void onShapePicked(ShapeType shapeType) {
        mPhotoEditor.setShape(mShapeBuilder.withShapeType(shapeType));
    }

    @Override
    public void onEmojiClick(String emojiUnicode) {
        mPhotoEditor.addEmoji(emojiUnicode);
        mTxtCurrentTool.setText(R.string.label_emoji);
    }

    @Override
    public void onStickerClick(Bitmap bitmap) {
        mPhotoEditor.addImage(bitmap);
        mTxtCurrentTool.setText(R.string.label_sticker);
    }

    @Override
    public void isPermissionGranted(boolean isGranted, String permission) {
        if (isGranted) {
            saveImage();
        }
    }

    private void showSaveDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.msg_save_image));
        builder.setPositiveButton("Save", (dialog, which) -> saveImage());
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.setNeutralButton("Discard", (dialog, which) -> finish());
        builder.create().show();

    }

    @Override
    public void onFilterSelected(PhotoFilter photoFilter) {
        mPhotoEditor.setFilterEffect(photoFilter);
    }

    @Override
    public void onToolSelected(ToolType toolType) {
        switch (toolType) {
            case SHAPE:
                mPhotoEditor.setBrushDrawingMode(true);
                mShapeBuilder = new ShapeBuilder();
                mPhotoEditor.setShape(mShapeBuilder);
                mTxtCurrentTool.setText(R.string.label_shape);
                showBottomSheetDialogFragment(mShapeBSFragment);
                break;
            case TEXT:
                TextEditorDialogFragment textEditorDialogFragment = TextEditorDialogFragment.show(this);
                textEditorDialogFragment.setOnTextEditorListener((inputText, colorCode) -> {
                    final TextStyleBuilder styleBuilder = new TextStyleBuilder();
                    styleBuilder.withTextColor(colorCode);

                    mPhotoEditor.addText(inputText, styleBuilder);
                    mTxtCurrentTool.setText(R.string.label_text);
                });
                break;
            case ERASER:
                mPhotoEditor.brushEraser();
                mTxtCurrentTool.setText(R.string.label_eraser_mode);
                break;
            case FILTER:
                mTxtCurrentTool.setText(R.string.label_filter);
                showFilter(true);
                break;
            case EMOJI:
                showBottomSheetDialogFragment(mEmojiBSFragment);
                break;
            case STICKER:
                showBottomSheetDialogFragment(mStickerBSFragment);
                break;
        }
    }

    private void showBottomSheetDialogFragment(BottomSheetDialogFragment fragment) {
        if (fragment == null || fragment.isAdded()) {
            return;
        }
        fragment.show(getSupportFragmentManager(), fragment.getTag());
    }


    void showFilter(boolean isVisible) {
        mIsFilterVisible = isVisible;
        mConstraintSet.clone(mRootView);

        if (isVisible) {
            mConstraintSet.clear(mRvFilters.getId(), ConstraintSet.START);
            mConstraintSet.connect(mRvFilters.getId(), ConstraintSet.START,
                    ConstraintSet.PARENT_ID, ConstraintSet.START);
            mConstraintSet.connect(mRvFilters.getId(), ConstraintSet.END,
                    ConstraintSet.PARENT_ID, ConstraintSet.END);
        } else {
            mConstraintSet.connect(mRvFilters.getId(), ConstraintSet.START,
                    ConstraintSet.PARENT_ID, ConstraintSet.END);
            mConstraintSet.clear(mRvFilters.getId(), ConstraintSet.END);
        }

        ChangeBounds changeBounds = new ChangeBounds();
        changeBounds.setDuration(350);
        changeBounds.setInterpolator(new AnticipateOvershootInterpolator(1.0f));
        TransitionManager.beginDelayedTransition(mRootView, changeBounds);

        mConstraintSet.applyTo(mRootView);
    }


    //상단 액션 바에 메뉴 생성
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.complite_menu, menu);
        return true;
    }



    //상단 메뉴바 아이템을 클릭했을때 발생되는 이벤트
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        Log.d("메뉴 뭐 눌렀노",Integer.toString(id));
        Log.d("메뉴 뭐 눌렀노",Integer.toString(R.id.action_complite));

        if (id == R.id.home) {//뒤로가기 버튼 눌렀을 경우
            onBackPressed();
            Log.d("들어오지마","후엥");
        }else if(id == R.id.action_complite){   //완료 버튼 눌렀을 경우

            Log.d("들어오지마","후엥1");
            edit_uri = getImageUri(getApplicationContext(),final_image_bitmap);
            Log.d("들어오지마","후엥2");
        }
        Log.d("들어오지마","후엥3");
        return true;
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


    @Override
    public void onBackPressed() {
        if (mIsFilterVisible) {
            showFilter(false);
            mTxtCurrentTool.setText(R.string.app_name);
        } else if (!mPhotoEditor.isCacheEmpty()) {
            showSaveDialog();
        } else {
            super.onBackPressed();
        }
    }
}

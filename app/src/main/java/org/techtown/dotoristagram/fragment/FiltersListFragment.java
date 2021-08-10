package org.techtown.dotoristagram.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zomato.photofilters.FilterPack;
import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.utils.ThumbnailItem;
import com.zomato.photofilters.utils.ThumbnailsManager;

import org.techtown.dotoristagram.R;
import org.techtown.dotoristagram.activity.MainActivity;
import org.techtown.dotoristagram.activity.image_edit_activity;
import org.techtown.dotoristagram.util.BitmapUtils;
import org.techtown.dotoristagram.util.SpacesItemDecoration;
import org.techtown.dotoristagram.util.ThumbnailsAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FiltersListFragment extends Fragment implements ThumbnailsAdapter.ThumbnailsAdapterListener {
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    ThumbnailsAdapter mAdapter;

    List<ThumbnailItem> thumbnailItemList;

    FiltersListFragmentListener listener;

    public void setListener(FiltersListFragmentListener listener) {
        this.listener = listener;
    }

    public FiltersListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.d("onAttatch_fragment","onAttatch_fragment");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("oncreate_fragment","oncreate_fragment");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_filters_list, container, false);

        ButterKnife.bind(this, view);


        thumbnailItemList = new ArrayList<>();
        mAdapter = new ThumbnailsAdapter(getActivity(), thumbnailItemList, this);
        Log.d("나오나","헝");



        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        int space = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8,
                getResources().getDisplayMetrics());
        recyclerView.addItemDecoration(new SpacesItemDecoration(space));
        recyclerView.setAdapter(mAdapter);

        prepareThumbnail(null);

        return view;
    }

    /**
     * Renders thumbnails in horizontal list
     * loads default image from Assets if passed param is null
     *
     * @param bitmap
     */
    public void prepareThumbnail(final Bitmap bitmap) {
        Runnable r = new Runnable() {
            public void run() {
                Bitmap thumbImage;

                if (bitmap == null) {   //비트맵이 널일때는 최초 image_deit_activity 로 진입했을때 기본 사진 적용해주기
                    thumbImage = image_edit_activity.IMAGE_NAME;
                } else {
                    //현재 pixel 형태 그대로 2배 늘려버려서 이미지가 깨지거나 흐려보이게 만듬
                    thumbImage = Bitmap.createScaledBitmap(bitmap, 100, 100, false);
                }

                //thumbImage값이 널일때는 오류가 나지 않게 return해주기
                if (thumbImage == null)
                    return;

                ThumbnailsManager.clearThumbs();    //리스트들 초기화 해주기
                thumbnailItemList.clear();          //thumbnailItemList 초기화 해주기

/*
                // add normal bitmap first
                ThumbnailItem thumbnailItem = new ThumbnailItem();  //ThumbnailItem 객체 생성하기
                thumbnailItem.image = thumbImage;       //image값을 비트맵 받고 수정한 값으로 지정해주기
                thumbnailItem.filterName = getString(R.string.filter_normal);   //필터 이름을 normal로 설정해주기
                ThumbnailsManager.addThumb(thumbnailItem); //List<ThumbnailItem> filterThumbs  리스트에 방금 만든 thumbnailItem 객체 add해주기
*/

                List<Filter> filters = FilterPack.getFilterPack(getActivity()); // Filter타입의 filters라는 이름의 리스트 생성, 그리고 filter리스트를 넣어주기
                                                                                // 즉, 모든 필터가 들어있는 리스트 하나 만들어주기


                for (Filter filter : filters) { //for문을 통하여 모든 filter 를 적용
                    ThumbnailItem tI = new ThumbnailItem();
                    tI.image = thumbImage;
                    tI.filter = filter;
                    tI.filterName = filter.getName();
                    ThumbnailsManager.addThumb(tI);
                }



                thumbnailItemList.addAll(ThumbnailsManager.processThumbs(getActivity()));

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
        };

        new Thread(r).start();
    }


    @Override
    public void onFilterSelected(Filter filter) {
        if (listener != null)
            listener.onFilterSelected(filter);
    }

    public interface FiltersListFragmentListener {
        void onFilterSelected(Filter filter);
    }
}
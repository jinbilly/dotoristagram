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
        Log.d("?????????","???");



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

                if (bitmap == null) {   //???????????? ???????????? ?????? image_deit_activity ??? ??????????????? ?????? ?????? ???????????????
                    thumbImage = image_edit_activity.IMAGE_NAME;
                } else {
                    //?????? pixel ?????? ????????? 2??? ??????????????? ???????????? ???????????? ??????????????? ??????
                    thumbImage = Bitmap.createScaledBitmap(bitmap, 100, 100, false);
                }

                //thumbImage?????? ???????????? ????????? ?????? ?????? return?????????
                if (thumbImage == null)
                    return;

                ThumbnailsManager.clearThumbs();    //???????????? ????????? ?????????
                thumbnailItemList.clear();          //thumbnailItemList ????????? ?????????

/*
                // add normal bitmap first
                ThumbnailItem thumbnailItem = new ThumbnailItem();  //ThumbnailItem ?????? ????????????
                thumbnailItem.image = thumbImage;       //image?????? ????????? ?????? ????????? ????????? ???????????????
                thumbnailItem.filterName = getString(R.string.filter_normal);   //?????? ????????? normal??? ???????????????
                ThumbnailsManager.addThumb(thumbnailItem); //List<ThumbnailItem> filterThumbs  ???????????? ?????? ?????? thumbnailItem ?????? add?????????
*/

                List<Filter> filters = FilterPack.getFilterPack(getActivity()); // Filter????????? filters?????? ????????? ????????? ??????, ????????? filter???????????? ????????????
                                                                                // ???, ?????? ????????? ???????????? ????????? ?????? ???????????????


                for (Filter filter : filters) { //for?????? ????????? ?????? filter ??? ??????
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
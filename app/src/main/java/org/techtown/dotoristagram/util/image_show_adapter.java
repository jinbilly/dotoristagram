package org.techtown.dotoristagram.util;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.techtown.dotoristagram.R;

import java.util.ArrayList;

public class image_show_adapter extends RecyclerView.Adapter<image_show_adapter.ViewHolder> implements OnItemClickListener{


    private ArrayList<imageEditItem> arrayList;
    OnItemClickListener listener;

    //생성자로 itemclass초기화 시켜주기
    public image_show_adapter(ArrayList<imageEditItem> arrayList) {
        this.arrayList = arrayList;

        Log.d("어댑터생성","11");
        Log.d("어댑터생성22","" + arrayList.size());
    }

    //  onCreateViewHolder :아이템 뷰를 위한 뷰홀더 객체를 생성하여 리턴
    @NonNull
    @Override
    public image_show_adapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("onCreateViewHolder","22");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_image_edit_item, parent, false);
        ViewHolder holder = new ViewHolder(view);

        return holder;
    }

    // onBindViewHolder : position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시
    @Override
    public void onBindViewHolder(@NonNull image_show_adapter.ViewHolder holder, int position) {
        Log.d("여기 ", "111");

        Glide.with(holder.itemView.getContext()).load(arrayList.get(position).getImage_uri()).centerCrop().into(holder.image); //이미지 세팅 시켜주기
        //Glide.with(holder.itemView.getContext()).load(R.drawable.home).centerCrop().into(holder.image); //이미지 세팅 시켜주기
        /*클릭 했을때 이벤트 발생 */
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //현재 뷰홀더로 부터 어떤 뷰홀더 인지 getadapterposition을 통하여 받아옴
                int position = holder.getAdapterPosition();

                if(listener !=null){
                    listener.onItemClick(holder,v,position);
                }

            }
        });

    }

    //전체 데이터의 개수를 리턴
    @Override
    public int getItemCount() {
        return arrayList.size();
    }


    /*새로 추가한 부분*/
    public void setOnItemClicklistener (OnItemClickListener listener){
        this.listener = listener;
    }



    @Override
    public void onItemClick(RecyclerView.ViewHolder holder, View view, int position) {
        if(listener != null){
            listener.onItemClick(holder,view,position);
        }
    }

    @Override
    public void onItemLongClick(RecyclerView.ViewHolder holder, View view, int position) {
        if(listener !=null){
            listener.onItemLongClick(holder,view,position);
        }
    }






    // 아이템 뷰를 저장하는 뷰홀더 클래스.
    public class ViewHolder extends RecyclerView.ViewHolder {
        protected ImageView image;

        ViewHolder(View itemView) {
            super(itemView);

            // 뷰 객체에 대한 참조. (hold strong reference)
            image = itemView.findViewById(R.id.image);
        }
    }


}



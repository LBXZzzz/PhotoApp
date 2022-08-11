package com.example.photoapp.adapter;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.photoapp.R;
import com.example.pikachu.Pikachu;
import com.example.widght.NumberCheckBox;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class PhotoRecyclerAdapter extends RecyclerView.Adapter<PhotoRecyclerAdapter.ViewHolder> {
    private Context mContext;
    private ArrayList<Uri> mUriList;
    protected boolean isScrolling = false;
    ArrayList<Uri> selectUri=new ArrayList<>();

    public PhotoRecyclerAdapter(Context context, ArrayList<Uri> uriList) {
        this.mContext = context;
        this.mUriList = uriList;
    }

    @NonNull
    @Override
    public PhotoRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoRecyclerAdapter.ViewHolder holder, int position) {
        Uri uri=mUriList.get(position);
        (holder).imageView.setImageResource(com.example.widght.R.drawable.photo_20220811101907);
        if(!isScrolling){
            Pikachu.with(mContext).load(uri)
                    .placeholder(com.example.widght.R.drawable.photo_20220811101907)
                    .centerCrop()
                    .resize(150,150)
                    .into(( holder).imageView);
           /* Picasso.with(mContext).load(mUriList.get(position)).resize(150,150).centerCrop().into(((ViewHolder) holder).imageView);*/
        }
        (holder).numberCheckBox.setOnStateChangeListener(new NumberCheckBox.OnOnStateChangeListener() {
            @Override
            public void onClick(boolean isSelected) {
                if(!isSelected){
                    selectUri.add(uri);
                }else {
                    selectUri.remove(uri);
                }
            }
        });


    }

    @Override
    public int getItemCount() {
        return mUriList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        NumberCheckBox numberCheckBox;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_rv_item);
            numberCheckBox=itemView.findViewById(R.id.nb_rv_item);
        }
    }

    public void setScrolling(boolean scrolling) {
        isScrolling = scrolling;
    }

    public ArrayList<Uri> getUri(){
        return selectUri;
    }

}

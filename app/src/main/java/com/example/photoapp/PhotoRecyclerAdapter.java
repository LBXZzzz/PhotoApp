package com.example.photoapp;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class PhotoRecyclerAdapter extends RecyclerView.Adapter<PhotoRecyclerAdapter.ViewHolder> {
    private Context mContext;
    private ArrayList<Uri> mUriList;
    public PhotoRecyclerAdapter(Context context, ArrayList<Uri> uriList){
        this.mContext=context;
        this.mUriList=uriList;
    }

    @NonNull
    @Override
    public PhotoRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoRecyclerAdapter.ViewHolder holder, int position) {
        Picasso.with(mContext).load(mUriList.get(position)).fit().into(((ViewHolder)holder).imageView);
//        ((ViewHolder)holder).imageView.setImageURI(mUriList.get(position));
    }

    @Override
    public int getItemCount() {
        return mUriList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView=itemView.findViewById(R.id.iv_rv_item);
        }
    }
}

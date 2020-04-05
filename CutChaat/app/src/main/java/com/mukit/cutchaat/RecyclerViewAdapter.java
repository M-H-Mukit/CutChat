package com.mukit.cutchaat;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.LayoutDirection;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private static final String TAG = "RecyclerViewAdapter";

    //variables
    private int[] images;
    private String[] image_names;
    private Context mContext;
    ZoomableViewGroup zoomableViewGroup;

    public RecyclerViewAdapter(Context context, int[] images, String[] image_names, ZoomableViewGroup zoomableViewGroup) {
        this.images = images;
        this.image_names = image_names;
        this.mContext = context;
        this.zoomableViewGroup =zoomableViewGroup;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        Log.d(TAG, "onCreate Called");
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_listitem, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        final int image_id = images[i];
        String image_name = image_names[i];
        viewHolder.bgImg.setImageResource(image_id);
        viewHolder.bgName.setText(image_name);
        viewHolder.bgImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zoomableViewGroup.setBackgroundResource(image_id);
            }
        });
    }

    @Override
    public int getItemCount() {
        return images.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView bgImg;
        TextView bgName;

        public ViewHolder(View itemView) {
            super(itemView);
            bgImg = itemView.findViewById(R.id.item_bg_view);
            bgName = itemView.findViewById(R.id.item_bg_name);
        }
    }

}

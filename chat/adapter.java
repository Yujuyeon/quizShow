package com.pedro.rtpstreamer.chat;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.pedro.rtpstreamer.R;

import java.util.ArrayList;

public class adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{

    public static class MyViewHolder extends RecyclerView.ViewHolder
    {
        TextView idTextview;
        TextView chatContentsTextview;

        MyViewHolder(View view)
        {
            super(view);
            idTextview = view.findViewById(R.id.chatId);
            chatContentsTextview = view.findViewById(R.id.chatContents);
        }
    }

    private ArrayList<chat> foodInfoArrayList;

    public adapter(ArrayList<chat> foodInfoArrayList)
    {
        this.foodInfoArrayList = foodInfoArrayList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chatview, parent, false);

        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
    {

        MyViewHolder myViewHolder = (MyViewHolder) holder;

        myViewHolder.idTextview.setText(foodInfoArrayList.get(position).id);
        myViewHolder.chatContentsTextview.setText(foodInfoArrayList.get(position).chatContents);
    }

    @Override
    public int getItemCount()
    {
        return foodInfoArrayList.size();
    }
}


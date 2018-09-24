package com.example.nitishkumar.newsappv2;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NewsDataAdapter extends RecyclerView.Adapter<NewsDataAdapter.NewsViewHolder>{

    private Context mContext;
    private List<NewsData> newsData;

    public NewsDataAdapter(Context mContext, List<NewsData> newsData) {
        this.mContext = mContext;
        this.newsData = newsData;
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item,parent,false);
        return new NewsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        NewsData currentNewsdata = newsData.get(position);

        holder.vNewsHeading.setText(currentNewsdata.getNewsHeading());
        holder.vNewsDate.setText(currentNewsdata.getNewsDate());
        holder.vNewsType.setText(currentNewsdata.getNewsType());
        holder.vNewsAuthor.setText(currentNewsdata.getNewsAuthor());

        RequestOptions options = new RequestOptions()
                .centerCrop().placeholder(R.mipmap.ic_launcher_round)
                .error(R.mipmap.ic_launcher_round);

        Glide.with(mContext).load(currentNewsdata.getNewsThumbNailImageUrl()).apply(options).into(holder.vNewsThumbNailImage);
    }

    @Override
    public int getItemCount() {
        return newsData.size();
    }

    public NewsData getItem(int position) {
        return newsData.get(position);
    }

    public class NewsViewHolder extends RecyclerView.ViewHolder{
        protected @BindView(R.id.thumbnailImageView) ImageView vNewsThumbNailImage;
        protected @BindView(R.id.headingTextView) TextView vNewsHeading;
        protected @BindView(R.id.dateTextView) TextView vNewsDate;
        protected @BindView(R.id.authorTextView) TextView vNewsAuthor;
        protected @BindView(R.id.newsTypeTextView) TextView vNewsType;
        public NewsViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
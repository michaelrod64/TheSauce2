package com.carolinagold.thesauce;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Observable;

/**
 * Created by woodyjean-louis on 12/9/16.
 */

public class NewsFeedAdaptor extends RecyclerView.Adapter<NewsFeedAdaptor.PostViewHolder> {

    // Store the context for easy access
    private Context context;

    private List<Post> posts;

    // Pass in the contact array into the constructor
    public NewsFeedAdaptor(Context context, final List<Post> posts) {
        this.context = context;
        this.posts = posts;

        registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                for (int i = 0; i < posts.size(); i++)
                notifyItemChanged(i);
            }
        });
    }

    // Easy access to the context object in the recyclerview
    private Context getContext() {
        return context;
    }

    public void updateFeedList(List<Post> list) {
        posts = list;
    }

    // Usually involves inflating a layout from XML and returning the holder
    @Override
    public NewsFeedAdaptor.PostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View postView = inflater.inflate(R.layout.news_feed_post_layout, parent, false);

        // Return a new holder instance
        PostViewHolder viewHolder = new PostViewHolder(postView);
        return viewHolder;
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(NewsFeedAdaptor.PostViewHolder viewHolder, int position) {
        if (posts != null) {
            // Get the data model based on position
            Post post = posts.get((posts.size() - 1) - position);

            // Set item views based on your views and data model
            Picasso.with(context).load(post.getUserProfilePicturePath()).into(viewHolder.userProfilePicture);
            viewHolder.userName.setText(post.getUserName());
            viewHolder.location.setText(post.getLocation());
            viewHolder.date.setText(post.getDate());
            Picasso.with(context).load(post.getImagePath()).into(viewHolder.imageView);
            viewHolder.caption.setText(post.getCaption());
        } else {
            //Implement some default design
        }
    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return posts.size();
    }

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public static class PostViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public ImageView userProfilePicture;
        TextView userName;
        TextView location;
        TextView date;
        ImageView imageView;
        TextView caption;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public PostViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            userProfilePicture = (ImageView) itemView.findViewById(R.id.news_feed_post_layout_profile_picture);
            userName = (TextView) itemView.findViewById(R.id.news_feed_post_layout_profile_name);
            location = (TextView) itemView.findViewById(R.id.news_feed_post_layout_location);
            date = (TextView) itemView.findViewById(R.id.news_feed_post_layout_date);
            imageView = (ImageView) itemView.findViewById(R.id.news_feed_post_layout_image);
            caption = (TextView) itemView.findViewById(R.id.news_feed_post_layout_caption);
        }
    }
}

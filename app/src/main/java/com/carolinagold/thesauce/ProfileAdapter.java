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

/**
 * Created by woodyjean-louis on 12/10/16.
 */

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ViewHolder> {

    // Store the context for easy access
    private Context context;

    private List<Post> posts;

    // Pass in the contact array into the constructor
    public ProfileAdapter(Context context, final List<Post> posts) {
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

    public void updateProfileGallery(List<Post> list) {
        posts = list;
    }

    // Usually involves inflating a layout from XML and returning the holder
    @Override
    public ProfileAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View postView = inflater.inflate(R.layout.profile_gallery_item, parent, false);

        // Return a new holder instance
        ProfileAdapter.ViewHolder viewHolder = new ProfileAdapter.ViewHolder(postView);
        return viewHolder;
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(ProfileAdapter.ViewHolder viewHolder, final int position) {
        if (posts != null) {
            // Get the data model based on position
            Post post = posts.get((posts.size() - 1) - position);

            // Set item views based on your views and data model
            Picasso.with(context).load(post.getImagePath()).into(viewHolder.imageView);

            viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    delete(position);
                    return true;
                }
            });

        } else {
            //Implement some default design
        }
    }

    private void delete(int position) {
        Post post = posts.get((posts.size() - 1) - position);
        posts.remove((posts.size() - 1) - position);
        post.deleteFromCloud(context);
        notifyDataSetChanged();
    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return posts.size();
    }

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        ImageView imageView;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            imageView = (ImageView) itemView.findViewById(R.id.profile_gallery_item);

        }
    }

}

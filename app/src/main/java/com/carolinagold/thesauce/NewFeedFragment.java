package com.carolinagold.thesauce;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
//import android.app.Fragment;
import android.support.v7.util.SortedList;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class NewFeedFragment extends Fragment {

    RecyclerView recyclerView;



    private OnFragmentInteractionListener mListener;

    public NewFeedFragment() {
        // Required empty public constructor
    }

    public static NewFeedFragment newInstance() {
        NewFeedFragment fragment = new NewFeedFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_new_feed, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.news_feed_fragment_recycler_view);

        List<Post> theList = new ArrayList<Post>();

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        NewsFeedAdaptor adapter = new NewsFeedAdaptor(getContext(), theList);
        recyclerView.setAdapter(adapter);
        getLatestPost();
        return view;
    }

    public void getLatestPost() {
        FirebaseDatabase dbRef = FirebaseDatabase.getInstance();
        DatabaseReference myRef = dbRef.getReference("Post");

        ((MainActivity) getActivity()).showProgress(true);
        final NewsFeedAdaptor adaptor = (NewsFeedAdaptor) recyclerView.getAdapter();
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                List<Post> listOfPosts = new ArrayList<Post>();

                for (DataSnapshot postByUser : dataSnapshot.getChildren()) {
                    System.out.println(postByUser);
                    for (DataSnapshot post : postByUser.getChildren()) {
                        //System.out.println("value: " + post.getValue(Post.class));
                        listOfPosts.add(post.getValue(Post.class));
                    }

                    //System.out.println("child: " + post.getChildren());
                }
                Collections.sort(listOfPosts, new Comparator<Post>() {
                    @Override
                    public int compare(Post post, Post t1) {
                        return post.getName().compareTo(t1.getName());
                    }
                });
                ((MainActivity) getActivity()).showProgress(false);
                adaptor.updateFeedList(listOfPosts);
                adaptor.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i(Logs.POINT_OF_INTEREST, "Failed retrieving data from firebase from method: getLatestPost");
                ((MainActivity) getActivity()).showProgress(false);
            }
        });
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

}

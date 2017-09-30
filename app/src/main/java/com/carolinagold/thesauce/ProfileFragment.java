package com.carolinagold.thesauce;

import android.content.Context;
import android.media.ImageWriter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;



public class ProfileFragment extends Fragment implements View.OnLongClickListener {

    RecyclerView recyclerView;

    ImageView imageView;
    TextView textView;
    ScrollView scrollView;

    FirebaseUser user;

    private OnFragmentInteractionListener mListener;

    public ProfileFragment() {
        // Required empty public constructor
    }


    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();

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
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        user = ((MainActivity) getActivity()).getUser();
        imageView = (ImageView) rootView.findViewById(R.id.profile_fragment_profile_image);
        textView = (TextView) rootView.findViewById(R.id.profile_fragment_profile_name);
        if (user != null)
            setUpTopView();

        recyclerView = (RecyclerView) rootView.findViewById(R.id.profile_fragment_recycler_grid);

        List<Post> theList = new ArrayList<Post>();

        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3, GridLayout.VERTICAL, false) {
            @Override
            public boolean canScrollVertically() {
                return true;
            }
        });

        ProfileAdapter adapter = new ProfileAdapter(getContext(), theList);
        recyclerView.setAdapter(adapter);

        if (user != null)
            getAllProfilePost();

        return rootView;
    }


    public void getAllProfilePost() {
            FirebaseDatabase dbRef = FirebaseDatabase.getInstance();
            DatabaseReference myRef = dbRef.getReference("Post").child(user.getUid());

            ((MainActivity) getActivity()).showProgress(true);
            final ProfileAdapter adaptor = (ProfileAdapter) recyclerView.getAdapter();
            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    List<Post> listOfPosts = new ArrayList<Post>();

                    for (DataSnapshot postByUser : dataSnapshot.getChildren()) {
                        Log.i(Logs.POINT_OF_INTEREST, "In Profile fragment!!!");
                        System.out.println(postByUser);

                        listOfPosts.add(postByUser.getValue(Post.class));

                    }
                    Collections.sort(listOfPosts, new Comparator<Post>() {
                        @Override
                        public int compare(Post post, Post t1) {
                            return post.getName().compareTo(t1.getName());
                        }
                    });
                    Log.i(Logs.POINT_OF_INTEREST, "In Profile Fragment trying to get profile images");
                    ((MainActivity) getActivity()).showProgress(false);
                    adaptor.updateProfileGallery(listOfPosts);
                    adaptor.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.i(Logs.POINT_OF_INTEREST, "Failed retrieving data from firebase from method: getLatestPost");
                    ((MainActivity) getActivity()).showProgress(false);
                }
            });
        
    }

    private void setUpTopView() {

        FirebaseDatabase dbRef = FirebaseDatabase.getInstance();
        Log.i(Logs.POINT_OF_INTEREST, user.getUid());
        DatabaseReference myRef = dbRef.getReference("userProfileInfo").child(user.getUid());

        ((MainActivity) getActivity()).showProgress(true);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot userInfo : dataSnapshot.getChildren()) {
                    Log.i(Logs.POINT_OF_INTEREST, "In Profile fragment!!!");

                    if (userInfo.getKey().contains("userName")) {
                        textView.setText(userInfo.getValue(String.class));
                    } else {
                        Picasso.with(getActivity()).load(userInfo.getValue(String.class)).into(imageView);
                    }
                }
                ((MainActivity) getActivity()).showProgress(false);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i(Logs.POINT_OF_INTEREST, "Failed retrieving data from firebase from method: setUpTopView");
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

    @Override
    public boolean onLongClick(View view) {
        return false;
    }
}

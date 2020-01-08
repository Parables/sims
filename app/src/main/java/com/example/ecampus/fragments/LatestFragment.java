package com.example.ecampus.fragments;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ecampus.R;
import com.example.ecampus.adapters.ViewHolder;
import com.example.ecampus.models.Blog;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;

/**
 * A simple {@link Fragment} subclass.
 */
public class LatestFragment extends Fragment {

    private View view;

    private String thisYear = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
    private String thisMonth = DateFormat.format("MM", new Date()).toString();

    private CollectionReference News =
            FirebaseFirestore.getInstance().
                    collection("news")
                    .document(thisYear).collection(thisMonth);
    private Query query = News.orderBy("date", Query.Direction.DESCENDING);

    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;

    @BindView(R.id.myrecyclerview)
    RecyclerView recyclerView;

    @BindView(R.id.swipeRefresh)
    SwipeRefreshLayout swipeRefreshLayout;


    public LatestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_newsfeed, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView.hasFixedSize();
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        setAdapter();

        swipeRefreshLayout.setRefreshing(true);
        swipeRefreshLayout.setOnRefreshListener(() ->
        {
            refreshRecyclerView();
            swipeRefreshLayout.setRefreshing(false);
        });
    }


    public void refreshRecyclerView() {
    }

    @Override
    public void onStart() {
        super.onStart();
        firebaseRecyclerAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        firebaseRecyclerAdapter.stopListening();
    }

    private void setAdapter() {
        firebaseRecyclerAdapter = NewAdapter();
        recyclerView.setAdapter(firebaseRecyclerAdapter);
    }

    @NonNull
    private FirebaseRecyclerAdapter<Blog, ViewHolder> NewAdapter() {

        FirestoreRecyclerOptions<Blog> options =
                new FirestoreRecyclerOptions.Builder<Blog>()
                        .setQuery(query, Blog.class)
                        .setLifecycleOwner(this).build();

        return new FirebaseRecyclerAdapter<Blog, ViewHolder>(options) {

            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new ViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.news_row, parent, false));
            }

            @Override
            protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Blog model) {
                holder.bind(getActivity(), model);
            }

            @Override
            public void onDataChanged() {
                super.onDataChanged();
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onError(@NonNull DatabaseError error) {
                super.onError(error);
                swipeRefreshLayout.setRefreshing(false);
                Toasty.error(getActivity(), "Firestore error").show();
            }
        };
    }
}

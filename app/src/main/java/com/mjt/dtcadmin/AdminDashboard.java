package com.mjt.dtcadmin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;

public class AdminDashboard extends Fragment {

    public AdminDashboard() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_dashboard, container, false);

        // Initialize cards
        MaterialCardView addNotice = view.findViewById(R.id.addNotice);
        MaterialCardView addGalleryImage = view.findViewById(R.id.addGalleryImage);
        MaterialCardView addEbook = view.findViewById(R.id.addEbook);
        MaterialCardView addGroup = view.findViewById(R.id.addGroup);
        MaterialCardView addDelete = view.findViewById(R.id.addDelete);

        // Set click listeners for MaterialCardViews
        addNotice.setOnClickListener(v -> startActivity(new Intent(getActivity(), UploadNotice.class)));
        addGalleryImage.setOnClickListener(v -> startActivity(new Intent(getActivity(), UploadGallery.class)));
        addEbook.setOnClickListener(v -> startActivity(new Intent(getActivity(), UploadEbook.class)));
        addGroup.setOnClickListener(v -> startActivity(new Intent(getActivity(), FacultyActivity.class)));
        addDelete.setOnClickListener(v -> startActivity(new Intent(getActivity(), DeleteNotice.class)));

        return view;
    }
}

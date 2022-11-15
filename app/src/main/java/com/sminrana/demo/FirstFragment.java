package com.sminrana.demo;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.sminrana.demo.databinding.FragmentFirstBinding;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), VideoActivity.class);
                intent.putExtra("title", "Video One");

                // Make sure video URL is valid now
                // this one has no sound
                intent.putExtra("url", "https://www.shutterstock.com/shutterstock/videos/1094984573/preview/stock-footage-zombie-hand-rising-up-smartphone-with-green-screen-out-of-grave-holiday-event-halloween-concept.mp4");
                startActivity(intent);
            }
        });

        binding.buttonSecond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), VideoActivity.class);
                intent.putExtra("title", "Video Two");

                // Make sure video URL is valid now
                // this one has no sound
                intent.putExtra("url", "https://www.shutterstock.com/shutterstock/videos/1094984573/preview/stock-footage-zombie-hand-rising-up-smartphone-with-green-screen-out-of-grave-holiday-event-halloween-concept.mp4");
                startActivity(intent);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
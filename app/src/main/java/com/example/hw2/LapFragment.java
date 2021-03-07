package com.example.hw2;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class LapFragment extends Fragment {
    private TextView textView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.laps_fragment, container, false);
        textView = v.findViewById(R.id.lap_frag_tv);
        setRetainInstance(true);
        return v;
    }

    public void updateTextView(CharSequence text) {
        textView.setText(text);
    }

}

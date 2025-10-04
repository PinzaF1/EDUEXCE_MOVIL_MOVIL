package com.example.zavira_movil.ui.ranking.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.zavira_movil.Home.SubjectAdapter;
import com.example.zavira_movil.databinding.FragmentIslasBinding;
import com.example.zavira_movil.model.DemoData;

public class IslasFragment extends Fragment {
    private FragmentIslasBinding binding;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentIslasBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        binding.rvSubjects.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvSubjects.setAdapter(new SubjectAdapter(DemoData.subjects()));
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

package com.example.homescreen;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class TermsAndConditionsDialogFragment extends DialogFragment {

    public interface TermsAndConditionsListener {
        void onTermsAccepted();
    }

    private TermsAndConditionsListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (TermsAndConditionsListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement TermsAndConditionsListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_termsand_conditions, container, false);

        Button btnNext = view.findViewById(R.id.btnNext);
        btnNext.setOnClickListener(v -> {
            listener.onTermsAccepted();
            dismiss();
        });

        return view;
    }
}

package com.example.candyfisher.fragments;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.candyfisher.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SuccessFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SuccessFragment extends DialogFragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "imageId";
    //    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "SuccessFragment";
    // TODO: Rename and change types of parameters
    private int imageId;
//    private String mParam2;



    public SuccessFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     *               //     * @param param2 Parameter 2.
     * @return A new instance of fragment SuccessFragment.
     * <p>
     * Fragment for when a catch is succesfull
     */
    // TODO: Rename and change types and number of parameters
    public static SuccessFragment newInstance(String param1) {
        SuccessFragment fragment = new SuccessFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            imageId = getArguments().getInt(ARG_PARAM1);
        }

//        imageView = getView().findViewById(R.id.imageView2);
//        imageView.setImageResource(imageId);

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater layoutInflater = (LayoutInflater) requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.fragment_success, null);
        final Dialog dialog = new Dialog(getActivity());


        dialog.setContentView(layout);
        TextView textView = layout.findViewById(R.id.dialogText);
        textView.setText("Did this work?");
        ImageView imageView = layout.findViewById(R.id.dialogImage);
        imageView.setImageResource(imageId);

        dialog.show();
        return dialog;
//        ImageView imageView = new ImageView(getActivity());
//        imageView.setImageResource(imageId);
//        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
//        alertDialogBuilder.setPositiveButton("OK", (dialogInterface, i) -> Log.i(TAG, "onClick: Success"));
//        alertDialogBuilder.setView(imageView);
//        return alertDialogBuilder.create();

    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_success, container, false);
//    }
}
package com.icanvass.fragments;
import com.icanvass.R;
import com.icanvass.activities.HomeActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A fragment with go to website texts.
 * Use the {@link GoToWebsiteFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class GoToWebsiteFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String TITLE = "title";
    private static final String TOP_TEXT = "top_text";
    private static final String LOGIN_URL = "http://app.spotio.com";

    private String mTop;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param text Parameter 1.
     * @return A new instance of fragment GoToWebsiteFragment.
     */
    public static GoToWebsiteFragment newInstance(String title, String text) {
        GoToWebsiteFragment fragment = new GoToWebsiteFragment();
        Bundle args = new Bundle();
        args.putString(TITLE, title);
        args.putString(TOP_TEXT, text);
        fragment.setArguments(args);
        return fragment;
    }
    public GoToWebsiteFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTop = getArguments().getString(TOP_TEXT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_go_to_website, container, false);
        TextView topTextView = (TextView) view.findViewById(R.id.topTextView);
        topTextView.setText(getArguments().getString(TOP_TEXT));
        TextView linkTextView = (TextView) view.findViewById(R.id.linkTextView);
        linkTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(LOGIN_URL));
                startActivity(browserIntent);
            }
        });

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((HomeActivity) activity).onSectionAttached(
                getArguments().getString(TITLE));
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}

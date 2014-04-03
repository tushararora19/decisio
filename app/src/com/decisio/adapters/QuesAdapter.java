package com.decisio.adapters;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;

import com.decisio.R;

public class QuesAdapter extends ArrayAdapter {

    public QuesAdapter(Context context, List<String> list) {
        super(context, R.layout.quest_parameter, list);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        if (view == null){
            view = LayoutInflater.from(getContext()).inflate(R.layout.quest_parameter, null);
        }

        final String ques = (String) getItem(position);

        CheckedTextView nameView = (CheckedTextView) view.findViewById(R.id.ctv_ques);
        nameView.setText(ques);

        return view;
    }
}

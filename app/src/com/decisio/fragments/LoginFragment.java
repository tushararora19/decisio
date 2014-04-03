package com.decisio.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.decisio.R;

public class LoginFragment extends DialogFragment {

    public interface NotifyActivityListener{
        public void onSignIn(DialogFragment dialogFragment, String locId, String pwd);
        public void onCancelSignIn(DialogFragment dialogFragment);
    }

    private NotifyActivityListener listener;
    private EditText etId;
    private EditText etPwd;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        // Build the dialog and set up the button click handlers
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final View v = inflater.inflate(R.layout.dialog_sign_in, null);
        
        builder .setMessage(R.string.title_manager_login)
                .setView(v)
                .setPositiveButton(R.string.signin, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                
                etId = (EditText) v.findViewById(R.id.et_username);
                etPwd = (EditText) v.findViewById(R.id.et_password);
                
                // Send the positive button event back to the host activity
                listener.onSignIn(LoginFragment.this, etId.getText().toString(), etPwd.getText().toString());
            }
        })
        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Send the negative button event back to the host activity
                listener.onCancelSignIn(LoginFragment.this);
            }
        });
        return builder.create();
    }
    
    public void setListener(NotifyActivityListener listener) {
        this.listener = listener;
    }

}


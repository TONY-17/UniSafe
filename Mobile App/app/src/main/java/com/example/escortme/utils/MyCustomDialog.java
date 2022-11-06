package com.example.escortme.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.escortme.R;
import com.google.android.material.button.MaterialButton;

public class MyCustomDialog extends Dialog {
    public TextView textView;
    public  MaterialButton navigate;
    public final Dialog dialog;
    Context context;
    public MyCustomDialog(@NonNull Context context,Activity activity) {
        super(context);
        this.context = context;
        dialog = new Dialog(activity);
    }

    public void showDialog(String userPickLocation){
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.request_dialog);

        textView = dialog.findViewById(R.id.textView30);
        TextView location = dialog.findViewById(R.id.textView31);
        location.setText(userPickLocation);

        navigate = dialog.findViewById(R.id.navDriver);

        //Animation startAnimation = AnimationUtils.loadAnimation(context, R.anim.dialog_animation);
        //textView.startAnimation(startAnimation);
        dialog.show();
    }

}

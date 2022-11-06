package com.example.escortme.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.escortme.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class MyCustomDialogArrived extends Dialog {
    public  MaterialButton navigateToDropOff;
    public final Dialog dialogDropOff;
    public MaterialCardView containerDropOff;
    public MyCustomDialogArrived(@NonNull Context context, Activity activity) {
        super(context);
        dialogDropOff = new Dialog(activity);
    }

    public void showDialog(String userPickLocation){
        dialogDropOff.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogDropOff.setCancelable(false);
        dialogDropOff.setContentView(R.layout.arrive_dialog);
        containerDropOff = dialogDropOff.findViewById(R.id.containerCard);
        TextView location = dialogDropOff.findViewById(R.id.textView31);
        location.setText(userPickLocation);

        navigateToDropOff = dialogDropOff.findViewById(R.id.navDriver);
        dialogDropOff.show();
    }


}

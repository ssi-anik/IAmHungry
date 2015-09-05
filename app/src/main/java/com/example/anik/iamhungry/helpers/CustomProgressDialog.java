package com.example.anik.iamhungry.helpers;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.ImageView;

import com.example.anik.iamhungry.R;

/**
 * Created by Anik on 03-Aug-15, 003.
 */
public class CustomProgressDialog extends ProgressDialog {
    private AnimationDrawable animation;

    public CustomProgressDialog(Context context) {
        super(context);
    }

    public CustomProgressDialog(Context context, int theme) {
        super(context, theme);
    }

    public static ProgressDialog builder(Context context) {
        CustomProgressDialog dialog = new CustomProgressDialog(context);
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        return dialog;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_progress_dialog);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        ImageView progressDialogAnimation = (ImageView) findViewById(R.id.animation);
        progressDialogAnimation.setBackgroundResource(R.drawable.animation_images_list);
        animation = (AnimationDrawable) progressDialogAnimation.getBackground();
    }

    @Override
    public void show() {
        super.show();
        animation.start();
    }

    @Override
    public void dismiss() {
        super.dismiss();
        animation.stop();
    }
}

package com.yangxianwen.post176.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.yangxianwen.post176.R;
import com.yangxianwen.post176.utils.NavigationBarUtil;


public class ProgressDialog extends AlertDialog {

    public static final int update = 1;
    public static final int loading = 2;

    private ProgressBar progressBar;
    private TextView tvProgress;
    private int maxProgress;

    public ProgressDialog(@NonNull Context context, int type) {
        super(context, R.style.DialogTheme);
        if (type == update) {
            progressBar = (ProgressBar) LayoutInflater.from(context).inflate(R.layout.dialog_horizontal_progress_bar, null);
            tvProgress = new TextView(context);
            LinearLayout linearLayout = new LinearLayout(context);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.addView(progressBar);
            linearLayout.addView(tvProgress);
            setView(linearLayout, 50, 20, 50, 50);
            setCanceledOnTouchOutside(false);
        } else if (type == loading) {
            progressBar = (ProgressBar) LayoutInflater.from(context).inflate(R.layout.dialog_progress_bar, null);
            tvProgress = new TextView(context);
            LinearLayout linearLayout = new LinearLayout(context);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.addView(progressBar);
            linearLayout.addView(tvProgress);
            setView(linearLayout, 50, 50, 50, 50);
            setCanceledOnTouchOutside(false);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NavigationBarUtil.hideNavigationBar(getWindow());
    }

    public void setTitleText(String titleText) {
        setTitle(titleText);
    }

    public void setContentText(String contentText) {
        tvProgress.setText(contentText);
    }

    public void setMaxProgress(int max) {
        if (max > 0) {
            this.maxProgress = max;
            if (progressBar != null) {
                progressBar.setMax(max);
            }
        }
    }


    public void refreshProgress(int progress) {
        if (progressBar != null) {
            progressBar.setProgress(progress);
        }
        if (tvProgress != null) {
            tvProgress.setText(getContext().getString(R.string.progress_dialog_batch_register, progress, maxProgress));
        }
        if (progress == maxProgress) {
            dismiss();
        }
    }

    public void setProgress(int progress) {
        if (progressBar != null) {
            progressBar.setProgress(progress);
        }
    }
}

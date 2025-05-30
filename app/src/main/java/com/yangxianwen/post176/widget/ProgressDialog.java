package com.yangxianwen.post176.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.yangxianwen.post176.R;
import com.yangxianwen.post176.enmu.DialogType;
import com.yangxianwen.post176.utils.NavigationBarUtil;


public class ProgressDialog extends AlertDialog {

    private ProgressBar progressBar;
    private TextView tvProgress;
    private TextView tvTitle;
    private int maxProgress;

    public ProgressDialog(@NonNull Context context, DialogType type) {
        super(context, R.style.DialogTheme);
        if (type == DialogType.update) {
            View view = LayoutInflater.from(context).inflate(R.layout.dialog_horizontal_progress_bar, new FrameLayout(context), true);
            progressBar = view.findViewById(R.id.progress);
            progressBar.getProgressDrawable().setColorFilter(getContext().getResources().getColor(R.color.color_main), PorterDuff.Mode.SRC_IN);
            tvProgress = view.findViewById(R.id.tips);
            tvTitle = view.findViewById(R.id.title);
            setView(view);
            setCanceledOnTouchOutside(false);
            setCancelable(false);
        } else if (type == DialogType.loading) {
            View view = LayoutInflater.from(context).inflate(R.layout.dialog_progress_bar, new FrameLayout(context), true);
            progressBar = view.findViewById(R.id.progress);
            progressBar.getIndeterminateDrawable().setColorFilter(getContext().getResources().getColor(R.color.color_main), PorterDuff.Mode.SRC_IN);
            tvProgress = view.findViewById(R.id.tips);
            setView(view);
            setCanceledOnTouchOutside(false);
            setCancelable(false);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NavigationBarUtil.hideNavigationBar(getWindow());
    }

    public void setTitleText(String titleText) {
        if (tvTitle != null) {
            tvTitle.setText(titleText);
        }
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

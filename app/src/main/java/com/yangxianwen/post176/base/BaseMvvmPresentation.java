package com.yangxianwen.post176.base;

import android.app.Presentation;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.viewbinding.ViewBinding;

import com.yangxianwen.post176.R;

public abstract class BaseMvvmPresentation<VM extends BaseViewModel, VB extends ViewBinding> extends Presentation {

    protected VM mViewModel;
    protected VB mBinding;

    public BaseMvvmPresentation(Context outerContext, Display display) {
        super(outerContext, display, R.style.AppTheme);
    }

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = (VB) DataBindingUtil.inflate(LayoutInflater.from(getContext()), getLayoutId(), null, false);
        setContentView(mBinding.getRoot());
    }

    protected abstract int getLayoutId();

}

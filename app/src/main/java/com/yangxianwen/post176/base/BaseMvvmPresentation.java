package com.yangxianwen.post176.base;

import android.app.Presentation;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;

import androidx.databinding.DataBindingUtil;
import androidx.viewbinding.ViewBinding;

import com.yangxianwen.post176.R;
import com.yangxianwen.post176.manager.LiveDataManager;

public abstract class BaseMvvmPresentation<VM extends BaseViewModel, VB extends ViewBinding> extends Presentation {

    protected final String TAG = getClass().getName();

    protected VM mViewModel;
    protected VB mBinding;

    protected LiveDataManager mLiveDataManager;

    protected abstract int getLayoutId();

    public BaseMvvmPresentation(Context outerContext, Display display, VM viewModel) {
        super(outerContext, display, R.style.AppTheme);
        mViewModel = viewModel;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = (VB) DataBindingUtil.inflate(LayoutInflater.from(getContext()), getLayoutId(), null, false);
        setContentView(mBinding.getRoot());
        mLiveDataManager = new LiveDataManager();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mLiveDataManager.clearAllObservers();
    }
}

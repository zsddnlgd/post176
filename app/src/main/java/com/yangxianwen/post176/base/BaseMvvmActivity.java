package com.yangxianwen.post176.base;


import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewbinding.ViewBinding;

import com.yangxianwen.post176.manager.LiveDataManager;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class BaseMvvmActivity<VM extends BaseViewModel, VB extends ViewBinding> extends BaseActivity {

    protected VM mViewModel;
    protected VB mBinding;

    protected LiveDataManager mLiveDataManager;

    protected abstract int getLayoutId();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = (VB) DataBindingUtil.setContentView(getActivity(), getLayoutId());
        initViewModel();
        mLiveDataManager = new LiveDataManager();
        mLiveDataManager.observeForever(mViewModel.getTips(), s -> {
            if (s == null) {
                return;
            }
            showToast(s);
        });
        mLiveDataManager.observeForever(mViewModel.getCloseLoading(), o -> {
            if (o == null) {
                return;
            }
            dismissLoading();
            dismissUpdateLoading();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getLifecycle().removeObserver(mViewModel);
        mLiveDataManager.clearAllObservers();
    }

    private void initViewModel() {
        ParameterizedType parameterizedType = (ParameterizedType) getClass().getGenericSuperclass();
        if (parameterizedType == null) {
            finish();
            throw new IllegalArgumentException("incorrect base model class param");
        } else {
            Type type = parameterizedType.getActualTypeArguments()[0];
            Class<VM> viewModelClass = (Class<VM>) type;
            mViewModel = (new ViewModelProvider.AndroidViewModelFactory(getApplication())).create(viewModelClass);
            getLifecycle().addObserver(mViewModel);
        }
    }
}

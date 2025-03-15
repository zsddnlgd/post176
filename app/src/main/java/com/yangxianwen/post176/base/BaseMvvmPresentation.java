package com.yangxianwen.post176.base;

import android.app.Presentation;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewbinding.ViewBinding;

import com.yangxianwen.post176.App;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class BaseMvvmPresentation<VM extends BaseViewModel, VB extends ViewBinding> extends Presentation {

    protected VM mViewModel;
    protected VB mBinding;

    public BaseMvvmPresentation(Context outerContext, Display display) {
        super(outerContext, display);
    }

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = (VB) DataBindingUtil.setContentView(getOwnerActivity(), getLayoutId());
        initViewModel();
    }

    private void initViewModel() {
        ParameterizedType parameterizedType = (ParameterizedType) getClass().getGenericSuperclass();
        if (parameterizedType == null) {
            throw new IllegalArgumentException("incorrect base model class param");
        } else {
            Type type = parameterizedType.getActualTypeArguments()[0];
            Class<VM> viewModelClass = (Class<VM>) type;
            mViewModel = (new ViewModelProvider.AndroidViewModelFactory(App.getIns())).create(viewModelClass);
        }
    }

    protected abstract int getLayoutId();

}

package com.yangxianwen.post176.base;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

public class BaseViewModel extends AndroidViewModel implements IBaseViewModel {

    protected final String TAG = getClass().getName();

    protected final MutableLiveData<String> tips = new MutableLiveData<>();

    public BaseViewModel(@NonNull Application application) {
        super(application);
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onStart() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void onAny() {

    }

    public MutableLiveData<String> getTips() {
        return tips;
    }
}

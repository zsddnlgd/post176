package com.yangxianwen.post176.manager;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import java.util.ArrayList;
import java.util.List;

public class LiveDataManager {
    private final List<ObserverWrapper<?>> observers = new ArrayList<>();

    public LiveDataManager() {
    }

    // 注册观察者并保存引用
    public <T> void observeForever(LiveData<T> liveData, Observer<T> observer) {
        liveData.observeForever(observer);
        observers.add(new ObserverWrapper<>(liveData, observer));
    }

    // 批量解除所有观察者
    public void clearAllObservers() {
        for (ObserverWrapper wrapper : observers) {
            wrapper.liveData.removeObserver(wrapper.observer);
        }
        observers.clear();
    }

    // 包装类，关联 LiveData 和 Observer
    private static class ObserverWrapper<T> {
        final LiveData<T> liveData;
        final Observer<T> observer;

        ObserverWrapper(LiveData<T> liveData, Observer<T> observer) {
            this.liveData = liveData;
            this.observer = observer;
        }
    }
}

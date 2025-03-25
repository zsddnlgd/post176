package com.yangxianwen.post176.utils;

import android.content.Context;

import com.arcsoft.face.ActiveFileInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.enums.DetectFaceOrientPriority;
import com.yangxianwen.post176.App;
import com.yangxianwen.post176.face.common.Constants;
import com.yangxianwen.post176.face.util.ConfigUtil;
import com.yangxianwen.post176.interfaca.OnActiveResultListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ActiveUtil {

    //所需的动态库文件
    private static final String[] LIBRARIES = new String[]{
            // 人脸相关
            "libarcsoft_face_engine.so",
            "libarcsoft_face.so",
            // 图像库相关
            "libarcsoft_image_util.so"};

    /**
     * 激活人脸识别引擎
     */
    public static void activeEngine(Context context, OnActiveResultListener listener) {
        boolean libraryExists = checkSoFile(LIBRARIES);
        if (!libraryExists) {
            listener.onActiveResult(false);
            return;
        }
        Observable.create((ObservableOnSubscribe<Integer>) emitter -> {
            int activeCode = FaceEngine.activeOnline(context, Constants.APP_ID, Constants.SDK_KEY);
            emitter.onNext(activeCode);
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Integer activeCode) {
                switch (activeCode) {
                    case ErrorInfo.MOK:
                    case ErrorInfo.MERR_ASF_ALREADY_ACTIVATED:
                        listener.onActiveResult(true);
                        break;
                    default:
                        listener.onActiveResult(false);
                        break;
                }
                ActiveFileInfo activeFileInfo = new ActiveFileInfo();
                FaceEngine.getActiveFileInfo(context, activeFileInfo);
            }

            @Override
            public void onError(Throwable e) {
                listener.onActiveResult(false);
            }

            @Override
            public void onComplete() {

            }
        });

    }

    /**
     * 检查能否找到动态链接库，如果找不到，请修改工程配置
     *
     * @param libraries 需要的动态链接库
     * @return 动态库是否存在
     */
    public static boolean checkSoFile(String[] libraries) {
        File dir = new File(App.getIns().getApplicationInfo().nativeLibraryDir);
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            return false;
        }
        List<String> libraryNameList = new ArrayList<>();
        for (File file : files) {
            libraryNameList.add(file.getName());
        }
        boolean exists = true;
        for (String library : libraries) {
            exists &= libraryNameList.contains(library);
        }
        return exists;
    }
}

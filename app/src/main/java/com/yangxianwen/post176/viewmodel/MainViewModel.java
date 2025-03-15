package com.yangxianwen.post176.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.yangxianwen.post176.base.BaseViewModel;
import com.yangxianwen.post176.bean.Student;
import com.yangxianwen.post176.face.FaceManageActivity;
import com.yangxianwen.post176.utils.FileUtils;
import com.yangxianwen.post176.utils.HttpUtil;
import com.yangxianwen.post176.utils.SpUtil;

import java.util.ArrayList;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import okhttp3.ResponseBody;

public class MainViewModel extends BaseViewModel {

    private final MutableLiveData<Integer> studentSize = new MutableLiveData<>();
    private final MutableLiveData<Integer> studentProgress = new MutableLiveData<>();
    private final MutableLiveData<Boolean> studentFinish = new MutableLiveData<>();

    public MainViewModel(@NonNull Application application) {
        super(application);
    }

    public void registerStatusPic() {
        HttpUtil.getStudent(new Observer<>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(ArrayList<Student> students) {
                ArrayList<Student> subList = new ArrayList<>();
                for (Student student : students) {
                    //去掉无效图片
                    if (student.getCPic() == null) {
                        continue;
                    }
                    if ("".equals(student.getCPic())) {
                        continue;
                    }
                    if ("/Pic/StuImg/moren.png".equals(student.getCPic())) {
                        continue;
                    }
                    //先处理一年级数据
                    if (student.getCStudCode().startsWith("24")) {
                        subList.add(student);
                        SpUtil.putStudent(student.getCPic(), student);
                    }
                }

                studentSize.setValue(subList.size());

                //递归下载图片
                downLoadPhoto(subList, subList.size());
            }

            @Override
            public void onError(Throwable e) {
                tips.postValue("网络异常：" + e.getMessage());
                studentFinish.postValue(true);
            }

            @Override
            public void onComplete() {

            }
        });
    }

    private void downLoadPhoto(ArrayList<Student> students, int size) {
        String picPath = students.get(0).getCPic();
        String filePath = picPath.replace("/Pic/StuImg", FaceManageActivity.REGISTER_DIR);

        HttpUtil.downloadStudentPhoto(picPath, new Observer<>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(ResponseBody responseBody) {
                FileUtils.savePhoto(filePath, responseBody.byteStream());

                students.remove(0);
                studentProgress.postValue(size - students.size());

                if (!students.isEmpty()) {
                    downLoadPhoto(students, size);
                } else {
                    studentFinish.postValue(true);
                }
            }

            @Override
            public void onError(Throwable e) {
                tips.postValue("网络异常：" + e.getMessage());
                studentFinish.postValue(true);
            }

            @Override
            public void onComplete() {

            }
        });
    }

    public MutableLiveData<Integer> getStudentSize() {
        return studentSize;
    }

    public MutableLiveData<Integer> getStudentProgress() {
        return studentProgress;
    }

    public MutableLiveData<Boolean> getStudentFinish() {
        return studentFinish;
    }
}

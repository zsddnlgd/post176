package com.yangxianwen.post176.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.yangxianwen.post176.base.BaseViewModel;
import com.yangxianwen.post176.bean.Nfc;
import com.yangxianwen.post176.bean.Order;
import com.yangxianwen.post176.bean.Result;
import com.yangxianwen.post176.bean.Student;
import com.yangxianwen.post176.bean.StudentSports;
import com.yangxianwen.post176.utils.FileUtil;
import com.yangxianwen.post176.utils.GsonUtil;
import com.yangxianwen.post176.utils.HttpUtil;
import com.yangxianwen.post176.utils.SpUtil;
import com.yangxianwen.post176.values.Constants;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

public class MainViewModel extends BaseViewModel {

    private final MutableLiveData<Integer> studentSize = new MutableLiveData<>();
    private final MutableLiveData<Integer> studentProgress = new MutableLiveData<>();
    private final MutableLiveData<Object> syncFinish = new MutableLiveData<>();

    private boolean canDownload;

    public MainViewModel(@NonNull Application application) {
        super(application);
    }

    public void registerStatusPic() {
        canDownload = true;
        HttpUtil.getStudent(new Observer<>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(ArrayList<Student> students) {
                SpUtil.putStudent(students);

                String[] imgFileDir = new File(FileUtil.REGISTER_DIR).list();
                List<String> imgFiles = imgFileDir == null ? new ArrayList<>() : Arrays.asList(imgFileDir);

                Log.i(TAG, "registerStatusPic imgFiles = " + imgFiles);

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
                    //去掉重复图片
                    if (imgFiles.contains(student.getCPic().replace("/Pic/StuImg/", ""))) {
                        continue;
                    }
                    subList.add(student);
                }
                if (subList.isEmpty()) {
                    tips.postValue("您的数据已为最新了！");
                    return;
                }

                studentSize.postValue(subList.size());

                //递归下载图片
                downLoadPhoto(subList, subList.size());
            }

            @Override
            public void onError(Throwable e) {
                closeLoading.postValue(new Object());
                tips.postValue("更新数据失败，网络异常：" + e.getMessage());
            }

            @Override
            public void onComplete() {

            }
        });
    }

    private void downLoadPhoto(ArrayList<Student> students, int size) {
        String picPath = students.get(0).getCPic();
        String filePath = picPath.replace("/Pic/StuImg", FileUtil.REGISTER_DIR);

        HttpUtil.downloadStudentPhoto(picPath, new Observer<>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(ResponseBody responseBody) {
                boolean result = FileUtil.savePhoto(filePath, responseBody.byteStream());
                if (!result) {
                    Log.e(TAG, "savePhoto fail");
                }

                students.remove(0);
                studentProgress.postValue(size - students.size());

                if (!students.isEmpty() && canDownload) {
                    Observable.timer(300, TimeUnit.MILLISECONDS)
                            .subscribeOn(Schedulers.io())
                            .observeOn(Schedulers.io())
                            .blockingSubscribe(aLong -> downLoadPhoto(students, size));
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "downloadPhoto fail message = " + e.getMessage());

                students.remove(0);
                studentProgress.postValue(size - students.size());

                if (!students.isEmpty() || !canDownload) {
                    Observable.timer(300, TimeUnit.MILLISECONDS)
                            .subscribeOn(Schedulers.io())
                            .observeOn(Schedulers.io())
                            .blockingSubscribe(aLong -> downLoadPhoto(students, size));
                }
            }

            @Override
            public void onComplete() {

            }
        });
    }

    public void syncStudentInfo() {
        HttpUtil.getStudent(new Observer<>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(ArrayList<Student> students) {
                closeLoading.postValue(new Object());
                SpUtil.putStudent(students);
            }

            @Override
            public void onError(Throwable e) {
                closeLoading.postValue(new Object());
            }

            @Override
            public void onComplete() {

            }
        });
    }

    public void syncStudentNfc() {
        HttpUtil.getNfc(new Observer<>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(ArrayList<Nfc> nfcs) {
                SpUtil.setStudentNfc(nfcs);
                syncStudentSports();
            }

            @Override
            public void onError(Throwable e) {
                syncStudentSports();
            }

            @Override
            public void onComplete() {

            }
        });
    }

    public void syncStudentSports() {
        HttpUtil.getStudentSports(new Observer<>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(ArrayList<StudentSports> studentSports) {
                closeLoading.postValue(new Object());
                SpUtil.putStudentSports(studentSports);
                syncFinish.postValue(new Object());
            }

            @Override
            public void onError(Throwable e) {
                closeLoading.postValue(new Object());
                syncFinish.postValue(new Object());
            }

            @Override
            public void onComplete() {

            }
        });
    }

    public void uploadOrder() {
        ArrayList<Order> orders = SpUtil.getOrders();
        if (orders.isEmpty()) {
            tips.postValue("您的订单已全部上传！");
            closeLoading.postValue(new Object());
            return;
        }

        HashMap<String, ArrayList<Order>> requestMap = new HashMap<>();
        requestMap.put("requests", orders);
        String jsonRequest = GsonUtil.objToJson(requestMap);

        RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=UTF-8"), jsonRequest);

        HttpUtil.createOrder(body, new Observer<>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Result result) {
                closeLoading.postValue(new Object());
                if (result.getCode() == Constants.NFC_ID_UPDATE_SUCCESS) {
                    SpUtil.clearOrders();
                    tips.postValue("订单上传成功！");
                } else {
                    tips.postValue("订单上传失败：" + result.getMessage());
                }
            }

            @Override
            public void onError(Throwable e) {
                closeLoading.postValue(new Object());
                tips.postValue("订单上传失败：" + e.getMessage());
            }

            @Override
            public void onComplete() {

            }
        });
    }

    public synchronized void cancelDownload() {
        canDownload = false;
    }

    public MutableLiveData<Integer> getStudentSize() {
        return studentSize;
    }

    public MutableLiveData<Integer> getStudentProgress() {
        return studentProgress;
    }

    public MutableLiveData<Object> getSyncFinish() {
        return syncFinish;
    }
}

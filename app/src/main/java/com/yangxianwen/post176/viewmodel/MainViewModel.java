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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

public class MainViewModel extends BaseViewModel {

    private final MutableLiveData<Integer> studentSize = new MutableLiveData<>();
    private final MutableLiveData<Integer> studentProgress = new MutableLiveData<>();
    private final MutableLiveData<Object> syncFinish = new MutableLiveData<>();
    private final ExecutorService executorService;
    private int successNumber;
    private int failNumber;

    public MainViewModel(@NonNull Application application) {
        super(application);
        executorService = Executors.newSingleThreadExecutor();
    }

    public void registerStatusPic() {
        successNumber = 0;
        failNumber = 0;
        HttpUtil.getStudent(new Observer<>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(ArrayList<Student> students) {
                SpUtil.putStudent(students);

                String[] imgFileDir = new File(FileUtil.REGISTER_DIR).list();
                ArrayList<String> imgFiles = imgFileDir == null ? new ArrayList<>() : new ArrayList<>(Arrays.asList(imgFileDir));

                ArrayList<String> containList = new ArrayList<>();
                ArrayList<String> downloadList = new ArrayList<>();
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
                    String picName = student.getCPic().replace("/Pic/StuImg/", "");
                    if (imgFiles.contains(picName)) {
                        containList.add(picName);
                    } else {
                        downloadList.add(picName);
                    }
                }

                //删除库中没有的数据
                executorService.execute(() -> {
                    imgFiles.removeAll(containList);
                    Log.i(TAG, "delete imgFiles size = " + imgFiles.size());
                    for (String imgFile : imgFiles) {
                        File registerFile = new File(FileUtil.REGISTER_DIR + File.separator + imgFile);
                        if (registerFile.exists() && registerFile.delete()) {
                            Log.i(TAG, "deleteRegisterFile fileName = " + imgFile);
                        }
                        File saveImgFile = new File(FileUtil.SAVE_IMG_DIR + File.separator + imgFile);
                        if (saveImgFile.exists() && saveImgFile.delete()) {
                            Log.i(TAG, "deleteSaveImgFile fileName = " + imgFile);
                        }
                        File saveFeatureFile = new File(FileUtil.SAVE_FEATURE_DIR + File.separator + imgFile);
                        if (saveFeatureFile.exists() && saveFeatureFile.delete()) {
                            Log.i(TAG, "deleteSaveFeatureFile fileName = " + imgFile);
                        }
                    }
                });

                if (downloadList.isEmpty()) {
                    closeLoading.postValue(new Object());
                    tips.postValue("您的数据已为最新了！");
                    return;
                }

                //递归下载图片
                downLoadPhoto(downloadList, downloadList.size());
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

    private void downLoadPhoto(ArrayList<String> downloadList, int size) {
        String downPath = "/Pic/StuImg/" + downloadList.get(0);
        String savePath = FileUtil.REGISTER_DIR + File.separator + downloadList.get(0);
        HttpUtil.downloadStudentPhoto(downPath, new Observer<>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(ResponseBody responseBody) {
                successNumber++;

                FileUtil.getInstance().savePhoto(savePath, responseBody.byteStream());

                downLoadPhotoTimer(downloadList, size);
            }

            @Override
            public void onError(Throwable e) {
                failNumber++;

                Log.e(TAG, "downloadPhoto fail message = " + e.getMessage());

                downLoadPhotoTimer(downloadList, size);
            }

            @Override
            public void onComplete() {

            }
        });
    }

    private void downLoadPhotoTimer(ArrayList<String> downloadList, int size) {
        downloadList.remove(0);
        studentSize.postValue(size);
        studentProgress.postValue(size - downloadList.size());

        if (downloadList.isEmpty()) {
            tips.postValue("下载图片完成：成功" + successNumber + "个，" + "失败" + failNumber + "个");
            return;
        }

        Observable.timer(500, TimeUnit.MILLISECONDS)
                .subscribe(new DisposableObserver<>() {
                    @Override
                    public void onNext(Long aLong) {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        downLoadPhoto(downloadList, size);
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

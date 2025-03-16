package com.yangxianwen.post176;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.yangxianwen.post176.base.BaseMvvmActivity;
import com.yangxianwen.post176.bean.Meal;
import com.yangxianwen.post176.bean.Student;
import com.yangxianwen.post176.databinding.DisplayOrderBinding;
import com.yangxianwen.post176.face.faceserver.FaceServer;
import com.yangxianwen.post176.resolver.BarcodeScannerResolver;
import com.yangxianwen.post176.utils.SpUtil;
import com.yangxianwen.post176.viewmodel.OrderViewModel;
import com.yangxianwen.post176.widget.ProgressDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class OrderActivity extends BaseMvvmActivity<OrderViewModel, DisplayOrderBinding> {

    private OrderDisplay orderDisplay;

    private ProgressDialog mProgressDialog;

    private final ArrayList<View> items = new ArrayList<>();

    private double totalPrice = 0;

    private BarcodeScannerResolver mBarcodeScannerResolver;

    @Override
    protected int getLayoutId() {
        return R.layout.display_order;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initObserveForever();

        initFace();

        initBarcodeScanner();

        initListener();

        mProgressDialog = new ProgressDialog(getActivity(), ProgressDialog.loading);
        mProgressDialog.setContentText("正在获取菜单...");
        mProgressDialog.show();
        mViewModel.getMeal();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBinding.face.destroy();
        mBarcodeScannerResolver.removeScanSuccessListener();
    }

    @Override
    public void afterRequestPermission(int requestCode, boolean isAllGranted) {

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean isIntercept = mBarcodeScannerResolver.resolveKeyEvent(event);
        if (isIntercept) {
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    private void initObserveForever() {
        mLiveDataManager.observeForever(mViewModel.getFinish(), o -> {
            if (o != null) {
                finish();
            }
        });

        mLiveDataManager.observeForever(mViewModel.getName(), s -> {
            if (s == null) {
                return;
            }
            mBinding.cStudName.setText(s);
        });

        mLiveDataManager.observeForever(mViewModel.getClassName(), s -> {
            if (s == null) {
                return;
            }
            mBinding.cClass.setText(s);
        });

        mLiveDataManager.observeForever(mViewModel.getLoading(), aBoolean -> {
            if (aBoolean == null) {
                return;
            }
            if (!aBoolean) {
                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
            }
        });

        mLiveDataManager.observeForever(mViewModel.getMealList(), meals -> {
            if (meals == null) {
                return;
            }

            items.clear();
            mBinding.stapleFoodContainer1.removeAllViews();
            mBinding.stapleFoodContainer2.removeAllViews();
            mBinding.sideDishContainer1.removeAllViews();
            mBinding.sideDishContainer2.removeAllViews();
            mBinding.sideDishContainer3.removeAllViews();
            mBinding.specialMealContainer.removeAllViews();
            mBinding.specialDishSoupContainer.removeAllViews();

            for (Meal meal : meals) {
                if ("主食".equals(meal.getCFoodType())) {
                    addFood(meal, mBinding.stapleFoodContainer1, mBinding.stapleFoodContainer2);
                } else if ("副食".equals(meal.getCFoodType())) {
                    addFood(meal, mBinding.sideDishContainer1, mBinding.sideDishContainer2, mBinding.sideDishContainer3);
                } else if ("特色餐".equals(meal.getCFoodType())) {
                    addFood(meal, mBinding.specialMealContainer);
                } else if ("特惠菜".equals(meal.getCFoodType())) {
                    addFood(meal, mBinding.specialDishSoupContainer);
                }
            }
        });

        mLiveDataManager.observeForever(mViewModel.getMealEmptyList(), meals -> {
            if (meals == null) {
                return;
            }

            for (View item : items) {
                View emptyView = item.findViewById(R.id.food_empty);
                Meal itemMeal = (Meal) item.getTag();
                emptyView.setVisibility(View.GONE);
                item.setEnabled(true);
                for (Meal meal : meals) {
                    if (Objects.equals(itemMeal.getCFoodCode(), meal.getCFoodCode())) {
                        item.setEnabled(false);
                        emptyView.setVisibility(View.VISIBLE);
                        break;
                    }
                }
            }
        });
    }

    private void initFace() {
        mBinding.face.init();
        mBinding.face.setRecognizeResultListener(result -> {
            if (result == null) {
                return;
            }

            mBinding.face.stop();

            File imgFile = new File(FaceServer.ROOT_PATH + File.separator + FaceServer.SAVE_IMG_DIR +
                    File.separator + result.getUserName() + FaceServer.IMG_SUFFIX);
            Glide.with(getActivity()).load(imgFile).into(mBinding.faceIcon);

            mViewModel.setStatusInfo(result.getUserName());
            mBinding.confirmButton.setVisibility(View.VISIBLE);
            mBinding.nextButton.setVisibility(View.VISIBLE);
            //显示副屏
            showPresentation(result.getUserName());
        });

    }

    private void initBarcodeScanner() {
        mBarcodeScannerResolver = new BarcodeScannerResolver();
        mBarcodeScannerResolver.setScanSuccessListener(barcode -> {
            if (barcode == null || barcode.length() != 17) {
                return;
            }
            barcode = barcode.replace("0000000", "");
            Student student = SpUtil.getStudentByNfc(barcode);
            if (student == null) {
                showToast("未获取到绑定学生信息");
                return;
            }

            String userName = student.getCPic().replace("/Pic/StuImg/", "")
                    .replace(".jpg", "");

            mBinding.face.stop();

            File imgFile = new File(FaceServer.ROOT_PATH + File.separator + FaceServer.SAVE_IMG_DIR +
                    File.separator + userName + FaceServer.IMG_SUFFIX);
            Glide.with(getActivity()).load(imgFile).into(mBinding.faceIcon);

            mViewModel.setStatusInfo(student);
            mBinding.confirmButton.setVisibility(View.VISIBLE);
            mBinding.nextButton.setVisibility(View.VISIBLE);
            //显示副屏
            showPresentation(userName);
        });
    }

    private void initListener() {
        mBinding.confirmButton.setOnClickListener(v -> {
            if (totalPrice == 0) {
                showToast("您还未选择任何菜品！");
                return;
            }

            orderDisplay.onSelectConfirm();

            mProgressDialog.setContentText("正在打餐...");
            mProgressDialog.show();
        });
        mBinding.nextButton.setOnClickListener(v -> {
            //取消打餐
            orderFinish();
        });
    }

    private void addFood(Meal meal, LinearLayout... linearLayouts) {
        LinearLayout layout;
        if (linearLayouts[0].getChildCount() < 3) {
            layout = linearLayouts[0];
        } else if (linearLayouts.length > 1 && linearLayouts[1].getChildCount() < 3) {
            layout = linearLayouts[1];
        } else {
            layout = linearLayouts[2];
        }

        View item = LayoutInflater.from(getActivity()).inflate(R.layout.item_food, new FrameLayout(getActivity()), false);
        TextView name = item.findViewById(R.id.food_name);
        TextView price = item.findViewById(R.id.food_price);
        name.setText(meal.getCFoodName());
        price.setText(String.format(Locale.getDefault(), "%.1f元", meal.getNSum()));
        layout.addView(item);

        item.setSelected(false);
        item.setTag(meal);
        item.setOnClickListener(v -> {
            View selectView = v.findViewById(R.id.food_select);
            if (mViewModel.containsMealSelect(meal)) {
                selectView.setVisibility(View.GONE);
                mViewModel.removeMealSelect(meal);
            } else {
                selectView.setVisibility(View.VISIBLE);
                mViewModel.addMealSelect(meal);
            }
            //更新总价
            updatePrice();
        });
        items.add(item);
    }

    private void updatePrice() {
        totalPrice = 0;

        for (View item : items) {
            Meal meal = (Meal) item.getTag();
            if (mViewModel.containsMealSelect(meal)) {
                totalPrice += meal.getNSum();
            }
        }
        mBinding.totalAmountText.setText(String.format(Locale.getDefault(), "总金额：%.1f元", totalPrice));
    }

    private void showPresentation(String userName) {
        orderDisplay = new OrderDisplay(this, getPresentationDisplays());
        orderDisplay.setViewModel(mViewModel);
        orderDisplay.setOnDismissListener(dialog -> {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
            orderFinish();
        });
        orderDisplay.setUserName(userName);
        orderDisplay.show();
    }

    private void orderFinish() {
        mBinding.face.reStart();

        mViewModel.clearStatusInfo();

        mBinding.confirmButton.setVisibility(View.GONE);
        mBinding.nextButton.setVisibility(View.GONE);

        mViewModel.getMealSelectList().setValue(new ArrayList<>());
        for (View item : items) {
            View selectView = item.findViewById(R.id.food_select);
            selectView.setVisibility(View.GONE);
            View emptyView = item.findViewById(R.id.food_empty);
            Meal meal = (Meal) item.getTag();
            if (mViewModel.containsMealEmpty(meal)) {
                emptyView.setVisibility(View.VISIBLE);
            } else {
                emptyView.setVisibility(View.GONE);
            }
        }
        //更新总价
        updatePrice();
    }
}

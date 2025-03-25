package com.yangxianwen.post176;

import android.app.AlertDialog;
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
import com.yangxianwen.post176.databinding.ActivityOrderBinding;
import com.yangxianwen.post176.enmu.OrderStatus;
import com.yangxianwen.post176.resolver.BarcodeScannerResolver;
import com.yangxianwen.post176.utils.FileUtil;
import com.yangxianwen.post176.utils.NavigationBarUtil;
import com.yangxianwen.post176.utils.SpUtil;
import com.yangxianwen.post176.viewmodel.OrderViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class OrderActivity extends BaseMvvmActivity<OrderViewModel, ActivityOrderBinding> {

    private OrderDisplay orderDisplay;

    private final ArrayList<View> items = new ArrayList<>();

    private BarcodeScannerResolver mBarcodeScannerResolver;

    private Student currentStudent = null;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_order;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initFaceRecognize();

        initBarcodeScanner();

        initObserveForever();

        initListener();

        getMeal();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyFaceRecognize();
        destroyBarcodeScanner();
    }

    @Override
    public void afterRequestPermission(int requestCode, boolean isAllGranted) {

    }

    @Override
    public boolean needHideNavigationBar() {
        return true;
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
        mLiveDataManager.observeForever(mViewModel.getFinish(), s -> {
            if (s != null) {
                AlertDialog alertDialog = new AlertDialog.Builder(this, R.style.AlertDialogTheme)
                        .setTitle(R.string.batch_process_notification)
                        .setMessage(s)
                        .setPositiveButton(R.string.ok, (dialog, which) -> finish())
                        .setCancelable(false)
                        .create();
                NavigationBarUtil.hideNavigationBar(alertDialog.getWindow());
                alertDialog.show();
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

        mLiveDataManager.observeForever(mViewModel.getBalance(), s -> {
            if (s == null) {
                return;
            }
            mBinding.nBalance.setText(s);
        });

        mLiveDataManager.observeForever(mViewModel.getNfcNumber(), s -> {
            if (s == null) {
                return;
            }
            mBinding.nfcId.setText(s);
        });

        mLiveDataManager.observeForever(mViewModel.getStepNumber(), s -> {
            if (s == null) {
                return;
            }
            mBinding.iStep.setText(s);
        });

        mLiveDataManager.observeForever(mViewModel.getDistance(), s -> {
            if (s == null) {
                return;
            }
            mBinding.iDistance.setText(s);
        });

        mLiveDataManager.observeForever(mViewModel.getCalorie(), s -> {
            if (s == null) {
                return;
            }
            mBinding.iCalorie.setText(s);
        });

        mLiveDataManager.observeForever(mViewModel.getHasFailOrder(), aBoolean -> {
            if (aBoolean == null) {
                return;
            }
            if (aBoolean) {
                mBinding.failOrder.setVisibility(View.VISIBLE);
            } else {
                mBinding.failOrder.setVisibility(View.INVISIBLE);
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
            mBinding.specialDishContainer.removeAllViews();
            mBinding.specialSoupContainer.removeAllViews();

            for (Meal meal : meals) {
                if ("主食".equals(meal.getCFoodType())) {
                    addFood(meal, mBinding.stapleFoodContainer1, mBinding.stapleFoodContainer2);
                } else if ("副食".equals(meal.getCFoodType())) {
                    addFood(meal, mBinding.sideDishContainer1, mBinding.sideDishContainer2, mBinding.sideDishContainer3);
                } else if ("特色餐".equals(meal.getCFoodType())) {
                    if ("晚餐".equals(meal.getCName())) {
                        mBinding.specialMealTitle.setText(getResources().getString(R.string.night_snack));
                    } else {
                        mBinding.specialMealTitle.setText(getResources().getString(R.string.star_anis));
                    }
                    addFood(meal, mBinding.specialMealContainer);
                } else if ("特惠菜".equals(meal.getCFoodType())) {
                    addFood(meal, mBinding.specialDishContainer);
                } else if ("免费汤".equals(meal.getCFoodType())) {
                    addFood(meal, mBinding.specialSoupContainer);
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
                emptyView.setVisibility(View.INVISIBLE);
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

        mLiveDataManager.observeForever(mViewModel.getOrderResult(), o -> {
            if (o == null) {
                return;
            }

            showLoading("正在打餐，请等待...");
            orderDisplay.onSelectConfirm();
        });
    }

    private void initFaceRecognize() {
        mBinding.face.initView();
        mBinding.face.setRecognizeResultListener(result -> {
            Student student = SpUtil.getStudentByPic(String.format("/Pic/StuImg/%s.jpg", result.getUserName()));
            showStudentInfo(student);
        });
    }

    private void destroyFaceRecognize() {
        mBinding.face.destroyView();
        mBinding.face.removeRecognizeResultListener();
    }

    private void initBarcodeScanner() {
        mBarcodeScannerResolver = new BarcodeScannerResolver();
        mBarcodeScannerResolver.setScanSuccessListener(barcode -> {
            if (barcode == null || barcode.length() != 17) {
                return;
            }
            barcode = barcode.replaceFirst("0000000", "");
            if (currentStudent == null) {
                Student student = SpUtil.getStudentByNfc(barcode);
                showStudentInfo(student);
            } else {
                showLoading("正在绑定NFC...");
                mViewModel.bindRfid(barcode, currentStudent);
            }
        });
    }

    private void destroyBarcodeScanner() {
        mBarcodeScannerResolver.removeScanSuccessListener();
        mBarcodeScannerResolver = null;
    }

    private void initListener() {
        mBinding.confirmButton.setOnClickListener(v -> {
            v.setClickable(false);

            if (currentStudent == null) {
                showToast("学生信息为空！");
                v.setClickable(true);
                return;
            }
            ArrayList<Meal> meals = mViewModel.getMealSelectList().getValue();
            if (meals == null || meals.isEmpty()) {
                showToast("您还未选择任何菜品！");
                v.setClickable(true);
                return;
            }
            double studentBalance = mViewModel.getStudentBalance();
            if (studentBalance < 0) {
                showToast("您的余额不足！");
                v.setClickable(true);
                return;
            }

            showLoading("正在打餐，请等待...");
            orderDisplay.onSelectConfirm();

            mViewModel.createLocalOrder(currentStudent, studentBalance, meals);
        });
        mBinding.nextButton.setOnClickListener(v -> {
            v.setClickable(false);
            //取消打餐
            orderDisplay.dismiss();
        });
    }

    private void getMeal() {
        showLoading("正在获取菜单...");
        mViewModel.getMeal();
    }

    private void getBalance(Student student) {
        showLoading("正在获取账户余额...");
        mViewModel.getBalances(student);
    }

    private void addFood(Meal meal, LinearLayout... linearLayouts) {
        LinearLayout layout;
        if (linearLayouts[0].getChildCount() < 3) {
            layout = linearLayouts[0];
        } else if (linearLayouts.length > 1 && linearLayouts[1].getChildCount() < 3) {
            layout = linearLayouts[1];
        } else if (linearLayouts.length > 2) {
            layout = linearLayouts[2];
        } else {
            return;
        }

        View item = LayoutInflater.from(getActivity()).inflate(R.layout.item_food, new FrameLayout(getActivity()), false);
        TextView name = item.findViewById(R.id.food_name);
        TextView price = item.findViewById(R.id.food_price);
        name.setText(meal.getCFoodName());
        price.setText(String.format(Locale.getDefault(), "%.2f元", meal.getNSum()));

        item.setTag(meal);
        item.setOnClickListener(v -> {
            if (mViewModel.cantPick()) {
                return;
            }

            View selectView = v.findViewById(R.id.food_select);
            if (mViewModel.containsMealSelect(meal)) {
                selectView.setVisibility(View.INVISIBLE);
                mViewModel.removeMealSelect(meal);
            } else {
                selectView.setVisibility(View.VISIBLE);
                mViewModel.addMealSelect(meal);
            }
            //更新总价
            updatePrice();
            //更新卡路里建议
            updateCalorie();
        });

        layout.addView(item);
        items.add(item);
    }

    private void updatePrice() {
        double totalPrice = 0;

        for (View item : items) {
            Meal meal = (Meal) item.getTag();
            if (mViewModel.containsMealSelect(meal)) {
                totalPrice += meal.getNSum();
            }
        }
        mBinding.totalAmountText.setText(String.format(Locale.getDefault(), "总金额：%.2f元", totalPrice));
    }

    private void updateCalorie() {
        if (!mViewModel.hasSportsData()) {
            for (View item : items) {
                item.setBackgroundResource(R.drawable.bg_food);
            }
            return;
        }

        for (View item : items) {
            Meal meal = (Meal) item.getTag();
            if (mViewModel.containsMealSelect(meal)) {
                item.setBackgroundResource(R.drawable.bg_food);
                continue;
            }
            if (mViewModel.suggestBuy(meal)) {
                item.setBackgroundResource(R.drawable.bg_food_select);
            } else {
                item.setBackgroundResource(R.drawable.bg_food_empty);
            }
        }
    }

    private void showPresentation(Student student) {
        orderDisplay = new OrderDisplay(this, getPresentationDisplays());
        orderDisplay.setViewModel(mViewModel);
        orderDisplay.setOnDismissListener(dialog -> {
            dismissLoading();
            orderFinish();
        });
        orderDisplay.setStudent(student);
        orderDisplay.show();
    }

    private void orderFinish() {
        currentStudent = null;

        mBinding.face.reStart();
        Glide.with(getActivity()).load(R.color.transparent).into(mBinding.faceIcon);

        mViewModel.getOrderStatus().setValue(OrderStatus.identify);
        mViewModel.clearStatusInfo();

        mBinding.confirmButton.setVisibility(View.INVISIBLE);
        mBinding.nextButton.setVisibility(View.INVISIBLE);

        mViewModel.getMealSelectList().setValue(new ArrayList<>());
        for (View item : items) {
            View selectView = item.findViewById(R.id.food_select);
            selectView.setVisibility(View.INVISIBLE);
            View emptyView = item.findViewById(R.id.food_empty);
            Meal meal = (Meal) item.getTag();
            if (mViewModel.containsMealEmpty(meal)) {
                emptyView.setVisibility(View.VISIBLE);
            } else {
                emptyView.setVisibility(View.INVISIBLE);
            }
        }

        //更新总价
        updatePrice();
        //更新卡路里建议
        updateCalorie();
    }

    private void showStudentInfo(Student student) {
        if (student == null) {
            showToast("未获取到学生信息");
            return;
        }

        currentStudent = student;

        String filePath = student.getCPic().replace("/Pic/StuImg", FileUtil.REGISTER_DIR);
        Glide.with(getActivity()).load(new File(filePath)).into(mBinding.faceIcon);

        mViewModel.getOrderStatus().setValue(OrderStatus.pick);
        mViewModel.showStatusInfo(student);

        mBinding.confirmButton.setClickable(true);
        mBinding.nextButton.setClickable(true);
        mBinding.confirmButton.setVisibility(View.VISIBLE);
        mBinding.nextButton.setVisibility(View.VISIBLE);

        //更新总价
        updatePrice();
        //更新卡路里建议
        updateCalorie();
        //显示副屏
        showPresentation(student);
        //实时查询余额
        getBalance(student);
    }
}

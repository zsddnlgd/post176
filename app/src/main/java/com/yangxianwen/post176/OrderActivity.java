package com.yangxianwen.post176;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.yangxianwen.post176.base.BaseMvvmActivity;
import com.yangxianwen.post176.bean.Meal;
import com.yangxianwen.post176.databinding.DisplayOrderBinding;
import com.yangxianwen.post176.viewmodel.OrderViewModel;
import com.yangxianwen.post176.widget.ProgressDialog;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class OrderActivity extends BaseMvvmActivity<OrderViewModel, DisplayOrderBinding> {

    private OrderDisplay orderDisplay;

    private ProgressDialog mProgressDialog;

    private final ArrayList<View> items = new ArrayList<>();

    private double totalPrice = 0;

    @Override
    protected int getLayoutId() {
        return R.layout.display_order;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initObserveForever();

        initFace();

        initListener();

        mProgressDialog = new ProgressDialog(getActivity(), ProgressDialog.loading);
        mProgressDialog.setContentText("正在获取菜单...");
        mProgressDialog.show();
        mViewModel.getMeal();
    }

    @Override
    public void afterRequestPermission(int requestCode, boolean isAllGranted) {

    }

    private void initObserveForever() {
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
                Meal itemMeal = (Meal) item.getTag();
                if (mViewModel.containsMealSelect(itemMeal)) {
                    item.setBackgroundResource(R.drawable.bg_food_select);
                } else {
                    item.setBackgroundResource(R.drawable.bg_food);
                }
                item.setEnabled(true);
                for (Meal meal : meals) {
                    if (Objects.equals(itemMeal.getCFoodCode(), meal.getCFoodCode())) {
                        item.setEnabled(false);
                        item.setBackgroundResource(R.drawable.bg_food_empty);
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
            mViewModel.getStatusInfo(result.getUserName());
            mBinding.confirmButton.setVisibility(View.VISIBLE);
            //显示副屏
            showPresentation(result.getUserName());
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
            if (mViewModel.containsMealSelect(meal)) {
                v.setBackgroundResource(R.drawable.bg_food);
                mViewModel.removeMealSelect(meal);
            } else {
                v.setBackgroundResource(R.drawable.bg_food_select);
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

            mBinding.face.reStart();

            mViewModel.getStatusInfo("null");

            mBinding.confirmButton.setVisibility(View.GONE);

            mViewModel.getMealSelectList().setValue(new ArrayList<>());
            for (View item : items) {
                Meal meal = (Meal) item.getTag();
                if (mViewModel.containsMealEmpty(meal)) {
                    item.setBackgroundResource(R.drawable.bg_food_empty);
                } else {
                    item.setBackgroundResource(R.drawable.bg_food);
                }
            }

            //更新总价
            updatePrice();
        });
        orderDisplay.setUserName(userName);
        orderDisplay.show();
    }
}

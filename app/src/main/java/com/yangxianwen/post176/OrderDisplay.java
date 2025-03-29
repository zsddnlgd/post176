package com.yangxianwen.post176;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.yangxianwen.post176.base.BaseMvvmPresentation;
import com.yangxianwen.post176.bean.Meal;
import com.yangxianwen.post176.databinding.ActivityOrderBinding;
import com.yangxianwen.post176.enmu.OrderStatus;
import com.yangxianwen.post176.viewmodel.OrderViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class OrderDisplay extends BaseMvvmPresentation<OrderViewModel, ActivityOrderBinding> {

    private double totalPrice = 0;

    private final ArrayList<View> items = new ArrayList<>();

    private final Handler mHandler = new Handler();

    public OrderDisplay(Context outerContext, Display display, OrderViewModel viewModel) {
        super(outerContext, display, viewModel);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_order;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initObserveForever();

        initListener();
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

        mLiveDataManager.observeForever(mViewModel.getHeadImage(), s -> {
            if (s == null) {
                return;
            }
            if ("transparent".equals(s)) {
                Glide.with(getContext()).load(R.color.transparent).into(mBinding.faceIcon);
            } else {
                Glide.with(getContext()).load(new File(s)).into(mBinding.faceIcon);
            }
        });

        mLiveDataManager.observeForever(mViewModel.getHasFailOrder(), aBoolean -> {
            if (aBoolean == null) {
                return;
            }
            if (aBoolean) {
                mBinding.failOrder.setVisibility(View.VISIBLE);
            } else {
                mBinding.failOrder.setVisibility(View.GONE);
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

        mLiveDataManager.observeForever(mViewModel.getMealSelectList(), meals -> {
            if (meals == null) {
                return;
            }

            for (View item : items) {
                View selectView = item.findViewById(R.id.food_select);
                Meal itemMeal = (Meal) item.getTag();
                selectView.setVisibility(View.INVISIBLE);
                for (Meal meal : meals) {
                    if (Objects.equals(itemMeal.getCFoodCode(), meal.getCFoodCode())) {
                        selectView.setVisibility(View.VISIBLE);
                        break;
                    }
                }
            }

            //更新菜品总价
            updatePrice(meals);
        });

        mLiveDataManager.observeForever(mViewModel.getOrderStatus(), status -> {
            if (status == null) {
                return;
            }

            if (status == OrderStatus.identify) {
                orderFinish();
            } else if (status == OrderStatus.createOrder) {
                orderCreateFinish();
            }
        });
    }

    private void initListener() {
        mBinding.confirmButton.setOnClickListener(v -> {
            v.setClickable(false);
            mViewModel.getOrderStatus().setValue(OrderStatus.identify);
        });
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

        View item = LayoutInflater.from(getContext()).inflate(R.layout.item_food, new FrameLayout(getContext()), false);
        TextView name = item.findViewById(R.id.food_name);
        TextView price = item.findViewById(R.id.food_price);
        View empty = item.findViewById(R.id.food_empty);
        name.setText(meal.getCFoodName());
        price.setText(String.format(Locale.getDefault(), "%.2f元", meal.getNSum()));
        empty.setVisibility(mViewModel.containsMealEmpty(meal) ? View.VISIBLE : View.INVISIBLE);

        item.setTag(meal);
        item.setOnClickListener(v -> {
            View emptyView = v.findViewById(R.id.food_empty);
            if (mViewModel.containsMealEmpty(meal)) {
                emptyView.setVisibility(View.INVISIBLE);
                mViewModel.removeMealEmpty(meal);
            } else {
                emptyView.setVisibility(View.VISIBLE);
                mViewModel.addMealEmpty(meal);
            }
        });

        layout.addView(item);
        items.add(item);
    }

    private void updatePrice(ArrayList<Meal> meals) {
        totalPrice = 0;

        for (Meal meal : meals) {
            totalPrice += meal.getNSum();
        }
        mBinding.totalAmountText.setText(String.format(Locale.getDefault(), "总金额：%.2f元", totalPrice));
    }

    private void orderFinish() {
        mBinding.confirmButton.setVisibility(View.INVISIBLE);
        //保证主屏逻辑处理完，延迟关闭副屏
        mHandler.postDelayed(this::dismiss, 500);
    }

    private void orderCreateFinish() {
        mBinding.confirmButton.setText(getResources().getString(R.string.next));
        mBinding.confirmButton.setClickable(true);
        mBinding.confirmButton.setVisibility(View.VISIBLE);
        mBinding.totalAmountText.setText(String.format(Locale.getDefault(), "总金额：%.2f元，请打餐！", totalPrice));
    }
}

package com.yangxianwen.post176;

import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.yangxianwen.post176.base.BaseMvvmPresentation;
import com.yangxianwen.post176.bean.Meal;
import com.yangxianwen.post176.bean.Student;
import com.yangxianwen.post176.databinding.DisplayOrderBinding;
import com.yangxianwen.post176.face.FaceManageActivity;
import com.yangxianwen.post176.manager.LiveDataManager;
import com.yangxianwen.post176.viewmodel.OrderViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class OrderDisplay extends BaseMvvmPresentation<OrderViewModel, DisplayOrderBinding> {

    protected LiveDataManager mLiveDataManager;

    private Student student;

    private double totalPrice = 0;

    private final ArrayList<View> items = new ArrayList<>();

    public OrderDisplay(Context outerContext, Display display) {
        super(outerContext, display);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.display_order;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initObserveForever();

        initFace();

        initListener();

        mBinding.confirmButton.setText("下一位");
    }

    private void initObserveForever() {
        mLiveDataManager = new LiveDataManager();

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

        mLiveDataManager.observeForever(mViewModel.getMealSelectList(), meals -> {
            if (meals == null) {
                return;
            }

            for (View item : items) {
                View selectView = item.findViewById(R.id.food_select);
                Meal itemMeal = (Meal) item.getTag();
                selectView.setVisibility(View.GONE);
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
    }

    private void initFace() {
        String filePath = student.getCPic().replace("/Pic/StuImg", FaceManageActivity.REGISTER_DIR);
        Glide.with(getContext()).load(new File(filePath)).into(mBinding.faceIcon);
    }

    private void initListener() {
        mBinding.nextButton.setOnClickListener(v -> {
            mLiveDataManager.clearAllObservers();
            dismiss();
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

        View item = LayoutInflater.from(getContext()).inflate(R.layout.item_food, new FrameLayout(getContext()), false);
        TextView name = item.findViewById(R.id.food_name);
        TextView price = item.findViewById(R.id.food_price);
        name.setText(meal.getCFoodName());
        price.setText(String.format(Locale.getDefault(), "%.2f元", meal.getNSum()));
        layout.addView(item);

        item.setSelected(false);
        item.setTag(meal);
        item.setOnClickListener(v -> {
            View emptyView = v.findViewById(R.id.food_empty);
            if (mViewModel.containsMealEmpty(meal)) {
                emptyView.setVisibility(View.GONE);
                mViewModel.removeMealEmpty(meal);
            } else {
                emptyView.setVisibility(View.VISIBLE);
                mViewModel.addMealEmpty(meal);
            }
        });
        items.add(item);
    }

    private void updatePrice(ArrayList<Meal> meals) {
        totalPrice = 0;

        for (Meal meal : meals) {
            totalPrice += meal.getNSum();
        }
        mBinding.totalAmountText.setText(String.format(Locale.getDefault(), "总金额：%.2f元", totalPrice));
    }

    public void setViewModel(OrderViewModel viewModel) {
        mViewModel = viewModel;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public void onSelectConfirm() {
        mBinding.totalAmountText.setText(String.format(Locale.getDefault(), "总金额：%.2f元，请打餐！", totalPrice));
        mBinding.nextButton.setVisibility(View.VISIBLE);
    }
}

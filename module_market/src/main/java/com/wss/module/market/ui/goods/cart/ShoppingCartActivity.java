package com.wss.module.market.ui.goods.cart;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.wss.common.base.BaseActionBarActivity;
import com.wss.common.bean.Event;
import com.wss.common.constants.Dic;
import com.wss.common.constants.EventAction;
import com.wss.common.manage.ActivityToActivity;
import com.wss.common.utils.JsonUtils;
import com.wss.module.market.R;
import com.wss.module.market.R2;
import com.wss.module.market.bean.GoodsInfo;
import com.wss.module.market.bean.Vendor;
import com.wss.module.market.ui.goods.cart.adapter.ShoppingCartAdapter;
import com.wss.module.market.ui.goods.cart.mvp.CartPresenter;
import com.wss.module.market.ui.goods.cart.mvp.contract.ShoppingCartContract;
import com.wss.module.market.utils.ShoppingCartUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.OnClick;

/**
 * Describe：购物车
 * Created by 吴天强 on 2018/11/5.
 */
public class ShoppingCartActivity extends BaseActionBarActivity<CartPresenter> implements ShoppingCartContract.View {


    @BindView(R2.id.recycle_view)
    RecyclerView recycleView;

    @BindView(R2.id.iv_check_all)
    ImageView ivCheckAll;

    @BindView(R2.id.tv_total)
    TextView tvTotal;

    @BindView(R2.id.btn_buy)
    TextView btnBuy;

    private TextView tvRight;
    private boolean isEdit;

    @Override
    protected CartPresenter createPresenter() {
        return new CartPresenter();
    }

    private List<Vendor> mData = new ArrayList<>();
    private ShoppingCartAdapter adapter;


    @Override
    protected int getLayoutId() {
        return R.layout.market_activity_shopping_cart;
    }

    @Override
    protected void initView() {
        setCenterText("购物车");
        adapter = new ShoppingCartAdapter(context, mData, (data, position) -> {

        });
        recycleView.setLayoutManager(new LinearLayoutManager(context));
        recycleView.setAdapter(adapter);
        tvRight = getTitleBar().getTextView();
        tvRight.setText("编辑");
        tvRight.setOnClickListener(v -> onRightChange());
        getTitleBar().setRightView(tvRight);
        getPresenter().start();
    }


    private void onRightChange() {
        isEdit = !isEdit;
        if (isEdit) {
            tvRight.setText("完成");
            tvTotal.setVisibility(View.GONE);
            btnBuy.setText("删除");
            btnBuy.setBackgroundResource(R.drawable.market_shopping_cart_next_btn_red);
        } else {
            tvTotal.setVisibility(View.VISIBLE);
            tvRight.setText("编辑");
            btnBuy.setText("下一步");
            btnBuy.setBackgroundResource(R.drawable.market_shopping_cart_next_btn);
        }
        adapter.setEdit(isEdit);
    }


    @Override
    protected boolean registerEventBus() {
        return true;
    }

    @Override
    public void onEventBus(Event event) {
        super.onEventBus(event);
        if (TextUtils.equals(EventAction.EVENT_SHOPPING_CART_CHANGED, event.getAction())) {
            //购物车有变化
            adapter.notifyDataSetChanged();
            ivCheckAll.setSelected(ShoppingCartUtils.isAllVendorChecked(mData));
            displayResult();
        } else if (TextUtils.equals(EventAction.EVENT_SHOPPING_CART_REFRESH, event.getAction())) {
            //重新获取购物车数据
            getPresenter().getCartData();
        }
    }

    private void displayResult() {
        btnBuy.setSelected(ShoppingCartUtils.isCheckedLeastOne(mData));
        tvTotal.setText(String.format("%s%s", getString(R.string.market_total), ShoppingCartUtils.getCartCountPrice(mData)));
    }


    @Override
    public void refreshCartData(List<Vendor> dataList) {
        this.mData.clear();
        this.mData.addAll(dataList);
        adapter.notifyDataSetChanged();
        displayResult();
    }


    @OnClick({R2.id.iv_check_all, R2.id.btn_buy})
    public void onViewClicked(View view) {
        int i = view.getId();
        if (i == R.id.iv_check_all) {
            boolean selected = view.isSelected();
            view.setSelected(!selected);
            ShoppingCartUtils.checkAll(mData, !selected);
            adapter.notifyDataSetChanged();
            displayResult();
        } else if (i == R.id.btn_buy) {
            List<GoodsInfo> checkedGoods = ShoppingCartUtils.getAllCheckedGoods(mData);
            if (checkedGoods.size() > 0) {
                if (isEdit) {
                    //删除
                    ShoppingCartUtils.delete(checkedGoods);
                } else {
                    Map<String, Object> param = new HashMap<>();
                    param.put(Dic.VENDOR_GOODS_INFO, JsonUtils.toJson(ShoppingCartUtils.getCheckedGoodsVendor(mData)));
                    ActivityToActivity.toActivity(context, OrderSettlementActivity.class, param);
                }
            }
        }
    }

    @Override
    public void onEmpty(Object tag) {
        super.onEmpty(tag);
        showEmptyView("车里空空如也");
    }
}

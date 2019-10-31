package com.tdr.rentalhouse.mvp.houseinfo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.facebook.drawee.view.SimpleDraweeView;
import com.tdr.rentalhouse.R;
import com.tdr.rentalhouse.base.Api;
import com.tdr.rentalhouse.base.BaseMvpActivity;
import com.tdr.rentalhouse.base.BaseView;
import com.tdr.rentalhouse.base.RequestCode;
import com.tdr.rentalhouse.bean.HouseBean;
import com.tdr.rentalhouse.bean.HouseInfoBean;
import com.tdr.rentalhouse.mvp.bindhouse.BindHouseActivity;
import com.tdr.rentalhouse.utils.FastClickUtils;
import com.tdr.rentalhouse.utils.FormatUtils;
import com.tdr.rentalhouse.utils.LoadingUtils;
import com.tdr.rentalhouse.utils.ObjectUtils;
import com.tdr.rentalhouse.utils.StatusBarUtils;
import com.tdr.rentalhouse.utils.ToastUtils;
import com.tdr.rentalhouse.widget.RVDividerItemDecoration;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Author：Li Bin on 2019/7/17 09:44
 * Description：
 */
public class HouseInfoActivity extends BaseMvpActivity<HouseInfoCantact.Presenter> implements BaseView {
    @BindView(R.id.view_status_bar)
    View viewStatusBar;
    @BindView(R.id.tv_title_name)
    TextView tvTitleName;
    @BindView(R.id.iv_title_back)
    ImageView ivTitleBack;
    @BindView(R.id.tv_title_more)
    TextView tvTitleMore;
    @BindView(R.id.tv_unit_name)
    TextView tvHouse;
    @BindView(R.id.rv_unit)
    RecyclerView rvHouse;

    List<HouseBean.DataBean.ListBean> dataList = new ArrayList<>();
    BaseQuickAdapter<HouseBean.DataBean.ListBean, BaseViewHolder> adapter;

    private HouseInfoBean houseInfoBean;
    private HouseBean.DataBean dataBean;

    @Override
    protected HouseInfoCantact.Presenter initPresenter() {
        return new HouseInfoPresenter();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_house);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        getData();
        initView();
        initData();
    }

    private void getData() {
        houseInfoBean = (HouseInfoBean) getIntent().getSerializableExtra("house");
    }

    private void initData() {
        LoadingUtils.getInstance().showLoading(this, "加载中");
        mPresenter.getHouseInfo(RequestCode.NetCode.HOUSE_INFO, houseInfoBean.getHouseId());

    }

    private void initView() {
        StatusBarUtils.getInstance().setStatusBarHeight(viewStatusBar);
        tvTitleName.setText("房屋信息");
        tvTitleMore.setVisibility(View.VISIBLE);
        tvTitleMore.setText("编辑");
        rvHouse.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BaseQuickAdapter<HouseBean.DataBean.ListBean, BaseViewHolder>(R.layout.layout_room_item_edit, dataList) {

            @Override
            protected void convert(BaseViewHolder helper, HouseBean.DataBean.ListBean item) {
                helper.setText(R.id.tv_room_name, item.getRoomNumber());
                helper.setText(R.id.tv_bed_num, "床铺：" + item.getBedNumber());
            }

        };

        rvHouse.setAdapter(adapter);
    }

    @OnClick({R.id.iv_title_back, R.id.tv_title_more})
    public void onViewClicked(View view) {
        if (FastClickUtils.isSingleClick()) {
            switch (view.getId()) {
                case R.id.iv_title_back:
                    finish();
                    break;
                case R.id.tv_title_more:
                    if (dataBean != null) {
                        Intent intent = new Intent(HouseInfoActivity.this, BindHouseActivity.class);
                        houseInfoBean.setType(4);
                        houseInfoBean.setLandlordName(dataBean.getLandlordName());
                        houseInfoBean.setIdNo(dataBean.getIDNumber());
                        houseInfoBean.setPhone(dataBean.getPhone());
                        houseInfoBean.setCityName(dataBean.getCityName());
                        houseInfoBean.setAreaName(dataBean.getAreaName());
                        houseInfoBean.setStreetName(dataBean.getSteetName());
                        houseInfoBean.setResidentialName(dataBean.getComminityName());

                        if (dataList.size() > 0) {
                            List<HouseInfoBean.Room> roomList = new ArrayList<>();
                            for (HouseBean.DataBean.ListBean listBean : dataList) {
                                HouseInfoBean.Room room = new HouseInfoBean.Room();
                                room.setId(listBean.getId());
                                room.setRoomNumber(listBean.getRoomNumber());
                                room.setBedNumber(listBean.getBedNumber());
                                room.setSortNo(listBean.getSortNo());
                                roomList.add(room);
                            }

                            houseInfoBean.setRoomList(roomList);

                        }
                        intent.putExtra("house", houseInfoBean);
                        startActivity(intent);
                    }


                    break;
            }
        }

    }

    @Override
    public void onSuccess(int what, Object object) {
        dataBean = (HouseBean.DataBean) object;
        dataList.clear();
        adapter.removeAllHeaderView();
        if (dataBean != null) {
            if (dataBean.getArchitecturalTypes() == 1) {
                tvHouse.setVisibility(View.GONE);
                addHeader1();
            } else {
                tvHouse.setVisibility(View.VISIBLE);
                String address = houseInfoBean.getCommunityName() + "/" + houseInfoBean.getBuildingName() + "幢/";
                if (!TextUtils.isEmpty(houseInfoBean.getUnitName())) {
                    address += houseInfoBean.getUnitName() + "单元/";
                }
                address += houseInfoBean.getFloorName() + "层/" + houseInfoBean.getHouseName() + "室";
                tvHouse.setText(address);
            }

            if (ObjectUtils.getInstance().isNotNull(dataBean.getList())) {
                dataList.addAll(dataBean.getList());
            }

            addHeader2();
        }

        adapter.notifyDataSetChanged();
    }

    private void addHeader2() {
        View view = LayoutInflater.from(this).inflate(R.layout.layout_room_header, null);
        TextView tvLandlordName = view.findViewById(R.id.tv_landlord_name);
        TextView tvCard = view.findViewById(R.id.tv_id_card);
        TextView tvPhone = view.findViewById(R.id.tv_phone_number);

        tvLandlordName.setText(dataBean.getLandlordName());
        tvCard.setText(dataBean.getIDNumber());
        tvPhone.setText(dataBean.getPhone());

        adapter.addHeaderView(view);
        adapter.notifyDataSetChanged();
    }

    private void addHeader1() {
        View view = LayoutInflater.from(this).inflate(R.layout.layout_manager_address_header, null);
        SimpleDraweeView iv = view.findViewById(R.id.iv_estate);
        TextView tvName = view.findViewById(R.id.tv_estate_name);
        TextView tvAddress = view.findViewById(R.id.tv_estate_address);
        iv.setImageURI(Api.IMG_HOST + dataBean.getOutlookOne());
        tvName.setText(dataBean.getRDNumber());


        String address = dataBean.getCityName();
        if (!TextUtils.isEmpty(dataBean.getAreaName())) {
            address += dataBean.getAreaName();
        }

        if (!TextUtils.isEmpty(dataBean.getSteetName())) {
            address += dataBean.getSteetName();
        }

        if (!TextUtils.isEmpty(dataBean.getComminityName())) {
            address += dataBean.getComminityName();
        }


        tvAddress.setText(address);


        adapter.addHeaderView(view);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onFail(int what, String msg) {
        ToastUtils.getInstance().showToast(msg);
    }

    @Override
    public void hideLoading() {
        LoadingUtils.getInstance().dismiss();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void editRoom(String msg) {
        if ("edit_room".equals(msg)) {
            initData();
        }
    }


    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}

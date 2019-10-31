package com.tdr.rentalhouse.mvp.bindhouse;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.facebook.drawee.view.SimpleDraweeView;
import com.tdr.rentalhouse.R;
import com.tdr.rentalhouse.mvp.communitydetail.CommunityDetailActivity;
import com.tdr.rentalhouse.mvp.scancode.ScanQRCodeActivity;
import com.tdr.rentalhouse.base.Api;
import com.tdr.rentalhouse.base.BaseMvpActivity;
import com.tdr.rentalhouse.base.BaseView;
import com.tdr.rentalhouse.base.RequestCode;
import com.tdr.rentalhouse.bean.HouseInfoBean;
import com.tdr.rentalhouse.bean.LocationBean;
import com.tdr.rentalhouse.bean.ScanResult;
import com.tdr.rentalhouse.utils.Base64Utils;
import com.tdr.rentalhouse.utils.FastClickUtils;
import com.tdr.rentalhouse.utils.FormatUtils;
import com.tdr.rentalhouse.utils.GsonUtils;
import com.tdr.rentalhouse.utils.LimitInputTextWatcher;
import com.tdr.rentalhouse.utils.LoadingUtils;
import com.tdr.rentalhouse.utils.LogUtils;
import com.tdr.rentalhouse.utils.ObjectUtils;
import com.tdr.rentalhouse.utils.PermissionUtils;
import com.tdr.rentalhouse.utils.StatusBarUtils;
import com.tdr.rentalhouse.utils.ToastUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.amap.api.location.AMapLocationClientOption.AMapLocationMode.Hight_Accuracy;

/**
 * Author：Li Bin on 2019/7/15 14:41
 * Description：房屋绑定
 */
public class BindHouseActivity extends BaseMvpActivity<BindHouseContact.Presenter> implements BaseView {
    @BindView(R.id.view_status_bar)
    View viewStatusBar;
    @BindView(R.id.tv_title_name)
    TextView tvTitleName;
    @BindView(R.id.iv_title_back)
    ImageView ivTitleBack;
    @BindView(R.id.tv_title_more)
    TextView tvTitleMore;
    @BindView(R.id.tv_unit_name)
    TextView tvUnitName;
    @BindView(R.id.rv_unit)
    RecyclerView rvUnit;

    private EditText etLandlordName;
    private EditText etIdCard;
    private EditText etPhoneNumber;

    private List<HouseInfoBean.Room> dataList = new ArrayList<>();
    private BaseQuickAdapter<HouseInfoBean.Room, BaseViewHolder> adapter;
    private HouseInfoBean houseInfoBean;

    private View tvNullRoom;
    private View viewLine;
    private SimpleDraweeView ivCommunity;
    private TextView tvAddress;
    private TextView tvName;

    @Override
    protected BindHouseContact.Presenter initPresenter() {
        return new BindHousePresenter();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_house);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        getData();
        initView();


    }


    private void getData() {
        houseInfoBean = (HouseInfoBean) getIntent().getSerializableExtra("house");
    }


    private void initView() {
        StatusBarUtils.getInstance().setStatusBarHeight(viewStatusBar);
        tvTitleName.setText("房屋绑定");
        tvTitleMore.setVisibility(View.VISIBLE);
        tvTitleMore.setText("完成");
        if (houseInfoBean.getBuildingType() == 1) {
            tvUnitName.setVisibility(View.GONE);
        } else {
            tvUnitName.setVisibility(View.VISIBLE);
            String address = houseInfoBean.getCommunityName() + "/" + houseInfoBean.getBuildingName() + "幢/";
            if (!TextUtils.isEmpty(houseInfoBean.getUnitName())) {
                address += houseInfoBean.getUnitName() + "单元/";
            }
            address += houseInfoBean.getFloorName() + "层/" + houseInfoBean.getHouseName() + "室";
            tvUnitName.setText(address);
        }


        rvUnit.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BaseQuickAdapter<HouseInfoBean.Room, BaseViewHolder>(R.layout.layout_room_item_edit, dataList) {
            @Override
            protected void convert(BaseViewHolder helper, HouseInfoBean.Room item) {
                helper.setText(R.id.tv_room_name, item.getRoomNumber());
                helper.setText(R.id.tv_bed_num, "床铺：" + item.getBedNumber());
                helper.setVisible(R.id.iv_delete_room, true);
                helper.addOnClickListener(R.id.iv_delete_room);
                helper.addOnClickListener(R.id.ll_room);

            }

        };

        if (houseInfoBean.getBuildingType() == 1) {
            View header1 = LayoutInflater.from(this).inflate(R.layout.layout_manager_address_header, null);
            ivCommunity = header1.findViewById(R.id.iv_estate);
            tvName = header1.findViewById(R.id.tv_estate_name);
            tvAddress = header1.findViewById(R.id.tv_estate_address);

            ivCommunity.setImageURI(Api.IMG_HOST + houseInfoBean.getImg());
            tvName.setText(houseInfoBean.getAreaNumber());


            String address = houseInfoBean.getCityName();
            if (!TextUtils.isEmpty(houseInfoBean.getAreaName())) {
                address += houseInfoBean.getAreaName();
            }

            if (!TextUtils.isEmpty(houseInfoBean.getStreetName())) {
                address += houseInfoBean.getStreetName();
            }

            if (!TextUtils.isEmpty(houseInfoBean.getResidentialName())) {
                address += houseInfoBean.getResidentialName();
            }


            tvAddress.setText(address);

            header1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(BindHouseActivity.this, CommunityDetailActivity.class);
                    intent.putExtra("id", houseInfoBean.getCommunityId());
                    startActivity(intent);
                }
            });
            adapter.addHeaderView(header1);
        }


        final View view = LayoutInflater.from(this).inflate(R.layout.layout_bind_room_header, null);
        etLandlordName = view.findViewById(R.id.et_landlord_name);
        etLandlordName.addTextChangedListener(new LimitInputTextWatcher(etLandlordName));
        etIdCard = view.findViewById(R.id.et_id_card);
        etPhoneNumber = view.findViewById(R.id.et_phone_number);


        adapter.addHeaderView(view);


        View viewFooter = LayoutInflater.from(this).inflate(R.layout.layout_room_footer_add, null);
        tvNullRoom = viewFooter.findViewById(R.id.tv_null_room);
        viewLine = viewFooter.findViewById(R.id.view_line_add);
        LinearLayout llAddRoom = viewFooter.findViewById(R.id.ll_add_room_item);
        adapter.addFooterView(viewFooter);

        llAddRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addRoom();
            }
        });

        rvUnit.setAdapter(adapter);


        if (houseInfoBean.getType() == 4) {
            etLandlordName.setText(houseInfoBean.getLandlordName());
            etPhoneNumber.setText(houseInfoBean.getPhone());
            etIdCard.setText(houseInfoBean.getIdNo());


            if (ObjectUtils.getInstance().isNotNull(houseInfoBean.getRoomList())) {
                dataList.addAll(houseInfoBean.getRoomList());
            }
            tvNullRoom.setVisibility(dataList.size() > 0 ? View.GONE : View.VISIBLE);
            viewLine.setVisibility(dataList.size() > 0 ? View.GONE : View.VISIBLE);
            adapter.notifyDataSetChanged();

        }

        adapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                switch (view.getId()) {
                    case R.id.iv_delete_room:
                        dataList.remove(position);
                        adapter.notifyDataSetChanged();
                        tvNullRoom.setVisibility(dataList.size() > 0 ? View.GONE : View.VISIBLE);
                        viewLine.setVisibility(dataList.size() > 0 ? View.GONE : View.VISIBLE);
                        break;
                    case R.id.ll_room:
                        updateRoomName(position);
                        break;
                }

            }
        });
    }

    /**
     * 新增房间
     */
    private void addRoom() {
        final PopupWindow mPopupWindow = new PopupWindow();
        View mPopBackView = LayoutInflater.from(this).inflate(R.layout.ppw_update_room, null);

        //设置Popup具体控件
        TextView tvCancel = mPopBackView.findViewById(R.id.tv_cancel_update);
        TextView tvConfirm = mPopBackView.findViewById(R.id.tv_update_room);
        final EditText etName = mPopBackView.findViewById(R.id.tv_update_name);
        final EditText etBed = mPopBackView.findViewById(R.id.et_bed_num);
        tvConfirm.setText("添加");

        etName.addTextChangedListener(new LimitInputTextWatcher(etName, LimitInputTextWatcher.DEFAULT_REGEX));


        //设置Popup具体参数
        mPopupWindow.setContentView(mPopBackView);
        mPopupWindow.setClippingEnabled(false);
        mPopupWindow.setWidth(LinearLayoutCompat.LayoutParams.MATCH_PARENT);//设置宽
        mPopupWindow.setHeight(LinearLayoutCompat.LayoutParams.MATCH_PARENT);//设置高
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));
        mPopupWindow.setFocusable(true);//点击空白，popup不自动消失
        mPopupWindow.setTouchable(true);//popup区域可触摸
        mPopupWindow.setOutsideTouchable(true);//非popup区域可触摸


        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopupWindow.dismiss();
            }
        });

        tvConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(etName.getText().toString().trim())) {
                    ToastUtils.getInstance().showToast("请输入房间名称");
                    return;
                }

                if (TextUtils.isEmpty(etBed.getText().toString().trim())) {
                    ToastUtils.getInstance().showToast("请输入床铺数量");
                    return;
                }


                HouseInfoBean.Room room = new HouseInfoBean.Room();

                boolean flag = true;
                for (int i = 0; i < dataList.size(); i++) {
                    if (dataList.get(i).getRoomNumber().equals(etName.getText().toString().trim())) {
                        flag = false;
                        break;
                    }
                }
                if (flag) {
                    mPopupWindow.dismiss();
                    room.setSortNo(dataList.size() + 1);
                    room.setRoomNumber(etName.getText().toString().trim());
                    room.setBedNumber(Integer.parseInt(etBed.getText().toString().trim()));

                    dataList.add(room);

                    tvNullRoom.setVisibility(dataList.size() > 0 ? View.GONE : View.VISIBLE);
                    viewLine.setVisibility(dataList.size() > 0 ? View.GONE : View.VISIBLE);
                    adapter.notifyDataSetChanged();
                } else {
                    ToastUtils.getInstance().showToast("已存在相同房间名称");
                }
            }
        });

        mPopupWindow.showAtLocation(getWindow().getDecorView(), Gravity.CENTER, 0, 0);
    }

    /**
     * @param position 修改房间名
     */
    private void updateRoomName(final int position) {
        final PopupWindow mPopupWindow = new PopupWindow();
        View mPopBackView = LayoutInflater.from(this).inflate(R.layout.ppw_update_room, null);

        //设置Popup具体控件
        TextView tvCancel = mPopBackView.findViewById(R.id.tv_cancel_update);
        TextView tvConfirm = mPopBackView.findViewById(R.id.tv_update_room);
        final EditText etName = mPopBackView.findViewById(R.id.tv_update_name);
        final EditText etBed = mPopBackView.findViewById(R.id.et_bed_num);

        etName.setText(dataList.get(position).getRoomNumber());
        etBed.setText(dataList.get(position).getBedNumber() + "");
        etName.addTextChangedListener(new LimitInputTextWatcher(etName, LimitInputTextWatcher.DEFAULT_REGEX));


        //设置Popup具体参数
        mPopupWindow.setContentView(mPopBackView);
        mPopupWindow.setClippingEnabled(false);
        mPopupWindow.setWidth(LinearLayoutCompat.LayoutParams.MATCH_PARENT);//设置宽
        mPopupWindow.setHeight(LinearLayoutCompat.LayoutParams.MATCH_PARENT);//设置高
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));
        mPopupWindow.setFocusable(true);//点击空白，popup不自动消失
        mPopupWindow.setTouchable(true);//popup区域可触摸
        mPopupWindow.setOutsideTouchable(true);//非popup区域可触摸


        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopupWindow.dismiss();
            }
        });

        tvConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(etName.getText().toString().trim())) {
                    ToastUtils.getInstance().showToast("请输入房间名称");
                    return;
                }

                if (TextUtils.isEmpty(etBed.getText().toString().trim())) {
                    ToastUtils.getInstance().showToast("请输入床铺数量");
                    return;
                }

                boolean flag = true;
                for (int i = 0; i < dataList.size(); i++) {
                    if (i != position && dataList.get(i).getRoomNumber().equals(etName.getText().toString().trim())) {
                        flag = false;
                        break;
                    }
                }
                if (flag) {
                    mPopupWindow.dismiss();
                    dataList.get(position).setRoomNumber(etName.getText().toString().trim());
                    dataList.get(position).setBedNumber(Integer.parseInt(etBed.getText().toString().trim()));
                    adapter.notifyDataSetChanged();
                } else {
                    ToastUtils.getInstance().showToast("已存在相同房间名称");
                }
            }
        });

        mPopupWindow.showAtLocation(getWindow().getDecorView(), Gravity.CENTER, 0, 0);
    }


    @OnClick({R.id.iv_title_back, R.id.tv_title_more})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_title_back:
                finish();
                break;
            case R.id.tv_title_more:
                if (FastClickUtils.isSingleClick()) {
                    submitData();
                }
                break;
        }
    }

    /**
     * 上传数据
     */
    private void submitData() {
        String landlordName = etLandlordName.getText().toString().trim();
        String idNo = etIdCard.getText().toString().trim();
        String phone = etPhoneNumber.getText().toString().trim();

        if (TextUtils.isEmpty(landlordName)) {
            ToastUtils.getInstance().showToast("请输入房东姓名");
            return;
        }

        if (!FormatUtils.getInstance().IsIDCard(idNo)) {
            ToastUtils.getInstance().showToast("请输入有效的身份证号码");
            return;
        }

        if (!FormatUtils.getInstance().IsHandset(phone)) {
            ToastUtils.getInstance().showToast("请输入有效的手机号码");
            return;
        }


        LoadingUtils.getInstance().showLoading(this, "加载中");


        String list = "";
        if (dataList.size() > 0) {
            list = GsonUtils.listToString(dataList);
        }

        mPresenter.bindHouse(RequestCode.NetCode.BIND_HOUSE, houseInfoBean.getHouseId(),
                landlordName, idNo, phone, list);

    }

    @Override
    public void onSuccess(int what, Object object) {

        if (houseInfoBean.getType() == 4) {
            ToastUtils.getInstance().showToast("编辑成功");
            EventBus.getDefault().post("edit_room");
        } else {
            ToastUtils.getInstance().showToast("绑定成功");
        }

        finish();
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
    public void editAddress(HouseInfoBean house) {

        ivCommunity.setImageURI(Api.IMG_HOST + house.getImg());
        tvName.setText(house.getAreaNumber());


        String address = house.getCityName();
        if (!TextUtils.isEmpty(house.getAreaName())) {
            address += house.getAreaName();
        }

        if (!TextUtils.isEmpty(house.getStreetName())) {
            address += house.getStreetName();
        }

        if (!TextUtils.isEmpty(house.getResidentialName())) {
            address += house.getResidentialName();
        }


        tvAddress.setText(address);

        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

}




package com.tdr.rentalhouse.mvp.bindhouse;

import com.tdr.rentalhouse.base.BaseBean;
import com.tdr.rentalhouse.base.BasePresenterImpl;
import com.tdr.rentalhouse.base.BaseView;
import com.tdr.rentalhouse.base.RxObserver;
import com.tdr.rentalhouse.base.Transformer;
import com.tdr.rentalhouse.bean.ScanCodeBean;
import com.tdr.rentalhouse.inter.Callback;
import com.tdr.rentalhouse.utils.RetrofitUtils;

/**
 * Author：Li Bin on 2019/7/23 14:29
 * Description：
 */
public class BindHousePresenter extends BasePresenterImpl<BaseView> implements BindHouseContact.Presenter {


    @Override
    public void bindHouse(final int what, int floorId, String landlordName, String idNo, String phone, String list) {
        if (!isViewAttached())
            return;

        RetrofitUtils.getInstance()
                .getService()
                .bindHouse(floorId, landlordName, idNo, phone, list)
                .compose(Transformer.switchSchedulers())
                .subscribe(new RxObserver(new Callback<BaseBean>() {
                    @Override
                    public void onSuccess(BaseBean baseBean) {
                        getView().hideLoading();
                        getView().onSuccess(what, null);
                    }

                    @Override
                    public void onFail(String msg) {
                        getView().hideLoading();
                        getView().onFail(what, msg);
                    }
                }));
    }

}

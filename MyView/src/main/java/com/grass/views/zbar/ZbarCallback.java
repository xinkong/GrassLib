package com.grass.views.zbar;

/**
 * Created by fengwei on 2017-06-04.
 */

public interface ZbarCallback {
    /**
     * 一维码扫描结果
     * @param result
     */
    void barCodeResult(String result);

    /**
     * 二维码扫描结果
     * @param result
     */
    void qrCodeResult(String result);
}

package com.grass.views.zbar;


import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;


import com.grass.views.zbar.view.ZbarViewfinderView;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

import java.util.List;




/**
 * Created by fengwei on 2017-06-04.
 */

public class ZbarUtils  implements  SurfaceHolder.Callback {
    private Camera camera;
    private Handler handler;
    private boolean previewing;
    private ImageScanner scanner;
    private ZbarViewfinderView zbarViewfinderView;
    private ZbarCallback zbarCallback;
    private boolean isContinue;
    private SurfaceHolder surfaceHolder;
    private boolean isOpenLight=false;

//    static {
//        System.loadLibrary("iconv");
//    }

    public ZbarUtils(Handler handler, ZbarViewfinderView zbarViewfinderView) {
        System.loadLibrary("iconv");
        this.handler = handler;
        this.zbarViewfinderView = zbarViewfinderView;
        scanner = new ImageScanner();
        scanner.setConfig(0, Config.X_DENSITY, 3);
        scanner.setConfig(0, Config.Y_DENSITY, 3);
//        openDriver();
    }

    /**
     * 打开摄像头
     *
     * @return
     */
    public boolean openDriver() {

        try {
            camera = Camera.open();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 关闭摄像头
     */
    public void closeDriver() {
        if (camera != null) {
            stopScan();
            camera.release();
            camera = null;
        }
    }

    /**
     * 初始化摄像头
     * @param holder
     */
    public void initCamera(SurfaceHolder holder) {
        holder.addCallback(this);


    }


    Camera.AutoFocusCallback autoFocusCB = new Camera.AutoFocusCallback() {
        public void onAutoFocus(boolean success, Camera camera) {
                camera.cancelAutoFocus();
                handler.postDelayed(doAutoFocus, 5);


        }
    };

    private Runnable doAutoFocus = new Runnable() {
        public void run() {
            if (previewing)
                camera.autoFocus(autoFocusCB);
        }
    };

    private Camera.PreviewCallback previewCb = new Camera.PreviewCallback() {

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            camera.setPreviewCallback(null);
            Camera.Parameters parameters = camera.getParameters();
            Size size = parameters.getPreviewSize();

            Image barcode = new Image(size.width,size.height, "Y800");
            Rect rect = zbarViewfinderView.getScanImageRect(size.height, size.width);

            barcode.setCrop(rect.top, 0, rect.bottom-rect.top, size.height);
            barcode.setData(data);
            int result = scanner.scanImage(barcode);
            if (result> 0) {
                pauseScan();
                SymbolSet syms = scanner.getResults();
                for (final Symbol sym : syms) {
                    if (sym.getType() == Symbol.CODE128
                            || sym.getType() == Symbol.CODE39
                            || sym.getType() == Symbol.CODE93
//                            || sym.getType() == Symbol.EAN13
                            ) {
                        if (zbarCallback != null) {
                            zbarCallback.barCodeResult(sym.getData().toString());
                            break;
                        }

                    }else if (sym.getType() == Symbol.QRCODE) {
                        if (zbarCallback != null) {
                            zbarCallback.qrCodeResult(sym.getData().toString());
                        }
                        break;
                    }else{
                        restartScan();
                        break;
                    }

                }
            }else{
                camera.setPreviewCallback(previewCb);
            }
        }
    };

    /**
     * 开始扫描
     */
    public void startScan(){
        if(camera!=null){
            previewing = true;
            camera.setPreviewCallback(previewCb);
            camera.startPreview();
            camera.autoFocus(autoFocusCB);
        }

    }

    /**
     * 停止扫描
     */
    public void stopScan(){
        if(camera!=null){
            previewing = false;
            camera.stopPreview();
            camera.setPreviewCallback(null);
//            camera.stopPreview();
//            previewing = false;
        }
    }

    /**
     * 暂停扫描
     */
    public void pauseScan(){
        if(camera!=null){
            previewing = false;
            camera.setPreviewCallback(null);

        }
    }


    public ZbarCallback getZbarCallback() {
        return zbarCallback;
    }

    public void setZbarCallback(ZbarCallback zbarCallback) {
        this.zbarCallback = zbarCallback;
    }

    public boolean isContinue() {
        return isContinue;
    }

    /**
     * 是否继续扫描
     * @param aContinue
     */
    public void setContinue(boolean aContinue) {
        isContinue = aContinue;
    }




    public Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.05;
        double targetRatio = (double) w / h;
        if (sizes == null)
            return null;
        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        int targetHeight = h;
        // Try to find an size match aspect ratio and size
        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }
        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }




    @Override
    public void surfaceCreated(SurfaceHolder holder) {


    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        openDriver();
        if(camera!=null){
                camera.stopPreview();
            try {
                Camera.Parameters parameters = camera.getParameters();
                List<Size> sizes = parameters.getSupportedPreviewSizes();
                Size optimalSize = getOptimalPreviewSize(sizes, width, height);
//                parameters.setPreviewSize(optimalSize.width, optimalSize.height);
                if(isOpenLight){
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);//开启
                }else{
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                }
                camera.setDisplayOrientation(90);
                camera.setParameters(parameters);
                camera.setPreviewDisplay(holder);
                startScan();
            } catch (Exception e) {
                Log.d("DBG", "Error starting camera preview: " + e.getMessage());
            }
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        closeDriver();

    }

    /**
     * 重新开始扫描
     */
    public void restartScan(){
        if(camera!=null){
            previewing = true;
            camera.setPreviewCallback(previewCb);

        }
    }
    /**
     * 打开或关闭闪光灯
     * @param open true 打开 false 关闭
     */
    public void openOrCloseLight(boolean open){
        if(camera!=null){
            Camera.Parameters parameter = camera.getParameters();
            if (open) {
                parameter.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            } else {
                parameter.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            }
            camera.setParameters(parameter);
        }

    }


}

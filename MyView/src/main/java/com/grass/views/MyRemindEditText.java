package com.grass.views;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;

/**
 * Created by huchao on 2018/2/5.
 */

public class MyRemindEditText extends MyEditText {

    private int mTextLenth = 0;
    private OnRemindTextAppearListener mListener;

    public MyRemindEditText(Context context) {
        super(context);
        init();
    }

    public MyRemindEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MyRemindEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {
        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String inputLenth = s.toString();
                if (inputLenth.length() > mTextLenth){
                    //获取最后一个字符
                   String laseCode = inputLenth.substring(inputLenth.length()-1,inputLenth.length());
                    if("@".equals(laseCode)){
                        if(mListener!=null){
                            mListener.remindTextAppear();
                        }
                    }
                }
                mTextLenth = inputLenth.length();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public void setOnRemindTextAppearListener(OnRemindTextAppearListener listener) {
        this.mListener = listener;
    }

    public interface OnRemindTextAppearListener {
        void remindTextAppear();
    }

}

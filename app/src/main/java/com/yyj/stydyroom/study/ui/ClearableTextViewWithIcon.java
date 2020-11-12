package com.yyj.stydyroom.study.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;


import com.yyj.stydyroom.R;

public class ClearableTextViewWithIcon extends TextView implements View.OnTouchListener {

    //尾部图片
    Drawable lastIcon = getResources().getDrawable(R.drawable.enter_idntify);

    //首部图片
    Drawable firstIcon;

    public ClearableTextViewWithIcon(Context context) {
        super(context);
        init();
    }

    public ClearableTextViewWithIcon(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ClearableTextViewWithIcon(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setFirstIcon(int id){
        firstIcon = getResources().getDrawable(id);
        firstIcon.setBounds(0, 0, firstIcon.getIntrinsicWidth(), firstIcon.getIntrinsicHeight());
        manageClearButton();
    }

    public void setLastIcon(int id){
        lastIcon = getResources().getDrawable(id);
        lastIcon.setBounds(0, 0, lastIcon.getIntrinsicWidth(), lastIcon.getIntrinsicHeight());
        manageClearButton();
    }


    /*
    初始化把图片插入
     */
    public void init(){
        ClearableTextViewWithIcon.this.setOnTouchListener(this);
        lastIcon.setBounds(0,0,lastIcon.getIntrinsicWidth(),lastIcon.getIntrinsicHeight());
        manageClearButton();
    }

    /*
    调整图片位置
     */
    void manageClearButton() {
        if (this.getText().toString().equals(""))               //如果没有内容的话就去除尾部图片
            removeClearButton();
        else
            addClearButton();
    }

    //移除图片
    void removeClearButton() {
        this.setCompoundDrawables(this.firstIcon, this.getCompoundDrawables()[1], null, this.getCompoundDrawables()[3]);
    }

    //添加图片
    void addClearButton() {
        this.setCompoundDrawables(this.firstIcon, this.getCompoundDrawables()[1], this.lastIcon,
                this.getCompoundDrawables()[3]);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }
}

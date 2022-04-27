package com.example.candyfisher.interfaces;

import android.content.Context;

public interface GameLogicContract {
    interface GameLogicView{
        void initView();
        void changeBackground(boolean fishing);
    }
    interface GameLogicPresenter{
        void initPresenter(Context context, GameLogicView view);
        void setValues(float[] values);
        void setTilt();
        boolean checkThrow();
        void startFishing();
        void stopFishing();
    }
}

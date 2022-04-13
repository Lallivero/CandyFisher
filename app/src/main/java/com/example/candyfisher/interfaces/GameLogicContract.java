package com.example.candyfisher.interfaces;

public interface GameLogicContract {
    interface GameLogicView{
        void initView();
    }
    interface GameLogicPresenter{
        void setValues(float[] values);
        void setTilt();
        void initPresenter();
        boolean checkThrow();

    }
}

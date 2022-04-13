package com.example.candyfisher.presenter;

import com.example.candyfisher.interfaces.GameLogicContract;
import com.example.candyfisher.utils.Fifo;
import com.example.candyfisher.utils.Tilt;
import com.example.candyfisher.utils.Utils;

public class MyGamePresenter implements GameLogicContract.GameLogicPresenter {

    private float[] values;
    private Tilt previousTilt;
    private Tilt tilt;

    private Fifo fifo;

    @Override
    public void initPresenter() {
        if (values == null) {
            values = new float[3];
        }
        fifo = new Fifo();
    }

    @Override
    public void setValues(float[] values) {
        this.values = Utils.lowPassFilter(values, this.values);

    }

    @Override
    public void setTilt() {
        float tiltValue = 3f;
        previousTilt = tilt;
        if (values[0] > tiltValue && values[0] > Math.abs(values[1])) {
            tilt = Tilt.LEFT;
//        }
//        else if(filteredValues[1] > tiltValue && filteredValues[2] > 2){
//            tilt = Tilt.LEANINGFORWARDS;
        } else if (values[0] < -tiltValue && Math.abs(values[0]) > Math.abs(values[1])) {
            tilt = Tilt.RIGHT;
        } else if (values[1] > tiltValue && values[1] > Math.abs(values[0])) {
            tilt = Tilt.UPRIGHT;
        } else if (values[1] < -tiltValue && Math.abs(values[1]) > Math.abs(values[0])) {
            tilt = Tilt.UPSIDEDOWN;
        } else {
            tilt = Tilt.FACEUP;
        }
        if(tilt != previousTilt){
            fifo.push(tilt);
        }
    }


    @Override
    public boolean checkThrow() {
        Fifo key1 = new Fifo();
        key1.push(Tilt.UPRIGHT);
        key1.push(Tilt.FACEUP);
        Fifo key2 = new Fifo();
        key2.push(Tilt.UPSIDEDOWN);
        key2.push(Tilt.FACEUP);

        return (fifo.equals(key2) || fifo.equals(key1));
    }
}

package com.example.candyfisher.presenter;

import static android.content.Context.VIBRATOR_SERVICE;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import com.example.candyfisher.interfaces.GameLogicContract;
import com.example.candyfisher.utils.Fifo;
import com.example.candyfisher.utils.Tilt;
import com.example.candyfisher.utils.Utils;

import java.lang.ref.WeakReference;
import java.util.Random;
@Deprecated
public class MyGamePresenter implements GameLogicContract.GameLogicPresenter {
    private static final String TAG = "MyGamePresenter";
    private float[] values;
    private Tilt previousTilt;
    private Tilt tilt;
    private Context myContext;
    private Fifo fifo;
    private WeakReference<GameLogicContract.GameLogicView> view;
    private boolean fishing = false;

    @Override
    public void initPresenter(Context context, GameLogicContract.GameLogicView view) {
        if (values == null) {
            values = new float[3];
        }
        myContext = context;
        this.view = new WeakReference<>(view);
        this.view.get().initView();
        fifo = new Fifo();
    }

    @Override
    public void setValues(float[] values) {
        this.values = Utils.lowPassFilter(values, this.values);
//        this.values = values;
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
        if (tilt != previousTilt) {
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
        Log.d(TAG, String.valueOf(fifo));
        return (fifo.equals(key2) || fifo.equals(key1));
    }

    @Override
    public void startFishing() {
        fishing = true;
        view.get().changeBackground(fishing);
        long currentTime = System.currentTimeMillis();

        Random random = new Random();
        int randomTime = random.nextInt(3000);
        boolean loop = true;
        while (loop) {
//            view.get().changeBackground(fishing);
            if ((System.currentTimeMillis() - currentTime) > (randomTime + 2000)) {
                for (int i = 0; i < 3; i++) {
                    vibrate();
                    long vibTime = System.currentTimeMillis();
                    while ((System.currentTimeMillis() - vibTime) < (200)) {

                    }

                }
                loop = false;
            }
        }
        fifo.remove(0);
        fifo.remove(0);
        previousTilt = null;

    }

    @Override
    public void stopFishing() {
        fishing = false;
//        view.get().changeBackground(fishing);
    }

    private void vibrate() {
        if (Build.VERSION.SDK_INT >= 26) {
            ((Vibrator) myContext.getSystemService(VIBRATOR_SERVICE)).
                    vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            ((Vibrator) myContext.getSystemService(VIBRATOR_SERVICE)).vibrate(100);
        }
    }

}

package com.zzsoft.pcmtomp3.utils;

import android.util.Log;

/**
 * FFT 数据处理工厂
 */
public class FftFactory {
    private static final String TAG = FftFactory.class.getSimpleName();


    public byte[] makeFftData(short[] pcmData) {

        if (pcmData.length < 1024) {
            Log.d(TAG, "makeFftData");
            return null;
        }

        double[] doubles = ByteUtils.toHardDouble(pcmData);
        double[] fft = FFT.fft(doubles, 0);

        return ByteUtils.toSoftBytes(fft);
    }


}
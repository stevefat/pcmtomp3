package com.zzsoft.pcmtomp3;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import com.zzsoft.pcmtomp3.utils.FftFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import lameencode.LameEncode;

public class AudioCapture {
    private static final String TAG = "AudioCapture.class";

    //默认参数
    private static final int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    private static final int SAMPLE_RATE = 44100;
    private static final int CHANNEL_CONFIGS = AudioFormat.CHANNEL_IN_STEREO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    public static int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIGS, AUDIO_FORMAT);

    private boolean isStartRecord = false;
    private boolean isStopRecord = false;
    private boolean isDebug = true;

    private AudioRecord audioRecord;
    private Activity mContext;

    //文件流
    private FileOutputStream fileOutputStream;
    //mp3_buff
    private byte[] mp3_buff;
    //FFT格式化pcm数据
    private FftFactory fftFactory;
    //Handler此处暂时简写
    private Handler formatHandle = new Handler();


    /**
     * 采集子线程
     */
    private Thread threadCapture;

    public AudioCapture(Activity mContext) {
        this.mContext = mContext;
        initData();
    }

    public void start() {
        start(AUDIO_SOURCE, SAMPLE_RATE, CHANNEL_CONFIGS, AUDIO_FORMAT);
    }

    public void start(int audioSource, int sampleRate, int channels, int audioFormat) {
        if (isStartRecord) {
            if (isDebug)
                Log.d(TAG, "音频录制已经开启");
            return;
        }

        //各厂商实现存在差异
        bufferSize = AudioRecord.getMinBufferSize(sampleRate, channels, audioFormat);

        if (bufferSize == AudioRecord.ERROR_BAD_VALUE) {
            if (isDebug)
                Log.d(TAG, "无效参数");
            return;
        }

        if (isDebug)
            Log.d(TAG, "bufferSize = ".concat(String.valueOf(bufferSize)).concat(" byte"));

        isStartRecord = true;
        isStopRecord = false;

        audioRecord = new AudioRecord(AudioCapture.AUDIO_SOURCE, sampleRate, channels, audioFormat, bufferSize);
        audioRecord.startRecording();

        threadCapture = new Thread(new CaptureRunnable());
        threadCapture.start();

        if (isDebug) {
            Log.d(TAG, "音频录制开启...");
        }
    }

    private void initData() {
        fftFactory = new FftFactory();
        // calculate mp3buf_size in bytes = 1.25*num_samples + 7200 --  defined in lame.h 697 line
        //lame编码器中的MP3——buffer计算公式,定义在lame.h头文件中
        mp3_buff = new byte[(int) ((int) (7200 + (AudioCapture.bufferSize * 2 * 1.25 * 2)))];

//        initFileStream(filePath);启动录音时再初始化

        //初始化lame编码器,当前初始化采用lame默认参数,即输入流参数 = 输出流参数
        LameEncode.init(44100, 2, 16, 7);

    }



    public void stop() {
        if (!isStartRecord) {
            return;
        }
        isStopRecord = true;
        threadCapture.interrupt();

        audioRecord.stop();
        audioRecord.release();

        isStartRecord = false;
    }

    /**
     * 子线程读取采集到的数据
     */
    private class CaptureRunnable implements Runnable {

        @Override
        public void run() {
            while (!isStopRecord) {
                short[] pcmBuffer = new short[bufferSize];
//                byte[] pcmBuffer = new byte[bufferSize];
                int readRecord = audioRecord.read(pcmBuffer, 0, bufferSize);
                if (readRecord > 0) {

                    convertMp3(pcmBuffer, readRecord);
                    formatPcmData(pcmBuffer);
                    if (isDebug) {
                        Log.d(TAG, "音频采集数据源 -- ".concat(String.valueOf(readRecord)).concat(" -- bytes"));
                    }
                } else {
                    if (isDebug)
                        Log.d(TAG, "录音采集异常");
                }
                //延迟写入 SystemClock  --  Android专用
                SystemClock.sleep(10);
            }
        }
    }

    /**
     * pcm转换mp3
     *
     * @param audioSource   :pcm音频数据源
     * @param audioReadSize ：采样数
     */
    private void convertMp3(short[] audioSource, int audioReadSize) {
        //pcm转MP3使用ShortArray
        if (mContext.isFinishing() || mContext.isDestroyed()) {
            //资源被回收
            return;
        }
        if (audioReadSize <= 0) {
            return;
        }
        if (fileOutputStream == null) {
            return;
        }
        int mp3_byte = LameEncode.encoder(audioSource, mp3_buff, audioReadSize);
        if (mp3_byte < 0) {
            Log.d(TAG, "onCaptureListener: MP3编码失败 :" + mp3_byte);
            return;
        }
        try {
            Log.d(TAG, "onCaptureListener: 编码长度" + mp3_byte);
            fileOutputStream.write(mp3_buff, 0, mp3_byte);
        } catch (IOException e) {
            Log.d(TAG, "onCaptureListener: MP3文件写入失败");
            e.printStackTrace();
        }
    }

    /**
     * 回写lame缓冲区剩余字节数据
     */
    public void writeFlush() {
        int flushResult = LameEncode.flush(mp3_buff);
        if (flushResult > 0) {
            try {
                fileOutputStream.write(mp3_buff, 0, flushResult);
                fileOutputStream.close();
                fileOutputStream = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * FFT格式化pcm数据源
     *
     * @param audioSource ：pcm
     */
    private void formatPcmData(final short[] audioSource) {
        formatHandle.post(new Runnable() {
            @Override
            public void run() {
                byte[] data = fftFactory.makeFftData(audioSource);
                byte[] newData = new byte[data.length - 36];
                if (newData.length >= 0) {
                    System.arraycopy(data, 36, newData, 0, newData.length);
                }
            }
        });

    }


    /**
     * 初始化输出流
     *
     * @param filePath ：文件路径
     */
    public void initFileStream(String filePath) {
        try {
            fileOutputStream = new FileOutputStream(new File(filePath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            fileOutputStream = null;
        }
    }

}

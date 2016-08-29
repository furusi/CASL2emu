package com.example.furusho.casl2emu;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

/**
 * Created by furusho on 16/08/29.
 */

public class Casl2SoundGenerator {
    // とりあえず１オクターブ分の音階を確保（半音階含む）
    public static final double FREQ_A  = 220.0;
    public static final double FREQ_As = 233.081880;
    public static final double FREQ_B  = 246.941650;
    public static final double FREQ_C  = 261.625565;
    public static final double FREQ_Cs = 277.182630;
    public static final double FREQ_D  = 293.664767;
    public static final double FREQ_Ds = 311.126983;
    public static final double FREQ_E  = 329.627556;
    public static final double FREQ_F  = 349.228231;
    public static final double FREQ_Fs = 369.994227;
    public static final double FREQ_G  = 391.994535;
    public static final double FREQ_Gs = 415.304697;

    public static final double EIGHTH_NOTE = 0.125;
    public static final double FORTH_NOTE = 0.25;
    public static final double HALF_NOTE = 0.5;
    public static final double WHOLE_NOTE = 1.0;

    private AudioTrack audioTrack;

    // サンプリング周波数
    private int sampleRate;
    // バッファ・サイズ
    private int bufferSize;

    public Casl2SoundGenerator(int sampleRate, int bufferSize) {
        this.sampleRate = sampleRate;
        this.bufferSize = bufferSize;

        // AudioTrackを作成
        this.audioTrack = new AudioTrack(
                AudioManager.STREAM_MUSIC,  // 音楽ストリームを設定
                sampleRate, // サンプルレート
                AudioFormat.CHANNEL_OUT_MONO, // モノラル
                AudioFormat.ENCODING_DEFAULT,   // オーディオデータフォーマットPCM16とかPCM8とか
                bufferSize, // バッファ・サイズ
                AudioTrack.MODE_STREAM); // Streamモード。データを書きながら再生する
    }

    /**
     * サウンド生成
     * @param frequency 鳴らしたい音の周波数
     * @param soundLengh 音の長さ
     * @return 音声データ
     */
    public byte[] getSound(int frequency, int soundLength) {
        // byteバッファを作成
        double _frequency;
        double _soundLength;
        switch (frequency){
            case 1:
                _frequency=FREQ_A;
                break;
            case 2:
                _frequency=FREQ_As;
                break;
            case 3:
                _frequency=FREQ_B;
                break;
            case 4:
                _frequency=FREQ_C;
                break;
            case 5:
                _frequency=FREQ_Cs;
                break;
            case 6:
                _frequency=FREQ_D;
                break;
            case 7:
                _frequency=FREQ_Ds;
                break;
            case 8:
                _frequency=FREQ_E;
                break;
            case 9:
                _frequency=FREQ_F;
                break;
            case 10:
                _frequency=FREQ_Fs;
                break;
            case 11:
                _frequency=FREQ_G;
                break;
            case 12:
                _frequency=FREQ_Gs;
                break;
            default:
                _frequency=FREQ_C;
                break;
        }
        switch (soundLength){
            case 1:
                _soundLength = WHOLE_NOTE;
                break;
            case 2:
                _soundLength = HALF_NOTE;
                break;
            case 4:
                _soundLength = FORTH_NOTE;
                break;
            case 8:
                _soundLength = EIGHTH_NOTE;
                break;
            default:
               _soundLength=FORTH_NOTE;
                break;

        }
        byte[] buffer = new byte[(int)Math.ceil(bufferSize * _soundLength)];
        for(int i=0; i<buffer.length; i++) {
            double wave = i / (this.sampleRate / _frequency) * (Math.PI * 2);
            wave = Math.sin(wave);
            buffer[i] = (byte)(wave > 0.0 ? Byte.MAX_VALUE : Byte.MIN_VALUE);
        }

        return buffer;
    }

    /**
     * いわゆる休符
     * @param frequency
     * @param soundLength
     * @return 無音データ
     */
    public byte[] getEmptySound(int soundLength) {
        double _soundLength;
        switch (soundLength){
            case 1:
                _soundLength = WHOLE_NOTE;
                break;
            case 2:
                _soundLength = HALF_NOTE;
                break;
            case 4:
                _soundLength = FORTH_NOTE;
                break;
            case 8:
                _soundLength = EIGHTH_NOTE;
                break;
            default:
                _soundLength=FORTH_NOTE;
                break;

        }
        byte[] buff = new byte[(int)Math.ceil(bufferSize * _soundLength)];

        for(int i=0; i<buff.length; i++) {
            buff[i] = (byte)0;
        }
        return buff;
    }

    /**
     *
     * @return
     */
    public AudioTrack getAudioTrack() {
        return this.audioTrack;
    }

}

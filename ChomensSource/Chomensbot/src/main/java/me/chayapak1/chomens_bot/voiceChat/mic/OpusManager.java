/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.voiceChat.mic;

import me.chayapak1.chomens_bot.voiceChat.mic.JavaOpusDecoder;
import me.chayapak1.chomens_bot.voiceChat.mic.JavaOpusEncoder2;
import org.concentus.OpusApplication;

public class OpusManager {
    public static final int SAMPLE_RATE = 48000;
    public static final int FRAME_SIZE = 960;

    public static JavaOpusEncoder2 createEncoder(int sampleRate, int frameSize, int maxPayloadSize, OpusApplication application) {
        return new JavaOpusEncoder2(sampleRate, frameSize, maxPayloadSize, application);
    }

    public static JavaOpusEncoder2 createEncoder() {
        OpusApplication application = OpusApplication.OPUS_APPLICATION_AUDIO;
        return OpusManager.createEncoder(48000, 960, 1024, application);
    }

    public static JavaOpusDecoder createDecoder(int sampleRate, int frameSize, int maxPayloadSize) {
        return new JavaOpusDecoder(sampleRate, frameSize, maxPayloadSize);
    }

    public static JavaOpusDecoder createDecoder() {
        return OpusManager.createDecoder(48000, 960, 1024);
    }
}


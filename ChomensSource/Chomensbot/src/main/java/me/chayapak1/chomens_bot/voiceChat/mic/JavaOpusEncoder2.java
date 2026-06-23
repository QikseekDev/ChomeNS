/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.voiceChat.mic;

import org.concentus.OpusApplication;
import org.concentus.OpusEncoder;

public class JavaOpusEncoder2 {
    public OpusEncoder opusEncoder;
    public final byte[] buffer;
    public final int sampleRate;
    public final int frameSize;
    public final int maxPayloadSize;
    public final OpusApplication application;

    public JavaOpusEncoder2(int sampleRate, int frameSize, int maxPayloadSize, OpusApplication application) {
        this.sampleRate = sampleRate;
        this.frameSize = frameSize;
        this.maxPayloadSize = maxPayloadSize;
        this.application = application;
        this.buffer = new byte[maxPayloadSize];
        this.open();
    }

    private void open() {
        if (this.opusEncoder != null) {
            return;
        }
        try {
            this.opusEncoder = new OpusEncoder(this.sampleRate, 1, this.application);
        }
        catch (Exception e) {
            throw new IllegalStateException("Opus encoder error " + e.getMessage());
        }
    }

    public byte[] encode(short[] rawAudio) {
        int result;
        if (this.isClosed()) {
            throw new IllegalStateException("Encoder is closed");
        }
        try {
            result = this.opusEncoder.encode(rawAudio, 0, this.frameSize, this.buffer, 0, this.buffer.length);
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to encode audio data: " + e.getMessage());
        }
        if (result < 0) {
            throw new RuntimeException("Failed to encode audio data");
        }
        byte[] audio = new byte[result];
        System.arraycopy(this.buffer, 0, audio, 0, result);
        return audio;
    }

    public void resetState() {
        if (this.isClosed()) {
            throw new IllegalStateException("Encoder is closed");
        }
        this.opusEncoder.resetState();
    }

    public boolean isClosed() {
        return this.opusEncoder == null;
    }

    public void close() {
        if (this.isClosed()) {
            return;
        }
        this.opusEncoder = null;
    }
}


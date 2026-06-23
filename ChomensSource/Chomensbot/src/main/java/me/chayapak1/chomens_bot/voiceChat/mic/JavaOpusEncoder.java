/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.voiceChat.mic;

import de.maxhenkel.opus4j.OpusEncoder;
import org.concentus.OpusApplication;
import org.concentus.OpusEncoder;

public class JavaOpusEncoder {
    public OpusEncoder opusEncoder;
    public final byte[] buffer;
    public final int sampleRate;
    public final int frameSize;
    public final OpusEncoder.Application application;

    public JavaOpusEncoder(int sampleRate, int frameSize, int maxPayloadSize, OpusEncoder.Application application) {
        this.sampleRate = sampleRate;
        this.frameSize = frameSize;
        this.application = application;
        this.buffer = new byte[maxPayloadSize];
        this.open();
    }

    private void open() {
        if (this.opusEncoder != null) {
            return;
        }
        try {
            this.opusEncoder = new OpusEncoder(this.sampleRate, 1, JavaOpusEncoder.getApplication(this.application));
        }
        catch (Exception e) {
            throw new IllegalStateException("Failed to create Opus encoder", e);
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
            throw new RuntimeException("Failed to encode audio", e);
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

    public static OpusApplication getApplication(OpusEncoder.Application application) {
        return switch (application) {
            case OpusEncoder.Application.AUDIO -> OpusApplication.OPUS_APPLICATION_AUDIO;
            case OpusEncoder.Application.LOW_DELAY -> OpusApplication.OPUS_APPLICATION_RESTRICTED_LOWDELAY;
            default -> OpusApplication.OPUS_APPLICATION_VOIP;
        };
    }
}


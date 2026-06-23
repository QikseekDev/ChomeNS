/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.voiceChat.mic;

import org.concentus.OpusDecoder;
import org.concentus.OpusException;

public class JavaOpusDecoder {
    protected OpusDecoder opusDecoder;
    protected final short[] buffer;
    protected final int sampleRate;
    protected final int frameSize;
    protected final int maxPayloadSize;

    public JavaOpusDecoder(int sampleRate, int frameSize, int maxPayloadSize) {
        this.sampleRate = sampleRate;
        this.frameSize = frameSize;
        this.maxPayloadSize = maxPayloadSize;
        this.buffer = new short[4096];
        this.open();
    }

    private void open() {
        if (this.opusDecoder != null) {
            return;
        }
        try {
            this.opusDecoder = new OpusDecoder(this.sampleRate, 1);
        }
        catch (OpusException e) {
            throw new IllegalStateException("Opus decoder error " + e.getMessage());
        }
    }

    public short[] decode(byte[] data) {
        int result;
        if (this.isClosed()) {
            throw new IllegalStateException("Decoder is closed");
        }
        try {
            result = data == null || data.length == 0 ? this.opusDecoder.decode(null, 0, 0, this.buffer, 0, this.frameSize, false) : this.opusDecoder.decode(data, 0, data.length, this.buffer, 0, this.frameSize, false);
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to decode audio data: " + e.getMessage());
        }
        short[] audio = new short[result];
        System.arraycopy(this.buffer, 0, audio, 0, result);
        return audio;
    }

    public boolean isClosed() {
        return this.opusDecoder == null;
    }

    public void close() {
        if (this.opusDecoder == null) {
            return;
        }
        this.opusDecoder = null;
    }

    public void resetState() {
        if (this.isClosed()) {
            throw new IllegalStateException("Decoder is closed");
        }
        this.opusDecoder.resetState();
    }
}


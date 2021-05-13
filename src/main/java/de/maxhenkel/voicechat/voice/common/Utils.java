package de.maxhenkel.voicechat.voice.common;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class Utils {

    public static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
        }
    }

    public static float percentageToDB(float percentage) {
        return (float) (10D * Math.log(percentage));
    }

    public static short bytesToShort(byte b1, byte b2) {
        return (short) (((b2 & 0xFF) << 8) | (b1 & 0xFF));
    }

    public static byte[] shortToBytes(short s) {
        return new byte[]{(byte) (s & 0xFF), (byte) ((s >> 8) & 0xFF)};
    }

    /**
     * Changes the volume of 16 bit audio
     * Note that this modifies the input array
     *
     * @param audio  the audio data
     * @param volume the amplification
     * @return the adjusted audio
     */
    public static byte[] adjustVolumeMono(byte[] audio, float volume) {
        for (int i = 0; i < audio.length; i += 2) {
            short audioSample = bytesToShort(audio[i], audio[i + 1]);

            audioSample = (short) (audioSample * volume);

            audio[i] = (byte) audioSample;
            audio[i + 1] = (byte) (audioSample >> 8);

        }
        return audio;
    }

    /**
     * Changes the volume of 16 bit audio
     * Note that this modifies the input array
     *
     * @param audio       the audio data
     * @param volumeLeft  the amplification of the left audio
     * @param volumeRight the amplification of the right audio
     * @return the adjusted audio
     */
    public static byte[] adjustVolumeStereo(byte[] audio, float volumeLeft, float volumeRight) {
        for (int i = 0; i < audio.length; i += 2) {
            short audioSample = bytesToShort(audio[i], audio[i + 1]); //(short) (((audio[i + 1] & 0xff) << 8) | (audio[i] & 0xff));

            audioSample = (short) (audioSample * (i % 4 == 0 ? volumeLeft : volumeRight));

            audio[i] = (byte) audioSample;
            audio[i + 1] = (byte) (audioSample >> 8);

        }
        return audio;
    }

    /**
     * Convorts 16 bit mono audio to stereo
     *
     * @param audio the audio data
     * @return the adjusted audio
     */
    public static byte[] convertToStereo(byte[] audio) {
        byte[] stereo = new byte[audio.length * 2];
        for (int i = 0; i < audio.length; i += 2) {
            stereo[i * 2] = audio[i];
            stereo[i * 2 + 1] = audio[i + 1];

            stereo[i * 2 + 2] = audio[i];
            stereo[i * 2 + 3] = audio[i + 1];
        }
        return stereo;
    }

    /**
     * Convorts 16 bit mono audio to stereo
     *
     * @param audio       the audio data
     * @param volumeLeft  the volume modifier for the left audio
     * @param volumeRight tthe volume modifier for the right audio
     * @return the adjusted audio
     */
    public static byte[] convertToStereo(byte[] audio, float volumeLeft, float volumeRight) {
        byte[] stereo = new byte[audio.length * 2];
        for (int i = 0; i < audio.length; i += 2) {
            short audioSample = bytesToShort(audio[i], audio[i + 1]);
            short left = (short) (audioSample * volumeLeft);
            short right = (short) (audioSample * volumeRight);
            stereo[i * 2] = (byte) left;
            stereo[i * 2 + 1] = (byte) (left >> 8);

            stereo[i * 2 + 2] = (byte) right;
            stereo[i * 2 + 3] = (byte) (right >> 8);
        }
        return stereo;
    }

    private static float normalizeAngle(float angle) {
        angle = angle % 360F;
        if (angle <= -180F) {
            angle += 360F;
        } else if (angle > 180F) {
            angle -= 360F;
        }
        return angle;
    }

    private static float angle(Vec2 vec1, Vec2 vec2) {
        return (float) Math.toDegrees(Math.atan2(vec1.x * vec2.x + vec1.y * vec2.y, vec1.x * vec2.y - vec1.y * vec2.x));
    }

    private static float magnitude(Vec2 vec1) {
        return Mth.sqrt(Math.pow(vec1.x, 2) + Math.pow(vec1.y, 2));
    }

    private static float multiply(Vec2 vec1, Vec2 vec2) {
        return vec1.x * vec2.x + vec1.y * vec2.y;
    }

    private static Vec2 rotate(Vec2 vec, float angle) {
        return new Vec2(vec.x * Mth.cos(angle) - vec.y * Mth.sin(angle), vec.x * Mth.sin(angle) + vec.y * Mth.cos(angle));
    }

    /**
     * Calculates the audio level of a signal with specific samples.
     *
     * @param samples the samples of the signal to calculate the audio level of
     * @param offset  the offset in samples in which the samples start
     * @param length  the length in bytes of the signal in samples starting at offset
     * @return the audio level of the specified signal in db
     */
    public static double calculateAudioLevel(byte[] samples, int offset, int length) {
        double rms = 0D; // root mean square (RMS) amplitude

        for (int i = offset; i < length; i += 2) {
            double sample = (double) Utils.bytesToShort(samples[i], samples[i + 1]) / Short.MAX_VALUE;
            rms += sample * sample;
        }

        int sampleCount = length / 2;

        rms = (sampleCount == 0) ? 0 : Math.sqrt(rms / sampleCount);

        double db;

        if (rms > 0D) {
            db = Math.min(Math.max(20D * Math.log10(rms), -127D), 0D);
        } else {
            db = -127D;
        }

        return db;
    }

    /**
     * Calculates the highest audio level in packs of 100
     *
     * @param samples the audio samples
     * @return the audio level in db
     */
    public static double getHighestAudioLevel(byte[] samples) {
        double highest = -127D;
        for (int i = 0; i < samples.length; i += 100) {
            double level = Utils.calculateAudioLevel(samples, i, Math.min(i + 100, samples.length));
            if (level > highest) {
                highest = level;
            }
        }
        return highest;
    }

    /**
     * Gets the offset of the highest audio level in packs of 100
     *
     * @param samples         the audio samples
     * @param activationLevel the activation threshold
     * @return the audio level in db
     */
    public static int getActivationOffset(byte[] samples, double activationLevel) {
        int highestPos = -1;
        for (int i = 0; i < samples.length; i += 100) {
            double level = Utils.calculateAudioLevel(samples, i, Math.min(i + 100, samples.length));
            if (level >= activationLevel) {
                highestPos = i;
            }
        }
        return highestPos;
    }

    /**
     * Converts a dB value to a percentage value (-127 - 0) - (0 - 1)
     *
     * @param db the decibel value
     * @return the percantage
     */
    public static double dbToPerc(double db) {
        return (db + 127D) / 127D;
    }

    /**
     * Converts a percentage to a dB value (0 - 1) - (-127 - 0)
     *
     * @param perc the percantage
     * @return the decibel value
     */
    public static double percToDb(double perc) {
        return (perc * 127D) - 127D;
    }

}

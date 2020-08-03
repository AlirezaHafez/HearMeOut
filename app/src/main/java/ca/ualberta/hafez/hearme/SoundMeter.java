package ca.ualberta.hafez.hearme;

import android.media.MediaRecorder;

import java.io.IOException;
//by Dorsa
public class SoundMeter {
    private MediaRecorder mediaRecorder;
    private boolean started = false;

    public void start() {
        if (started) {
            return;
        }
        if (mediaRecorder == null) {
            mediaRecorder = new MediaRecorder();

            mediaRecorder.setAudioSource(
                    MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(
                    MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(
                    MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setOutputFile("/dev/null");

            try {
                mediaRecorder.prepare();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mediaRecorder.start();
            started = true;
        }
    }

    public double getAmplitude() {
        return mediaRecorder.getMaxAmplitude();
    }
}
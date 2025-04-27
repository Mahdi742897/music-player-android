package com.example.musicplayer;

import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private SeekBar seekBar;
    private SeekBar volumeSeekBar;
    private TextView timeLabel;
    private Button playButton;
    private boolean isPlaying = false;
    private Handler handler = new Handler();
    private boolean isPrepared = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        playButton = findViewById(R.id.playButton);
        Button stopButton = findViewById(R.id.stopButton);
        seekBar = findViewById(R.id.seekBar);
        timeLabel = findViewById(R.id.timeLabel);

        mediaPlayer = new MediaPlayer();
        String audioUrl = "https://dls.musics-fa.com/song/alibz/dlswm/Mohsen%20Chavoshi%20-%20Saate%20Divari%20(320).mp3";


        try {
            mediaPlayer.setDataSource(audioUrl);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            showError("Error setting up media player");
            e.printStackTrace();
        }

        mediaPlayer.setOnPreparedListener(mp -> {
            isPrepared = true;
            seekBar.setMax(mediaPlayer.getDuration());
            updateTimeLabel();
            playButton.setEnabled(true);
        });

        mediaPlayer.setOnErrorListener((mp, what, extra) -> {
            showError("Media player error occurred");
            return false;
        });

        playButton.setOnClickListener(v -> {
                togglePlayPause();
        });

        stopButton.setOnClickListener(v -> stopPlayer());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        handler.postDelayed(updateSeekBar, 1000);
    }



    private void togglePlayPause() {
        if (!isPrepared) {
            showError("Media not ready yet");
            return;
        }

        if (isPlaying) {
            mediaPlayer.pause();
            playButton.setText("Play");
        } else {
            mediaPlayer.start();
            playButton.setText("Pause");
        }
        isPlaying = !isPlaying;
    }

    private void stopPlayer() {
        if (mediaPlayer != null && isPlaying) {
            mediaPlayer.stop();
            try {
                mediaPlayer.prepareAsync();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            playButton.setText("Play");
            isPlaying = false;
            seekBar.setProgress(0);
        }
    }

    private Runnable updateSeekBar = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null && isPlaying) {
                seekBar.setProgress(mediaPlayer.getCurrentPosition());
                updateTimeLabel();
            }
            handler.postDelayed(this, 1000);
        }
    };

    private void updateTimeLabel() {
        if (mediaPlayer != null) {
            int currentPos = mediaPlayer.getCurrentPosition();
            int duration = mediaPlayer.getDuration();
            timeLabel.setText(String.format("%02d:%02d / %02d:%02d",
                    (currentPos / 1000) / 60,
                    (currentPos / 1000) % 60,
                    (duration / 1000) / 60,
                    (duration / 1000) % 60));
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        handler.removeCallbacks(updateSeekBar);

    }
}
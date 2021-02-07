package org.assignment.smarthomegesturecontrol;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

public class PracticeActivity extends AppCompatActivity {

    private static final String VIDEO_SAMPLE = "";
    private VideoView mVideoView;
    private TextView mBufferingTextView;

    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practice);

        button = (Button) findViewById(R.id.buttonPractice);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToVideoRecordingScreen();
            }
        });

        mBufferingTextView = findViewById(R.id.buffering_textview); // get a reference to the TextView in the layout

        mVideoView = (VideoView) findViewById(R.id.videoView); // get a reference to the VideoView in the layout

        Long optionSelected = 0l;

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            optionSelected = bundle.getLong("optionSelected");
            Log.i("onStart", "optionSelected val: " + optionSelected);
        }

        Uri videoUri = getMedia(optionSelected);
        initializePlayer(videoUri);
    }

    private void goToVideoRecordingScreen() {
        Intent intent = new Intent(this, VideoRecordingActivity.class);
        startActivity(intent);
    }

    private Uri getMedia(Long optionSelected) {

        String mediaName = "";

        switch (optionSelected.toString()) {
            case "1":
                mediaName = "https://d18ky98rnyall9.cloudfront.net/8tD_kauISI6Q_5GriPiO_A.processed/full/360p/index.mp4?Expires=1612742400&Signature=OqXagPm4kGbSDzEgKqYR4DM1yRZPm3cnbfiaeh68yzWxY5giYX~BVyU-jWs6zuIB22~7uUZLzCT07EpS255~IOVTM3MiJacH0Ne5HBDx3qlxJtu3EQr1rwnnf2iPygFeC00ED1d4V3Mv5beHPi94sQAAc4Ui8zoFFBK43~lZGHc_&Key-Pair-Id=APKAJLTNE6QMUY6HBC5A";
                break;
            case "2":
                mediaName = "https://d18ky98rnyall9.cloudfront.net/P22OQRsxTh2tjkEbMX4dZQ.processed/full/360p/index.mp4?Expires=1612742400&Signature=kvBqzMsC-KF9Ofs5242r4o1oXElGVveMnOgkKcYHtT-M~lFBKEWqdk330bQf9uckFUfe4uQU9xVxAW5bhoB6AEDFig5f3wuxb3Fm-E5UjsXabPqijq~mvYrXVS~u6-Rv5rrbvXKYxBAoLjqconirry39cV9M6oaqkzTZILCSrK8_&Key-Pair-Id=APKAJLTNE6QMUY6HBC5A";
                break;
            case "3":
                mediaName = "";
                break;
            case "4":
                mediaName = "";
                break;
            case "5":
                mediaName = "";
                break;
            case "6":
                mediaName = "";
                break;
            case "7":
                mediaName = "";
                break;
            case "8":
                mediaName = "";
                break;
            case "9":
                mediaName = "";
                break;
            default:
                mediaName = VIDEO_SAMPLE;
                break;
        }

        Log.i("medianame", "media name: " + mediaName);

        if (URLUtil.isValidUrl(mediaName)) {
            // media name is an external URL
            return Uri.parse(mediaName);
        } else { // media name is a raw resource embedded in the app
            return Uri.parse("android.resource://" + getPackageName() +
                    "/raw/" + mediaName);
        }
    }

    private void initializePlayer(Uri videoUri) {

        // Show the "Buffering..." message while the video loads.
        mBufferingTextView.setVisibility(VideoView.VISIBLE);

        mVideoView.setVideoURI(videoUri);

        mVideoView.setOnPreparedListener(
                new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        mBufferingTextView.setVisibility(VideoView.INVISIBLE);

                        mVideoView.seekTo(0);
//                        if (mCurrentPosition > 0) {
//                            mVideoView.seekTo(mCurrentPosition);
//                        } else {
//                            mVideoView.seekTo(1);
//                        }

                        mVideoView.start();
                    }
                });

        // Listener for onCompletion() event (runs after media has finished
        // playing).
        mVideoView.setOnCompletionListener(
                new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        Toast.makeText(PracticeActivity.this,
                                "Playback completed",
                                Toast.LENGTH_SHORT).show();

                        // Return the video position to the start.
                        mVideoView.seekTo(0);

                        Log.i("mediaplayer", "video play completed");
                    }
                });
    }

    private void releasePlayer() {
        mVideoView.stopPlayback();
    }

    @Override
    protected void onStart() {
        super.onStart();
//        Long optionSelected = 0l;
//
//        Bundle bundle = getIntent().getExtras();
//        if (bundle != null) {
//            optionSelected = bundle.getLong("optionSelected");
//            Log.i("onStart", "optionSelected val: " + optionSelected);
//        }
//
//        Uri videoUri = getMedia(optionSelected);
//        initializePlayer(videoUri);
    }

    @Override
    protected void onStop() {
        super.onStop();

        releasePlayer();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // for versions of Android older than N (lower than 7.0, API 24). If the app is running on an older version of Android, pause the VideoView here.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            mVideoView.pause();
        }
    }
}
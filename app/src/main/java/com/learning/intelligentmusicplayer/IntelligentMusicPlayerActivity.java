package com.learning.intelligentmusicplayer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

public class IntelligentMusicPlayerActivity extends AppCompatActivity {

    private RelativeLayout parentRelativeLayout;
    private SpeechRecognizer speechRecognizer;
    private Intent speechRecogniserIntent;
    private String keeper="";

    private ImageView pausePlayBtn, nextBtn, previousBtn;
    private TextView songNameTxt;

    private ImageView imageView;
    private RelativeLayout lowerRelativeLayout;
    private Button voiceEnabledBtn;
    private String mode = "ON";

    private MediaPlayer myMediaPlayer;
    private ArrayList<File> mySongs;
    private int Position;
    private String mSongName;

    private ImageView micLogo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intelligent_player);

        checkVoiceCommandPermission();

        pausePlayBtn = findViewById(R.id.play_pause_btn);
        nextBtn = findViewById(R.id.next_btn);
        previousBtn = findViewById(R.id.previous_btn);
        imageView = findViewById(R.id.logo);

        lowerRelativeLayout = findViewById(R.id.lower);
        voiceEnabledBtn = findViewById(R.id.voice_enabled_btn);
        songNameTxt = findViewById(R.id.songName);

        micLogo = findViewById(R.id.audioRecorder);

        parentRelativeLayout = findViewById(R.id.parentRelativeLayout);

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(IntelligentMusicPlayerActivity.this);
        speechRecogniserIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecogniserIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM );
        speechRecogniserIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());


        validateReceiveValueAndStartPlaying();

        imageView.setBackgroundResource(R.drawable.logo);




        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int i) {

            }

            @Override
            public void onResults(Bundle bundle) {
                ArrayList<String> matchesFound = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if(matchesFound!=null)
                {
                   if(mode.equals("ON"))
                   {
                       keeper = matchesFound.get(0);

                       if(keeper.equals("pause the song")||keeper.equals("pause song"))
                       {
                           playPauseSong();
                           Toast.makeText(IntelligentMusicPlayerActivity.this, "Command = " + keeper, Toast.LENGTH_SHORT).show();
                       }

                       else if(keeper.equals("play the song")||keeper.equals("play song"))
                       {
                           playPauseSong();
                           Toast.makeText(IntelligentMusicPlayerActivity.this, "Command = " + keeper, Toast.LENGTH_SHORT).show();
                       }

                       else if(keeper.equals("play next song")||keeper.equals("next song"))
                       {
                           playNextSong();
                           Toast.makeText(IntelligentMusicPlayerActivity.this, "Command = " + keeper, Toast.LENGTH_SHORT).show();
                       }

                       else if(keeper.equals("play previous song")||keeper.equals("previous song"))
                       {
                           playPreviousSong();
                           Toast.makeText(IntelligentMusicPlayerActivity.this, "Command = " + keeper, Toast.LENGTH_SHORT).show();
                       }


                   }

                }
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });

        micLogo.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                switch(motionEvent.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        speechRecognizer.startListening(speechRecogniserIntent);
                        keeper="";
                        break;
                    case MotionEvent.ACTION_UP:
                        speechRecognizer.stopListening();
                        break;
                }
                return false;
            }
        });

        voiceEnabledBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                if(mode.equals("ON"))
                {

                    mode = "OFF";
                    voiceEnabledBtn.setText("Voice Enabled Mode - OFF");
                    lowerRelativeLayout.setVisibility(View.VISIBLE);
                    micLogo.setVisibility(View.GONE);
                }
                else
                {

                    mode = "ON";
                    voiceEnabledBtn.setText("Voice Enabled Mode - ON");
                    lowerRelativeLayout.setVisibility(View.GONE);
                    micLogo.setVisibility(View.VISIBLE);
                }
            }
        });



        pausePlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playPauseSong();
            }
        });


        previousBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(myMediaPlayer.getCurrentPosition()>0)
                {
                    playPreviousSong();
                }
            }
        });


        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(myMediaPlayer.getCurrentPosition()>0)
                {
                    playNextSong();
                }
            }
        });


    }



    private void validateReceiveValueAndStartPlaying()
    {

        if(myMediaPlayer != null)
        {
            myMediaPlayer.stop();
            myMediaPlayer.release();
        }
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        Position = bundle.getInt("position", 0);

        mySongs = (ArrayList) bundle.getParcelableArrayList("song");
        mSongName = mySongs.get(Position).getName();

        String songName = intent.getStringExtra("name");

        songNameTxt.setText(mSongName);
        songNameTxt.setSelected(true);

        Uri uri = Uri.parse(mySongs.get(Position).toString());

        myMediaPlayer = MediaPlayer.create(IntelligentMusicPlayerActivity.this, uri);

        myMediaPlayer.start();

    }



    private void checkVoiceCommandPermission()
    {
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.RECORD_AUDIO)
                .withListener(new PermissionListener() {
                    @Override public void onPermissionGranted(PermissionGrantedResponse response)
                    {
                        if(!(ContextCompat.checkSelfPermission(IntelligentMusicPlayerActivity.this, Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED))
                        {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package: "+getPackageName()));
                            startActivity(intent);
                            finish();
                        }
                    }
                    @Override public void onPermissionDenied(PermissionDeniedResponse response)
                    {

                    }
                    @Override public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token)
                    {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    private void playPauseSong()
    {
        imageView.setBackgroundResource(R.drawable.four);

        if(myMediaPlayer.isPlaying())
        {
            pausePlayBtn.setImageResource(R.drawable.play);
            myMediaPlayer.pause();
        }

        else
        {
            pausePlayBtn.setImageResource(R.drawable.pause);
            myMediaPlayer.start();

            imageView.setBackgroundResource(R.drawable.five);
        }


    }

    private void playNextSong()
    {
        myMediaPlayer.pause();
        myMediaPlayer.stop();
        myMediaPlayer.release();

        Position = ((Position+1)%mySongs.size());

        Uri uri = Uri.parse(mySongs.get(Position).toString());

        myMediaPlayer = MediaPlayer.create(IntelligentMusicPlayerActivity.this, uri);
        mSongName = mySongs.get(Position).toString();
        songNameTxt.setText(mSongName);
        myMediaPlayer.start();

        imageView.setBackgroundResource(R.drawable.three);

        if(myMediaPlayer.isPlaying())
        {
            pausePlayBtn.setImageResource(R.drawable.pause);
        }

        else
        {
            pausePlayBtn.setImageResource(R.drawable.play);
            imageView.setBackgroundResource(R.drawable.five);
        }

    }

    private void playPreviousSong()
    {
        myMediaPlayer.pause();
        myMediaPlayer.stop();
        myMediaPlayer.release();

        if((Position-1)<0)
        {
            Position = mySongs.size()-1;
        }
        else
        {
            Position = Position-1;
        }

        Uri uri = Uri.parse(mySongs.get(Position).toString());

        myMediaPlayer = MediaPlayer.create(IntelligentMusicPlayerActivity.this, uri);

        mSongName = mySongs.get(Position).toString();
        songNameTxt.setText(mSongName);
        myMediaPlayer.start();

        imageView.setBackgroundResource(R.drawable.two);

        if(myMediaPlayer.isPlaying())
        {
            pausePlayBtn.setImageResource(R.drawable.pause);
        }

        else
        {
            pausePlayBtn.setImageResource(R.drawable.play);
            imageView.setBackgroundResource(R.drawable.five);
        }
    }


}

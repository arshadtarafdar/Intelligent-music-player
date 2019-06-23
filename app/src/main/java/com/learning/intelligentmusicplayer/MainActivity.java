package com.learning.intelligentmusicplayer;

import android.Manifest;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Arshad on 13-06-2019.
 */
public class MainActivity extends AppCompatActivity {

    private String[] itemsAll;
    private ListView mSongsList;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mSongsList = findViewById(R.id.songsList);

        appExternalStoragePermission();
    }

    public ArrayList<File> readOnlyAudioFiles(File file)
    {
        ArrayList<File> arrayList = new ArrayList<>();
        File allFiles[] = file.listFiles();
        for(File individualFile: allFiles)
        {
            if(individualFile.isDirectory() && !(individualFile.isHidden()))
            {
                arrayList.addAll(readOnlyAudioFiles(individualFile));
            }
            else
            {
                if(individualFile.getName().endsWith(".mp3") || individualFile.getName().endsWith(".wav") || individualFile.getName().endsWith(".wma") || individualFile.getName().endsWith(".aac"))
                {
                    arrayList.add(individualFile);
                }
            }
        }
        return arrayList;
    }

    private void displayAudioSongName()
    {
        final ArrayList<File> audioSongs = readOnlyAudioFiles(Environment.getExternalStorageDirectory()); //CHANGE TO EXTERNAL DIRECTORY
        itemsAll = new String[audioSongs.size()];
        for(int songCounter=0; songCounter<audioSongs.size(); songCounter++)
        {
            itemsAll[songCounter] = audioSongs.get(songCounter).getName();
        }
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, itemsAll);
        mSongsList.setAdapter(arrayAdapter);



        mSongsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                String songName = mSongsList.getItemAtPosition(i).toString();
                //Toast.makeText(MainActivity.this, songName, Toast.LENGTH_LONG).show();

                Intent intent = new Intent(MainActivity.this, IntelligentMusicPlayerActivity.class);
                intent.putExtra("song", audioSongs);
                intent.putExtra("name", songName);
                intent.putExtra("position", i);
                startActivity(intent);
            }
        });

    }

    private void appExternalStoragePermission()
    {
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override public void onPermissionGranted(PermissionGrantedResponse response)
                    {
                        displayAudioSongName(); //THIS IS THE PROBLEM
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


}

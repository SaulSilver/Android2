package a2dv606.com.dv606hh222ixassignment2.Exercise4;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import a2dv606.com.dv606hh222ixassignment2.R;

/**
 * Bind the music playing 'Service' to interact with playback.
 * Classname has to be the same as the one in the Manifest file.
 * Created by hatem on 2017-03-20.
 */

public class MusicService extends Service implements MediaPlayer.OnPreparedListener,
                                                        MediaPlayer.OnErrorListener,
                                                            MediaPlayer.OnCompletionListener {

    private MediaPlayer player;     //media player
    private ArrayList<Song> songs = null;      //song list
    private int songPosition;       //current position
    private String songTitle = "";

    private MusicBinder musicBind = new MusicBinder();

    private static NotificationManager notificationManager;
    private Notification notification;
    private static final int NOTIFY_ID = 1;

    private boolean shuffle = false;
    private Random randomizer;

    public void onCreate() {
        super.onCreate();       //create the service
        songPosition = 0;       //initialize the position
        player = new MediaPlayer(); //create player

        initMusicPlayer();
        notificationManager = ((NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE));
        randomizer = new Random();
    }

    //Initialize MusicPlayer
    public void initMusicPlayer() {
        songs = new ArrayList<>();

        //set player properties
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        //Set the class as listener for when the MediaPlayer instance is
        //prepared, when a song has completed playback, and when an is thrown.
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    //An interaction method between 'Activity' and 'Service' for
    // passing the list of songs from the Activity class
    public void setList(ArrayList<Song> songsList) {
        songs = songsList;
    }

    /**
     * A method to provide song selection by the user
     */
    public void setSong(int songIndex) {
        songPosition = songIndex;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    /**
     * Stop playing when the app is exited
     */
    @Override
    public boolean onUnbind(Intent intent) {
        player.stop();
        player.release();
        return false;
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        notificationManager.cancel(NOTIFY_ID);
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if (player.getCurrentPosition() >= 0) {
            mediaPlayer.reset();
            playNext();
        }
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        mediaPlayer.reset();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {

        mediaPlayer.start();    //start the playback

        Intent notificationIntent = new Intent(this, MP3Player.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                                        PendingIntent.FLAG_UPDATE_CURRENT);

        notification = new NotificationCompat.Builder(this)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.play)
                .setContentText("Playing")
                .setContentTitle(songTitle)
                .build();

        startForeground(NOTIFY_ID, notification);
    }


    /**
     * A class to bind between Activity and Service
     */
    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    public void playSong() {

        Song playSong = songs.get(songPosition);    //get a song
        songTitle = playSong.getTitle();            //get song title
        long currentSong = playSong.getId();        //get id

        Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currentSong);

        player.reset();
        //Set the URI as the data source for the MediaPlayer instance
        try {
            player.setDataSource(getApplicationContext(), trackUri);
        } catch (IOException e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }

        player.prepareAsync();
    }

    //----------- Respond to the music controller

    public int getPosn(){
        return player.getCurrentPosition();
    }

    public int getDur(){
        return player.getDuration();
    }

    public boolean isPng(){
        return player.isPlaying();
    }

    public void pausePlayer(){
        player.pause();
    }

    public void seek(int posn){
        player.seekTo(posn);
    }

    public void go(){
        player.start();
    }

    //------------ Skipping to previous and next songs. And Shuffle

    public void playPrevious() {
        songPosition--;
        if (songPosition <= 0)
            songPosition = songs.size()-1;
        playSong();
    }

    public void playNext() {
        //If shuffle is on, randomize the next song position
        if (shuffle) {
            int newSong = songPosition;
            while (newSong == songPosition) {
                newSong = randomizer.nextInt(songs.size());
            }
            songPosition = newSong;
        }
        else {
            songPosition++;
            if (songPosition >= songs.size())
                songPosition = 0;
        }
        playSong();
    }

    public void setShuffle() {
        if (shuffle)
            shuffle = false;
        else shuffle = true;
    }
}

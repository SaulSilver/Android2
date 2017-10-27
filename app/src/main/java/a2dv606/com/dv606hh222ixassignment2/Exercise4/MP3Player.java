package a2dv606.com.dv606hh222ixassignment2.Exercise4;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import a2dv606.com.dv606hh222ixassignment2.R;
import a2dv606.com.dv606hh222ixassignment2.Exercise4.MusicService.MusicBinder;

public class MP3Player extends AppCompatActivity implements MediaController.MediaPlayerControl {

    private ArrayList<Song> songList;
    private ListView songView;

    private MusicService musicService;
    private Intent playIntent;
    private boolean musicBound = false;     //Keep track of whether the Activity class is bound to the Service class

    private MusicController musicController;

    private boolean paused = false;
    private boolean playbackPaused = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mp3_player);

        songView = (ListView) findViewById(R.id.song_list);
        songList = new ArrayList<>();

        //Check for external storage access permission
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                //No explanation needed, we can request the permission
                ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        } else {
            getSongList();      //fill up the song list once permission is acquired
        }

        Collections.sort(songList, new Comparator<Song>() {
            @Override
            public int compare(Song a, Song b) {
                return a.getTitle().compareTo(b.getTitle());
            }
        });

        SongAdapter songAdapter = new SongAdapter(this, songList);
        songView.setAdapter(songAdapter);

        musicService = new MusicService();

        setMusicController();       //Setup music controller
    }

    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            musicService = ((MusicBinder)service).getService();
            //pass list
            musicService.setList(songList);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicService = null;
            musicBound = false;
        }
    };

    /**
     * Start the Service when the Activity starts
     */
    @Override
    protected void onStart() {
        super.onStart();
        if(playIntent == null){
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }

    /**
     * adding the menu options to the activity
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.song_menu, menu);
        return true;
    }

    //react to the end and shuffle buttons when clicked
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_shuffler:
                musicService.setShuffle();
                break;
            case R.id.action_end:
                stopService(playIntent);
                musicService = null;
                System.exit(0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
      super.onPause();
        paused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (paused) {
            setMusicController();
            paused = false;
        }
    }

    @Override
    protected void onStop() {
        musicController.hide();
        unbindService(musicConnection);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        stopService(playIntent);
        musicService = null;
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                //If the request is cancelled, the result arrays are empty
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Permission was granted
                    getSongList();
                } else {
                    //Permission denied. Disable the functionality that depends on this permission
                    Toast.makeText(getBaseContext(), "Permission denied to access the external storage", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    /**
     * Retrieve the songs from the external storage
     */
    public void getSongList() {
        //Retrieve song info
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            int titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);

            //add songs to list
            while (musicCursor.moveToNext()) {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                songList.add(new Song(thisId, thisTitle, thisArtist));
            }
        }
        musicCursor.close();
    }

    /**
     * A method for the onClick property of each song e.g. play the song
     */
    public void songPicked(View view) {
        musicService.setSong(Integer.parseInt(String.valueOf(view.getTag())));
        musicService.playSong();
        if (playbackPaused) {
            setMusicController();
            playbackPaused = false;
        }
        musicController.show(0);
    }

    //----------------- MediaPlayerControl methods ------------------

    /**
     * Set the music controller because it's gonne be reused more than once
     */
    private void setMusicController(){
        musicController = new MusicController(this);

        //Adding functionality to the next & previous buttons
        musicController.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playPrevious();
            }
        });

        musicController.setMediaPlayer(this);
        musicController.setAnchorView(findViewById(R.id.song_list));
        musicController.setEnabled(true);
    }

    /**
     * play next song
     */
    private void playNext() {
        musicService.playNext();

        if (playbackPaused) {
            setMusicController();
            playbackPaused = false;
        }
        musicController.show(0);
    }

    /**
     * play previous song
     */
    private void playPrevious() {
        musicService.playPrevious();

        if (playbackPaused) {
            setMusicController();
            playbackPaused = false;
        }
        musicController.show(0);
    }

    @Override
    public void start() {
        musicService.go();
    }

    @Override
    public void pause() {
        playbackPaused = true;
        musicService.pausePlayer();
    }

    @Override
    public int getDuration() {
        if (musicService != null && musicBound && musicService.isPng())
            return musicService.getDur();

        else return 0;
    }

    @Override
    public int getCurrentPosition() {
        if (musicService != null && musicBound && musicService.isPng())
            return musicService.getPosn();

        else return 0;
    }

    @Override
    public void seekTo(int i) {
        musicService.seek(i);
    }

    @Override
    public boolean isPlaying() {
        if (musicService != null && musicBound)
            return musicService.isPng();

        else return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }
}

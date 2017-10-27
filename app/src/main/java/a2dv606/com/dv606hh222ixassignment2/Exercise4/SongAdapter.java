package a2dv606.com.dv606hh222ixassignment2.Exercise4;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import a2dv606.com.dv606hh222ixassignment2.R;

/**
 * An extension of Adapter to map the songs to the list view
 * Created by hatem on 2016-09-19.
 */
public class SongAdapter extends BaseAdapter {

    private ArrayList<Song> songs = null;
    private LayoutInflater songInf;

    public SongAdapter(Context context, ArrayList<Song> songsList) {
        songs = songsList;
        songInf = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return songs.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //map to song layout
        LinearLayout songLayout = (LinearLayout) songInf.inflate(R.layout.song, parent, false);

        //get title and artist views
        TextView songView = (TextView) songLayout.findViewById(R.id.song_title);
        TextView artistView = (TextView) songLayout.findViewById(R.id.song_artist);

        //get song position
        Song currentSong = songs.get(position);

        //get title and artist
        songView.setText(currentSong.getTitle());
        artistView.setText(currentSong.getArtist());
        //set position as tag
        songLayout.setTag(position);

        return songLayout;
    }
}

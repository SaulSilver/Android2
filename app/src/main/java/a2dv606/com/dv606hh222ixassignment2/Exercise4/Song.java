package a2dv606.com.dv606hh222ixassignment2.Exercise4;

/**
 * A Song Object
 * Created by hatem on 2016-09-19.
 */
public class Song {

    private long id;
    private String title;
    private String artist;

    private Song next = null;
    private Song previous = null;

    public Song(long songID, String songTitle, String songArtist) {
        artist = songArtist;
        id = songID;
        title = songTitle;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public Song getNext() {
        return next;
    }

    public Song getPrevious() {
        return previous;
    }

    public void setNext(Song next) {
        this.next = next;
    }

    public void setPrevious(Song previous) {
        this.previous = previous;
    }
}


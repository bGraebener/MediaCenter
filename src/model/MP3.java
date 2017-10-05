package model;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import javax.imageio.ImageIO;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.media.Media;

public class MP3 implements Serializable {

	private static final long serialVersionUID = 5865872445781533526L;

	private String artist;
	private String album;
	private String title;
//	private String duration;
	private String location;
	private long lengthInSeconds;

	public MP3(String location) {
		this.location = location;
		setMP3Info();
	}

	private void setMP3Info() {
		Mp3File file;
		try {
			file = new Mp3File(location);

			if (file.hasId3v2Tag()) {

				ID3v2 tags = file.getId3v2Tag();

				this.title = tags.getTitle();
				this.album = tags.getAlbum();
				this.artist = tags.getArtist();
//				this.duration = String.format("%02d:%02d", file.getLengthInSeconds() / 60,
//						file.getLengthInSeconds() % 60);
				this.lengthInSeconds = file.getLengthInSeconds();

			}

		} catch (UnsupportedTagException | InvalidDataException | IOException e) {
			e.printStackTrace();
		}
	}

	public Image getCoverArt() {

		Mp3File file;
		Image image = null;

		try {
			file = new Mp3File(location);
			byte[] cover = file.getId3v2Tag().getAlbumImage();
			if (cover == null) {

				image = new Image(getClass().getResourceAsStream("/res/music_placeholder.png"));

			} else {
				image = SwingFXUtils
						.toFXImage(ImageIO.read(new ByteArrayInputStream(file.getId3v2Tag().getAlbumImage())), null);
			}

		} catch (UnsupportedTagException | InvalidDataException | IOException e) {
			e.printStackTrace();
		}
		return image;
	}
	
	public String getLyrics(){
		
		Mp3File file;
		String lyrics = null;

		try {
			file = new Mp3File(location);
			
			lyrics = file.getId3v2Tag().getLyrics();
			
		} catch (UnsupportedTagException | InvalidDataException | IOException e) {
			e.printStackTrace();
		}
		
		return lyrics == null ? "" : lyrics;
	}

	public String getArtist() {
		return artist;
	}

	public String getAlbum() {
		return album;
	}

	public String getTitle() {
		return title;
	}

	public String getDuration() {
		return String.format("%02d:%02d", getLengthInSeconds() / 60,
				getLengthInSeconds() % 60);
	}
	
	public long getLengthInSeconds(){
		return lengthInSeconds;
	}

	public String getLocation() {
		return location;
	}

	public Media getMP3AsMedia() {
		return new Media(new File(location).toURI().toString());
	}

	@Override
	public String toString() {
		return "MP3 [" + (artist != null ? "artist=" + artist + ", " : "")
				+ (album != null ? "album=" + album + ", " : "") + (title != null ? "title=" + title + ", " : "")
				+ (location != null ? "location=" + location + ", " : "") + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((location == null) ? 0 : location.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MP3 other = (MP3) obj;
		if (location == null) {
			if (other.location != null)
				return false;
		} else if (!location.equals(other.location))
			return false;
		return true;
	}

}

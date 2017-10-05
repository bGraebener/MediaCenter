package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class MusicPlaylist {

	private List<MP3> members;

	public MusicPlaylist(MP3... mp3s) {
		this();
		this.addAll(mp3s);
	}

	public MusicPlaylist(Collection<MP3> listOfMP3) {
		this();
		this.addAll(listOfMP3);
	}

	public MusicPlaylist() {
		members = new ArrayList<MP3>();
	}

	public ObservableList<MP3> getMP3sAsObservable() {
		return FXCollections.observableArrayList(members);
	}
	
	public List<MP3> getMembers(){
		return new ArrayList<>(members);
	}

	public boolean addAll(Collection<MP3> collection) {
		return members.addAll(collection);
	}

	public boolean addAll(MP3... mp3s) {
		return members.addAll(Arrays.asList(mp3s));
	}

	public boolean addSong(MP3 song) {
		return members.add(song);
	}

	public boolean removeSong(MP3 song) {
		return members.removeIf(members::contains);
	}

	public boolean removeAll(Collection<MP3> collection) {
		return members.removeAll(collection);
	}

	public boolean removeAll(MP3... collection) {
		return members.removeAll(Arrays.asList(collection));
	}

}

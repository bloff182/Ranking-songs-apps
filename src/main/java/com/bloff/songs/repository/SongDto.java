package com.bloff.songs.repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.bloff.songs.model.Category;
import com.bloff.songs.model.Song;

@Repository
public class SongDto {
	
	private List<Song> listSongs;

	public SongDto() {
		this.listSongs = new ArrayList<>();
		listSongs.add(new Song("Baby John","Abba","Life",Category.POP,5));
		listSongs.add(new Song("Makumba","Big Cyc","Afro",Category.ALTERNATIVE,2));
		listSongs.add(new Song("Birds","Imagin Dragons","Fly",Category.ROCK,9));
		listSongs.add(new Song("Iris","Goo Goo Dolls","New year",Category.ROCK,1));
		listSongs.add(new Song("Adams Song","Blink 182","Genaration",Category.ROCK,4));
		listSongs.add(new Song("Miss you","Blink 182","Genaration",Category.ROCK,6));
		
		orderedList();
	}

    public SongDto(List<Song> listSongs) {
		this.listSongs = listSongs;
	}

	public List<Song> getListSongs() {
		orderedList();
		return listSongs;
	}

	public void setListSongs(List<Song> listSongs) {
		this.listSongs = listSongs;
	}

	public Song addSong(Song song) {
        listSongs.add(song);
        return song;
    }

	public List<Song> orderedList() {
		listSongs.sort(Comparator.comparing(Song::getTitle));
		return listSongs;
	}
}

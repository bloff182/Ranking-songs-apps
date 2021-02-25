package com.bloff.songs.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bloff.songs.model.Category;
import com.bloff.songs.model.Song;
import com.bloff.songs.repository.SongDto;

@Service
public class SongServiceImpl implements SongService {

	private int indexExistingSong;
	
	private SongDto songDto;
	
	@Autowired
	public SongServiceImpl(SongDto songDto) {
		this.songDto = songDto;
	}

	@Override
	public List<Song> findAll() {
		return songDto.getListSongs();
	}

	@Override
	public Song save(Song song) {
		List<Song> songs = songDto.getListSongs();
		Boolean checkSong = findSong(song);
		Song existing = songs.get(indexExistingSong);
		if(checkSong) {
			existing.setVotes(existing.getVotes()+song.getVotes());
			song = existing;
		} else 
			songDto.addSong(song);
		
		return song;
	}

	@Override
	public List<Song> getRankingTopTen() {
		return createList(10);
	}

	@Override
	public List<Song> getRankingTopThree() {
		return createList(3);
	}
	
	@Override
	public List<Song> getRankingAll() {
		return sortByVote(songDto.getListSongs());
	}

	@Override
	public List<Song> getReportCategory(Category category) {
		List<Song> songsCategory = new ArrayList<>();
		List<Song> songs = songDto.getListSongs();
		for(Song s:songs) {
			if(s.getCategory().equals(category))
				songsCategory.add(s);
		}
		
		return songsCategory;
	}

	@Override
	public Boolean findSong(Song song) {
		
		List<Song> songs = songDto.getListSongs();
		Boolean existing = false;
		for(int i = 0; i< songs.size(); i++) {
			if(song.getTitle().equalsIgnoreCase(songs.get(i).getTitle())&&
					song.getAuthor().equalsIgnoreCase(songs.get(i).getAuthor())&&
					song.getAlbum().equalsIgnoreCase(songs.get(i).getAlbum())&&
					song.getCategory().equals(songs.get(i).getCategory())) {
				existing = true;
				indexExistingSong = i;
				break;
			}
		}
		return existing;
	}

	@Override
	public Song findExistSongAddOrResetVote(String title, String author, String album, Category category, int type) {
		List<Song> list = songDto.getListSongs();
		Song tempSong = null;
		
		for(Song song:list) {
			if(song.getTitle().equals(title)&&song.getAuthor().equals(author)&&
					song.getAlbum().equals(album)&&song.getCategory().equals(category)) {
				if(type == 0)
					song.setVotes(song.getVotes()+1);
				if(type == 1) {
					song.setVotes(0);
				}
				tempSong = song;
				break;
			}
		}
		return tempSong;
	}

	private List<Song> createList(int x) {
		
		List<Song> tempList = new ArrayList<>();
		List<Song> list = songDto.getListSongs();
		sortByVote(list);
		
		if(list.size()>x) {
			for(int i=0; i<x;i++) {
				tempList.add(list.get(i));
			}
		} else
			tempList.addAll(list);
		return tempList;
	}

	private List<Song> sortByVote(List<Song> tempList) {
		tempList.sort(Comparator.comparing(Song::getVotes).reversed());
		
		return tempList;
		
	}
	
}

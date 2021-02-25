package com.bloff.songs.service;

import java.util.List;

import com.bloff.songs.model.Category;
import com.bloff.songs.model.Song;

public interface SongService {

    List<Song> findAll();

	Song save(Song song);
	
	List<Song> getRankingTopTen();
	
	List<Song> getRankingTopThree();
	
	List<Song> getRankingAll();
	
	List<Song> getReportCategory(Category category);
	
	Boolean findSong(Song song);
	
	Song findExistSongAddOrResetVote(String title, String author, String album, Category category, int type);
}

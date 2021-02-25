package com.bloff.songs.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.bloff.songs.model.Category;
import com.bloff.songs.model.Song;
import com.bloff.songs.service.SongService;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ParsingException;
import nu.xom.Serializer;

@Controller
@RequestMapping("/songs")
public class SongController {

	private static final String REDIRECT_ALL = "redirect:/songs/list";
	private static final String SONGS_FORM = "songs-list";

	private Category categoryEnum = null;
	private Boolean errorSong = false;

	private SongService songService;

	@Autowired
	public SongController(SongService songService) {
		this.songService = songService;
	}

	/**
	 * 
	 * @param theModel
	 * @return 
	 */
	@GetMapping("/list")
	public String getList(Model theModel) {

		getCategories(theModel);

		List<Song> songs = songService.findAll();
//test
		printSongs(songs);

		if (errorSong) {
			theModel.addAttribute("errorr", "Wrong category format");
		}

		theModel.addAttribute("songs", songs);
		theModel.addAttribute("errorSong", errorSong);

		return SONGS_FORM;
	}
// TO DO tests
	@GetMapping("/resetError")
	public String resetError(Model theModel) {
		List<Song> songs = songService.findAll();
		theModel.addAttribute("errorSong", errorSong);
		theModel.addAttribute("songs", songs);
		errorSong = false;
		return REDIRECT_ALL;
	}

	@GetMapping("/showAddForm")
	public String showAddForm(Model theModel) {

		Song song = new Song();

		theModel.addAttribute("song", song);

		return "add-song";
	}

// method do save song from add form
// test to do validation
	@PostMapping("/saveSong")
	public String saveSong(@Valid @ModelAttribute("song") Song song, BindingResult binding) {

		if (binding.hasErrors()) {
			return "add-song";
		}

		song.setVotes(0);

		songService.save(song);

		return REDIRECT_ALL;
	}

	@GetMapping("/addVote/{title}/{author}/{album}/{category}")
	public String addVote(@PathVariable("title") String title, @PathVariable("author") String author,
			@PathVariable("album") String album, @PathVariable("category") String category) {

		songService.findExistSongAddOrResetVote(title, author, album, convertCategory(category), 0);

		return REDIRECT_ALL;
	}

	@GetMapping("/resetVote/{title}/{author}/{album}/{category}")
	public String resetVote(@PathVariable("title") String title, @PathVariable("author") String author,
			@PathVariable("album") String album, @PathVariable("category") String category) {

		songService.findExistSongAddOrResetVote(title, author, album, convertCategory(category), 1);
		return REDIRECT_ALL;
	}

	@GetMapping("/resetVoteForAll")
	public String resetVoteForAll() {

		for (Song song : songService.findAll()) {
			song.setVotes(0);
		}
		return REDIRECT_ALL;
	}

// do i need theSong? check it, może można cos modelAttr z lista lub z nazwa pliku a moze metode save dac do metody csvReader?
// check metod csvReader
//	Test to do
	@PostMapping("/loadFile")
	public String loadFile(@ModelAttribute("theFile") String theFile, Model theModel) {

		List<Song> songs = new ArrayList<>();
		System.out.println("file: " + theFile);

		String formatFile = formatFile(theFile);

		if (formatFile.equalsIgnoreCase("csv")) {
			songs = csvReader(theFile);
		} else if (formatFile.equalsIgnoreCase("xml")) {
			songs = xmlReader(theFile);
		}
		for (Song song : songs) {
			songService.save(song);
		}

		theModel.addAttribute("songs", songs);

		return REDIRECT_ALL;
	}

	@GetMapping("/raportTopThree")
	public String makeReportTopThree(Model theModel) {

		getCategories(theModel);

		List<Song> topThree = songService.getRankingTopThree();

		theModel.addAttribute("songs", topThree);

		return SONGS_FORM;
	}

	@GetMapping("/raportTopTen")
	public String makeReportTopTen(Model theModel) {

		getCategories(theModel);

		List<Song> topTen = songService.getRankingTopTen();

		theModel.addAttribute("songs", topTen);

		return SONGS_FORM;
	}

	@GetMapping("/raportAll")
	public String makeReportAll(Model theModel) {

		getCategories(theModel);

		List<Song> all = songService.getRankingAll();

		theModel.addAttribute("songs", all);

		return SONGS_FORM;
	}

	@GetMapping("/getByCategory")
	public String getByCategory(@RequestParam("theCategory") String category, Model theModel) {

		getCategories(theModel);

		convertCategory(category);

		List<Song> songs = songService.getReportCategory(categoryEnum);

		theModel.addAttribute("songs", songs);
		theModel.addAttribute("info_category", category);

		return SONGS_FORM;
	}

	@GetMapping("/createReport")
	public String createReport(@RequestParam("theFile") String report) {

		List<Song> list = null;
		String file = null;

		switch (report) {
		case "1": {
			list = songService.getRankingAll();
			file = "reportAll.csv";
			break;
		}
		case "2": {
			list = songService.getRankingTopThree();
			file = "reportTopThree.csv";
			break;
		}
		case "3": {
			list = songService.getRankingTopTen();
			file = "reportTopTen.csv";
			break;
		}
		case "4": {
			list = songService.getReportCategory(categoryEnum);
			file = "reportCategory.csv";
			break;
		}
		case "5": {
			list = songService.getRankingAll();
			file = "reportAll.xml";
			break;
		}
		case "6": {
			list = songService.getRankingTopThree();
			file = "reportTopThree.xml";
			break;
		}
		case "7": {
			list = songService.getRankingTopTen();
			file = "reportTopTen.xml";
			break;
		}
		case "8": {
			list = songService.getReportCategory(categoryEnum);
			file = "reportCategory.xml";
			break;
		}
		default: {
			break;
		}
		}
		String formatFile = formatFile(file);
		if (formatFile.equalsIgnoreCase("csv")) {

			csvReport(file, list);
		} else if (formatFile.equalsIgnoreCase("xml")) {

			try {
				xmlReport(file, list);
			} catch (IOException exc) {
				exc.printStackTrace();
			}
		}

		return REDIRECT_ALL;
	}

	public String formatFile(String file) {
		String formatFile = file.substring(file.length() - 3, file.length());
		return formatFile;
	}

	public void getCategories(Model theModel) {
		Set<Category> categories = new TreeSet<>();
		List<Song> songsCategory = songService.findAll();
		for (int i = 0; i < songsCategory.size(); i++) {
			categories.add(songsCategory.get(i).getCategory());
		}

		theModel.addAttribute("categories", categories);

	}

	public void csvReport(String file, List<Song> list) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(file));) {

			for (Song song : list) {
				StringBuilder builder = new StringBuilder();
				builder.append(song.getTitle()).append(",").append(song.getAuthor()).append(",").append(song.getAlbum())
						.append(",").append(song.getCategory()).append(",").append(song.getVotes());
				writer.write(builder.toString());
				writer.newLine();
			}
		} catch (IOException exc) {
			exc.printStackTrace();
		}
	}

	public List<Song> csvReader(String file) {

		List<Song> songs = new LinkedList<>();
		File theFile = new File(file);
		try (BufferedReader reader = new BufferedReader(new FileReader(theFile))) {
			boolean firstLine = true;
			while (true) {

				String line = reader.readLine();
				if (line == null)
					break;
				if (firstLine) {
					firstLine = false;
					continue;
				}
				String[] elements = line.split(",");

				Song theSong = new Song(elements[0], elements[1], elements[2],
						Category.valueOf(elements[3].toUpperCase()), Integer.parseInt(elements[4]));
				songs.add(theSong);
			}
			errorSong = false;
		} catch (IOException exc) {
			exc.printStackTrace();
		} catch (IllegalArgumentException exc) {
			System.out.println("Błędny format kategorii");
			errorSong = true;
			exc.printStackTrace();
		}
		return songs;
	}

	public List<Song> xmlReader(String file) {
		List<Song> songs = new LinkedList<>();
		File theFile = new File(file);

		// builder builds xml data
		Builder builder = new Builder();
		Document doc;
		try {
			doc = builder.build(theFile);

			// get the root element <songs>
			Element root = doc.getRootElement();

			// gets all element with tag <song>
			Elements songsElement = root.getChildElements("song");

			for (int i = 0; i < songsElement.size(); i++) {
				// get the current song element
				Element song = songsElement.get(i);

				String title = song.getFirstChildElement("title").getValue();
				String author = song.getFirstChildElement("author").getValue();
				String album = song.getFirstChildElement("album").getValue();
				Category category = Category.valueOf(song.getFirstChildElement("category").getValue().toUpperCase());
				int votes = Integer.parseInt(song.getFirstChildElement("votes").getValue());

				Song tempSong = new Song(title, author, album, category, votes);
				songs.add(tempSong);
				System.out.println(tempSong);
			}
			errorSong = false;
		} catch (ParsingException | IOException e) {
			System.out.println("Błędny format kategorii");
			errorSong = true;
			e.printStackTrace();
		}

		return songs;
	}

	public void xmlReport(String file, List<Song> list) throws UnsupportedEncodingException, IOException {

		// root element <example>
		Element root = new Element("songs");

		// add all the songs
		for (Song song : list) {

			// make the main song element <song>
			Element songElement = new Element("song");

			// make the name element
			Element titleElement = new Element("title");
			Element authorElement = new Element("author");
			Element albumElement = new Element("album");
			Element categoryElement = new Element("category");
			Element voteElement = new Element("votes");

			// add names to element
			titleElement.appendChild(song.getTitle());
			authorElement.appendChild(song.getAuthor());
			albumElement.appendChild(song.getAlbum());
			categoryElement.appendChild(song.getCategory().name());
			voteElement.appendChild(String.valueOf(song.getVotes()));

			// add all contents to song element
			songElement.appendChild(titleElement);
			songElement.appendChild(authorElement);
			songElement.appendChild(albumElement);
			songElement.appendChild(categoryElement);
			songElement.appendChild(voteElement);

			// add song to root
			root.appendChild(songElement);
		}

		// create doc off of root
		Document doc = new Document(root);

		// get a file output stream ready
		FileOutputStream fileOutputStream = new FileOutputStream(new File(file));

		// use the serializer class to write it all
		Serializer serializer = new Serializer(fileOutputStream, "UTF-8");
		serializer.setIndent(5);
		serializer.write(doc);

	}

	private Category convertCategory(String category) {
		categoryEnum = Category.valueOf(category);
		return categoryEnum;
	}

	private void printSongs(List<Song> sss) {
		for (int i = 0; i < sss.size(); i++) {

			System.out.println(i + " = " + sss.get(i));

		}
	}

}

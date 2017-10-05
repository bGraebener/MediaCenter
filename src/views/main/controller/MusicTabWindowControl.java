package views.main.controller;

import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.*;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import model.MP3;
import model.MusicPlaylist;

public class MusicTabWindowControl extends Tab implements Initializable {
	@FXML
	private MediaView musicMediaView;
	@FXML
	private ImageView musicCoverImageView;
	@FXML
	private Button musicPrevButon, musicPlayButton, musicStopButton, musicNextButton, moveRowUpButton,
			moveRowDownButton, saveMusicPlaylistButton, musicSearchButton, randomButton;
	@FXML
	private Slider musicTimeSlider, musicVolumeSlider;
	@FXML
	private ToggleButton musicRepeatButton, musicShuffleButton;
	@FXML
	private TableView<MP3> playListTableView;
	@FXML
	private TableColumn<MP3, Integer> numberColumn;
	@FXML
	private TableColumn<MP3, String> artistsColumn, albumColumn, musicTitleColumn, musicDurationColumn;
	@FXML
	private TreeView<String> artistsTreeView;
	@FXML
	private Tab albumTab, artistsTab, musicPlaylistTab, musicAllTitlesTab;
	@FXML
	private ListView<String> albumsListView;
	@FXML
	private ListView<MusicPlaylist> playlistsListView;
	@FXML
	private ListView<MP3> allMusicListView;
	@FXML
	private ListView<MP3> filteredMusicTitlesListView;
	@FXML
	private TextField musicSearchTextField;
	@FXML
	private VBox playerVBox;
	@FXML
	private ProgressBar songProgressBar;
	@FXML
	private HBox coverArtHBox;
	@FXML
	private TabPane filterTabPane;
	@FXML
	private Label mediaInformation, totalDurationLabel, musicTimeLabel, bottomInformationLabel;
	@FXML
	private TextArea lyricsTextArea;

	private MediaPlayer currentPlayer;
	private Path rootForMusic;
	private static List<MP3> allMp3s;
	private ObservableList<String> albumsObservableList;
	private List<String> allArtists;
	private ObservableList<MP3> currentPlayListObservable;
	private FilteredList<MP3> filteredList;
	private ObservableValue<? extends Predicate<? super MP3>> artistsFilterPredicate, albumsFilterPredicate,
			searchFilterPredicate;
	// private DataFormat customDataFormat;
	private ResourceBundle resources;

	public MusicTabWindowControl() {

		resources = ResourceBundle.getBundle("res.lang", Locale.GERMAN);
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/main/views/MusicTabWindow.fxml"), resources);

		loader.setRoot(this);
		loader.setController(this);

		try {
			loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initialize(URL location, ResourceBundle resources) {

		rootForMusic = Paths.get("C:/Users/Basti/Music/");
		// customDataFormat = new DataFormat("5865872445781533526L");

		// allMp3s = importMP3s();

		try (ObjectInputStream inStream = new ObjectInputStream(new FileInputStream("res/allMP3.ser"))) {
			allMp3s = (ArrayList<MP3>) inStream.readObject();

		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}

		// allMp3s = allMp3s.parallelStream().unordered().filter(
		// mp3 -> Files.exists(Paths.get(mp3.getLocation())) && mp3.getArtist()
		// != null && mp3.getAlbum() != null)
		// .collect(Collectors.toList());

		initArtistTreeView();
		initFilteredMP3ListView();
		initPlayListTableView();
		initListenersAndBindings();

		albumsObservableList = FXCollections.observableArrayList(getAllAlbums());
		albumsListView.setItems(albumsObservableList);

		bottomInformationLabel.textProperty()
				.bind(Bindings.concat(
						String.format("%s: %-10d %s: %-10d %s: %d", resources.getString("numOfArtist"),
								getNumOfArtists(), resources.getString("numOfAlbums"), getNumOfAlbums(),
								resources.getString("numOfMP3s"), getNumOfMP3s()),
						"\t\t\t\t\t\t\t\t\t\t# of Songs: ", Bindings.size(filteredList)));

		// filteredMusicTitlesListView.setOnDragDetected(event -> {
		// Dragboard dragboard =
		// filteredMusicTitlesListView.startDragAndDrop(TransferMode.ANY);
		// ClipboardContent cbc = new ClipboardContent();
		// cbc.put(customDataFormat,
		// new
		// ArrayList<MP3>(filteredMusicTitlesListView.getSelectionModel().getSelectedItems()));
		// dragboard.setContent(cbc);
		// event.consume();
		// });
		//
		// playListTableView.setOnDragOver(event -> {
		// event.acceptTransferModes(TransferMode.ANY);
		// });
		//
		// playListTableView.setOnDragDropped(event -> {
		// Dragboard dragboard = event.getDragboard();
		// System.out.println(dragboard.hasContent( new
		// DataFormat("5865872445781533526L")));
		// System.out.println(dragboard.getContentTypes());
		//
		// if (dragboard.hasContent( DataFormat.)) {
		// }
		// event.setDropCompleted(true);
		// event.consume();
		//
		// });

	}

	/**
	 * 
	 */
	private void initListenersAndBindings() {
		musicVolumeSlider.setValue(.5);
		lyricsTextArea.prefWidthProperty().bind(coverArtHBox.widthProperty().subtract(270));
		songProgressBar.prefWidthProperty().bind(playerVBox.widthProperty().subtract(5));
		songProgressBar.setProgress(0);

		albumsListView.setOnKeyReleased(event -> {
			if (event.getCode().isLetterKey()) {
				for (int x = 0; x < albumsListView.getItems().size(); x++) {
					if (albumsListView.getItems().get(x).toUpperCase().startsWith(event.getCode().getName())) {
						albumsListView.getSelectionModel().select(x);
						albumsListView.scrollTo(x);
						return;
					}
				}
			}
		});

		playListTableView.setOnKeyReleased(event -> {
			if (event.getCode() == KeyCode.DELETE) {
				currentPlayListObservable.removeAll(playListTableView.getSelectionModel().getSelectedItems());
				playListTableView.getSelectionModel().clearSelection();
			}
		});

		currentPlayListObservable.addListener((ListChangeListener.Change<? extends MP3> obs) -> {
			long totalInSeconds = currentPlayListObservable.stream().map(mp3 -> mp3.getLengthInSeconds())
					.reduce((a, b) -> a + b).orElse(0L);

			totalDurationLabel.setText(String.format("Total: %02d:%02d:%02d", totalInSeconds / 3600,
					totalInSeconds % 3600 / 60, totalInSeconds % 60));
		});

		// albumTab.setOnSelectionChanged(event ->
		// albumsListView.getSelectionModel().clearSelection());
		// artistsTab.setOnSelectionChanged(event ->
		// artistsTreeView.getSelectionModel().clearSelection());
		filterTabPane.getTabs().forEach(tab -> {
			tab.selectedProperty().addListener(listener -> {
				if (tab.isSelected()) {
					Platform.runLater(() -> {
						if (tab.equals(artistsTab)) {
							artistsTreeView.requestFocus();
							albumsListView.getSelectionModel().clearSelection();
							filteredList.predicateProperty().bind(artistsFilterPredicate);
						} else if (tab.equals(albumTab)) {
							albumsListView.requestFocus();
							artistsTreeView.getSelectionModel().clearSelection();
							filteredList.predicateProperty().bind(albumsFilterPredicate);
						}
					});
				}
			});
		});

		filterTabPane.setOnKeyPressed(event -> {
			if (event.getCode() == KeyCode.TAB) {
				int currentIndex = filterTabPane.getSelectionModel().getSelectedIndex();
				filterTabPane.getSelectionModel().clearAndSelect((currentIndex + 1) % filterTabPane.getTabs().size());
			}

		});

		playerVBox.setOnScroll(event -> {
			musicVolumeSlider.setValue(musicVolumeSlider.getValue() + (event.getDeltaY() / 500));
		});

		searchFilterPredicate = Bindings.createObjectBinding(() -> mp3 -> {
			return musicSearchTextField.getText().isEmpty()
					|| mp3.getArtist().toLowerCase().startsWith(musicSearchTextField.getText().toLowerCase())
					|| mp3.getAlbum().toLowerCase().startsWith(musicSearchTextField.getText().toLowerCase())
					|| mp3.getTitle().toLowerCase().startsWith(musicSearchTextField.getText().toLowerCase());

		}, musicSearchTextField.textProperty());

		musicSearchTextField.focusedProperty().addListener(listener -> {
			if (musicSearchTextField.isFocused()) {
				filteredList.predicateProperty().bind(searchFilterPredicate);
			}
		});

	}

	/**
	 * 
	 */
	private void initPlayListTableView() {
		currentPlayListObservable = FXCollections.observableArrayList();

		playListTableView.setItems(currentPlayListObservable);

		playListTableView.getColumns().forEach(column -> column.setSortable(false));

		numberColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<Integer>(
				playListTableView.getItems().indexOf(cellData.getValue()) + 1));
		artistsColumn.setCellValueFactory(new PropertyValueFactory<>("artist"));
		albumColumn.setCellValueFactory(new PropertyValueFactory<>("album"));
		musicTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
		musicDurationColumn.setCellValueFactory(new PropertyValueFactory<>("duration"));
		playListTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
	}

	/**
	 * 
	 */
	private void initFilteredMP3ListView() {
		filteredMusicTitlesListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		filteredList = new FilteredList<>(FXCollections.observableArrayList(allMp3s));

		artistsFilterPredicate = Bindings.createObjectBinding(() -> mp3 -> {

			TreeItem<String> selectedItemArtistItem = artistsTreeView.getSelectionModel().getSelectedItem();
			String selectedArtist;
			String selectedParent;

			if (selectedItemArtistItem == null) {
				selectedArtist = "";
				selectedParent = "";
			} else {
				selectedArtist = selectedItemArtistItem.getValue();
				selectedParent = artistsTreeView.getSelectionModel().getSelectedItem().getParent().getValue();
			}

			return selectedArtist.equals(mp3.getArtist())
					|| (selectedArtist.equals(mp3.getAlbum()) && selectedParent.equals(mp3.getArtist()));

		}, artistsTreeView.getSelectionModel().selectedItemProperty());

		albumsFilterPredicate = Bindings.createObjectBinding(() -> mp3 -> {
			String selectedItemAlbumTab = albumsListView.getSelectionModel().getSelectedItem();

			if (selectedItemAlbumTab == null) {
				selectedItemAlbumTab = "";
			}
			return selectedItemAlbumTab.equals(mp3.getAlbum());
		}, albumsListView.getSelectionModel().selectedItemProperty());

		filteredList.predicateProperty().bind(artistsFilterPredicate);

		filteredMusicTitlesListView.setCellFactory(column -> {
			ListCell<MP3> cell = new ListCell<MP3>() {

				@Override
				public void updateItem(MP3 item, boolean empty) {
					super.updateItem(item, empty);

					if (!empty && item != null) {
						setText(item.getArtist() + " - " + item.getTitle());
					} else {
						setText(null);
					}
				}
			};

			return cell;
		});
		filteredMusicTitlesListView.setItems(filteredList);

		ContextMenu filteredListContextMenu = new ContextMenu();
		MenuItem addSongsToPlaylist = new MenuItem(resources.getString("addSongsMenuItem"));
		addSongsToPlaylist.setOnAction(event -> {
			currentPlayListObservable.addAll(filteredMusicTitlesListView.getSelectionModel().getSelectedItems());
		});
		filteredListContextMenu.getItems().add(addSongsToPlaylist);

		filteredMusicTitlesListView.setContextMenu(filteredListContextMenu);
	}

	/**
	 * 
	 */
	private void initArtistTreeView() {
		TreeItem<String> rootTreeItem = new TreeItem<>("Artists");

		allArtists = getAllArtists();

		allArtists.stream().forEach(artist -> {

			TreeItem<String> artistItem = new TreeItem<>(artist);
			rootTreeItem.getChildren().add(artistItem);

			getAllMP3s().stream().filter(mp3 -> mp3.getArtist().equals(artist)).map(mp3 -> mp3.getAlbum())
					.filter(album -> !album.equals("misc")).distinct().forEach(album -> {
						TreeItem<String> albumItem = new TreeItem<>(album);
						artistItem.getChildren().add(albumItem);

					});
		});

		artistsTreeView.setRoot(rootTreeItem);
		artistsTreeView.getSelectionModel().select(new Random().nextInt(getNumOfArtists()));
		artistsTreeView.scrollTo(artistsTreeView.getSelectionModel().getSelectedIndex());
		Platform.runLater(() -> artistsTreeView.requestFocus());
		artistsTreeView.setOnKeyPressed(event -> {
			if (event.getCode().isLetterKey()) {
				for (int x = 0; x < rootTreeItem.getChildren().size(); x++) {
					if (rootTreeItem.getChildren().get(x).getValue().toUpperCase()
							.startsWith(event.getCode().getName().toUpperCase())) {
						artistsTreeView.getSelectionModel().select(x);
						artistsTreeView.scrollTo(x);
						return;
					}
				}
			}
		});
	}

	/**
	 * Imports all MP3's from a specified root path. Assumes all physical files
	 * are stored in following folder structure: Artist-> Album-> MP3. Where the
	 * Album folder is missing a virtual Album folder called "Singles" is
	 * created. The method traverses the file system and creates a MP3 Object
	 * from each file with a "*.mp3" extension. The folder closest to the root
	 * is assumed the artists' name, the folder just above the file is assumed
	 * the albums' name.
	 * 
	 * @return a list with all found MP3's
	 */
	private List<MP3> importMP3s() {

		List<MP3> allMP3s = new ArrayList<>();

		try {
			Files.walkFileTree(rootForMusic, new SimpleFileVisitor<Path>() {

				@Override
				public FileVisitResult preVisitDirectory(Path file, BasicFileAttributes attr) {

					if (file.getFileName().toString().endsWith("Tagged")
							|| file.getFileName().toString().endsWith("Hörbücher")) {
						return FileVisitResult.SKIP_SUBTREE;
					}
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {

					if (file.toAbsolutePath().toString().endsWith(".mp3")) {
						if (allMP3s.stream()
								.noneMatch((mp3) -> mp3.getLocation().equals(file.toAbsolutePath().toString()))) {
							MP3 newMP3 = new MP3(file.toAbsolutePath().toString());
							allMP3s.add(newMP3);
						}
					}
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
		return allMP3s;
	}

	/**
	 * Retrieves a sorted List all distinct artists from the list of all mp3's.
	 * 
	 * @return list of artists
	 */
	private List<String> getAllArtists() {
		return allMp3s.stream().map(mp3 -> mp3.getArtist()).filter(artist -> artist != null).distinct()
				.sorted((a, b) -> a.toLowerCase().compareTo(b.toLowerCase())).collect(Collectors.toList());
	}

	/**
	 * Retrieves a sorted list of all distinct albums from the list of all MP3's
	 * 
	 * @return list of albums
	 */
	private List<String> getAllAlbums() {
		return allMp3s.stream().map(mp3 -> mp3.getAlbum()).filter(album -> album != null).distinct().sorted()
				.collect(Collectors.toList());
	}

	// DONE when mp3 gets moved up or down the index passed to playPlaylist
	// needs to be updated
	/**
	 * 
	 * @param index
	 */
	private void playPlayList(int index) {

		if (currentPlayer != null) {
			songProgressBar.setProgress(0);
			musicTimeSlider.setValue(0);
			currentPlayer.dispose();
			currentPlayer.stop();
		}

		if (index == currentPlayListObservable.size()) {
			if (musicRepeatButton.isSelected()) {
				index = 0;
			} else {
				return;
			}
		}

		MP3 currentMP3 = currentPlayListObservable.get(index);

		if (Files.exists(Paths.get(currentMP3.getLocation()))) {

			musicCoverImageView.setImage(currentMP3.getCoverArt());
			currentPlayer = new MediaPlayer(currentMP3.getMP3AsMedia());
			currentPlayer.setOnEndOfMedia(() -> {
				playPlayList((playListTableView.getSelectionModel().getSelectedIndex() + 1));
			});

			lyricsTextArea
					.setText(currentMP3.getArtist() + "\n" + currentMP3.getTitle() + "\n\n" + currentMP3.getLyrics());

			playListTableView.getSelectionModel().clearAndSelect(index);
			setMediaPlayerListeners(currentPlayer);
			musicMediaView.setMediaPlayer(currentPlayer);
			currentPlayer.play();
		} else {
			// TODO couldn't play song dialog
		}
	}

	/**
	 * 
	 * @param player
	 */
	private void setMediaPlayerListeners(MediaPlayer player) {
		player.currentTimeProperty().addListener((ob, old, newVal) -> {
			musicTimeLabel
					.setText(String.format("%02d:%02d", (int) newVal.toSeconds() / 60, (int) newVal.toSeconds() % 60));
			songProgressBar.setProgress(newVal.toSeconds() / player.getTotalDuration().toSeconds());
			musicTimeSlider.setValue(newVal.toSeconds() / player.getTotalDuration().toSeconds());
		});

		player.volumeProperty().bind(musicVolumeSlider.valueProperty());

		musicTimeSlider.setValue(0);
		musicTimeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
			if (musicTimeSlider.isValueChanging()) {
				player.seek(player.getMedia().getDuration().multiply(musicTimeSlider.getValue()));
			}
		});

	}

	@FXML
	private void importMusicFiles(ActionEvent event) {
		System.out.println("importing music");

		Alert importAlert = new Alert(AlertType.CONFIRMATION);
		int before = getNumOfMP3s();
		allMp3s = importMP3s();
		int after = getNumOfMP3s();
		importAlert.setContentText(after - before + "");
		importAlert.show();
	}

	// Event Listener on Button[#musicPrevButon].onAction
	@FXML
	public void previousSongButton(ActionEvent event) {
		int nextIndex = playListTableView.getSelectionModel().getSelectedIndex() - 1;
		if (nextIndex >= 0) {
			playPlayList(nextIndex);
			playListTableView.getSelectionModel().clearAndSelect(nextIndex);
		}
	}

	// Event Listener on Button[#musicNextButton].onAction
	@FXML
	public void nextSongButton(ActionEvent event) {
		int nextIndex = playListTableView.getSelectionModel().getSelectedIndex() + 1;
		if (nextIndex < currentPlayListObservable.size()) {
			playPlayList(nextIndex);
			playListTableView.getSelectionModel().clearAndSelect(nextIndex);
		}
	}

	// Event Listener on Button[#musicPlayButton].onAction
	@FXML
	public void playMusicButton(ActionEvent event) {

		if (currentPlayListObservable.isEmpty()) {
			return;
		}

		if (currentPlayer == null) {
			int index = playListTableView.getSelectionModel().getSelectedIndex() < 0 ? 0
					: playListTableView.getSelectionModel().getSelectedIndex();
			playPlayList(index);
			musicPlayButton.setText("Pause");
			playListTableView.getSelectionModel().select(index);
		} else if (currentPlayer.getStatus() == MediaPlayer.Status.STOPPED
				|| currentPlayer.getStatus() == MediaPlayer.Status.PAUSED
				|| currentPlayer.getStatus() == MediaPlayer.Status.READY) {
			currentPlayer.play();
			musicPlayButton.setText("Pause");
		} else {
			musicPlayButton.setText(" Play ");
			currentPlayer.pause();
		}
	}

	// Event Listener on Button[#musicStopButton].onAction
	@FXML
	public void stopMusicButton(ActionEvent event) {
		musicPlayButton.setText(" Play ");
		if (currentPlayer != null) {
			currentPlayer.stop();
		}
	}

	// Event Listener on Button[#musicShuffleButton].onAction
	@FXML
	public void shuffleMusicButton(ActionEvent event) {
		Collections.shuffle(currentPlayListObservable);
	}

	@FXML
	private void randomPlaylist(ActionEvent event) {
		new Random().ints(10, 0, getNumOfMP3s()).forEach((index) -> currentPlayListObservable.add(allMp3s.get(index)));
	}

	// Event Listener on TableView[#playListTableView].onDragDropped
	@FXML
	public void importIntoPlaylistTable(DragEvent event) {
		// TODO Autogenerated
	}

	// Event Listener on TableView[#playListTableView].onDragOver
	@FXML
	public void acceptDragIntoPlaylistTable(DragEvent event) {
		// TODO Autogenerated
	}

	// Event Listener on TableView[#playListTableView].onMouseClicked
	@FXML
	public void songInPlayListClicked(MouseEvent event) {
		if (event.getClickCount() == 2) {
			musicPlayButton.setText("Pause");
			playPlayList(playListTableView.getSelectionModel().getSelectedIndex());
		}
	}

	// Event Listener on Button[#moveRowUpButton].onAction
	@FXML
	public void moveMusicPlaylistRowUp(ActionEvent event) {
		int selectedIndex = playListTableView.getSelectionModel().getSelectedIndex();
		if (selectedIndex - 1 >= 0) {
			currentPlayListObservable.add(selectedIndex - 1, playListTableView.getItems().remove(selectedIndex));
			playListTableView.getSelectionModel().clearAndSelect(selectedIndex - 1);
		}
	}

	// Event Listener on Button[#moveRowDownButton].onAction
	@FXML
	public void moveMusicPlaylistRowDown(ActionEvent event) {
		int selectedIndex = playListTableView.getSelectionModel().getSelectedIndex();
		if (selectedIndex + 1 < playListTableView.getItems().size()) {
			currentPlayListObservable.add(selectedIndex + 1, playListTableView.getItems().remove(selectedIndex));
			playListTableView.getSelectionModel().clearAndSelect(selectedIndex + 1);
		}
	}

	// Event Listener on Button[#saveMusicPlaylistButton].onAction
	@FXML
	public void saveMusicPlayList(ActionEvent event) {
		// TODO Autogenerated
	}

	@FXML
	public void clearMusicPlayList(ActionEvent event) {
		if (currentPlayer != null) {
			currentPlayer.dispose();
			currentPlayer.stop();
		}
		currentPlayListObservable.clear();
		musicPlayButton.setText(" Play ");
	}

	// Event Listener on ListView[#filteredMusicTitlesListView].onDragDetected
	@FXML
	public void dragSingleSongs(MouseEvent event) {
		// TODO Autogenerated
	}

	// Event Listener on ListView[#filteredMusicTitlesListView].onMouseClicked
	@FXML
	public void songInFilteredSongListClicked(MouseEvent event) {
		if (event.getClickCount() == 2) {
			currentPlayListObservable.add(filteredMusicTitlesListView.getSelectionModel().getSelectedItem());
			if (playListTableView.getItems().size() == 1) {
				musicPlayButton.setText("Pause");
				playPlayList(0);
			} else if (currentPlayer.getStatus() == MediaPlayer.Status.STOPPED) {
				musicPlayButton.setText("Pause");
				playPlayList(currentPlayListObservable.size() - 1);
			}
		}
	}

	// Event Listener on TreeView[#artistsTreeView].onMouseClicked &&
	// albumsTreeView
	@FXML
	private void moveSongsToPlayListTable(MouseEvent event) {
		if (event.getClickCount() == 2) {
			boolean empty = playListTableView.getItems().size() == 0;
			currentPlayListObservable.addAll(filteredMusicTitlesListView.getItems());

			if (empty) {
				musicPlayButton.setText("Pause");
				playPlayList(0);
			}
		}
	}

	// Event Listener on Button[#musicSearchButton].onAction
	@FXML
	public void searchForMusicFile(ActionEvent event) {
		// TODO Autogenerated
	}

	public int getNumOfAlbums() {
		return albumsObservableList.size();
	}

	public int getNumOfArtists() {
		return allArtists.size();
	}

	public int getNumOfMP3s() {
		return allMp3s.size();
	}

	public static List<MP3> getAllMP3s() {
		return allMp3s;
	}

}

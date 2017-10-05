package views.main.controller;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.media.MediaView;

public class VideoTabWindowControl extends Tab{
	@FXML
	private MediaView videoMediaView;
	@FXML
	private Button playVideo;
	
	public VideoTabWindowControl() {
		
		ResourceBundle resources = ResourceBundle.getBundle("res.lang", Locale.GERMAN);
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/main/views/VideoTabWindow.fxml"), resources);
		
		loader.setRoot(this);
		loader.setController(this);
		
		try {
			loader.load();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}

	// Event Listener on Button[#musicSearchButton].onAction
	@FXML
	public void playVideo(ActionEvent event) {
		System.out.println("play video");
		
	}
}

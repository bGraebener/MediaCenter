package application;
	
import java.io.*;
import java.util.Locale;
import java.util.ResourceBundle;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import views.main.controller.MusicTabWindowControl;


public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			
			ResourceBundle resources = ResourceBundle.getBundle("res.lang", Locale.GERMAN);
			
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/main/views/MainWindowMediaCenter.fxml"), resources);
			
			BorderPane root = (BorderPane)loader.load();
			
			
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
			primaryStage.setMinWidth(1300);
			
			primaryStage.setOnCloseRequest(event -> {
				
				try (ObjectOutputStream outStream = new ObjectOutputStream(new FileOutputStream("res/allMP3.ser"))) {

					outStream.writeObject(MusicTabWindowControl.getAllMP3s());
					outStream.flush();

				} catch (IOException e) {

				}
				
			});
			
			
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}

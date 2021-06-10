package op.tools.docx2wiki_ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class MainApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("Main.fxml"));
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getClassLoader().getResource("application.css").toExternalForm());


        primaryStage.setTitle("Word转Wiki工具 by zlp 2021");
        primaryStage.setResizable(false);
        //primaryStage.getIcons().add(new Image(getClass().getClassLoader().getResource("application.png")
        // .toExternalForm()));
        primaryStage.setScene(scene);
        primaryStage.show();

    }

}

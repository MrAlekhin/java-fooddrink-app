package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.sql.SQLException;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("loginForm.fxml"));
        primaryStage.setTitle("Food Drink");
        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(false);
        primaryStage.show();
        DatabaseHandler dataBase = new DatabaseHandler();
//        try {
//            dataBase.DatabaseConnecting();
//        }catch (SQLException ex){
//            System.out.println(ex.getMessage());
//        }
//        catch (Exception ex){
//            System.out.println(ex.getMessage());
//        }

    }


    public static void main(String[] args) {
        launch(args);
    }
}

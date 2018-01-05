package sample;

import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by artem on 2017-07-31.
 */
public class SignupController {
    DatabaseHandler databaseHandler;

    @FXML
    private JFXTextField email;

    @FXML
    private JFXPasswordField newPassword;

    @FXML
    private JFXPasswordField confirmPassword;

    @FXML
    public void onLoginBack(ActionEvent event){
        ((Node) event.getSource()).getScene().getWindow().hide();
        Stage main = new Stage();
        try {
            main.setScene(new Scene(FXMLLoader.load(getClass().getResource("loginForm.fxml"))));
            main.setResizable(false);
            main.show();
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(null);
            alert.setContentText("Sorry, something went wrong (" + ex.getMessage() + ").");
            alert.showAndWait();
        }
    }

    @FXML
    public void onSignupClick(ActionEvent event){
        if (newPassword.getText().equals(confirmPassword.getText())&&!newPassword.getText().isEmpty()&&!email.getText().isEmpty()){
            databaseHandler = new DatabaseHandler();
            String qu = "SELECT email from CUSTOMER where email='" + email.getText() + "'";
            try{
                if (databaseHandler.execQuery(qu).next()){
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setHeaderText(null);
                    alert.setContentText("Sorry, such email is already exists.");
                    alert.showAndWait();
                }else {
                    //            stmt.execute("CREATE TABLE " + TABLE_NAME + "("
//                    + "	userid int UNIQUE not null primary key GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),\n"
//                    + "	customername varchar(200),\n"
//                    + "	surname varchar(200),\n"
//                    + "	email UNIQUE varchar(1000),\n"
//                    + "	password varchar(1000)"
//                    + " )");
                    qu = "INSERT INTO CUSTOMER (email, password) VALUES ("
                            + "'" + email.getText() + "',"
                            + "'" + newPassword.getText() + "'"
                            + ")";
                    System.out.println(qu);
                    if (databaseHandler.execAction(qu)) {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setHeaderText(null);
                        alert.setContentText("Success");
                        alert.showAndWait();

                    } else //Error
                    {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setHeaderText(null);
                        alert.setContentText("Failed");
                        alert.showAndWait();
                    }
                    int id;
//                        databaseHandler.execQuery(qu).next();
                    qu="SELECT userid from CUSTOMER where email='" + email.getText() + "'";
                    ResultSet rs = databaseHandler.execQuery(qu);
                    if (rs.next()){
                        id=rs.getInt("userid");
                    }
                    else {
                        throw new Exception("fail to get id");
                    }
//                    stmt.execute("CREATE TABLE " + TABLE_NAME + "("
//                            + "	sessionid int GENERATED ALWAYS AS IDENTITY not null primary key,\n"
//                            + "	userid int not null,\n"
//                            + "	orderdate date,\n"
//                            + "	active boolean default true,\n"
//                            + "	FOREIGN KEY (userid) REFERENCES CUSTOMER (userid)"
//                            + " )");
                    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                    Date date = new Date();
                    qu="INSERT INTO CUSTOMERORDER (userid, orderdate)   VALUES ("
                            + "" + id + ","
                            + "'" + dateFormat.format(date) + "'"
                            + ")";
                    if (databaseHandler.execAction(qu)) {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setHeaderText(null);
                        alert.setContentText("Session created");
                        alert.showAndWait();
                        ((Node) event.getSource()).getScene().getWindow().hide();
                        Stage main = new Stage();
                        try {
                            main.setScene(new Scene(FXMLLoader.load(getClass().getResource("main.fxml"))));
                            main.setResizable(false);
                            main.show();
                        } catch (Exception ex) {
                            alert = new Alert(Alert.AlertType.ERROR);
                            alert.setHeaderText(null);
                            alert.setContentText("Sorry, something went wrong (" + ex.getMessage() + ").");
                            alert.showAndWait();
                        }
                    }
                    else //Error
                    {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setHeaderText(null);
                        alert.setContentText("Failed");
                        alert.showAndWait();
                    }
                }
            }catch (Exception ex){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText(null);
                alert.setContentText("Failed");
                alert.showAndWait();
            }
        }
        else if (email.getText().isEmpty() || newPassword.getText().isEmpty() || newPassword.getText().isEmpty()){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText(null);
            alert.setContentText("Sorry, all fields are required.");
            alert.showAndWait();
        }
        else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText(null);
            alert.setContentText("The password fields does not match!");
            alert.showAndWait();
        }

    }
}

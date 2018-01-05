package sample;

import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by artem on 2017-07-19.
 */
public class LoginController {
    @FXML
    public JFXTextField email;
    @FXML
    public JFXPasswordField password;
    DatabaseHandler databaseHandler;


    @FXML
    public void login(ActionEvent event){
        //checks if all fields are not empty
        if (email.getText().isEmpty()|| password.getText().isEmpty()){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText(null);
            alert.setContentText("Sorry, all fields are required.");
            alert.showAndWait();
        }
        else {
            //creates the connection
            databaseHandler=new DatabaseHandler();
            checkdata();
            //check if such user exists
            String qu="SELECT email, password from CUSTOMER where email='"+email.getText()+"' and password='"+password.getText()+"'";
            System.out.println(qu);
            try {
                if (databaseHandler.execQuery(qu).next()){
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setHeaderText(null);
                    alert.setContentText("Successful login");
                    alert.showAndWait();
                    //TODO: something wrong here and in date
                    qu="SELECT userid from CUSTOMER where email='" + email.getText() + "'";
                    int id;
//                        databaseHandler.execQuery(qu).next();
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
                    qu="INSERT INTO HISTORY (userid, orderdate)   VALUES ("
                            + "" + id + ","
                            + "'" + dateFormat.format(date) + "'"
                            + ")";
                    if (databaseHandler.execAction(qu)) {
                        alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setHeaderText(null);
                        alert.setContentText("Session created");
                        alert.showAndWait();
                        ((Node)event.getSource()).getScene().getWindow().hide();
                        Stage main = new Stage();
                        try{
                            main.setScene(new Scene(FXMLLoader.load(getClass().getResource("main.fxml"))));
                            main.setResizable(false);
                            main.show();
                        }catch (Exception ex) {
                            alert = new Alert(Alert.AlertType.ERROR);
                            alert.setHeaderText(null);
                            alert.setContentText("Sorry, cannot open main window (" + ex.getMessage()+ ").");
                            alert.showAndWait();
                        }
                    } else //Error
                    {
                        alert = new Alert(Alert.AlertType.ERROR);
                        alert.setHeaderText(null);
                        alert.setContentText("Failed");
                        alert.showAndWait();
                    }
                }
                else {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setHeaderText(null);
                    alert.setContentText("Wrong email address or password");
                    alert.showAndWait();
                }

            }catch (Exception ex){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText(null);
                alert.setContentText("Failed");
                alert.showAndWait();
            }

        }
    }
    @FXML
    public void openSignUpForm(ActionEvent event){
        ((Node)event.getSource()).getScene().getWindow().hide();
        Stage signup = new Stage();
        try{
            signup.setScene(new Scene(FXMLLoader.load(getClass().getResource("signupForm.fxml"))));
            signup.setResizable(false);
            signup.show();
        }catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(null);
            alert.setContentText("Sorry, cannot open signup window (" + ex.getMessage()+ ").");
            alert.showAndWait();
        }


    }
    private void checkdata(){
        String qu="SELECT * from CUSTOMER";
        ResultSet rs = databaseHandler.execQuery(qu);
        try {
            while (rs.next()){
                String id = rs.getString("userid");
                String email = rs.getString("email");
                String pasword = rs.getString("password");
                System.out.println(id+" "+email+" "+pasword);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

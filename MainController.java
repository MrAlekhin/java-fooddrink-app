package sample;

import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXTreeTableColumn;
import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.stage.Stage;
import javafx.util.Callback;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

/**
 * Created by artem on 2017-08-13.
 */
public class MainController implements Initializable {
    DatabaseHandler databaseHandler;
    ObservableList<Order> orders;
    JFXTreeTableColumn<Order,String> orderidCol;
    JFXTreeTableColumn<Order,String> totalPriceCol;
    JFXTreeTableColumn<Order,String> dateCol;
    int orderId=-1;
    int userId=-1;
    int itemid=-1;
    double netTotal=0.0;
    Stage stage;
    String qu;
    @FXML
    private Label userName;

    @FXML
    private Label userSurname;

    @FXML
    private Label email;

    @FXML
    private JFXListView<Label> list;

    @FXML
    private Label tax;

    @FXML
    private Label totalPrice;

    @FXML
    private JFXTreeTableView<Order> treeView;

    @FXML
    private LineChart<String,Number> lineChart;

    @FXML
    private PieChart pieChart;

    class Order extends RecursiveTreeObject<Order>{
        StringProperty orderid;
        StringProperty totalPrice;
        StringProperty date;

        public Order(String orderid, String totalPrice, String date) {
            this.orderid = new SimpleStringProperty(orderid);
            this.totalPrice = new SimpleStringProperty(totalPrice);
            this.date = new SimpleStringProperty(date);
        }
    }
    @Override
    //gets the user id from HISTORY and sets the information on user info screen
    public void initialize(URL url, ResourceBundle rb) {
        list.setExpanded(true);
        databaseHandler = new DatabaseHandler();
        qu="SELECT * FROM HISTORY ORDER BY sessionid DESC FETCH FIRST 1 ROWS ONLY";
        ResultSet rs = databaseHandler.execQuery(qu);
        try {
            if (rs.next()){
                userId = rs.getInt("userid");
                qu = "SELECT * from CUSTOMER where userid="+userId;
                rs = databaseHandler.execQuery(qu);
                if (rs.next()){
                    userName.setText(rs.getString("customername"));
                    userSurname.setText(rs.getString("surname"));
                    email.setText(rs.getString("email"));
                }
                orderidCol = new JFXTreeTableColumn<>("Number");
                orderidCol.setPrefWidth(150);
                orderidCol.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<Order, String>, ObservableValue<String>>() {
                    @Override
                    public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<Order, String> param) {
                        return param.getValue().getValue().orderid;
                    }
                });
                totalPriceCol = new JFXTreeTableColumn<>("Total Price");
                totalPriceCol.setPrefWidth(150);
                totalPriceCol.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<Order, String>, ObservableValue<String>>() {
                    @Override
                    public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<Order, String> param) {
                        return param.getValue().getValue().totalPrice;
                    }
                });
                dateCol = new JFXTreeTableColumn<>("Date");
                dateCol.setPrefWidth(150);
                dateCol.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<Order, String>, ObservableValue<String>>() {
                    @Override
                    public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<Order, String> param) {
                        return param.getValue().getValue().date;
                    }
                });
                orders=FXCollections.observableArrayList();
                updateOrdersList();
                updateLineChart();
                updatePieChart();
            }
        }catch (SQLException ex){
            errorAlert("Sorry, cannot get user data (" + ex.getMessage()+ ").");
        }


    }
    //on plus button click event
    @FXML void onAddItem(ActionEvent event){
        if(list.getItems().size()==0){
            addOrder();
        }
        try {
            Node node=((Node)event.getSource()).getParent().getParent().getChildrenUnmodifiable().get(0);
            itemid = getItemId(((Label)node).getText());
            if (includedListCheck()){
                itemAddAmount();
            }
            else {
                addItem();
            }
            listUpdate();
            totalUpdate();
            updateOrdersList();
            updateLineChart();
            updatePieChart();
        }catch (Exception ex){
            errorAlert(ex.getMessage());
        }

    }
    //on minus button click event
    @FXML void onRemoveItem(ActionEvent event){
        if(list.getItems().size()!=0){
            try {
                Node node=((Node)event.getSource()).getParent().getParent().getChildrenUnmodifiable().get(0);
                itemid = getItemId(((Label)node).getText());
                if (includedListCheck()){
                    if (getItemAmount()>1){
                        itemRemoveAmount();
                    }
                    else {
                        removeItem();
                        if (list.getItems().size()==1){
                            deleteOrder();
                        }
                    }
                    listUpdate();
                    totalUpdate();
                    updateOrdersList();
                    updateLineChart();
                }
                else {
                    infoAlert("Sorry, the item is not included in the order list");
                }
            }catch (Exception ex){
                errorAlert(ex.getMessage());
            }
        }
        else {
            infoAlert("Sorry, you don't have any item in the order list.");
        }
    }
    //on cancel click (remove all records and order
    @FXML void onCancel(ActionEvent event){
        if (orderId!=-1){
            try {
                clearList();
                deleteOrder();
                listUpdate();
                totalUpdate();
                updateOrdersList();
                updateLineChart();
            }catch (Exception ex){
                errorAlert("Cannot cancel the order.");
            }
        }
    }
    //on submitting order
    @FXML void onSubmit(ActionEvent event){
        if (orderId!=-1){
            orderId = -1;
            listUpdate();
            totalUpdate();
            updateOrdersList();
            updateLineChart();

        }
    }
    //return the id of the item from ITEM table
    public int getItemId(String item_name)throws Exception {
        qu = "SELECT * FROM ITEM where itemtitle='" + item_name + "'";
        ResultSet rs = databaseHandler.execQuery(qu);
        if (rs.next()) {
            return rs.getInt("itemid");
        } else {
            infoAlert("Could not find item");
            return -1;
        }
    }
    //return the id of the item type from ITEM table
    public int getItemType(int itemid)throws Exception{
        qu = "SELECT * FROM ITEM where itemid="+itemid;
        ResultSet rs = databaseHandler.execQuery(qu);
        if (rs.next()) {
            return rs.getInt("typeid");
        } else {
            infoAlert("Could not find item");
            return -1;
        }
    }
    //updates the listview
    public void listUpdate(){
        list.getItems().removeAll(list.getItems());
        ObservableList<Label> observableList = FXCollections.observableArrayList();
        qu = "SELECT * FROM LIST where orderid=" + orderId;
        ResultSet rs = databaseHandler.execQuery(qu);
        try {
            while (rs.next()){
                String temp=getItem(rs.getInt("itemid"));
                if (temp==null){
                    throw new Exception("Cannot update list");
                }
                else {
                    observableList.add(new Label(temp+" x "+rs.getString("amount")));
                }

            }
        }catch (Exception ex){
            errorAlert(ex.getMessage());
        }
        list.setItems(observableList);

    }
    //gets the string information about the price (title and price)
    public String getItem(int itemid) throws Exception{
        qu="SELECT * FROM ITEM where itemid="+itemid;
        ResultSet rs = databaseHandler.execQuery(qu);
        if (rs.next()) {
            return rs.getString("itemtitle").replaceAll("\\s+"," ") + " - $" + rs.getString("price");
        }
        else return null;
    }
    //returns the item price
    public float getItemPrice(int itemid) throws Exception{
        qu="SELECT * FROM ITEM where itemid="+itemid;
        ResultSet rs = databaseHandler.execQuery(qu);
        if (rs.next()) {
            return rs.getFloat("price");
        }
        return -1;
    }
    //updates the order total cost and total label
    public void totalUpdate(){
        netTotal=0;
        if(orderId!=-1){
            try {
                qu="SELECT * FROM LIST where orderid="+orderId;
                ResultSet rs = databaseHandler.execQuery(qu);
                while (rs.next()){
                    netTotal+=(getItemPrice(rs.getInt("itemid")))*rs.getInt("amount");
                }
                qu = "UPDATE CUSTOMERORDER SET total="+String.format("%.2f",netTotal*1.13)+" WHERE orderid=" + orderId;
                if (databaseHandler.execAction(qu)){
                    tax.setText(String.format("$%.2f",netTotal*0.13));
                    totalPrice.setText(String.format("$%.2f",netTotal*1.13));
                }
                else {
                    throw new Exception();
                }
            }catch (Exception ex){
                errorAlert(ex.getMessage());
            }
        }
    }
    //checks if the added item is already included in the list table
    public boolean includedListCheck() throws Exception {
        if (orderId!=-1&&itemid!=-1)
        qu = "SELECT * FROM LIST where itemid=" + itemid + " and orderid=" + orderId;
        if (databaseHandler.execQuery(qu).next()) {
            return true;
        } else {
            return false;
        }
    }
    //returns the item amount from the list table
    public int getItemAmount() throws Exception{
        qu="SELECT * FROM LIST where itemid=" + itemid + " and orderid=" + orderId;
        ResultSet rs = databaseHandler.execQuery(qu);
        if (rs.next()){
            return rs.getInt("amount");
        }
        return -1;
    }
    //increases the item amount in the list table by one
    public void itemAddAmount(){
        if (orderId!=-1&&itemid!=-1) {
            qu = "UPDATE LIST SET amount=amount+1 WHERE itemid=" + itemid + "and orderid=" + orderId;
            if (databaseHandler.execAction(qu)) {
                infoAlert("item added to the order");
            } else {
                infoAlert("fail to add item");
            }
        }else {
            errorAlert("Could not add item");
        }

    }
    //decreases the item amount im the list table by one
    public void itemRemoveAmount(){
        if (orderId!=-1&&itemid!=-1) {
            qu = "UPDATE LIST SET amount=amount-1 WHERE itemid=" + itemid + "and orderid=" + orderId;
            if (databaseHandler.execAction(qu)) {
                infoAlert("item removed from the order");
            } else {
                infoAlert("fail to remove item");
            }
        }else {
            errorAlert("Could not add item");
        }
    }
    //removes the record from the list table
    public void removeItem(){
        if (orderId!=-1&&itemid!=-1){
            qu="DELETE FROM LIST where itemid=" + itemid + " and orderid=" + orderId;
            if (databaseHandler.execAction(qu)){
                infoAlert("Item delete successful");
            }
            else {
                infoAlert("Could not delete item");
            }
        }
        else {
            errorAlert("Could not remove item");
        }
    }
    //adds the record to the list table
    public void addItem(){
        if (orderId!=-1&&itemid!=-1){
            qu="INSERT INTO LIST (orderid, itemid, amount) VALUES" +
                    "("+orderId+","+itemid+",1)";
            if (databaseHandler.execAction(qu)) {
                infoAlert("item added to the order");
            } else {
                infoAlert("fail to add item");
            }

        }else {
            errorAlert("Could not add item");
        }
    }

    public boolean hasChildren() throws Exception{
        qu = "SELECT * FROM LIST where itemid=" + itemid + "and orderid=" + orderId;
        if (databaseHandler.execQuery(qu).next()){
            return true;
        }
        else {
            return false;
        }
    }
    //adds the new record to the CUSTOMERORDER table
    public void addOrder(){
        qu="INSERT INTO CUSTOMERORDER (userid, total)   VALUES" +
                "("+userId+","+0.0+")";
        if(databaseHandler.execAction(qu)) {
            qu = "SELECT * FROM CUSTOMERORDER ORDER BY orderid DESC FETCH FIRST 1 ROWS ONLY";
            ResultSet rs = databaseHandler.execQuery(qu);
            try {
                if (rs.next()) {
                    orderId = rs.getInt("orderid");
                }
            }catch (Exception ex){
                errorAlert(ex.getMessage());
            }

        }
    }
    //removes the record from the CUSTOMERORDER table
    public void deleteOrder() {
        qu = "DELETE FROM CUSTOMERORDER where orderid="+orderId;
        if (databaseHandler.execAction(qu)) {
            orderId=-1;
            infoAlert("The order has been deleted");
        }else {
            errorAlert("Couldn't remove order");
        }
    }
    //deletes all items from order list
    public void clearList(){
        qu = "DELETE FROM LIST where orderid="+orderId;
        if (databaseHandler.execAction(qu)) {
            infoAlert("The records has been deleted");
        }else {
            errorAlert("Couldn't remove order");
        }
    }
    //updates the order list
    public void updateOrdersList(){
        try {
            orders.removeAll(orders);
            treeView.setRoot(null);
            qu="SELECT * FROM CUSTOMERORDER where userid="+userId+" ORDER BY orderid DESC FETCH FIRST 40 ROWS ONLY";
            ResultSet rs = databaseHandler.execQuery(qu);
            while (rs.next()){
                orders.add(new Order("#"+rs.getString("orderid"), "$"+rs.getString("total"), rs.getString("orderdate")));
            }
            final TreeItem<Order> root = new RecursiveTreeItem<Order>(orders, RecursiveTreeObject::getChildren);
            treeView.getColumns().setAll(orderidCol, totalPriceCol, dateCol);
            treeView.setRoot(root);
            treeView.setShowRoot(false);
        }catch (Exception ex){
            errorAlert(ex.getMessage());
        }
    }
    //updates the line chart data
    public void updateLineChart(){
        try {
            XYChart.Series<String, Number> series = new XYChart.Series<String,Number>();
            qu="SELECT * FROM CUSTOMERORDER where userid="+userId+" ORDER BY orderid DESC FETCH FIRST 10 ROWS ONLY";
            ResultSet rs = databaseHandler.execQuery(qu);
            while (rs.next()){
                series.getData().add(0,new XYChart.Data<String, Number>("#"+rs.getString("orderid"), rs.getFloat("total")));
            }
            series.setName("Your last 10 orders");
            lineChart.getData().setAll(series);
        }catch (Exception ex){
            errorAlert(ex.getMessage());
        }
    }
    public void updatePieChart(){
        try {
            int drinks=0;
            int snacks=0;
            int deserts=0;
            int sum =0;
            qu="SELECT * FROM CUSTOMERORDER where userid="+userId;
            ResultSet rs = databaseHandler.execQuery(qu);
            while (rs.next()){
                qu="SELECT * FROM LIST where orderid="+rs.getInt("orderid");
                ResultSet rsInner = databaseHandler.execQuery(qu);
                while (rsInner.next()) {
                    sum++;
                    switch (getItemType(rsInner.getInt("itemid"))) {
                        case 1:
                            drinks++;
                            break;
                        case 2:
                            snacks++;
                            break;
                        case 3:
                            deserts++;
                            break;
                        case -1:
                            throw new Exception("Fail to update the pie chart");
                    }
                }

            }
            ObservableList<PieChart.Data> preferencesList = FXCollections.observableArrayList();
            preferencesList.add(new PieChart.Data("Drinks", Math.round(drinks/sum)));
            preferencesList.add(new PieChart.Data("Snacks", Math.round(snacks/sum)));
            preferencesList.add(new PieChart.Data("Deserts", Math.round(deserts/sum)));

            pieChart.getData().setAll(preferencesList);

        }catch (Exception ex){
            errorAlert("Fail. " + ex.getMessage());
        }
    }
    //show the infoAlert
    public void infoAlert(String message){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    //shows the error alert
    public void errorAlert(String message){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

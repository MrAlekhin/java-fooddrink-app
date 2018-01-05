package sample;


import javax.swing.*;
import java.sql.*;

/**
 * Created by artem on 2017-08-11.
 */
public class DatabaseHandler {
    private static DatabaseHandler handler;
    private static final String DB_URL= "jdbc:derby:myDatabase;create=true";
    private static Connection conn = null;
    private static Statement stmt = null;

    public DatabaseHandler(){
        createConnection();
        setupCustomerTable();
        setupTypeTable();
        setupItemTable();
        setupOrderTable();
        setupListTable();
        setupHistoryTable();
    }
//connects to the database
    void createConnection(){
        try{
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
            conn = DriverManager.getConnection(DB_URL);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    //the order table has the total amount and belongs to the specific user
    void setupOrderTable(){
        String TABLE_NAME = "CUSTOMERORDER";
        try {
            stmt = conn.createStatement();
            DatabaseMetaData dbm = conn.getMetaData();
            ResultSet tables = dbm.getTables(null, null, TABLE_NAME.toUpperCase(), null);
            if(tables.next()){
                System.out.println("Table "+TABLE_NAME+ " already exists. Ready for go!");
//                stmt.execute("DROP TABLE CUSTOMERORDER");
//                System.out.println(TABLE_NAME+ " was removed!");
            }else {
                stmt.execute("CREATE TABLE " + TABLE_NAME + "("
                        + "	orderid int GENERATED ALWAYS AS IDENTITY not null primary key,\n"
                        + "	userid int not null,\n"
                        + "	total float not null,\n"
                        + "	orderdate date,\n"
                        + "	address varchar(1000),\n"
                        + "	FOREIGN KEY (userid) REFERENCES CUSTOMER (userid)"
                        + " )");
            }
        }catch (SQLException e){
            System.err.println(e.getMessage() + "...setupDatabase " + TABLE_NAME);
        }finally {

        }
    }
    //shows the session history of the user. used to get the id of the user
    void setupHistoryTable(){
        String TABLE_NAME = "HISTORY";
        try {
            stmt = conn.createStatement();
            DatabaseMetaData dbm = conn.getMetaData();
            ResultSet tables = dbm.getTables(null, null, TABLE_NAME.toUpperCase(), null);
            if(tables.next()){
                System.out.println("Table "+TABLE_NAME+ " already exists. Ready for go!");
//                stmt.execute("DROP TABLE HISTORY");
//                System.out.println(TABLE_NAME+ " was removed!");

            }else {
                stmt.execute("CREATE TABLE " + TABLE_NAME + "("
                        + "	sessionid int GENERATED ALWAYS AS IDENTITY not null primary key,\n"
                        + "	userid int not null,\n"
                        + "	orderdate varchar(20),\n"
                        + "	active boolean default true,\n"
                        + "	FOREIGN KEY (userid) REFERENCES CUSTOMER (userid)"
                        + " )");
            }
        }catch (SQLException e){
            System.err.println(e.getMessage() + "...setupDatabase " + TABLE_NAME);
        }finally {

        }
    }
    //the list table of amount items to specific order
    void setupListTable(){
        String TABLE_NAME = "LIST";
        try {
            stmt = conn.createStatement();
            DatabaseMetaData dbm = conn.getMetaData();
            ResultSet tables = dbm.getTables(null, null, TABLE_NAME.toUpperCase(), null);
            if(tables.next()){
                System.out.println("Table "+TABLE_NAME+ " already exists. Ready for go!");
            }else {
                stmt.execute("CREATE TABLE " + TABLE_NAME + "("
                        + "	cartid int GENERATED ALWAYS AS IDENTITY not null primary key,\n"
                        + "	orderid int not null,\n"
                        + "	itemid int not null,\n"
                        + "	amount int not null,\n"
                        + "	FOREIGN KEY (orderid) REFERENCES CUSTOMERORDER (orderid),\n"
                        + "	FOREIGN KEY (itemid) REFERENCES ITEM (itemid)"
                        + " )");
            }
        }catch (SQLException e){
            System.err.println(e.getMessage() + "...setupDatabase " + TABLE_NAME);
        }finally {

        }
    }
    //the item table (all items with foreign key to type)
    void setupItemTable(){
            String TABLE_NAME = "ITEM";
        try {
            stmt = conn.createStatement();
            DatabaseMetaData dbm = conn.getMetaData();
            ResultSet tables = dbm.getTables(null, null, TABLE_NAME.toUpperCase(), null);
            if(tables.next()) {
                System.out.println("Table " + TABLE_NAME + " already exists. Ready for go!");
//                stmt.execute("ALTER TABLE ITEM ADD ITEMTITLE CHAR(50)");
//                System.out.println(TABLE_NAME+ " was removed!");
                ResultSet rs = this.execQuery("SELECT * from ITEM");
//                try {
//                    while (rs.next()){
//                        String itemid = rs.getString("itemid");
//                        String typeid = rs.getString("typeid");
//                        String price = rs.getString("price");
//                        String itemtitle = rs.getString("itemtitle");
//                        System.out.println(itemid+" "+typeid+" "+price+" "+itemtitle);
//                    }
//                } catch (SQLException e) {
//                    e.printStackTrace();
//                }
            }else {
                stmt.execute("CREATE TABLE " + TABLE_NAME + "("
                        + "	itemid int GENERATED ALWAYS AS IDENTITY not null primary key,\n"
                        + "	typeid int not null,\n"
                        + "	price float not null,\n"
                        + "	itemtitle varchar(50),\n"
                        + "	FOREIGN KEY (typeid) REFERENCES ITEMTYPE (typeid)"
                        + " )");
                //default data insert
                stmt.execute("INSERT INTO ITEM (typeid, price, itemtitle)   VALUES " +
                        "(1, 2.99, 'Americano' ), " +
                        "(1, 2.99, 'Moccasino'), " +
                        "(1, 2.99, 'Brewed Cofee'), " +
                        "(1, 2.99, 'Capuchino'), " +
                        "(1, 2.99, 'Tea')," +
                        "(1, 2.99, 'Dolche Coffe'), " +
                        "(2, 5.99, 'Egg Burger'), " +
                        "(2, 5.99, 'Bacon Egg Burger'), " +
                        "(2, 5.99, 'Beef Burger'), " +
                        "(2, 8.99, 'Ham Sandwich'), " +
                        "(2, 8.99, 'Bacon Sandwich'), " +
                        "(2, 8.99, 'Chicken Sandwich'), " +
                        "(3, 1.99, 'Cafe Au Lete'), " +
                        "(3, 1.99, 'Salted Chocolate'), " +
                        "(3, 1.99, 'Toasted Coconut'), " +
                        "(3, 1.99, 'Mocha-almond'), " +
                        "(3, 1.99, 'Hibiscus'), " +
                        "(3, 1.99, 'Passion Fruit'), " +
                        "(3, 1.99, 'Chocolate-Cocoa'), " +
                        "(3, 1.99, 'Nutella'), " +
                        "(3, 1.99, 'Lemon Poppy') " );
            }
        }catch (SQLException e){
            System.err.println(e.getMessage() + "...setupDatabase " + TABLE_NAME);
        }finally {

        }
    }
    // the type of the item (drink, snack, desert)
    void setupTypeTable(){
        String TABLE_NAME = "ITEMTYPE";
        try {
            stmt = conn.createStatement();
            DatabaseMetaData dbm = conn.getMetaData();
            ResultSet tables = dbm.getTables(null, null, TABLE_NAME.toUpperCase(), null);
            if(tables.next()){
                System.out.println("Table "+TABLE_NAME+ " already exists. Ready for go!");

                ResultSet rs = this.execQuery("SELECT * from ITEMTYPE");
//                try {
//                    while (rs.next()){
//                        String typeid = rs.getString("typeid");
//                        String typename = rs.getString("typename");
//                        System.out.println(typeid+" "+typename);
//                    }
//                } catch (SQLException e) {
//                    e.printStackTrace();
//                }
            }else {
                stmt.execute("CREATE TABLE " + TABLE_NAME + "("
                        + "	typeid int GENERATED ALWAYS AS IDENTITY not null primary key,\n"
                        + "   typename varchar(50) not null"
                        + " )");
                //default data insert
                stmt.execute("INSERT INTO ITEMTYPE (typename) VALUES" +
                        "('Drinks')," +
                        "('Snacks')," +
                        "('Deserts')");
            }
        }catch (SQLException e){
            System.err.println(e.getMessage() + "...setupDatabase " + TABLE_NAME);
        }finally {

        }
    }
    //the customer (user table)
    void setupCustomerTable(){
        String TABLE_NAME = "CUSTOMER";
        try {
            stmt = conn.createStatement();
            DatabaseMetaData dbm = conn.getMetaData();
            ResultSet tables = dbm.getTables(null, null, TABLE_NAME.toUpperCase(), null);
            if(tables.next()){
                System.out.println("Table "+TABLE_NAME+ " already exists. Ready for go!");
            }else {
                stmt.execute("CREATE TABLE " + TABLE_NAME + "("
                        + "	userid int GENERATED ALWAYS AS IDENTITY not null,\n"
                        + "	customername varchar(200),\n"
                        + "	surname varchar(200),\n"
                        + "	email varchar(50),\n"
                        + "	password varchar(50),\n" //TODO: the encryption has to be created
                        + "   PRIMARY KEY (userid)"
                        + " )");
            }
        }catch (SQLException e){
            System.err.println(e.getMessage() + "...setupDatabase " + TABLE_NAME);
        }finally {

        }

    }
    //the query function (returns the result set if the action successful)
    public ResultSet execQuery(String query) {
        ResultSet result;
        try {
            stmt = conn.createStatement();
            result = stmt.executeQuery(query);
        } catch (SQLException ex) {
            System.out.println("Exception at execQuery:dataHandler" + ex.getLocalizedMessage());
            return null;
        } finally {
        }
        return result;
    }
    //the action function (returns true if the action successful)
    public boolean execAction(String qu) {
        try {
            stmt = conn.createStatement();
            stmt.execute(qu);
            return true;
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error:" + ex.getMessage(), "Error Occured", JOptionPane.ERROR_MESSAGE);
            return false;
        } finally {
        }
    }
}

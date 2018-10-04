package inf226;

import inf226.Storage.Id;
import inf226.Storage.KeyedStorage;
import inf226.Storage.Storage;
import inf226.Storage.Stored;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Class used to securely store the users in a longtime storage
 *
 * @author RakNoel
 * @version 1.0
 * @since 03.10.18
 */
public class DataBaseUserStorage implements KeyedStorage<UserName, User> {

    private static DataBaseUserStorage single_instance = null;

    private final TreeMap<Id, Stored<User>> memory;
    private Connection conn;
    private String url = "jdbc:sqlite:users.sqlite";
    private final Id.Generator id_generator;

    private DataBaseUserStorage() {
        memory = new TreeMap<>();
        id_generator = new Id.Generator();
        try {
            conn = DriverManager.getConnection(url);
            System.out.println("Connection to SQLite established.");

            //Will fill DB with needed tables if new
            String query = "SELECT COUNT(*) AS C FROM sqlite_master WHERE type='table' AND name='USERS';";
            ResultSet red = conn.prepareStatement(query).executeQuery();
            if (red.getString("C").equals("0")) fillDatabase();

        } catch (SQLException ignore) {
            System.out.println("Unable to connect to DB, exiting");
            ignore.printStackTrace();
            System.exit(1);
        }
    }

    public static DataBaseUserStorage getInstance() {
        if (single_instance == null)
            single_instance = new DataBaseUserStorage();

        return single_instance;
    }

    /**
     * Method that will create needed tables if new DB
     *
     * @throws SQLException
     */
    private void fillDatabase() throws SQLException {
        assert this.conn != null;
        ArrayList<String> querys = new ArrayList<>();
        querys.add("CREATE TABLE USERS (uname Varchar(30) PRIMARY KEY, passwd Varchar(50), salt char(50));");
        querys.add("CREATE TABLE MESSAGES(id INTEGER PRIMARY KEY ASC,user_to Varchar(30),user_from Varchar(30),msg TEXT,sentTime DateTime,FOREIGN KEY(user_to) REFERENCES USERS(uname),FOREIGN KEY(user_from) REFERENCES USERS(uname));");

        for (String q : querys) {
            conn.prepareStatement(q).execute();
        }
    }

    @Override
    public synchronized Maybe<Stored<User>> lookup(UserName key) {
        try {
            String query = "SELECT * FROM 'USERS' WHERE uname= ?";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, key.toString());
            ResultSet res = statement.executeQuery();

            String salt = res.getString("salt");
            UserName username = new UserName(res.getString("uname"));
            Password passwd = new Password(res.getString("passwd"));
            Stored<User> stored = new Stored<>(id_generator, new User(username, passwd, salt));

            return Maybe.just(stored);
        } catch (SQLException | inf226.Maybe.NothingException e) {
            return Maybe.nothing();
        }
    }

    public synchronized Maybe<String> getSalt(UserName key) {
        try {
            String query = "SELECT * FROM 'USERS' WHERE uname= ?";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, key.toString());
            ResultSet res = statement.executeQuery();

            return Maybe.just(res.getString("salt"));
        } catch (SQLException e) {
            return Maybe.nothing();
        }
    }

    @Override
    public synchronized Stored<User> save(User value) throws IOException {
        String query = "INSERT INTO USERS(uname, passwd, salt)  VALUES(?,?,?)";
        try {
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, value.getName().toString());
            statement.setString(2, value.getPass().toString());
            statement.setString(3, value.getSalt());

            statement.execute();
            Stored<User> h = new Stored<>(id_generator, value);
            memory.put(h.id(), h);
            return h;
        } catch (SQLException e) {
            throw new IOException("Unable to write user to DB");
        }
    }

    @Override
    public Stored<User> refresh(Stored<User> old) throws ObjectDeletedException {
        Stored<User> newValue = memory.get(old.id());
        if (newValue == null)
            throw new ObjectDeletedException(old.id());
        return newValue;
    }

    @Override
    public synchronized Stored<User> update(Stored<User> old, User newValue)
            throws ObjectDeletedException, Storage.ObjectModifiedException {
        Stored<User> stored = memory.get(old.id());
        if (stored == null) {
            throw new Storage.ObjectDeletedException(old.id());
        }

        if (!stored.equals(old)) {
            throw new Storage.ObjectModifiedException(stored);
        }

        try {
            Message last = newValue.getMessages().iterator().next();
            String query = "INSERT INTO MESSAGES(user_from, user_to, msg) VALUES(?,?,?)";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, last.sender);
            statement.setString(2, last.recipient);
            statement.setString(3, last.message);

            statement.execute();
        } catch (SQLException | NullPointerException ex) {
            throw new Storage.ObjectDeletedException(old.id());
        }

        Stored<User> newStored = new Stored<>(old, newValue);
        memory.put(old.id(), newStored);

        return newStored;
    }

    public synchronized void delete(Stored<User> old) throws ObjectDeletedException, ObjectModifiedException {
        Stored<User> stored = memory.get(old.id());
        if (stored == null) {
            throw new Storage.ObjectDeletedException(old.id());
        }

        if (!stored.equals(old)) {
            throw new Storage.ObjectModifiedException(stored);
        }

        memory.remove(old.id());
    }
}

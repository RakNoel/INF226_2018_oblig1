package inf226;

import inf226.Storage.Id;
import inf226.Storage.KeyedStorage;
import inf226.Storage.Stored;

import java.io.IOException;
import java.sql.*;

/**
 * Class used to securely store the users in a longtime storage
 *
 * @author RakNoel
 * @version 1.0
 * @since 03.10.18
 */
public class DataBaseUserStorage implements KeyedStorage<UserName, User> {

    private static DataBaseUserStorage single_instance = null;

    private Connection conn;
    private String url = "jdbc:sqlite:users.sqlite";

    private final Id.Generator id_generator;

    private DataBaseUserStorage() {
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
        String query = "CREATE TABLE USERS (uname Varchar(30), passwd Varchar(50), salt char(50));";
        assert this.conn != null;
        conn.prepareStatement(query).execute();
    }

    @Override
    public Maybe<Stored<User>> lookup(UserName key) {
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

    public Maybe<String> getSalt(UserName key) {
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
    public Stored<User> save(User value) throws IOException {
        String query = "INSERT INTO USERS(uname, passwd, salt)  VALUES(?,?,?)";
        try {
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, value.getName().toString());
            statement.setString(2, value.getPass().toString());
            statement.setString(3, value.getSalt());

            statement.execute();
            return new Stored<>(id_generator, value);
        } catch (SQLException e) {
            throw new IOException("Unable to write user to DB");
        }
    }

    @Override
    public Stored<User> refresh(Stored<User> old) throws ObjectDeletedException, IOException {
        return null;
    }

    @Override
    public Stored<User> update(Stored<User> old, User newValue) throws ObjectModifiedException, ObjectDeletedException, IOException {
        return null;
    }

    @Override
    public void delete(Stored<User> old) throws ObjectModifiedException, ObjectDeletedException, IOException {

    }
}

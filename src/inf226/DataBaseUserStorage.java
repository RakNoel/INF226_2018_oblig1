package inf226;

import inf226.Storage.KeyedStorage;
import inf226.Storage.Stored;

import java.io.IOException;

/**
 * Class used to securely store the users in a longtime storage
 *
 * @author RakNoel
 * @version 1.0
 * @since 03.10.18
 */
public class DataBaseUserStorage implements KeyedStorage<UserName,User> {
    @Override
    public Maybe<Stored<User>> lookup(UserName key) {
        return null;
    }

    @Override
    public Stored<User> save(User value) throws IOException {
        return null;
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

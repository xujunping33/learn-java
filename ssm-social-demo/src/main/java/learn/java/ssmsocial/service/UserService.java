package learn.java.ssmsocial.service;

import learn.java.ssmsocial.mapper.UserMapper;
import learn.java.ssmsocial.model.User;
import learn.java.ssmsocial.security.Passwords;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserMapper userMapper;

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public User register(String username, String password) {
        String u = normalizeUsername(username);
        requirePassword(password);
        if (userMapper.findByUsername(u) != null) {
            throw new IllegalArgumentException("username already exists");
        }

        String salt = Passwords.newSaltHex();
        String hash = Passwords.hashHex(salt, password);

        User user = new User();
        user.setUsername(u);
        user.setSalt(salt);
        user.setPasswordHash(hash);
        userMapper.insertUser(user);
        return userMapper.findById(user.getId());
    }

    public User login(String username, String password) {
        String u = normalizeUsername(username);
        requirePassword(password);

        User found = userMapper.findByUsername(u);
        if (found == null) {
            return null;
        }
        if (!Passwords.verify(found.getSalt(), found.getPasswordHash(), password)) {
            return null;
        }
        return found;
    }

    public User findByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    public User findById(long id) {
        return userMapper.findById(id);
    }

    private static String normalizeUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username is required");
        }
        String u = username.trim();
        if (u.length() < 3 || u.length() > 32) {
            throw new IllegalArgumentException("username length must be 3~32");
        }
        return u;
    }

    private static void requirePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("password is required");
        }
        if (password.length() < 6 || password.length() > 64) {
            throw new IllegalArgumentException("password length must be 6~64");
        }
    }
}


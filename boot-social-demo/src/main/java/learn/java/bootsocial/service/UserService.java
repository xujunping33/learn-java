package learn.java.bootsocial.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import learn.java.bootsocial.mapper.UserMapper;
import learn.java.bootsocial.model.User;
import learn.java.bootsocial.security.Passwords;
import learn.java.bootsocial.web.exception.BizException;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Profile({"dev", "docker"})
public class UserService {

    private final UserMapper userMapper;

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Transactional
    public User register(String username, String password) {
        String u = normalizeUsername(username);
        String p = normalizePassword(password);

        User existed = userMapper.selectOne(Wrappers.lambdaQuery(User.class).eq(User::getUsername, u));
        if (existed != null) {
            throw new BizException(HttpStatus.CONFLICT, "CONFLICT", "username already exists");
        }

        String salt = Passwords.newSaltHex(16);
        String hash = Passwords.sha256Hex(salt, p);

        User user = new User();
        user.setUsername(u);
        user.setSalt(salt);
        user.setPasswordHash(hash);
        userMapper.insertUser(user);
        return userMapper.findById(user.getId());
    }

    public User login(String username, String password) {
        String u = normalizeUsername(username);
        String p = normalizePassword(password);
        User existed = userMapper.selectOne(Wrappers.lambdaQuery(User.class).eq(User::getUsername, u));
        if (existed == null) {
            return null;
        }
        if (!Passwords.verify(existed.getSalt(), p, existed.getPasswordHash())) {
            return null;
        }
        return existed;
    }

    public User findById(long id) {
        if (id <= 0) {
            return null;
        }
        return userMapper.findById(id);
    }

    private static String normalizeUsername(String username) {
        if (username == null) {
            username = "";
        }
        String u = username.trim();
        if (u.isEmpty()) {
            throw new BizException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "username is required");
        }
        if (u.length() > 64) {
            throw new BizException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "username too long");
        }
        return u;
    }

    private static String normalizePassword(String password) {
        if (password == null) {
            password = "";
        }
        String p = password.trim();
        if (p.isEmpty()) {
            throw new BizException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "password is required");
        }
        if (p.length() < 6) {
            throw new BizException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "password too short");
        }
        if (p.length() > 100) {
            throw new BizException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "password too long");
        }
        return p;
    }
}


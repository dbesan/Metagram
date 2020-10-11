package org.studyproject.metagram.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.studyproject.metagram.domain.Role;
import org.studyproject.metagram.domain.User;
import org.studyproject.metagram.repos.MessageRepo;
import org.studyproject.metagram.repos.UserRepo;

import java.util.*;
import java.util.stream.Collectors;

import static org.studyproject.metagram.config.Literals.*;

@Service
public class UserService implements UserDetailsService {
    PasswordEncoder passwordEncoder;
    private UserRepo userRepo;
    private SMTPMailSender smtpMailSender;
    private MessageRepo messageRepo;
    @Value("${hostname}")
    private String hostname;

    @Autowired
    public UserService(PasswordEncoder passwordEncoder, UserRepo userRepo, SMTPMailSender smtpMailSender, MessageRepo messageRepo) {
        this.passwordEncoder = passwordEncoder;
        this.userRepo = userRepo;
        this.smtpMailSender = smtpMailSender;
        this.messageRepo = messageRepo;
    }

    /**
     * public UserDetails loadUserByUsername(String username)
     * get user from database
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepo.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException(USER_NOT_FOUND);
        }
        return user;
    }

    /**
     * public boolean addUser(User user)
     * saving user to database and send verify email
     */
    public boolean addUser(User user) {
        User userFromDb = userRepo.findByUsername(user.getUsername());
        user.setActive(false);
        if (userFromDb != null) {

            return false;
        }
        user.setRoles(Collections.singleton(Role.USER));
        user.setActivationCode(UUID.randomUUID().toString());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepo.save(user);
        sendVerifyEmail(user);
        return true;
    }

    /**
     * deleteUser(User user)
     * delete user from database
     */
    public boolean deleteUser(User user) {
        if (userRepo.findByUsername(user.getUsername()) != null) {
            messageRepo.deleteAll(messageRepo.findAllByAuthor(user));
            userRepo.delete(user);
            return true;
        }
        return false;

    }

    /**
     * sendVerifyEmail(User user)
     * send email to verify account
     */
    public boolean sendVerifyEmail(User user) {
        if (!StringUtils.isEmpty(user.getEmail())) {
            String message = String.format(
                    ACTIVATION_EMAIL,
                    user.getUsername(),
                    hostname,
                    user.getActivationCode()
            );

            smtpMailSender.send(user.getEmail(), ACTIVATION_CODE, message);
            return true;
        }
        return false;

    }

    /**
     * public boolean activateUser(String code)
     * user activation
     */
    public boolean activateUser(String code) {
        User user = userRepo.findByActivationCode(code);
        if (user == null) {
            return false;
        }
        user.setActive(true);
        user.setActivationCode(null);
        userRepo.save(user);

        return true;
    }

    /**
     * public List<User> findAll()
     * returns all users from database
     */
    public List<User> findAll() {
        return userRepo.findAll();
    }

    /**
     * public void save(User user, String username, Map<String, String> form)
     * save user to database
     */
    public void save(User user, String username, Map<String, String> form) {
        user.setUsername(username);

        Set<String> roles = Arrays.stream(Role.values())
                .map(Role::name)
                .collect(Collectors.toSet());

        user.getRoles().clear();

        for (String key : form.keySet()) {
            if (roles.contains(key)) {
                user.getRoles().add(Role.valueOf(key));
            }
        }
        userRepo.save(user);
    }

    /**
     * public void updateProfile(User user, String password, String email)
     * save edited user profile
     */
    public void updateProfile(User user, String password, String email) {
        String userEmail = user.getEmail();
        boolean isEmailChanged = (email != null && !email.equals(userEmail)) || (userEmail != null && userEmail.equals(email));
        if (isEmailChanged) {
            user.setEmail(email);

            if (!StringUtils.isEmpty(email)) {
                user.setActivationCode(UUID.randomUUID().toString());
            }
        }
        if (!StringUtils.isEmpty(password)) {
            user.setPassword(password);
        }
        userRepo.save(user);
        if (isEmailChanged) {
            sendVerifyEmail(user);
        }

    }

    /**
     * public void subscribe(User currentUser, User user)
     * subscribe to another user
     */
    public void subscribe(User currentUser, User user) {
        user.getSubscribers().add(currentUser);
        userRepo.save(user);
    }

    /**
     * public void unsubscribe(User currentUser, User user)
     * unsubscribe to another user
     */
    public void unsubscribe(User currentUser, User user) {
        user.getSubscribers().remove(currentUser);
        userRepo.save(user);
    }
}

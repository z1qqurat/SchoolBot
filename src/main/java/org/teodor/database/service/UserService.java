package org.teodor.database.service;

import org.teodor.database.DataSourceProvider;
import org.teodor.database.dao.user.UserDAO;
import org.teodor.database.dao.user.UserDAOImpl;
import org.teodor.database.dto.UserDTO;

import java.util.List;

public class UserService {

    private final UserDAO userDao;

    public UserService() {
        userDao = new UserDAOImpl(DataSourceProvider.get());
    }

    public UserDTO registerUser(Long id, String name) {
        userDao.deleteById(id);

        UserDTO user = generateBaseUser(id).setName(name);

        userDao.save(user);
        return user;
    }

    public void updateTracking(Long id, boolean isTeacher, String trackingId) {
        UserDTO user = userDao.findById(id).orElse(generateBaseUser(id));
        user.setTrackingId(trackingId);
        user.setTeacher(isTeacher);
        userDao.update(user);
    }

    public UserDTO getUser(Long id) {
        return userDao.findById(id).orElse(new UserDTO());
    }

    public List<UserDTO> getAllUser() {
        return userDao.findAll();
    }

    public List<UserDTO> getAllNotificationUsers() {
        return userDao.findAllWithActiveNotification();
    }

    private UserDTO generateBaseUser(Long id) {
        UserDTO user = new UserDTO();
        user.setId(id);
        user.setNotification(false);
        return user;

    }
}

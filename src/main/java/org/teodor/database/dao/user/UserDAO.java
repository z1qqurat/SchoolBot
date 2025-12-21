package org.teodor.database.dao.user;

import org.teodor.database.dto.UserDTO;

import java.util.Optional;
import java.util.List;

public interface UserDAO {

    Optional<UserDTO> findById(Long id);

    List<UserDTO> findAll();

    List<UserDTO> findAllWithActiveNotification();

    void save(UserDTO user);

    void update(UserDTO user);

    void deleteById(Long id);
}

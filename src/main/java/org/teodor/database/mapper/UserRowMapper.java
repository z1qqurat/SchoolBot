package org.teodor.database.mapper;

import org.teodor.database.dto.UserDTO;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserRowMapper {

    public UserDTO map(ResultSet rs) throws SQLException {
        UserDTO user = new UserDTO();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setFirstName(rs.getString("first_name"));
        user.setTeacher(rs.getBoolean("is_teacher"));
        user.setTrackingId(rs.getString("tracking_id"));
        user.setNotification(rs.getBoolean("is_notification"));
        return user;
    }

    public List<UserDTO> mapToList(ResultSet rs) throws SQLException {
        List<UserDTO> userDTOList = new ArrayList<>();

        while (rs.next()) {
            UserDTO user = new UserDTO();
            user.setId(rs.getLong("id"));
            user.setUsername(rs.getString("username"));
            user.setFirstName(rs.getString("first_name"));
            user.setTeacher(rs.getBoolean("is_teacher"));
            user.setTrackingId(rs.getString("tracking_id"));
            user.setNotification(rs.getBoolean("is_notification"));
            userDTOList.add(user);
        }
        return userDTOList;
    }
}

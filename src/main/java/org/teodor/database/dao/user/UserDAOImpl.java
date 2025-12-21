package org.teodor.database.dao.user;

import org.teodor.database.dto.UserDTO;
import org.teodor.database.mapper.UserRowMapper;
import org.teodor.exception.DataAccessException;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

public class UserDAOImpl implements UserDAO {

    private final DataSource dataSource;
    private final UserRowMapper mapper = new UserRowMapper();

    public UserDAOImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<UserDTO> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";

        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, id);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapper.map(rs));
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new DataAccessException("findById failed: ", e);
        }
    }

    @Override
    public List<UserDTO> findAll() {
        String sql = "SELECT * FROM users";

        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();
            return mapper.mapToList(rs);

        } catch (SQLException e) {
            throw new DataAccessException("findAll failed: ", e);
        }
    }

    @Override
    public List<UserDTO> findAllWithActiveNotification() {
        String sql = "SELECT * FROM users WHERE is_notification = TRUE";

        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();
            return mapper.mapToList(rs);

        } catch (SQLException e) {
            throw new DataAccessException("findAllWithActiveNotification failed: ", e);
        }
    }

    @Override
    public void save(UserDTO user) {
        String sql = """
                    INSERT INTO users (id, name, is_teacher, tracking_id, is_notification)
                    VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, user.getId());
            ps.setString(2, user.getName());
            ps.setBoolean(3, user.isTeacher());
            ps.setString(4, user.getTrackingId());
            ps.setBoolean(5, user.isNotification());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("save failed: ", e);
        }
    }

    @Override
    public void update(UserDTO user) {
        String sql = """
                    UPDATE users
                    SET name = ?, is_teacher = ?, tracking_id = ?, is_notification = ?
                    WHERE id = ?
                """;

        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (Objects.nonNull(user.getName())) {
                ps.setString(1, user.getName());

            }
            ps.setBoolean(2, user.isTeacher());
            if (Objects.nonNull(user.getTrackingId())) {
                ps.setString(3, user.getTrackingId());

            }
            ps.setBoolean(4, user.isNotification());
            ps.setLong(5, user.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("update user failed: ", e);
        }
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("deleteById failed: ", e);
        }
    }
}

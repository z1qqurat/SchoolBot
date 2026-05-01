package org.teodor.database.dao.schedule;

import lombok.extern.log4j.Log4j2;
import org.teodor.database.dto.BackupScheduleDTO;
import org.teodor.exception.DataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Log4j2
public class BackupScheduleDAOImpl implements BackupScheduleDAO {

    private final DataSource dataSource;
    private static final String LOG_MESSAGE = "Executing SQL statement: {}";

    public BackupScheduleDAOImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public BackupScheduleDTO find() {
        String sql = "SELECT * FROM backup_schedule";
        log.info(LOG_MESSAGE, sql);
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new BackupScheduleDTO(rs.getString("raw_schedule"), rs.getString("eTag"), rs.getString("last_modified"));
            }
            return new BackupScheduleDTO();

        } catch (SQLException e) {
            throw new DataAccessException("find schedule is failed: ", e);
        }
    }

    @Override
    public void update(BackupScheduleDTO schedule) {
        String sql = "UPDATE backup_schedule SET raw_schedule = ?, eTag = ?, last_modified = ? WHERE id = 1";
        log.info(LOG_MESSAGE, sql);
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, schedule.getRawSchedule());
            ps.setString(2, schedule.getETag());
            ps.setString(3, schedule.getLastModified());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("update backup schedule failed: ", e);
        }
    }

    @Override
    public void create(BackupScheduleDTO schedule) {
        String sql = "INSERT INTO backup_schedule (id, raw_schedule, eTag, last_modified) VALUES (1, ?, ?, ?)";
        log.info(LOG_MESSAGE, sql);
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, schedule.getRawSchedule());
            ps.setString(2, schedule.getETag());
            ps.setString(3, schedule.getLastModified());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("add new backup schedule failed: ", e);
        }
    }
}

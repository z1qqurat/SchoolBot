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

    public BackupScheduleDAOImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    @Override
    public BackupScheduleDTO find() {
        String sql = "SELECT * FROM backup_schedule";
        log.info(sql);

        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new BackupScheduleDTO(rs.getString("raw_schedule"), rs.getInt("hashcode"));
            }
            return null;

        } catch (SQLException e) {
            throw new DataAccessException("find schedule is failed: ", e);
        }
    }

    @Override
    public void update(BackupScheduleDTO schedule) {
        String sql = """
                    UPDATE backup_schedule
                    SET raw_schedule = ?, hashcode = ?
                """;

        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, schedule.getRawSchedule());
            ps.setInt(2, schedule.getHashcode());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("update backup schedule failed: ", e);
        }
    }
}

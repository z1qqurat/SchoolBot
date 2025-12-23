package org.teodor.database.dao.schedule;

import org.teodor.database.dto.BackupScheduleDTO;

public interface BackupScheduleDAO {

    BackupScheduleDTO find();

    void update(BackupScheduleDTO schedule);

    void create(BackupScheduleDTO schedule);
}

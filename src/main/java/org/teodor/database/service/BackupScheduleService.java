package org.teodor.database.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.teodor.database.DataSourceProvider;
import org.teodor.database.dao.schedule.BackupScheduleDAO;
import org.teodor.database.dao.schedule.BackupScheduleDAOImpl;
import org.teodor.database.dto.BackupScheduleDTO;
import org.teodor.pojo.ScheduleDto;
import org.teodor.util.WebPageParser;

@Log4j2
public class BackupScheduleService {

    private final BackupScheduleDAO backupScheduleDAO;

    public BackupScheduleService() {
        backupScheduleDAO = new BackupScheduleDAOImpl(DataSourceProvider.get());
    }

    public ScheduleDto updateBackupSchedule() {
//        ScheduleDto scheduleDto = JsonParser.extractScheduleFromFile();
        ScheduleDto scheduleDto = WebPageParser.extractJsonFromResponse();
        BackupScheduleDTO oldBackupScheduleDTO = getBackup();

        if (scheduleDto.hashCode() != oldBackupScheduleDTO.getHashcode()) {
            ObjectMapper mapper = new ObjectMapper();

            BackupScheduleDTO newBackupScheduleDTO = null;
            try {
                newBackupScheduleDTO = new BackupScheduleDTO()
                        .setRawSchedule(mapper.writeValueAsString(scheduleDto))
                        .setHashcode(scheduleDto.hashCode());
            } catch (JsonProcessingException e) {
//                log.error("Failed to parse schedule to JSON string: ", e);
                throw new RuntimeException(e);
            }
            backupScheduleDAO.update(newBackupScheduleDTO);
            log.info("Schedule backup was updated with hashcode: {}", scheduleDto.hashCode());
            return scheduleDto;
        }
        log.info("Schedule backup was not updated. Schedule is already up to date");
        return scheduleDto;
    }

    public BackupScheduleDTO getBackup() {
        return backupScheduleDAO.find();
    }
}

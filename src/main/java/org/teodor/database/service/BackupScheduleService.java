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

import static org.teodor.util.MapperHelper.getKeyByValue;

@Log4j2
public class BackupScheduleService {

    private final BackupScheduleDAO backupScheduleDAO;

    public BackupScheduleService() {
        backupScheduleDAO = new BackupScheduleDAOImpl(DataSourceProvider.get());
    }

    public ScheduleDto updateBackupSchedule() {
//        ScheduleDto scheduleDto = JsonParser.extractScheduleFromFile();
        ScheduleDto scheduleDto = sanitizeSchedule(WebPageParser.extractJsonFromResponse());

        BackupScheduleDTO oldBackupScheduleDTO = getBackup();
        if (oldBackupScheduleDTO == null) {
            ObjectMapper mapper = new ObjectMapper();
            BackupScheduleDTO newBackupScheduleDTO = null;
            try {
                newBackupScheduleDTO = new BackupScheduleDTO()
                        .setRawSchedule(mapper.writeValueAsString(scheduleDto))
                        .setHashcode(scheduleDto.hashCode());
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            backupScheduleDAO.create(newBackupScheduleDTO);
            log.info("Old backup was not found. Added a new one: {}", scheduleDto.hashCode());
            return scheduleDto;
        }
        if (scheduleDto.hashCode() != oldBackupScheduleDTO.getHashcode()) {
            ObjectMapper mapper = new ObjectMapper();
            BackupScheduleDTO newBackupScheduleDTO = null;
            try {
                newBackupScheduleDTO = new BackupScheduleDTO()
                        .setRawSchedule(mapper.writeValueAsString(scheduleDto))
                        .setHashcode(scheduleDto.hashCode());
            } catch (JsonProcessingException e) {
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

    private ScheduleDto sanitizeSchedule(ScheduleDto scheduleDto){
        scheduleDto.getTeachers().values()
                .forEach(teacher -> teacher
                        .setName(teacher.getName()
                                .trim()
                                .replace(". ", ".")
                                .replace("  ", " ")));

        String firstKey = getKeyByValue(scheduleDto.getPredms(), "Досліджую історію та суспільство");
        String secondKey = getKeyByValue(scheduleDto.getPredms(), "ІК \"Здоров’я, безпека та добробут\"");
        scheduleDto.getPredms().put(firstKey, "Досліджую іст. та сусп.");
        scheduleDto.getPredms().put(secondKey, "Здоров’я, безпека та добробут");
        return scheduleDto;
    }
}

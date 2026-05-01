package org.teodor.database.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.teodor.api.ScheduleApiController;
import org.teodor.database.DataSourceProvider;
import org.teodor.database.dao.schedule.BackupScheduleDAO;
import org.teodor.database.dao.schedule.BackupScheduleDAOImpl;
import org.teodor.database.dto.BackupScheduleDTO;
import org.teodor.pojo.GetScheduleResponseDto;
import org.teodor.pojo.ScheduleDto;

import java.util.Objects;

import static org.teodor.util.MapperHelper.getKeyByValue;

@Log4j2
public class BackupScheduleService {

    private final BackupScheduleDAO backupScheduleDAO;

    public BackupScheduleService() {
        backupScheduleDAO = new BackupScheduleDAOImpl(DataSourceProvider.get());
    }

    public ScheduleDto updateBackupSchedule() {
        ObjectMapper mapper = new ObjectMapper();
        BackupScheduleDTO oldBackupSchedule = getBackup();
        GetScheduleResponseDto scheduleResponse = ScheduleApiController.getSchedule(oldBackupSchedule.getETag(), oldBackupSchedule.getLastModified());

        if (Objects.isNull(scheduleResponse)) {
            log.info("Schedule is up to date.");
            try {
                return mapper.readValue(oldBackupSchedule.getRawSchedule(), ScheduleDto.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        scheduleResponse.setSchedule(sanitizeSchedule(scheduleResponse.getSchedule()));
        if (Objects.isNull(oldBackupSchedule.getETag())) {
            try {
                BackupScheduleDTO newBackupScheduleDTO = new BackupScheduleDTO()
                        .setRawSchedule(mapper.writeValueAsString(scheduleResponse.getSchedule()))
                        .setETag(scheduleResponse.getETag())
                        .setLastModified(scheduleResponse.getLastModified());
                backupScheduleDAO.create(newBackupScheduleDTO);
                log.info("Old backup was not found. Added a new one with etag: {}", scheduleResponse.getETag());
                return scheduleResponse.getSchedule();
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        if (!oldBackupSchedule.getETag().equals(scheduleResponse.getETag()) || !oldBackupSchedule.getLastModified().equals(scheduleResponse.getLastModified())) {
            try {
                BackupScheduleDTO newBackupScheduleDTO = new BackupScheduleDTO()
                        .setRawSchedule(mapper.writeValueAsString(scheduleResponse.getSchedule()))
                        .setETag(scheduleResponse.getETag())
                        .setLastModified(scheduleResponse.getLastModified());
                backupScheduleDAO.update(newBackupScheduleDTO);
                log.info("Schedule backup was updated with etag: {}", scheduleResponse.getETag());
                return scheduleResponse.getSchedule();
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

        }
        return scheduleResponse.getSchedule();
    }

    public BackupScheduleDTO getBackup() {
        return backupScheduleDAO.find();
    }

    private ScheduleDto sanitizeSchedule(ScheduleDto scheduleResponse) {
        scheduleResponse.getTeachers().values()
                .forEach(teacher -> teacher
                        .setName(teacher.getName()
                                .trim()
                                .replace(". ", ".")
                                .replace("  ", " ")));

        String firstKey = getKeyByValue(scheduleResponse.getPredms(), "Досліджую історію та суспільство");
        String secondKey = getKeyByValue(scheduleResponse.getPredms(), "ІК \"Здоров’я, безпека та добробут\"");
        scheduleResponse.getPredms().put(firstKey, "Досліджую іст. та сусп.");
        scheduleResponse.getPredms().put(secondKey, "Здоров’я, безпека та добробут");
        return scheduleResponse;
    }
}

package io.github.ferrazsergio.libraryapi.application.service;

import io.github.ferrazsergio.libraryapi.infrastructure.repository.ActivityRepository;
import io.github.ferrazsergio.libraryapi.interfaces.dto.RecentActivityDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ActivityService {

    private final ActivityRepository activityRepository;

    @Autowired
    public ActivityService(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    public List<RecentActivityDTO> getRecentActivities(int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "timestamp"));

        return activityRepository.findAll(pageRequest)
                .getContent()
                .stream()
                .map(this::mapToRecentActivity)
                .collect(Collectors.toList());
    }

    private RecentActivityDTO mapToRecentActivity(Activity activity) {
        return RecentActivityDTO.builder()
                .id(activity.getId())
                .activityType(activity.getActivityType())
                .description(activity.getDescription())
                .timestamp(activity.getTimestamp())
                .userName(activity.getUserName())
                .bookTitle(activity.getBookTitle())
                .build();
    }

    // Método para registrar uma nova atividade
    public Activity logActivity(String activityType, String description, String userName, String bookTitle) {
        Activity activity = new Activity();
        activity.setActivityType(activityType);
        activity.setDescription(description);
        activity.setUserName(userName);
        activity.setBookTitle(bookTitle);

        return activityRepository.save(activity);
    }
}

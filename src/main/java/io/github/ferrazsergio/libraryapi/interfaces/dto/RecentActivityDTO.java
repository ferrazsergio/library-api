package io.github.ferrazsergio.libraryapi.interfaces.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentActivityDTO {
    private Long id;
    private String activityType; // "LOAN", "RETURN", "NEW_BOOK", etc.
    private String description;
    private LocalDateTime timestamp;
    private String userName;
    private String bookTitle;
}
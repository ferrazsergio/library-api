package io.github.ferrazsergio.libraryapi.interfaces.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDataDTO {
    private long totalBooks;
    private long totalLoans;
    private long activeLoans;
    private long overdueLoans;
    private long totalUsers;
    private List<CategoryStatisticsDTO> mostBorrowedCategories;
    private List<RecentActivityDTO> recentActivities;
}
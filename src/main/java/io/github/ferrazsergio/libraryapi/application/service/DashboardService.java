package io.github.ferrazsergio.libraryapi.application.service;


import io.github.ferrazsergio.libraryapi.interfaces.dto.CategoryStatisticsDTO;
import io.github.ferrazsergio.libraryapi.interfaces.dto.DashboardDataDTO;
import io.github.ferrazsergio.libraryapi.interfaces.dto.RecentActivityDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardService {

    private final BookService bookService;
    private final LoanService loanService;
    private final UserService userService;
    private final ActivityService activityService;

    @Autowired
    public DashboardService(BookService bookService,
                            LoanService loanService,
                            UserService userService,
                            ActivityService activityService) {
        this.bookService = bookService;
        this.loanService = loanService;
        this.userService = userService;
        this.activityService = activityService;
    }

    public DashboardDataDTO getDashboardData() {
        return DashboardDataDTO.builder()
                .totalBooks(bookService.getTotalBooks())
                .totalLoans(loanService.getTotalLoans())
                .activeLoans(loanService.getActiveLoansCount())
                .overdueLoans(loanService.getOverdueLoansCount())
                .totalUsers(userService.getTotalUsers())
                .mostBorrowedCategories(getMostBorrowedCategories())
                .recentActivities(getRecentActivities())
                .build();
    }

    private List<CategoryStatisticsDTO> getMostBorrowedCategories() {
        // Buscar as categorias mais emprestadas
        return bookService.getMostBorrowedCategories(5); // Limitar a 5 categorias
    }

    private List<RecentActivityDTO> getRecentActivities() {
        // Buscar as atividades mais recentes
        return activityService.getRecentActivities(10); // Limitar a 10 atividades
    }
}

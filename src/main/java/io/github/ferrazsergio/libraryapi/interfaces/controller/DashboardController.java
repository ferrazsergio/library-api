package io.github.ferrazsergio.libraryapi.interfaces.controller;


import io.github.ferrazsergio.libraryapi.application.service.DashboardService;
import io.github.ferrazsergio.libraryapi.interfaces.dto.DashboardDataDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    private final DashboardService dashboardService;

    @Autowired
    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public ResponseEntity<DashboardDataDTO> getDashboardData() {
        DashboardDataDTO data = dashboardService.getDashboardData();
        return ResponseEntity.ok(data);
    }
}
package com.bff.bff_service.dto;

import lombok.Data;
import java.util.List;

@Data
public class DashboardResponse {
    private List<UserSummaryDTO> users;
    private List<RouteSummaryDTO> routes;
    private List<Object> trucks;
    private List<Object> alerts;
}
package searchengine.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.model.SiteTable;
import searchengine.model.StatusEnum;
import searchengine.repositories.SiteRepositories;
import searchengine.services.StatisticsService;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final StatisticsService statisticsService;
    private final SiteRepositories siteRepositories;

    public ApiController(StatisticsService statisticsService, SiteRepositories siteRepositories) {
        this.statisticsService = statisticsService;
        this.siteRepositories = siteRepositories;
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }
    @GetMapping("/startIndexing")
    public ResponseEntity<?> startIndexing(){
        SitesList sitesList = new SitesList();
        for(Site site : sitesList.getSites()){
            SiteTable siteTable = new SiteTable();
            siteTable.setName(site.getName());
            siteTable.setUrl(site.getUrl());
            siteTable.setStatus(StatusEnum.INDEXED);
            siteRepositories.delete(siteTable);
            siteRepositories.save(siteTable);

        }
        return ResponseEntity.ok().body("true");
    }
}

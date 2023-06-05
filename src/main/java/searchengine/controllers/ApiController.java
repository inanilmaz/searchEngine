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
import searchengine.services.IndexingService;
import searchengine.services.StatisticsService;

import javax.persistence.criteria.CriteriaBuilder;

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
        IndexingService indexingService = new IndexingService(siteRepositories);
        indexingService.indexingSite();
        return ResponseEntity.ok().body("true");
    }
}

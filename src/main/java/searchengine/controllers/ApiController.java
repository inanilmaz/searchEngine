package searchengine.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.repositories.PageRepositories;
import searchengine.repositories.SiteRepositories;
import searchengine.services.IndexingService;
import searchengine.services.StatisticsService;

import java.io.IOException;


@RestController
@RequestMapping("/api")
public class ApiController {

    private final StatisticsService statisticsService;
    private final SiteRepositories siteRepositories;
    private final PageRepositories pageRepositories;

    public ApiController(StatisticsService statisticsService, SiteRepositories siteRepositories, PageRepositories pageRepositories) {
        this.statisticsService = statisticsService;
        this.siteRepositories = siteRepositories;
        this.pageRepositories = pageRepositories;
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }
    @GetMapping("/startIndexing")
    public ResponseEntity<?> startIndexing() throws IOException {
        SitesList sitesList = new SitesList();
        for(Site site : sitesList.getSites()){
            IndexingService indexingService = new IndexingService(siteRepositories,site,pageRepositories);
            indexingService.startIndexing();
        }
        return ResponseEntity.ok().body("true");
    }
}

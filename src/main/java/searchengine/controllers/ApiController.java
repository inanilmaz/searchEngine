package searchengine.controllers;

import org.springframework.http.HttpStatus;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ForkJoinPool;


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
    public ResponseEntity<?> startIndexing(){
        Boolean isIndexing = false;
        Map<String, String> response = new HashMap<>();
        SitesList sitesList = new SitesList();
        for(Site site : sitesList.getSites()){
           isIndexing = new ForkJoinPool().invoke(new IndexingService(siteRepositories,site,pageRepositories));
        }
        if(isIndexing){
            response.put("result",isIndexing.toString());
            return new ResponseEntity<>(response,HttpStatus.OK);
        }else {
            response.put("result",isIndexing.toString());
            response.put("error","Индексация уже запущена");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

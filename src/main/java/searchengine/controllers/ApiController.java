package searchengine.controllers;

import org.springframework.beans.factory.annotation.Autowired;
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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;


@RestController
@RequestMapping("/api")
public class ApiController {
    @Autowired
    private StatisticsService statisticsService;
    private Boolean isIndexing = false;
    private ForkJoinPool fjp = new ForkJoinPool();
    @Autowired
    private SitesList sitesList;


    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }
    @GetMapping("/startIndexing")
    public ResponseEntity<?> startIndexing(){
        Map<String, String> response = new HashMap<>();
        for(Site site : sitesList.getSites()){
           isIndexing = fjp.invoke(new IndexingService(site));
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
    @GetMapping("/stopIndexing")
    public ResponseEntity<?> stopIndexing(){
        Map<String, String> response = new HashMap<>();
        if(!isIndexing){
            fjp.shutdown();
            response.put("result",isIndexing.toString());
            response.put("error","Индексация уже запущена");
            return new ResponseEntity<>(response,HttpStatus.OK);
        }else {
            response.put("result",isIndexing.toString());
            return new ResponseEntity<>(response,HttpStatus.OK);
        }
    }
}

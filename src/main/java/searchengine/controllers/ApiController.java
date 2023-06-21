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
import searchengine.services.FJPService;
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
            FJPService fjpService = new FJPService();
            isIndexing = fjpService.createFJP(site);
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
        FJPService fjpService = new FJPService();
        boolean isActive = fjpService.stopFJP();
        if(isActive){
            response.put("result", Boolean.toString(isActive));
            return new ResponseEntity<>(response,HttpStatus.OK);
        }else {
            response.put("result", Boolean.toString(isActive));
            response.put("error","Индексация не запущена");
            return new ResponseEntity<>(response,HttpStatus.OK);
        }
    }
}

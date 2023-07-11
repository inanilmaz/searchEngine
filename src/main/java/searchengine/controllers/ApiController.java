package searchengine.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.utils.FJPService;
import searchengine.services.StatisticsService;



@RestController
@RequestMapping("/api")
public class ApiController {
    @Autowired
    private StatisticsService statisticsService;
    @Autowired
    FJPService fjpService;


    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }
    @GetMapping("/startIndexing")
    public ResponseEntity<?> startIndexing(){
        boolean notIndexing = fjpService.createFJP();
        if(notIndexing){
            String errorMessage = "Индексация уже запущена";
            return ResponseEntity.ok().body("{\"result\": false, \"error\":\""
                    + errorMessage +"\"}");
        }else {
            return ResponseEntity.ok().body("{\"result\": true}");
        }
    }
    @GetMapping("/stopIndexing")
    public ResponseEntity<?> stopIndexing(){
        boolean isActive = fjpService.stopFJP();
        if(isActive){
            return ResponseEntity.ok().body("{\"result\": true}");
        }else {
            String errorMessage = "Индексация не запущена";
            return ResponseEntity.ok().body("{\"result\": false, \"error\":\""
                    + errorMessage +"\"}");
        }
    }
    @GetMapping("/indexPage{pageUrl}")
    public ResponseEntity<?>indexPage(@PathVariable String pageUrl){
        boolean correctUrl = false;
        if(correctUrl){
            return ResponseEntity.ok().body("{\"result\": true}");
        }else {
            String errorMessage = "Данная страница находится за пределами сайтов," +
                    "указанных в конфигурационном файле";
            return ResponseEntity.ok().body("{\"result\": false, \"error\":\""
                    + errorMessage +"\"}");
        }
    }
}

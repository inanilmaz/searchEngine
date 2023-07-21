package searchengine.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.ReIndexingPage;
import searchengine.services.Indexing;
import searchengine.services.StatisticsService;

import java.io.IOException;


@RestController
@RequestMapping("/api")
public class ApiController {
    @Autowired
    private StatisticsService statisticsService;
    @Autowired
    Indexing indexing;
    @Autowired
    ReIndexingPage indexingPage;

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<?> startIndexing() {
        boolean isIndexing = indexing.startIndexing();
        if (isIndexing) {
            String errorMessage = "Индексация уже запущена";
            return ResponseEntity.ok().body("{\"result\": false, \"error\":\""
                    + errorMessage + "\"}");
        } else {
            return ResponseEntity.ok().body("{\"result\": true}");
        }
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<?> stopIndexing() {
        boolean isActive = indexing.stopIndexing();
        if (isActive) {
            return ResponseEntity.ok().body("{\"result\": true}");
        } else {
            String errorMessage = "Индексация не запущена";
            return ResponseEntity.ok().body("{\"result\": false, \"error\":\""
                    + errorMessage + "\"}");
        }
    }

    @GetMapping("/indexPage")
    public ResponseEntity<?> indexPage(@RequestParam("url") String url) throws IOException {
        System.out.println(url + " pageUrl");
        if (indexingPage.isCorrectUrl(url)) {
            return ResponseEntity.ok().body("{\"result\": true}");
        } else {
            String errorMessage = "Данная страница находится за пределами сайтов," +
                    "указанных в конфигурационном файле";
            return ResponseEntity.ok().body("{\"result\": false, \"error\":\"" + errorMessage + "\"}");
        }
    }
}

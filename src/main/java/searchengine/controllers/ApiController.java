package searchengine.controllers;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.ReIndexingPage;
import searchengine.services.Indexing;
import searchengine.services.SearchService;
import searchengine.services.StatisticsService;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;


@RestController
@RequestMapping("/api")
public class ApiController {
    @Autowired
    private StatisticsService statisticsService;
    @Autowired
    Indexing indexing;
    @Autowired
    ReIndexingPage indexingPage;
    @Autowired
    SearchService searchService;

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<?> startIndexing() {
        boolean isIndexing = indexing.isIndexingInProgress();
        if (isIndexing) {
            String errorMessage = "Индексация уже запущена";
            return ResponseEntity.ok().body("{\"result\": false, \"error\":\""
                    + errorMessage + "\"}");
        } else {
            CompletableFuture.runAsync(() -> {
                indexing.startIndexing();
            });
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

    @PostMapping("/indexPage")
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
    @SneakyThrows
    @GetMapping("/search")
    public ResponseEntity<?> search(
            @RequestParam(required = true) String query,
            @RequestParam(required = false) String site,
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) Integer limit
    ){
        System.out.println(query);
        if (query == null || query.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"result\": false, \"error\": \"Задан пустой поисковый запрос\"}");
        }else {
            String jsonResult = searchService.performSearch(query, site, offset, limit);
            System.out.println(jsonResult);
            return ResponseEntity.status(HttpStatus.OK).body(jsonResult);
        }
    }
}

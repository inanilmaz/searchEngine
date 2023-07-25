package searchengine.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.model.Lemma;
import searchengine.model.SearchIndex;
import searchengine.model.SearchResult;
import searchengine.repositories.LemmaRepositories;
import searchengine.repositories.SearchIndexRepositories;
import searchengine.utils.LemmatizationUtils;

import java.io.IOException;
import java.util.*;

@Service
public class SearchService {
    private final SearchResult searchResult;
    @Autowired
    SearchIndexRepositories searchIndexRepositories;
    @Autowired
    LemmaRepositories lemmaRepositories;


    public SearchService() {
        searchResult = new SearchResult();
    }

    private SearchResult performSearch(String query) throws IOException {
        List<SearchIndex> matchingSearchIndexes = new ArrayList<>();
        boolean isFirstIteration = true;
        LemmatizationUtils lemma = new LemmatizationUtils();
        Map<String,Integer> lemmas = lemma.getLemmaMap(query);
        Map<String, Integer> sortedLemmasByFrequency = lemmas.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(LinkedHashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()), LinkedHashMap::putAll);
        for(String word : sortedLemmasByFrequency.keySet()){
            Optional<Lemma> lemmaRep = lemmaRepositories.findByLemma(word);
            if(lemmaRep.isPresent()){
                int lemmaId = lemmaRep.get().getId();
                List<SearchIndex> lemmaIndexes = searchIndexRepositories.findByLemmaId(lemmaId);
                if(isFirstIteration){
                    matchingSearchIndexes.addAll(lemmaIndexes);
                    isFirstIteration = false;
                }
                else{
                    matchingSearchIndexes.retainAll(lemmaIndexes);
                }
            }
        }

        setSearchResult();

        return searchResult;
    }
    private void setSearchResult(){
        searchResult.setResult(true);
    }


}

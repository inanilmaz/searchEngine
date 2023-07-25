package searchengine.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.model.Lemma;
import searchengine.model.PageData;
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
        double maxRelevance = calculateMaxRelevance(matchingSearchIndexes);
        List<PageData> pdList = new ArrayList<>();
        for(SearchIndex si : matchingSearchIndexes) {
            pdList.add(setPageData(si,sortedLemmasByFrequency,maxRelevance));
        }

        setSearchResult();

        return searchResult;
    }
    private void setSearchResult(){
        searchResult.setResult(true);
    }

    private PageData setPageData(SearchIndex matchingSearchIndexes,
                                 Map<String, Integer> sortedLemmasByFrequency,double maxRelevance ){
        LemmatizationUtils lemmatizationUtils = new LemmatizationUtils();
        String siteName = matchingSearchIndexes.getPageId().getSiteId().getName();
        String url = matchingSearchIndexes.getPageId().getPath();
        String site= matchingSearchIndexes.getPageId().getSiteId().getUrl();
        String fullText = matchingSearchIndexes.getPageId().getContent();
        List<String> lemmas = new ArrayList<>(sortedLemmasByFrequency.keySet());
        String snippet = lemmatizationUtils.getMatchingSnippet(fullText, lemmas);
        String title = lemmatizationUtils.getTitle(fullText);
        double absolutRelevance = calculateAbsoluteRelevance(matchingSearchIndexes.getPageId().getId());
        PageData pageData = new PageData();
        pageData.setSite(siteName);
        pageData.setUrl(url);
        pageData.setSite(site);
        pageData.setSnippet("<b>" + snippet + "</b>");
        pageData.setTitle(title);
        pageData.setRelevance(absolutRelevance/maxRelevance);
        return pageData;
    }
    private Double calculateMaxRelevance(List<SearchIndex> matchingSearchIndexes){
        double maxRelevance = 0;
        for(SearchIndex si : matchingSearchIndexes){
            double rank = si.getRank();
            if(maxRelevance<rank){
                maxRelevance = rank;
            }
        }
        return maxRelevance;
    }
    private Double calculateAbsoluteRelevance(int id){
        List<SearchIndex> siList = searchIndexRepositories.findByPageId(id);
        double absoluteRelevance = 0;
        for(SearchIndex si:siList){
            absoluteRelevance += si.getRank();
        }
        return absoluteRelevance;
    }
}

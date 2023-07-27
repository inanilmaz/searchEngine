package searchengine.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.model.*;
import searchengine.repositories.LemmaRepositories;
import searchengine.repositories.SearchIndexRepositories;
import searchengine.repositories.SiteRepositories;
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
    @Autowired
    SiteRepositories siteRepositories;
    private Integer offset;
    private Integer limit;


    public SearchService() {
        searchResult = new SearchResult();
    }

    public SearchResult performSearch(String query, String site, Integer offset, Integer limit) throws IOException {
        this.offset = offset;
        this.limit = limit;
        List<SearchIndex> matchingSearchIndexes = new ArrayList<>();
        List<SearchIndex> tempMatchingIndexes = new ArrayList<>();
        LemmatizationUtils lemmaUtils = new LemmatizationUtils();
        Map<String, Integer> lemmas = lemmaUtils.getLemmaMap(query);
        lemmas.forEach((k, v) -> System.out.println(k + " key " + v + " value"));
        Map<String, Integer> sortedLemmasByFrequency = lemmas.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(LinkedHashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()), LinkedHashMap::putAll);
        for (String word : sortedLemmasByFrequency.keySet()) {
            Optional<Lemma> lemmaRep;
            if (site == null) {
                lemmaRep = lemmaRepositories.findByLemma(word);
            } else {
                SiteTable siteTable = siteRepositories.findByUrl(site);
                lemmaRep = lemmaRepositories.findByLemmaAndSiteId(word, siteTable);
            }
            if (lemmaRep.isPresent()) {
                Lemma lemma = lemmaRep.get();
                tempMatchingIndexes.addAll(searchIndexRepositories.findByLemmaId(lemma));
            }
        }
        if (!tempMatchingIndexes.isEmpty()) {
            matchingSearchIndexes.addAll(tempMatchingIndexes);
        }

        Map<Integer, Double> relevance = calculateMaxRelevance(matchingSearchIndexes);
        List<PageData> pdList = new ArrayList<>();
        pdList.addAll(setPageData(matchingSearchIndexes,
                    sortedLemmasByFrequency, relevance));
        setSearchResult(pdList, matchingSearchIndexes.size());

        return searchResult;
    }


    private void setSearchResult(List<PageData> pdList, int count) {
        searchResult.setData(pdList);
        searchResult.setCount(count);
        searchResult.setResult(true);
    }

    private List<PageData> setPageData(List<SearchIndex> matchingSearchIndexes,
                                 Map<String, Integer> sortedLemmasByFrequency,
                                 Map<Integer, Double> relevance) {
        List<PageData> pageDataResult = new ArrayList<>();
        LemmatizationUtils lemmatizationUtils = new LemmatizationUtils();
        double maxRelevance = Collections.max(relevance.values());
        int startOffset = Math.max(offset != null ? offset : 0, 0);
        int endOffset = Math.min(startOffset + (limit != null && limit > 0 ? limit : 20), matchingSearchIndexes.size());
        Set<Integer> uniqPageId = new HashSet<>();
        for (int i = startOffset; i < endOffset; i++) {
            int newPageId = matchingSearchIndexes.get(i).getPageId().getId();
            if(uniqPageId.add(newPageId)){
                String siteName = matchingSearchIndexes.get(i).getPageId().getSiteId().getName();
                String url = matchingSearchIndexes.get(i).getPageId().getPath();
                String site = matchingSearchIndexes.get(i).getPageId().getSiteId().getUrl();
                String fullText = matchingSearchIndexes.get(i).getPageId().getContent();
                List<String> lemmas = new ArrayList<>(sortedLemmasByFrequency.keySet());
                String snippet = lemmatizationUtils.getMatchingSnippet(fullText, lemmas);
                String title = lemmatizationUtils.getTitle(fullText);
                PageData pageData = new PageData();
                double absolutRelevance = 0;
                for (Integer pagId : relevance.keySet()) {
                    if (newPageId == pagId) {
                        absolutRelevance = relevance.get(pagId);
                    }
                }
                pageData.setSite(siteName);
                pageData.setUrl(url);
                pageData.setSite(site);
                pageData.setSnippet("<b>" + snippet + "</b>");
                pageData.setTitle(title);
                pageData.setRelevance(absolutRelevance / maxRelevance);
                pageDataResult.add(pageData);
            }
        }

        System.out.println(pageDataResult.size());
        return pageDataResult;
    }

    private Map<Integer, Double> calculateMaxRelevance(List<SearchIndex> matchingSearchIndexes) {
        Map<Integer, Double> pageRelevenceMap = new HashMap<>();
        for (SearchIndex si : matchingSearchIndexes) {
            double relevance = si.getRank();
            int pageId = si.getPageId().getId();
            if (pageRelevenceMap.containsKey(pageId)) {
                double currentPageRelevance = pageRelevenceMap.get(pageId);
                double newPageRelevance = currentPageRelevance + relevance;
                pageRelevenceMap.put(pageId, newPageRelevance);
            } else {
                pageRelevenceMap.put(pageId, relevance);
            }
        }
        return pageRelevenceMap;
    }

}
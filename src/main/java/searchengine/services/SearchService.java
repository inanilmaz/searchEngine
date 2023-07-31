package searchengine.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.model.*;
import searchengine.repositories.LemmaRepositories;
import searchengine.repositories.SearchIndexRepositories;
import searchengine.repositories.SiteRepositories;
import searchengine.search.PageData;
import searchengine.search.SearchResult;
import searchengine.utils.FindWordInText;
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
    private final LemmatizationUtils lemmatizationUtils;
    private final FindWordInText findWordInText;


    public SearchService() throws IOException {
        searchResult = new SearchResult();
        lemmatizationUtils = new LemmatizationUtils();
        findWordInText = new FindWordInText();
    }

    public String performSearch(String query, String site, Integer offset, Integer limit) throws IOException {
        this.offset = offset;
        this.limit = limit;
        List<SearchIndex> matchingSearchIndexes = new ArrayList<>();
        List<SearchIndex> tempMatchingIndexes = new ArrayList<>();
        Map<String, Integer> lemmas = lemmatizationUtils.getLemmaMap(query);
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
        List<PageData> pdList = setPageData(matchingSearchIndexes,
                sortedLemmasByFrequency, relevance);
        Collections.sort(pdList,(pd1,pd2)->Double.compare(pd2.getRelevance(), pd1.getRelevance()));
        setSearchResult(pdList, matchingSearchIndexes.size());
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonResult = objectMapper.writeValueAsString(searchResult);
        return jsonResult;
    }


    private void setSearchResult(List<PageData> pdList, int count) {
        searchResult.setData(pdList);
        searchResult.setCount(count);
        searchResult.setResult(true);
    }

    private List<PageData> setPageData(List<SearchIndex> matchingSearchIndexList,
                                 Map<String, Integer> sortedLemmasByFrequency,
                                 Map<Integer, Double> relevance) {
        List<PageData> pageDataResult = new ArrayList<>();
        double maxRelevance = Collections.max(relevance.values());
        int startOffset = Math.max(offset != null ? offset : 0, 0);
        int endOffset = Math.min(startOffset + (limit != null && limit > 0 ? limit : 20), matchingSearchIndexList.size());
        Set<Integer> uniqPageId = new HashSet<>();
        for (int i = startOffset; i < endOffset; i++) {
            int newPageId = matchingSearchIndexList.get(i).getPageId().getId();
            if(uniqPageId.add(newPageId)){
                String siteName = matchingSearchIndexList.get(i).getPageId().getSiteId().getName();
                String url = matchingSearchIndexList.get(i).getPageId().getPath();
                String site = matchingSearchIndexList.get(i).getPageId().getSiteId().getUrl();
                String fullText = matchingSearchIndexList.get(i).getPageId().getContent();
                List<String> lemmas = new ArrayList<>(sortedLemmasByFrequency.keySet());
                String snippet = findWordInText.getMatchingSnippet(fullText, lemmas);
                String title = findWordInText.getTitle(fullText);
                PageData pageData = new PageData();
                double absolutRelevance = relevance.getOrDefault(newPageId,0.0);
                pageData.setSiteName(siteName);
                pageData.setUrl(url);
                pageData.setSite(site);
                pageData.setSnippet("<b>" + snippet + "</b>");
                pageData.setTitle(title);
                pageData.setRelevance(absolutRelevance / maxRelevance);
                pageDataResult.add(pageData);
            }
        }
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
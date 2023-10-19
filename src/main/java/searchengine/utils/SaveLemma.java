package searchengine.utils;

import org.jsoup.nodes.Document;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.SearchIndex;
import searchengine.repositories.LemmaRepositories;
import searchengine.repositories.SearchIndexRepositories;
import searchengine.utils.LemmatizationUtils;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public class SaveLemma {
    private LemmaRepositories lemmaRepositories;
    private SearchIndexRepositories searchIndexRepositories;

    public SaveLemma(LemmaRepositories lemmaRepositories, SearchIndexRepositories searchIndexRepositories) {
        this.lemmaRepositories = lemmaRepositories;
        this.searchIndexRepositories = searchIndexRepositories;
    }

    public void saveOrUpdateLemma(Document doc, Page page) throws IOException {
        LemmatizationUtils lemmas = new LemmatizationUtils();
        String htmlText = doc.text();
        Map<String, Integer> lemmasMap = lemmas.getLemmaMap(htmlText);
        for (String word : lemmasMap.keySet()) {
            int countLemma = lemmasMap.get(word);
            Optional<Lemma> existingLemmaOpt = lemmaRepositories.findByLemma(word);
            if (existingLemmaOpt.isPresent()) {
                System.out.println("saveOrUpdateLemma method : update lemma");
                Lemma existingLemma = existingLemmaOpt.get();
                int count = existingLemma.getFrequency() + countLemma;
                existingLemma.setFrequency(count);
                lemmaRepositories.save(existingLemma);
                saveSearchIndex(page, existingLemma, count);
            } else {
                System.out.println("saveOrUpdateLemma method: new Lemma save");
                Lemma newLemma = new Lemma();
                newLemma.setSiteId(page.getSiteId());
                newLemma.setFrequency(countLemma);
                newLemma.setLemma(word);
                lemmaRepositories.save(newLemma);
                saveSearchIndex(page, newLemma, countLemma);
            }
        }
    }
    public void saveSearchIndex(Page page, Lemma lemma, int count) {
        SearchIndex searchIndex = new SearchIndex();
        searchIndex.setPageId(page);
        searchIndex.setLemmaId(lemma);
        searchIndex.setRank(count);
        searchIndexRepositories.save(searchIndex);
    }
}

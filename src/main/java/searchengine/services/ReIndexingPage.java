package searchengine.services;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.SiteTable;
import searchengine.repositories.LemmaRepositories;
import searchengine.repositories.SiteRepositories;
import searchengine.utils.CountingLemma;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ReIndexingPage {
    @Autowired
    private SitesList sitesList;
    @Autowired
    private SiteRepositories siteRepositories;
    @Autowired
    private LemmaRepositories lemmaRepositories;
    private Connection.Response response = null;
    private SiteTable siteTable;
    public boolean isCorrectUrl(String url) throws IOException {
        for(Site site : sitesList.getSites()){
            if(url.contains(site.getUrl()) && !url.equals(site.getUrl())){
                response = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36")
                        .referrer("http://www.google.com")
                        .execute();
                Document doc = response.parse();
                String htmlContent = doc.getAllElements().toString();
                siteTable = siteId(site);
                savePage(site,htmlContent,url);
                saveOrUpdateLemma(doc);
                return true;
            }else {
                return false;
            }
        }
        return false;
    }
    public void savePage(Site site,String content,String url){
        Page page = new Page();
        page.setSiteId(siteTable);
        page.setCode(response.statusCode());
        page.setPath(url.replaceAll(site.getUrl(),""));
        page.setContent(content);
    }
    public void saveOrUpdateLemma(Document doc) throws IOException {
        CountingLemma lemmas = new CountingLemma();
        String htmlText = doc.text();
        Map<String,Integer> lemmasMap =lemmas.getLemmaMap(htmlText);
        for(String word : lemmasMap.keySet() ){
            int countLemma = lemmasMap.get(word);
            Optional<Lemma> existingLemmaOpt = lemmaRepositories.findByLemma(word);
            if(existingLemmaOpt.isPresent()){
                Lemma existingLemma = existingLemmaOpt.get();
                existingLemma.setFrequency(existingLemma.getFrequency() + countLemma);
                lemmaRepositories.save(existingLemma);
            }else {
                Lemma newLemma = new Lemma();
                newLemma.setSiteId(siteTable);
                newLemma.setFrequency(1);
                newLemma.setLemma(word);

            }
        }
    }
    public SiteTable siteId(Site site) {
        List<SiteTable> sites = siteRepositories.findAll();
        Optional<SiteTable> matchingSite = sites.stream()
                .filter(site1 -> site1.getName().equals(site.getName()))
                .findFirst();
        return matchingSite.orElse(null);
    }
}

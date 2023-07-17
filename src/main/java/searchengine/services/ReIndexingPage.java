package searchengine.services;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.model.Page;
import searchengine.model.SiteTable;
import searchengine.repositories.SiteRepositories;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class ReIndexingPage {
    @Autowired
    private SitesList sitesList;
    @Autowired
    private SiteRepositories siteRepositories;
    private Connection.Response response = null;
    public boolean isCorrectUrl(String url) throws IOException {
        for(Site site : sitesList.getSites()){
            if(url.contains(site.getUrl())){
                response = Jsoup.connect(url).userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                        .referrer("http://www.google.com")
                        .execute();
                Document doc = response.parse();
                String htmlContent = doc.getAllElements().toString();
                savePage(site,htmlContent,url);
                return true;
            }else {
                return false;
            }
        }
        return false;
    }
    public void savePage(Site site,String content,String url){
        Page page = new Page();
        page.setSiteId(siteId(site));
        page.setCode(response.statusCode());
        page.setPath(url.replaceAll(site.getUrl(),""));
        page.setContent(content);
    }
    public SiteTable siteId(Site site) {
        List<SiteTable> sites = siteRepositories.findAll();
        Optional<SiteTable> matchingSite = sites.stream()
                .filter(site1 -> site1.getName().equals(site.getName()))
                .findFirst();
        return matchingSite.orElse(null);
    }
}

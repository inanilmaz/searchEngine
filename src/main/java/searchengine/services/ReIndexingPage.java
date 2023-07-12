package searchengine.services;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;

import java.io.IOException;

@Service
public class ReIndexingPage {
    @Autowired
    SitesList sitesList;
    public boolean isCorrectUrl(String url) throws IOException {
        for(Site site : sitesList.getSites()){
            if(url.contains(site.getUrl())){
                Document doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                        .referrer("http://www.google.com")
                        .get();
                String htmlContent = doc.getAllElements().toString();
                return true;
            }else {
                return false;
            }
        }
        return false;
    }
}

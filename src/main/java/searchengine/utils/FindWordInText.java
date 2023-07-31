package searchengine.utils;

import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.*;

public class FindWordInText {
    private final LemmatizationUtils lemmatizationUtils;
    @SneakyThrows
    public FindWordInText() {
       lemmatizationUtils = new LemmatizationUtils();

    }

    public String getMatchingSnippet(String fullText, List<String> lemmas) {
        String text = clearTagOnHtml(fullText);
        System.out.println(lemmas);
        System.out.println(text);
        List<Integer> indexInText = new ArrayList<>();
        String newText = text.toLowerCase(Locale.ROOT).replaceAll("([^а-я\\s])", "").trim();
        String[] words = newText.split("\\s");

        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            for (String lemma : lemmas) {
                if (isLemmaInText(lemma, word)) {
                    indexInText.add(text.toLowerCase().indexOf(word));
                }
            }
        }

        Collections.sort(indexInText);

        Map<Integer,Integer> countLemmaList = new HashMap<>();

        for (int i = 0; i < indexInText.size(); i++) {
            int indexLength = indexInText.get(i) + 150;
            int bestStartIndexCountLemma = 0;
            for(int j = i ;j<indexInText.size(); j++){
                if(indexInText.get(i)<indexLength){
                    bestStartIndexCountLemma++;
                }
            }
            countLemmaList.put(indexInText.get(i),bestStartIndexCountLemma);
        }
        Map<Integer, Integer> sortedLemmasByCountLemma = countLemmaList.entrySet()
                .stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                .collect(LinkedHashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()), LinkedHashMap::putAll);
        Set<Integer> keySet = sortedLemmasByCountLemma.keySet();
        int startIndex = 0;
        for(int i : keySet){
            startIndex = i;
            break;
        }
        int endIndex = startIndex + 150;
        int nextSpaceIndex = text.indexOf(" ", endIndex + 1);
        if (nextSpaceIndex != -1) {
            endIndex = nextSpaceIndex;
        }
        return text.substring(startIndex,endIndex);
    }
    public String getTitle(String html){
        Document doc = Jsoup.parse(html);
        String title = doc.title();
        return title;
    }
    private String clearTagOnHtml(String html){
        Document doc = Jsoup.parse(html);
        String text = doc.text();
        return text;
    }
    public boolean isLemmaInText(String lemma, String word){
        if(word.isBlank()){
            return false;
        }
        List<String> wordBaseForms = lemmatizationUtils.luceneMorph.getMorphInfo(word);
        if(lemmatizationUtils.anyWordBaseBelongToParticle(wordBaseForms)){
            return false;
        }
        List<String> normalForms = lemmatizationUtils.luceneMorph.getNormalForms(word);
        if (normalForms.isEmpty()) {
            return false;
        }
        return normalForms.get(0).equals(lemma);
    }
}

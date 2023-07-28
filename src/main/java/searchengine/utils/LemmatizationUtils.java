package searchengine.utils;


import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;


import java.io.IOException;
import java.util.*;

public class LemmatizationUtils {
    private static final String[] particlesNames = new String[]{"МЕЖД", "ПРЕДЛ", "СОЮЗ"};

    public Map<String, Integer> getLemmaMap(String text) throws IOException {
        LuceneMorphology luceneMorph = new RussianLuceneMorphology();
        String[] textArray = arrayContainsRussianWords(text);
        HashMap<String, Integer> lemmas = new HashMap<>();
        for(String word : textArray){
            if(word.isBlank()){
                continue;
            }
            List<String> wordBaseForms = luceneMorph.getMorphInfo(word);
            if(anyWordBaseBelongToParticle(wordBaseForms)){
                continue;
            }
            List<String> normalForms = luceneMorph.getNormalForms(word);
            if (normalForms.isEmpty()) {
                continue;
            }
            String normalWord = normalForms.get(0);
            if (lemmas.containsKey(normalWord)) {
                lemmas.put(normalWord, lemmas.get(normalWord) + 1);
            } else {
                lemmas.put(normalWord, 1);
            }
        }
        return lemmas;
    }
    private boolean anyWordBaseBelongToParticle(List<String> wordBaseForms) {
        return wordBaseForms.stream().anyMatch(this::hasParticleProperty);
    }
    private boolean hasParticleProperty(String wordBase) {
        for (String property : particlesNames) {
            if (wordBase.toUpperCase().contains(property)) {
                return true;
            }
        }
        return false;
    }
    private String[] arrayContainsRussianWords(String text) {
        return text.toLowerCase(Locale.ROOT)
                .replaceAll("([^а-я\\s])", " ")
                .trim()
                .split("\\s+");
    }
    private String clearTagOnHtml(String html){
        Document doc = Jsoup.parse(html);
        String text = doc.text();
        return text;
    }
    public String getMatchingSnippet(String fullText, List<String> lemmas) {
        String cleanText = clearTagOnHtml(fullText);
        String snippet = "";
        List<Integer> indices = new ArrayList<>();
        int targetLength = 200;

        for (String lemma : lemmas) {
            int index = cleanText.indexOf(lemma);

            while (index != -1) {
                indices.add(index);
                index = cleanText.indexOf(lemma, index + 1);
            }
        }

        Collections.sort(indices);

        for (int index : indices) {
            if (index != -1) {
                int startIndex = Math.max(0, index - targetLength / 2);
                int endIndex = Math.min(cleanText.length(), index + targetLength / 2);

                while (startIndex > 0 && Character.isLetter(cleanText.charAt(startIndex - 1))) {
                    startIndex--;
                }
                while (endIndex < cleanText.length() - 1 && Character.isLetter(cleanText.charAt(endIndex))) {
                    endIndex++;
                }

                snippet = cleanText.substring(startIndex, endIndex);
                break;
            }
        }
        return snippet;
    }

    public String getTitle(String html){
        Document doc = Jsoup.parse(html);
        String title = doc.title();
        return title;
    }
}

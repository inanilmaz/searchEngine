package searchengine.utils;


import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;


import java.io.IOException;
import java.util.*;

public class LemmatizationUtils {
    private static final String[] particlesNames = new String[]{"МЕЖД", "ПРЕДЛ", "СОЮЗ"};
    LuceneMorphology ruLuceneMorph;
    LuceneMorphology engLuceneMorph;

    public LemmatizationUtils() throws IOException {
        ruLuceneMorph = new RussianLuceneMorphology();
        engLuceneMorph = new EnglishLuceneMorphology();
    }

    public Map<String, Integer> getLemmaMap(String text) throws IOException {
        String[] textArray = arrayContainsRussianWords(text);
        HashMap<String, Integer> lemmas = new HashMap<>();
        for(String word : textArray){
            if(word.isBlank()){
                continue;
            }
            List<String> ruWordBaseForms = ruLuceneMorph.getMorphInfo(word);
            List<String> engWordBaseForms = engLuceneMorph.getMorphInfo(word);
            if(ruWordBaseForms.size()>engWordBaseForms.size()){
                if(anyWordBaseBelongToParticle(ruWordBaseForms)){
                    continue;
                }
                List<String> normalForms = ruLuceneMorph.getNormalForms(word);
                if (normalForms.isEmpty()) {
                    continue;
                }
                String normalWord = normalForms.get(0);
                if (lemmas.containsKey(normalWord)) {
                    lemmas.put(normalWord, lemmas.get(normalWord) + 1);
                } else {
                    lemmas.put(normalWord, 1);
                }
            }else{
                if(anyWordBaseBelongToParticle(engWordBaseForms)){
                    continue;
                }
                List<String> normalForms = engLuceneMorph.getNormalForms(word);
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
        }
        return lemmas;
    }

    public boolean anyWordBaseBelongToParticle(List<String> wordBaseForms) {
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

}

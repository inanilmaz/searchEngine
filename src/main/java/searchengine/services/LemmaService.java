package searchengine.services;


import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LemmaService {
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
}

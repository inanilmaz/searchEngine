package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.SearchIndex;

import java.util.List;


public interface SearchIndexRepositories extends JpaRepository<SearchIndex,Integer>{
    List<SearchIndex> findByLemmaId(Lemma lemma);
    List<SearchIndex> findByPageId(Page page);
}

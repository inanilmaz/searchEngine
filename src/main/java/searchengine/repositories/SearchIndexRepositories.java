package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import searchengine.model.SearchIndex;

import java.util.List;


public interface SearchIndexRepositories extends JpaRepository<SearchIndex,Integer>{
    List<SearchIndex> findByLemmaId(int id);
    List<SearchIndex> findByPageId(int id);
}

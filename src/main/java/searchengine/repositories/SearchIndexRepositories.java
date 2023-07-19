package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import searchengine.model.SearchIndex;

public interface SearchIndexRepositories extends JpaRepository<SearchIndex,Integer>{

}

package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import searchengine.model.PageTable;

public interface PageRepositories extends JpaRepository<PageTable,Integer> {
}

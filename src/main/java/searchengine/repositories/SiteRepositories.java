package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import searchengine.model.SiteTable;

public interface SiteRepositories extends JpaRepository<SiteTable,Integer> {
}

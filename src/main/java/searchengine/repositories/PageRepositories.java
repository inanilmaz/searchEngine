package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import searchengine.model.Page;

import java.util.Optional;

public interface PageRepositories extends JpaRepository<Page,Integer> {
    Optional<Page> findByPath(String path);
}

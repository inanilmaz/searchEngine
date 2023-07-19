package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import searchengine.model.Lemma;

import java.util.Optional;

public interface LemmaRepositories extends JpaRepository<Lemma,Integer> {
    Optional<Lemma> findByLemma(String lemma);
}

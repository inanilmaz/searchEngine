package searchengine.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "Lemma")
@Getter
@Setter
public class Lemma {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private int id;
    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id",nullable = false)
    private SiteTable siteId;
    @Column(nullable = false,columnDefinition = "VARCHAR(255)")
    private String lemma;
    @Column(nullable = false)
    private int frequency;
}

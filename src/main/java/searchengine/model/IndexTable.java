package searchengine.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "IndexTable")
@Getter
@Setter
public class IndexTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private int id;
    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinColumn(name = "page_id",nullable = false)
    private PageTable pageId;
    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "lemma_id",nullable = false)
    private LemmaTable lemmaId;
    @Column(nullable = false,columnDefinition = "FLOAT")
    private float rank;

}

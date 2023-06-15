package searchengine.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "index")
@Getter
@Setter
public class IndexTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private int id;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "page_id",nullable = false)
    private PageTable pageId;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "lemma_id",nullable = false)
    private LemmaTable lemmaId;
    @Column(nullable = false,columnDefinition = "FLOAT")
    private float rank;

}

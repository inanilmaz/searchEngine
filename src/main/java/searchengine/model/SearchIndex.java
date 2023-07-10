package searchengine.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "SearchIndex")
@Getter
@Setter
public class SearchIndex {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private int id;
    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinColumn(name = "page_id",nullable = false)
    private Page pageId;
    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "lemma_id",nullable = false)
    private Lemma lemmaId;
    @Column(nullable = false,columnDefinition = "FLOAT",name = "amount")
    private float rank;

}

package searchengine.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Site")
@Getter
@Setter
public class SiteTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private int id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusEnum status;

    @Column(columnDefinition = "DataTime",nullable = false,name = "status_time")
    private LocalDateTime statusTime;

    @Column(columnDefinition = "TEXT",name = "last_error")
    private String lastError;

    @Column(columnDefinition = "VARCHAR(255)",nullable = false)
    private String url;

    @Column(columnDefinition = "VARCHAR(255)",nullable = false)
    private String name;
}

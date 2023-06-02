package searchengine.model;

import javax.persistence.*;
import java.time.LocalDateTime;
@Entity
@Table(name = "Site")
public class SiteData {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private int id;
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

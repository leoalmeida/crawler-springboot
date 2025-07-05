package space.lasf.crawler_app.entity;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.TreeSet;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidade que representa um produto.
 */
@Entity
@Table(name  = "crawlers", indexes={@Index(name = "crawlers_search_key_idx", columnList = "search_key", unique = true)})
@Data
@NoArgsConstructor
public class Crawler {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "search_key", unique = true)
    @Size(min = 8, max = 8, message = "Search key should have 8 characters")
    private String searchKey;

    @Column(name = "keyword", nullable = false)
    @Size(min = 4, max = 32, message = "Keyword should have between 4 and 32 characters")
    private String keyword;

    @Column(name = "crawler_status", nullable = false)
    private String status;

    @Column(name = "urls")
    @ElementCollection(fetch=FetchType.EAGER)
    @CollectionTable(name = "crawler_urls", joinColumns = @JoinColumn(name = "crawler_id"))
    private Set<String> urls;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;
    
    @Column(name = "last_update", nullable = true)
    private LocalDateTime lastUpdate;
    
    public Crawler startProcess() {
        this.status = CrawlStatus.ACTIVE.name();
        this.startDate = LocalDateTime.now();
        this.urls = new TreeSet<>();
        return this;
    }

    public Crawler endProcess() {
        this.status = (CrawlStatus.ERROR.name().contentEquals(this.status))
                ?this.status
                :CrawlStatus.DONE.name();
        this.lastUpdate = LocalDateTime.now();
        return this;
    }
    
    public Crawler errorProcess() {
        this.status = CrawlStatus.ERROR.name();
        return this;
    }

    public Crawler addLink(String link){
        this.urls.add(link);
        this.lastUpdate = LocalDateTime.now();
        return this;
    }

}

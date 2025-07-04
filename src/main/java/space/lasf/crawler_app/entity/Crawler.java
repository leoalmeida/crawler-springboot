package space.lasf.crawler_app.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidade que representa um produto.
 */
@Entity
@Table(name  = "crawlers", indexes={@Index(name = "crawlers_key_idx", columnList = "search_key,crawler_status", unique = true)})
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
    @ElementCollection
    @CollectionTable(name = "crawler_urls", joinColumns = @JoinColumn(name = "crawler_id"))
    private Set<String> urls;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;
    
    @Column(name = "last_update", nullable = true)
    private LocalDateTime lastUpdate;
    
    public void startProcess() {
        this.status = CrawlStatus.ACTIVE.name();
        this.startDate = LocalDateTime.now();
    }

    public void endProcess() {
        this.status = CrawlStatus.DONE.name();
        this.lastUpdate = LocalDateTime.now();
    }

}

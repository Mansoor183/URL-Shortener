package com.url.url_shortener.Entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "short_url")
public class ShortUrl {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private String id;
    @Column(name = "long_url", nullable = false, unique = true)
    private String longUrl;
    @Column(name = "shortened_url", nullable = false)
    private String shortenedUrl;
    @Column(name = "alias", unique = true)
    private String alias;
    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void setCreatedBy(User user) {
        this.createdBy = user;
    }
    public User getCreatedBy() {
        return createdBy;
    }
}

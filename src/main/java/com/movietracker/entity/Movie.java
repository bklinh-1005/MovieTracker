package com.movietracker.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "movies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ID phim gốc trên hệ thống TMDB (dùng để tra cứu cache không bị trùng)
    @Column(name = "tmdb_id", nullable = false, unique = true)
    private Long tmdbId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String overview;

    @Column(name = "poster_path")
    private String posterPath;

    @Column(name = "backdrop_path")
    private String backdropPath;

    @Column(name = "release_year")
    private Integer releaseYear;

    // Lưu danh sách thể loại (ví dụ: "Hành động, Viễn tưởng")
    @Column(length = 255)
    private String genres;

    @Column(length = 150)
    private String director;

    // Lưu tên các diễn viên chính
    @Column(name = "cast_members", columnDefinition = "TEXT")
    private String castMembers;

    // Điểm trung bình trên TMDB
    @Column(name = "vote_average")
    private Double voteAverage;

    // Mã video YouTube để nhúng Trailer
    @Column(name = "trailer_key", length = 100)
    private String trailerKey;

    @Column(name = "cached_at")
    private LocalDateTime cachedAt;

    @PrePersist
    @PreUpdate
    protected void onCache() {
        this.cachedAt = LocalDateTime.now();
    }
}
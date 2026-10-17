package com.movietracker.repository;

import com.movietracker.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {
    // Tìm phim theo mã ID gốc của TMDB (dùng để kiểm tra xem phim đã được cache chưa)
    Optional<Movie> findByTmdbId(Long tmdbId);
}
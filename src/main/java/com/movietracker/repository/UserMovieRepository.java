package com.movietracker.repository;

import com.movietracker.entity.UserMovie;
import com.movietracker.entity.WatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserMovieRepository extends JpaRepository<UserMovie, Long> {
    // Lấy toàn bộ danh sách phim theo dõi của một user cụ thể
    List<UserMovie> findByUserId(Long userId);

    // Lấy danh sách phim theo dõi của user lọc theo trạng thái (WATCHED, WATCHLIST, WATCHING)
    List<UserMovie> findByUserIdAndStatus(Long userId, WatchStatus status);

    // Tìm bản ghi tương tác giữa 1 user cụ thể và 1 bộ phim cụ thể
    Optional<UserMovie> findByUserIdAndMovieId(Long userId, Long movieId);
}
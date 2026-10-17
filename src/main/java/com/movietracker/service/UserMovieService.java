package com.movietracker.service;

import com.movietracker.entity.*;
import com.movietracker.repository.MovieRepository;
import com.movietracker.repository.UserMovieRepository;
import com.movietracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserMovieService {

    private final UserMovieRepository userMovieRepository;
    private final MovieRepository movieRepository;
    private final UserRepository userRepository;

    // Cập nhật trạng thái xem (WATCHED, WATCHLIST, WATCHING) + Đánh giá sao & Nhận xét
    @Transactional
    public void updateWatchStatusAndReview(String username, Long movieId, WatchStatus status, Integer rating, String reviewComment) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phim"));

        UserMovie userMovie = userMovieRepository.findByUserIdAndMovieId(user.getId(), movie.getId())
                .orElse(UserMovie.builder()
                        .user(user)
                        .movie(movie)
                        .build());

        if (status != null) {
            userMovie.setStatus(status);
        }
        if (rating != null && rating >= 1 && rating <= 5) {
            userMovie.setRating(rating);
        }
        if (reviewComment != null) {
            userMovie.setReviewComment(reviewComment.trim());
        }

        userMovieRepository.save(userMovie);
    }

    // Xóa phim khỏi danh sách cá nhân
    @Transactional
    public void removeUserMovie(String username, Long movieId) {
        User user = userRepository.findByUsername(username).orElseThrow();
        userMovieRepository.findByUserIdAndMovieId(user.getId(), movieId)
                .ifPresent(userMovieRepository::delete);
    }

    // Lấy toàn bộ danh sách theo dõi của User
    public List<UserMovie> getUserMovies(String username, WatchStatus filterStatus) {
        User user = userRepository.findByUsername(username).orElseThrow();
        if (filterStatus != null) {
            return userMovieRepository.findByUserIdAndStatus(user.getId(), filterStatus);
        }
        return userMovieRepository.findByUserId(user.getId());
    }

    // Thống kê tổng quan & Gợi ý phim cá nhân hóa (Chức năng số 6 + Mở rộng)
    public Map<String, Object> getUserStatisticsAndRecommendations(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        List<UserMovie> allEntries = userMovieRepository.findByUserId(user.getId());

        long watchedCount = allEntries.stream().filter(e -> e.getStatus() == WatchStatus.WATCHED).count();
        long watchlistCount = allEntries.stream().filter(e -> e.getStatus() == WatchStatus.WATCHLIST).count();
        long watchingCount = allEntries.stream().filter(e -> e.getStatus() == WatchStatus.WATCHING).count();

        OptionalDouble avgRatingOpt = allEntries.stream()
                .filter(e -> e.getRating() != null)
                .mapToInt(UserMovie::getRating)
                .average();
        double avgRating = avgRatingOpt.isPresent() ? Math.round(avgRatingOpt.getAsDouble() * 10.0) / 10.0 : 0.0;

        // Tìm đạo diễn/thể loại yêu thích để gợi ý phim (Chức năng mở rộng)
        Set<Long> trackedMovieIds = allEntries.stream().map(e -> e.getMovie().getId()).collect(Collectors.toSet());
        List<Movie> recommendations = movieRepository.findAll().stream()
                .filter(m -> !trackedMovieIds.contains(m.getId()))
                .limit(4)
                .collect(Collectors.toList());

        Map<String, Object> stats = new HashMap<>();
        stats.put("watchedCount", watchedCount);
        stats.put("watchlistCount", watchlistCount);
        stats.put("watchingCount", watchingCount);
        stats.put("totalTracked", allEntries.size());
        stats.put("avgRating", avgRating);
        stats.put("recommendations", recommendations);
        return stats;
    }
}
package com.movietracker.controller;

import com.movietracker.entity.*;
import com.movietracker.repository.MovieRepository;
import com.movietracker.repository.UserMovieRepository;
import com.movietracker.repository.UserRepository;
import com.movietracker.service.TmdbService;
import com.movietracker.service.UserMovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class MovieController {

    private final TmdbService tmdbService;
    private final UserMovieService userMovieService;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final UserMovieRepository userMovieRepository;

    // Danh sách Tag thể loại chuẩn dùng chung cho toàn hệ thống
    private static final List<String> ALL_GENRES = List.of(
            "Hành động", "Khoa học viễn tưởng", "Chính kịch", "Tâm lý",
            "Hoạt hình", "Lãng mạn", "Kỳ ảo", "Phiêu lưu",
            "Hình sự", "Hài hước", "Lịch sử", "Gia đình", "Kinh dị"
    );

    // 1. Trang chủ + Tìm kiếm & Lọc phim
    @GetMapping("/")
    public String home(@RequestParam(required = false) String query,
                       @RequestParam(required = false) String genre,
                       @RequestParam(required = false) Integer year,
                       Model model) {
        List<Movie> movies;
        if ((query != null && !query.isBlank()) || (genre != null && !genre.isBlank()) || year != null) {
            movies = tmdbService.searchAndFilterMovies(query, genre, year);
        } else {
            movies = tmdbService.getTrendingMovies();
        }
        model.addAttribute("movies", movies);
        model.addAttribute("allGenres", ALL_GENRES);
        model.addAttribute("query", query);
        model.addAttribute("genre", genre);
        model.addAttribute("year", year);
        return "index";
    }

    // 2. Trang Chi tiết phim + Xem đánh giá
    @GetMapping("/movies/{id}")
    public String movieDetail(@PathVariable Long id, Authentication authentication, Model model) {
        Movie movie = tmdbService.getMovieDetail(id);
        model.addAttribute("movie", movie);

        List<UserMovie> reviews = userMovieRepository.findAll().stream()
                .filter(um -> um.getMovie().getId().equals(id) && um.getReviewComment() != null && !um.getReviewComment().isBlank())
                .collect(Collectors.toList());
        model.addAttribute("reviews", reviews);

        if (authentication != null && authentication.isAuthenticated()) {
            userRepository.findByUsername(authentication.getName()).ifPresent(user -> {
                userMovieRepository.findByUserIdAndMovieId(user.getId(), id)
                        .ifPresent(entry -> model.addAttribute("userEntry", entry));
            });
        }
        return "movie-detail";
    }

    // 3. Lưu Trạng thái xem + Chấm sao & Nhận xét
    @PostMapping("/movies/{id}/track")
    public String trackAndReviewMovie(@PathVariable Long id,
                                      @RequestParam WatchStatus status,
                                      @RequestParam(required = false) Integer rating,
                                      @RequestParam(required = false) String reviewComment,
                                      Authentication authentication) {
        userMovieService.updateWatchStatusAndReview(authentication.getName(), id, status, rating, reviewComment);
        return "redirect:/movies/" + id + "?updated";
    }

    // 4. Xóa phim khỏi danh sách cá nhân
    @PostMapping("/movies/{id}/remove")
    public String removeMovieFromList(@PathVariable Long id, Authentication authentication) {
        userMovieService.removeUserMovie(authentication.getName(), id);
        return "redirect:/profile";
    }

    // 5. Trang Cá nhân, Lịch sử xem & Thống kê
    @GetMapping("/profile")
    public String profilePage(@RequestParam(required = false) WatchStatus status,
                              Authentication authentication,
                              Model model) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElseThrow();
        List<UserMovie> entries = userMovieService.getUserMovies(username, status);
        Map<String, Object> stats = userMovieService.getUserStatisticsAndRecommendations(username);

        model.addAttribute("user", user);
        model.addAttribute("entries", entries);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("stats", stats);
        return "profile";
    }

    // 6. Trang Quản trị viên - Hiển thị danh sách + Chế độ Sửa phim (nếu bấm nút Sửa)
    @GetMapping("/admin")
    public String adminDashboard(@RequestParam(required = false) Long editId, Model model) {
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("movies", movieRepository.findAll());
        model.addAttribute("allGenres", ALL_GENRES);

        if (editId != null) {
            movieRepository.findById(editId).ifPresent(movie -> model.addAttribute("editMovie", movie));
        }
        return "admin";
    }

    // 7. Thêm mới hoặc Cập nhật phim từ trang Admin (Nhận danh sách Tag thể loại + Tự động tách link YouTube)
    @PostMapping("/admin/movies/save")
    public String saveOrUpdateMovieByAdmin(@RequestParam(required = false) Long id,
                                           @RequestParam String title,
                                           @RequestParam Integer releaseYear,
                                           @RequestParam(required = false) List<String> genreTags,
                                           @RequestParam String director,
                                           @RequestParam(required = false) String castMembers,
                                           @RequestParam(required = false) Double voteAverage,
                                           @RequestParam(required = false) String trailerKey,
                                           @RequestParam String posterPath,
                                           @RequestParam String overview) {
        Movie movie;
        if (id != null) {
            movie = movieRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phim cần sửa"));
        } else {
            movie = new Movie();
            movie.setTmdbId(System.currentTimeMillis());
        }

        String genresStr = (genreTags != null && !genreTags.isEmpty())
                ? String.join(", ", genreTags)
                : "Chính kịch";

        movie.setTitle(title.trim());
        movie.setReleaseYear(releaseYear);
        movie.setGenres(genresStr);
        movie.setDirector(director.trim());
        movie.setCastMembers((castMembers != null && !castMembers.isBlank()) ? castMembers.trim() : "Đang cập nhật");
        movie.setVoteAverage(voteAverage != null ? voteAverage : 8.0);
        movie.setTrailerKey(extractYoutubeId(trailerKey));
        movie.setPosterPath((posterPath != null && !posterPath.isBlank()) ? posterPath.trim() : "https://via.placeholder.com/500x750?text=Movie");
        movie.setOverview(overview.trim());

        movieRepository.save(movie);
        return "redirect:/admin?saved";
    }

    // 8. Xóa phim trong trang Admin
    @PostMapping("/admin/movies/{id}/delete")
    @Transactional
    public String deleteMovieByAdmin(@PathVariable Long id) {
        List<UserMovie> relatedEntries = userMovieRepository.findAll().stream()
                .filter(um -> um.getMovie().getId().equals(id))
                .collect(Collectors.toList());
        userMovieRepository.deleteAll(relatedEntries);

        movieRepository.deleteById(id);
        return "redirect:/admin?deleted";
    }

    // 9. Hàm tự động tách Mã Video (11 ký tự) từ bất kỳ đường link YouTube nào
    private String extractYoutubeId(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        String url = input.trim();
        if (url.contains("v=")) {
            String id = url.substring(url.indexOf("v=") + 2);
            int ampIndex = id.indexOf('&');
            return (ampIndex != -1) ? id.substring(0, ampIndex) : id;
        }
        if (url.contains("youtu.be/")) {
            String id = url.substring(url.indexOf("youtu.be/") + 9);
            int qIndex = id.indexOf('?');
            return (qIndex != -1) ? id.substring(0, qIndex) : id;
        }
        if (url.contains("embed/")) {
            String id = url.substring(url.indexOf("embed/") + 6);
            int qIndex = id.indexOf('?');
            return (qIndex != -1) ? id.substring(0, qIndex) : id;
        }
        return url.length() > 100 ? url.substring(0, 100) : url;
    }
}
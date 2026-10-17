package com.movietracker.service;

import com.movietracker.entity.Movie;
import com.movietracker.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TmdbService {

    private final MovieRepository movieRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${tmdb.api.key}")
    private String apiKey;

    @Value("${tmdb.api.base-url}")
    private String baseUrl;

    @Value("${tmdb.image.base-url}")
    private String imageBaseUrl;

    private boolean isApiKeyValid() {
        return apiKey != null && !apiKey.isBlank() && !apiKey.equals("YOUR_TMDB_API_KEY_HERE");
    }

    // 1. Lấy danh sách phim thịnh hành (Trending) và tự động Cache vào DB
    @SuppressWarnings("unchecked")
    public List<Movie> getTrendingMovies() {
        if (isApiKeyValid()) {
            try {
                String url = baseUrl + "/trending/movie/week?api_key=" + apiKey + "&language=vi-VN";
                Map<String, Object> response = restTemplate.getForObject(url, Map.class);
                if (response != null && response.containsKey("results")) {
                    List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
                    for (Map<String, Object> item : results) {
                        saveOrUpdateFromTmdbMap(item);
                    }
                }
            } catch (Exception e) {
                // Fallback an toàn về Database nội bộ khi TMDB API lỗi hoặc mất mạng
                System.out.println("TMDB API fallback kích hoạt: " + e.getMessage());
            }
        }
        return movieRepository.findAll();
    }

    // 2. Tìm kiếm và lọc phim theo Tên, Thể loại, Năm phát hành
    @SuppressWarnings("unchecked")
    public List<Movie> searchAndFilterMovies(String query, String genre, Integer year) {
        if (query != null && !query.isBlank() && isApiKeyValid()) {
            try {
                String url = baseUrl + "/search/movie?api_key=" + apiKey + "&query=" + query.trim() + "&language=vi-VN";
                Map<String, Object> response = restTemplate.getForObject(url, Map.class);
                if (response != null && response.containsKey("results")) {
                    List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
                    for (Map<String, Object> item : results) {
                        saveOrUpdateFromTmdbMap(item);
                    }
                }
            } catch (Exception e) {
                System.out.println("Tìm kiếm qua cache nội bộ do TMDB không phản hồi.");
            }
        }

        // Lọc từ Database Cache để đảm bảo tốc độ phản hồi < 2 giây
        return movieRepository.findAll().stream()
                .filter(m -> (query == null || query.isBlank()) ||
                        m.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                        (m.getDirector() != null && m.getDirector().toLowerCase().contains(query.toLowerCase())))
                .filter(m -> (genre == null || genre.isBlank()) ||
                        (m.getGenres() != null && m.getGenres().toLowerCase().contains(genre.toLowerCase())))
                .filter(m -> (year == null) ||
                        (m.getReleaseYear() != null && m.getReleaseYear().equals(year)))
                .collect(Collectors.toList());
    }

    // 3. Lấy chi tiết phim theo ID (Kèm nạp bổ sung Đạo diễn, Diễn viên, Trailer từ TMDB nếu có key)
    @SuppressWarnings("unchecked")
    public Movie getMovieDetail(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phim với ID: " + id));

        if (isApiKeyValid() && (movie.getDirector() == null || movie.getTrailerKey() == null)) {
            try {
                String url = baseUrl + "/movie/" + movie.getTmdbId() + "?api_key=" + apiKey + "&append_to_response=credits,videos&language=vi-VN";
                Map<String, Object> detail = restTemplate.getForObject(url, Map.class);
                if (detail != null) {
                    // Lấy thể loại
                    List<Map<String, Object>> genresList = (List<Map<String, Object>>) detail.get("genres");
                    if (genresList != null && !genresList.isEmpty()) {
                        String genresStr = genresList.stream().map(g -> (String) g.get("name")).collect(Collectors.joining(", "));
                        movie.setGenres(genresStr);
                    }
                    // Lấy đạo diễn & diễn viên
                    Map<String, Object> credits = (Map<String, Object>) detail.get("credits");
                    if (credits != null) {
                        List<Map<String, Object>> crew = (List<Map<String, Object>>) credits.get("crew");
                        if (crew != null) {
                            crew.stream().filter(c -> "Director".equals(c.get("job"))).findFirst()
                                    .ifPresent(d -> movie.setDirector((String) d.get("name")));
                        }
                        List<Map<String, Object>> cast = (List<Map<String, Object>>) credits.get("cast");
                        if (cast != null) {
                            String castStr = cast.stream().limit(5).map(c -> (String) c.get("name")).collect(Collectors.joining(", "));
                            movie.setCastMembers(castStr);
                        }
                    }
                    // Lấy Trailer YouTube
                    Map<String, Object> videos = (Map<String, Object>) detail.get("videos");
                    if (videos != null) {
                        List<Map<String, Object>> vResults = (List<Map<String, Object>>) videos.get("results");
                        if (vResults != null) {
                            vResults.stream().filter(v -> "YouTube".equals(v.get("site"))).findFirst()
                                    .ifPresent(v -> movie.setTrailerKey((String) v.get("key")));
                        }
                    }
                    movieRepository.save(movie);
                }
            } catch (Exception ignored) {
            }
        }
        return movie;
    }

    private void saveOrUpdateFromTmdbMap(Map<String, Object> item) {
        Number tmdbIdNum = (Number) item.get("id");
        if (tmdbIdNum == null) return;
        Long tmdbId = tmdbIdNum.longValue();

        Optional<Movie> existing = movieRepository.findByTmdbId(tmdbId);
        if (existing.isPresent()) return;

        String title = (String) item.getOrDefault("title", item.get("name"));
        if (title == null) return;

        String overview = (String) item.get("overview");
        String posterPath = (String) item.get("poster_path");
        String fullPoster = (posterPath != null) ? imageBaseUrl + posterPath : "https://via.placeholder.com/500x750?text=No+Poster";

        String releaseDate = (String) item.getOrDefault("release_date", "");
        Integer releaseYear = 2024;
        if (releaseDate != null && releaseDate.length() >= 4) {
            try {
                releaseYear = Integer.parseInt(releaseDate.substring(0, 4));
            } catch (NumberFormatException ignored) {}
        }

        Number voteNum = (Number) item.get("vote_average");
        Double voteAverage = voteNum != null ? Math.round(voteNum.doubleValue() * 10.0) / 10.0 : 0.0;

        Movie newMovie = Movie.builder()
                .tmdbId(tmdbId)
                .title(title)
                .overview(overview != null && !overview.isBlank() ? overview : "Chưa có mô tả tiếng Việt cho bộ phim này.")
                .posterPath(fullPoster)
                .releaseYear(releaseYear)
                .genres("Điện ảnh")
                .director("Đang cập nhật")
                .castMembers("Đang cập nhật")
                .voteAverage(voteAverage)
                .build();
        movieRepository.save(newMovie);
    }
}
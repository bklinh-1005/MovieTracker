package com.movietracker.config;

import com.movietracker.entity.Movie;
import com.movietracker.entity.Role;
import com.movietracker.entity.User;
import com.movietracker.repository.MovieRepository;
import com.movietracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // 1. Tạo tài khoản ADMIN và USER mẫu
        if (!userRepository.existsByUsername("admin")) {
            userRepository.save(User.builder()
                    .username("admin")
                    .email("admin@movietracker.com")
                    .fullName("Quản Trị Viên Hệ Thống")
                    .password(passwordEncoder.encode("123456"))
                    .role(Role.ADMIN)
                    .build());
        }
        if (!userRepository.existsByUsername("khanhlinh")) {
            userRepository.save(User.builder()
                    .username("khanhlinh")
                    .email("linh@phenikaa.edu.vn")
                    .fullName("Bùi Khánh Linh")
                    .password(passwordEncoder.encode("123456"))
                    .role(Role.USER)
                    .build());
        }

        // 2. Tạo sẵn dữ liệu phim mẫu
        if (movieRepository.count() == 0) {
            List<Movie> initialMovies = List.of(
                    Movie.builder().tmdbId(27205L).title("Inception - Kẻ Đánh Cắp Giấc Mơ")
                            .overview("Dom Cobb là một kẻ đánh cắp chuyên nghiệp, trích xuất những bí mật giá trị từ sâu trong tiềm thức của con người khi họ đang mơ.")
                            .posterPath("https://image.tmdb.org/t/p/w500/oYuLEt3zVCKq57qu2F8dT7NIa6f.jpg")
                            .releaseYear(2010).genres("Hành động, Khoa học viễn tưởng, Tâm lý")
                            .director("Christopher Nolan").castMembers("Leonardo DiCaprio, Joseph Gordon-Levitt, Elliot Page, Tom Hardy")
                            .voteAverage(8.4).trailerKey("YoHD9XEInc0").build(),

                    Movie.builder().tmdbId(157336L).title("Interstellar - Hố Đen Tử Thần")
                            .overview("Khi Trái Đất dần trở nên không thể sinh sống, một nhóm các nhà thám hiểm du hành qua lỗ sâu vũ trụ để tìm kiếm hành tinh mới cho nhân loại.")
                            .posterPath("https://image.tmdb.org/t/p/w500/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg")
                            .releaseYear(2014).genres("Khoa học viễn tưởng, Phiêu lưu, Chính kịch")
                            .director("Christopher Nolan").castMembers("Matthew McConaughey, Anne Hathaway, Jessica Chastain, Michael Caine")
                            .voteAverage(8.6).trailerKey("zSWdZVtXT7E").build(),

                    Movie.builder().tmdbId(872585L).title("Oppenheimer")
                            .overview("Câu chuyện về nhà vật lý lý thuyết J. Robert Oppenheimer và vai trò giám đốc Dự án Manhattan chế tạo vũ khí hạt nhân đầu tiên trong Thế chiến II.")
                            .posterPath("https://image.tmdb.org/t/p/w500/8Gxv8gSFCU0XGDykEGv7zR1n2ua.jpg")
                            .releaseYear(2023).genres("Chính kịch, Lịch sử")
                            .director("Christopher Nolan").castMembers("Cillian Murphy, Emily Blunt, Matt Damon, Robert Downey Jr.")
                            .voteAverage(8.1).trailerKey("uYPbbksJxIg").build(),

                    Movie.builder().tmdbId(496243L).title("Parasite - Ký Sinh Trùng")
                            .overview("Gia đình nghèo khó của Ki-taek dần thâm nhập vào cuộc sống của gia đình giàu có họ Park, dẫn đến một chuỗi biến cố bất ngờ và đen tối.")
                            .posterPath("https://image.tmdb.org/t/p/w500/7IiTTgloJzvGI1TAYymCfbfl3vT.jpg")
                            .releaseYear(2019).genres("Tâm lý, Hài đen, Chính kịch")
                            .director("Bong Joon-ho").castMembers("Song Kang-ho, Lee Sun-kyun, Cho Yeo-jeong, Choi Woo-shik")
                            .voteAverage(8.5).trailerKey("5xH0HfJHsaY").build(),

                    Movie.builder().tmdbId(129L).title("Spirited Away - Vùng Đất Linh Hồn")
                            .overview("Cô bé 10 tuổi Chihiro bị lạc vào thế giới linh hồn kỳ bí sau khi bố mẹ cô bị biến thành heo bởi phù thủy Yubaba.")
                            .posterPath("https://image.tmdb.org/t/p/w500/39wmItIWsg5sZMyRUHLkWBcuVCM.jpg")
                            .releaseYear(2001).genres("Hoạt hình, Gia đình, Kỳ ảo")
                            .director("Hayao Miyazaki").castMembers("Rumi Hiiragi, Miyu Irino, Mari Natsuki")
                            .voteAverage(8.5).trailerKey("ByXuk9QqQkk").build(),

                    Movie.builder().tmdbId(155L).title("The Dark Knight - Kỵ Sĩ Bóng Đêm")
                            .overview("Batman hợp tác cùng Trung úy Jim Gordon và Công tố viên Harvey Dent để triệt phá các tổ chức tội phạm tại Gotham, nhưng phải đối đầu với tên tội phạm điên loạn Joker.")
                            .posterPath("https://image.tmdb.org/t/p/w500/qJ2tW6WMUDux911r6m7haRef0WH.jpg")
                            .releaseYear(2008).genres("Hành động, Hình sự, Chính kịch")
                            .director("Christopher Nolan").castMembers("Christian Bale, Heath Ledger, Aaron Eckhart, Gary Oldman")
                            .voteAverage(8.5).trailerKey("EXeTwQWrcwY").build(),

                    Movie.builder().tmdbId(693134L).title("Dune: Part Two - Hành Tinh Cát 2")
                            .overview("Paul Atreides hợp nhất với tộc người Fremen và bắt đầu hành trình trả thù những kẻ đã hủy hoại gia tộc mình.")
                            .posterPath("https://image.tmdb.org/t/p/w500/1pdfLvkbY9ohJlCjQH2CZjjYVvJ.jpg")
                            .releaseYear(2024).genres("Khoa học viễn tưởng, Phiêu lưu, Hành động")
                            .director("Denis Villeneuve").castMembers("Timothée Chalamet, Zendaya, Rebecca Ferguson, Javier Bardem")
                            .voteAverage(8.3).trailerKey("Way9Dexny3w").build(),

                    Movie.builder().tmdbId(372058L).title("Your Name - Tên Cậu Là Gì?")
                            .overview("Hai học sinh trung học xa lạ phát hiện ra họ đang hoán đổi cơ thể cho nhau một cách kỳ lạ qua những giấc mơ.")
                            .posterPath("https://image.tmdb.org/t/p/w500/q719jXXEzOoYaps6babgKnONONX.jpg")
                            .releaseYear(2016).genres("Hoạt hình, Lãng mạn, Kỳ ảo")
                            .director("Makoto Shinkai").castMembers("Ryunosuke Kamiki, Mone Kamishiraishi")
                            .voteAverage(8.5).trailerKey("xU47nhruN-Q").build()
            );
            movieRepository.saveAll(initialMovies);
        }
    }
}
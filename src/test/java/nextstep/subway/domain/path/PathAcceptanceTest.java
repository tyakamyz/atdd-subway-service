package nextstep.subway.domain.path;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.AcceptanceTest;
import nextstep.subway.domain.line.acceptance.LineAcceptanceTest;
import nextstep.subway.domain.line.acceptance.LineSectionAcceptanceTest;
import nextstep.subway.domain.line.dto.LineRequest;
import nextstep.subway.domain.line.dto.LineResponse;
import nextstep.subway.domain.member.MemberAcceptanceTest;
import nextstep.subway.domain.path.dto.PathFinderResponse;
import nextstep.subway.domain.station.StationAcceptanceTest;
import nextstep.subway.domain.station.dto.StationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;


@DisplayName("지하철 경로 조회")
public class PathAcceptanceTest extends AcceptanceTest {

    private LineResponse 신분당선;
    private LineResponse 이호선;
    private LineResponse 삼호선;
    private StationResponse 강남역;
    private StationResponse 양재역;
    private StationResponse 교대역;
    private StationResponse 남부터미널역;
    private String accessToken;


    /**
     * 교대역     --- *2호선* --- 강남역
     *  |                       |
     * *3호선*                  *신분당선*
     *  |                       |
     * 남부터미널역 --- *3호선* --- 양재
     */
// 교대 - 강남 - 양재 : 20
// 교대 - 남부터미널역 - 양재 : 8
    @BeforeEach
    public void setUp() {
        super.setUp();
        final String email = "hongji3354@gmail.com";
        final String password = "hongji3354";
        MemberAcceptanceTest.회원_생성을_요청(email,password,20);
        accessToken = MemberAcceptanceTest.토큰_발급(email, password);
        강남역 = StationAcceptanceTest.지하철역_등록되어_있음("강남역").as(StationResponse.class);
        양재역 = StationAcceptanceTest.지하철역_등록되어_있음("양재역").as(StationResponse.class);
        교대역 = StationAcceptanceTest.지하철역_등록되어_있음("교대역").as(StationResponse.class);
        남부터미널역 = StationAcceptanceTest.지하철역_등록되어_있음("남부터미널역").as(StationResponse.class);

        신분당선 = LineAcceptanceTest.지하철_노선_등록되어_있음(new LineRequest("신분당선", "bg-red-600", 강남역.getId(), 양재역.getId(), 10)).as(LineResponse.class);
        이호선 = LineAcceptanceTest.지하철_노선_등록되어_있음(new LineRequest("이호선", "bg-green-400", 교대역.getId(), 강남역.getId(), 10)).as(LineResponse.class);
        삼호선 = LineAcceptanceTest.지하철_노선_등록되어_있음(new LineRequest("삼호선", "bg-orange-200", 교대역.getId(), 양재역.getId(), 5)).as(LineResponse.class);

        지하철_노선에_지하철역_등록되어_있음(삼호선, 교대역, 남부터미널역, 3);
    }

    @DisplayName("최단 경로 조회")
    @Test
    void shortestRouteInquiry() {
        // when
        final ExtractableResponse<Response> response = 최단_경로_조회_요청(교대역.getId(), 양재역.getId());

        // then
        최단_경로_응답됨(response, Arrays.asList("교대역","남부터미널역","양재역"), 5, 1250);
    }

    private void 최단_경로_응답됨(final ExtractableResponse<Response> response, List<String> stations, int distance, int fare) {
        final PathFinderResponse pathFinderResponse = response.as(PathFinderResponse.class);
        assertAll(() -> {
            assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
            assertThat(pathFinderResponse.getStations()).extracting("name").containsExactlyElementsOf(stations);
            assertThat(pathFinderResponse.getDistance()).isEqualTo(distance);
            assertThat(pathFinderResponse.getFare()).isEqualTo(fare);
        });
    }

    private ExtractableResponse<Response> 최단_경로_조회_요청(Long source, Long target) {
        return RestAssured.given().log().all()
                .auth().oauth2(accessToken)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .queryParam("source", source)
                .queryParam("target", target)
                .get("/paths")
                .then().log().all()
                .extract();
    }

    public static ExtractableResponse<Response> 지하철_노선에_지하철역_등록되어_있음(LineResponse lineResponse, StationResponse upStation, StationResponse downStation, int distance) {
        return LineSectionAcceptanceTest.지하철_노선에_지하철역_등록_요청(lineResponse, upStation, downStation, distance);
    }

}

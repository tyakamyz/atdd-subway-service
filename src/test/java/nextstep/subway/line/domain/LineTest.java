package nextstep.subway.line.domain;

import static org.assertj.core.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nextstep.subway.station.domain.Station;

class LineTest {
    private Station 강남역;
    private Station 광교역;

    @BeforeEach
    public void setUp() {
        // given
        강남역 = new Station("강남역");
        광교역 = new Station("광교역");
    }

    @Test
    void getStations() {
        Line line = new Line("신분당선", "red", 강남역, 광교역, 100);

        List<Station> stations = line.getStations();

        assertThat(stations).isEqualTo(Arrays.asList(강남역, 광교역));
    }

    @Test
    void findUpStation() {
        Line line = new Line("신분당선", "red", 강남역, 광교역, 100);

        Station upStation = line.findUpStation();

        assertThat(upStation).isEqualTo(강남역);
    }
}
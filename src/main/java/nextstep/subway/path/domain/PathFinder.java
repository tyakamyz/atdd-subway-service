package nextstep.subway.path.domain;

import nextstep.subway.line.domain.Sections;
import nextstep.subway.member.domain.MemberAge;
import nextstep.subway.station.domain.Station;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;

import java.util.Objects;

public class PathFinder {
    private static DijkstraShortestPath path;

    public PathFinder(DijkstraShortestPath path) {
        this.path = path;
    }

    public Path findPath(Station startStation, Station endStation, MemberAge age) {
        GraphPath path = this.path.getPath(startStation, endStation);
        validatePath(path);
        int distance = (int) this.path.getPathWeight(startStation, endStation);
        return new Path(path.getVertexList(), distance, Fare.of(Sections.of(path.getEdgeList()), new TotalDistance(distance), age).getFare());
    }

    private void validatePath(GraphPath path) {
        if (isNull(path)) {
            throw new IllegalArgumentException("경로가 존재하지 않습니다.");
        }
    }

    private boolean isNull(GraphPath path) {
        return Objects.isNull(path);
    }


}

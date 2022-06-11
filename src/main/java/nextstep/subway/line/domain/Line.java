package nextstep.subway.line.domain;

import nextstep.subway.BaseEntity;
import nextstep.subway.station.domain.Station;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.WeightedMultigraph;

import javax.persistence.*;
import java.util.List;
import java.util.Optional;

@Entity
public class Line extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String name;
    private String color;

    @Embedded
    private final Sections sections;

    public Line() {
        this(null, null);
    }

    public Line(String name, String color) {
        this.name = name;
        this.color = color;
        this.sections = new Sections();
    }

    public Line(String name, String color, Station upStation, Station downStation, int distance) {
        this(name, color);
        addSection(upStation, downStation, distance);
    }

    public void update(Line line) {
        this.name = line.getName();
        this.color = line.getColor();
    }

    public void removeSection(Section section) {
        sections.removeSection(section);
    }

    public List<Station> findStations() {
        return sections.findStations();
    }

    public void addSection(Station upStation, Station downStation, int distance) {
        sections.addSection(new Section(this, upStation, downStation, distance));
    }

    public void removeSectionByStation(Station station) {
        if (isOnlySection()) {
            throw new RuntimeException("구간이 하나뿐인 노선의 경우 역을 제외할 수 없습니다");
        }

        Optional<Section> upSection = sections.findSectionByUpStation(station);
        Optional<Section> downSection = sections.findSectionByDownStation(station);

        if (upSection.isPresent() && downSection.isPresent()) {
            reRegisterSection(upSection.get(), downSection.get());
        }

        upSection.ifPresent(this::removeSection);
        downSection.ifPresent(this::removeSection);
    }

    private boolean isOnlySection() {
        return sections.sectionsSize() <= 1;
    }

    private void reRegisterSection(Section upSection, Section downSection) {
        Station reUpStation = downSection.getUpStation();
        Station reDownStation = upSection.getDownStation();
        int reDistance = upSection.getDistance() + downSection.getDistance();
        sections.addSection(new Section(this, reUpStation, reDownStation, reDistance));
    }

    public void registerPath(WeightedMultigraph<Station, DefaultWeightedEdge> graph) {
        addVertices(graph);
        addEdges(graph);
    }

    private void addVertices(WeightedMultigraph<Station, DefaultWeightedEdge> graph) {
        findStations().forEach(graph::addVertex);
    }

    private void addEdges(WeightedMultigraph<Station, DefaultWeightedEdge> graph) {
        getSections().getSections()
                .forEach(section -> addEdgeWith(addEdge(section, graph), section.getDistance(), graph));
    }

    private DefaultWeightedEdge addEdge(Section section, WeightedMultigraph<Station, DefaultWeightedEdge> graph) {
        return graph.addEdge(section.getUpStation(), section.getDownStation());
    }

    private void addEdgeWith(DefaultWeightedEdge edge, int weight, WeightedMultigraph<Station, DefaultWeightedEdge> graph) {
        graph.setEdgeWeight(edge, weight);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public Sections getSections() {
        return sections;
    }
}

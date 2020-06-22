package algorithm.pathfinder;

import algorithm.AbstractAlgorithm;
import algorithm.search.Search;
import algorithm.sort.Sort;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import model.BoardModel;
import model.Vertex;
import model.VisualiserModel;

public abstract class AbstractPathfinder extends AbstractAlgorithm implements Pathfinder {
    private ObservableList<Vertex> fringe = FXCollections.observableArrayList();
    protected BoardModel model;
    protected Vertex startVertex = null, endVertex = null;


    public void setModel(VisualiserModel model) {
        this.model = (BoardModel) model;
    }


    /**
     * Sets the start and end vertices respectively
     * @param start
     * @param end
     */
    @Override
    public void setVertices(Vertex start, Vertex end) {
        assert(start != null && end != null);
        startVertex = start;
        endVertex = end;
    }

    /**
     * Returns the fringe of the algorithm
     */
    public ObservableList<Vertex> getFringe() {
        return fringe;
    }


    /**
     * Returns true if and only there is another step in the algorithm
     */
    @Override
    public boolean hasNext() {
        return !endVertex.isVisited() && !getFringe().isEmpty();
    }


    /**
     * Use the current state of the algorithm to visualise the current state
     */
    public void visualise() {
        for (Vertex vertex : model.getBoard()) {
            StringBuilder style = new StringBuilder();
            style.append("-fx-background-color: ");
            if (vertex.isWall())
                style.append("black;");
            else if (vertex == endVertex || vertex == startVertex)
                continue;
            else if (vertex.isVisited())
                style.append("lightseagreen;");
            else if (fringe.contains(vertex))
                style.append("powderblue;");
            else
                style.append("white;");
            style.append(Vertex.styles);
            vertex.setStyle(style.toString());
        }
    }

    /**
     * Visualise the final path
     */
    public void showResult() {
        if (!endVertex.isVisited()) {
            System.out.println("No path available");
            return;
        }
        var currentVertex = endVertex.getParentVertex();
        while (currentVertex != startVertex) {
            currentVertex.setStyle("-fx-background-color: navajowhite;" + Vertex.styles);
            currentVertex = currentVertex.getParentVertex();
        }
    }


    /**
     * Return the vertex with the smallest value in the map
     * @param map
     * @return the smallest vertex in the map
     */
    protected Vertex visitSmallestVertex(ObservableMap<Vertex, Double> map) {
        // Get the fringe node with smallest f value
        var currentVertex = getFringe().get(0);
        for (Vertex fringeNode : getFringe()) {
            if (map.get(fringeNode) < map.get(currentVertex)) {
                currentVertex = fringeNode;
            }
        }
        getFringe().remove(currentVertex);
        currentVertex.setVisited(true);
        return currentVertex;
    }

    /**
     * Used in a step of the algorithm, updates the values in the map of the neighbours according to the currentVertex
     * NB. hValue is set to 0 for algorithms that do not use it i.e. Dijkstra's
     * @param currentVertex
     * @param map
     */
    protected void updateNeighbours(Vertex currentVertex, ObservableMap<Vertex, Double> map) {
        for (Vertex neighbour : currentVertex.getNeighbours()) {
            if (neighbour.isWall())
                continue;
            boolean nonDiagonal = neighbour.getCol() == currentVertex.getCol()
                    || neighbour.getRow() == currentVertex.getRow();

            double distance = currentVertex.gValue() + (nonDiagonal ? NON_DIAG_COST : DIAG_COST);

            if (!neighbour.isVisited()) {
                if (getFringe().contains(neighbour)) {
                    if (distance < neighbour.gValue()) {
                        neighbour.setGValue(distance);
                        map.put(neighbour, neighbour.gValue() + neighbour.hValue());
                        neighbour.setParentVertex(currentVertex);
                    }
                } else {
                    getFringe().add(neighbour);
                    neighbour.setGValue(distance);
                    map.put(neighbour, neighbour.gValue() + neighbour.hValue());
                    neighbour.setParentVertex(currentVertex);
                }
            }
        }
    }

    public Vertex getStartVertex() {
        return startVertex;
    }

    public Vertex getEndVertex() {
        return endVertex;
    }

    public boolean isSort() {
        return false;
    }

    public boolean isSearch() {
        return false;
    }

    public boolean isPathfinder() {
        return true;
    }

    public Pathfinder asPathfinder() {
        return this;
    }

    public Sort asSort() {
        return null;
    }

    public Search asSearch() {
        return null;
    }

    @Override
    public void setVertexEventHandlers(Vertex vertex) {
        // Clicked, change state of vertices
        vertex.setOnMouseClicked(e -> {
            if (startVertex == vertex) {
                vertex.resetStyle();
                startVertex = null;
            } else if (endVertex == vertex) {
                vertex.resetStyle();
                endVertex = null;
            } else if (!startIsSet()) {
                vertex.setStart();
                startVertex = vertex;
            } else if (!endIsSet()) {
                vertex.setEnd();
                endVertex = vertex;
            }
            setVertices(startVertex, endVertex);
        });
    }

    private boolean startIsSet() {
        return startVertex != null;
    }

    private boolean endIsSet() {
        return endVertex != null;
    }

    public boolean canPlay() {
        return startIsSet() && endIsSet();

    }


}

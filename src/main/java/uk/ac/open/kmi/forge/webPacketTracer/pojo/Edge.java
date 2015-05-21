package uk.ac.open.kmi.forge.webPacketTracer.pojo;

import uk.ac.open.kmi.forge.webPacketTracer.api.http.Utils;

public class Edge {
    String id;  // E.g., a9101f6bef7c437291c29391e94ee233
    String from;  // E.g., 4e70e5d74399485eb4096c9d1c9446ea
    String to;  // E.g., 6fc7797b1a334fd78db11d6e7468db65

    public Edge() {
    }

    public Edge(String id, String from, String to) {
        this.id = id;
        this.from = from;
        this.to = to;
    }

    /**
     *
     * @param id
     *      E.g., {a9101f6b-ef7c-4372-91c2-9391e94ee233}
     * @param fromId
     *      E.g., {4e70e5d7-4399-485e-b409-6c9d1c9446ea}
     * @param toId
     *      E.g., {6fc7797b-1a33-4fd7-8db1-1d6e7468db65}
     * @return
     */
    public static Edge fromCiscoIds(String id, String fromId, String toId) {
        return new Edge( Utils.toSimplifiedUUID(id),
                         Utils.toSimplifiedUUID(fromId),
                         Utils.toSimplifiedUUID(toId) );
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Edge)) return false;

        Edge edge = (Edge) o;

        if (from != null ? !from.equals(edge.from) : edge.from != null) return false;
        if (id != null ? !id.equals(edge.id) : edge.id != null) return false;
        if (to != null ? !to.equals(edge.to) : edge.to != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (from != null ? from.hashCode() : 0);
        result = 31 * result + (to != null ? to.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Edge{" +
                "id='" + id + '\'' +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                '}';
    }
}

package data;

import lombok.Data;
import java.util.List;

/**
 * Represents a security mapping node in the COSA structure.
 */
@Data
public class MappingNode {
    /**
     * Name of the security category or mapping.
     */
    private String name;

    /**
     * Version of the mapping.
     */
    private String version;

    /**
     * Namespace for the mapping.
     */
    private String namespace;

    /**
     * Categories associated with this node.
     */
    private List<String> categories;

    /**
     * Child mapping nodes for hierarchical structure.
     */
    private List<MappingNode> children;
}

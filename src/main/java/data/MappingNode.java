package data;

import lombok.Data;
import java.util.List;

@Data
public class MappingNode {
    private String name;
    private String version;
    private String namespace;
    private List<String> categories;
    private List<MappingNode> children;
}

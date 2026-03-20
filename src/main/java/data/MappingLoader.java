package data;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Loader class for reading security mapping definitions from resources.
 */
public class MappingLoader {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String userHome = System.getProperty("user.home");

    /**
     * Loads all parent mappings from the local resources directory.
     *
     * @return a list of top-level {@link MappingNode}s
     */
    public List<MappingNode> loadAllParentMappings(){

        List<MappingNode> allMappings = new ArrayList<>();

            File mappingsFolder = new File(userHome,"COSA-Classification-of-Security-APIs-/src/main/resources/lib-mappings");

            if(!mappingsFolder.exists() || !mappingsFolder.isDirectory()){
                return new ArrayList<>();
            }

            File[] files = mappingsFolder.listFiles();

            if(files == null) {
                return new ArrayList<>();
            }

            for(File file : files){
                try {
                    MappingNode mapping = objectMapper.readValue(file, MappingNode.class);
                    allMappings.add(mapping);
                }catch (IOException e){
                    e.printStackTrace();
                }
            }


        return allMappings;
    }

    /**
     * Recursively retrieves all child mappings for a given node.
     *
     * @param mappingNode the root node to start from
     * @param children    the list to accumulate children into
     * @return the list containing all descendant mapping nodes
     */
    public List<MappingNode> getAllChildMappings(MappingNode mappingNode, ArrayList<MappingNode> children){
        if(mappingNode.getChildren() == null || mappingNode.getChildren().isEmpty()){
            return children;
        }

        for(MappingNode childNode : mappingNode.getChildren()){
            children.add(childNode);
            getAllChildMappings(childNode, children);
        }

        return children;
    }

}


package data;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MappingLoader {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String userHome = System.getProperty("user.home");

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

    public List<MappingNode> getAllChildMappings(MappingNode mappingNode,ArrayList<MappingNode> children){
        if(mappingNode.getChildren() == null || mappingNode.getChildren().isEmpty()){
            return null;
        }
        children.add(mappingNode);

        for(MappingNode childNode : mappingNode.getChildren()){
            getAllChildMappings(childNode, children);
        }

        return children;

    }

    public List<String> getNamespaces(MappingNode fileRootNode, ArrayList<String> namespaces){
        if(fileRootNode.getNamespace() == null){
            return new ArrayList<>();
        }

        namespaces.add(fileRootNode.getNamespace());

        for(MappingNode childNode : fileRootNode.getChildren()){
            getNamespaces(childNode, namespaces);
        }

        return namespaces;

    }
}


package data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;

public class SecurityAPILoader {

    public SecurityAPILoader() {
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("data/classes.json");
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(inputStream);
            JsonNode classesArray = root.get("classes");

            for (JsonNode clsNode : classesArray) {
                SecurityClass sc = objectMapper.treeToValue(clsNode, SecurityClass.class);
                SecurityClass.addClass(sc);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}




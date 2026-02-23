package SecurityClass;

import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SecurityclassUtils {

    @Getter
    private static List<SecurityClass> securityClasses = new ArrayList<>();

    @Getter
    private static Map<String, Map<Integer, String>> matchDetails = new HashMap<>();

    public static void addSecurityClass(SecurityClass securityClass){
        securityClasses.add(securityClass);
    }

    public static void addMatchDetail(String fileName, int lineNumber, String matchedText) {
        matchDetails
                .computeIfAbsent(fileName, k -> new HashMap<>())
                .put(lineNumber, matchedText);
    }

    public static String getMatchDetail(String fileName, int lineNumber) {
        Map<Integer, String> fileMap = matchDetails.get(fileName);
        if (fileMap == null) {
            return "";
        }
        return fileMap.getOrDefault(lineNumber, "");
    }

}

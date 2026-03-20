package SecurityClass;

import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for managing security classes and their match details.
 */
public class SecurityclassUtils {

    /**
     * List of all discovered security classes.
     */
    @Getter
    private final static List<SecurityClass> securityClasses = new ArrayList<>();

    /**
     * Map storing match details, keyed by file name and then by line number.
     */
    @Getter
    private final static Map<String, Map<Integer, String>> matchDetails = new HashMap<>();

    /**
     * Adds a security class to the global list.
     *
     * @param securityClass the security class to add
     */
    public static void addSecurityClass(SecurityClass securityClass){
        securityClasses.add(securityClass);
    }

    /**
     * Records a match detail for a specific file and line.
     *
     * @param fileName    the name of the file
     * @param lineNumber  the line number of the match
     * @param matchedText the text that was matched
     */
    public static void addMatchDetail(String fileName, int lineNumber, String matchedText) {
        matchDetails
                .computeIfAbsent(fileName, k -> new HashMap<>())
                .put(lineNumber, matchedText);
    }

    /**
     * Retrieves the match detail for a specific file and line.
     *
     * @param fileName   the name of the file
     * @param lineNumber the line number of the match
     * @return the matched text, or an empty string if not found
     */
    public static String getMatchDetail(String fileName, int lineNumber) {
        Map<Integer, String> fileMap = matchDetails.get(fileName);
        if (fileMap == null) {
            return "";
        }
        return fileMap.getOrDefault(lineNumber, "");
    }

}

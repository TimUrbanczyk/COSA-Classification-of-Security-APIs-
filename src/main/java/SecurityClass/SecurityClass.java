package SecurityClass;

import java.util.List;
import java.util.Map;

/**
 * Represents a security class and its occurrences in the codebase.
 *
 * @param name        the name of the security class
 * @param occurrences a map where the key is the file name and the value is a list of line numbers
 */
public record SecurityClass(String name, Map<String, List<Integer>> occurrences) {
}

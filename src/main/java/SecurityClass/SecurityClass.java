package SecurityClass;

import java.util.List;
import java.util.Map;

public record SecurityClass(String name, Map<String, List<Integer>> occurrences) {
}

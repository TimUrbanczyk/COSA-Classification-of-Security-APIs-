package SecurityClass;

import lombok.Data;
import java.io.File;
import java.util.List;
import java.util.Map;

@Data
public class SecurityClass {
    private final String name;
    public final  Map<String, List<Integer>> occurrences;
}

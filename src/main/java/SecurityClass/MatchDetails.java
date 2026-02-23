package SecurityClass;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MatchDetails {
    private String fileName;
    private int lineNumber;
    private String matchedText;
}


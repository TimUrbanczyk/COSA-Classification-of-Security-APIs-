package SecurityClass;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class SecurityclassUtils {

    @Getter
    private static List<SecurityClass> securityClasses = new ArrayList<>();

    public static void addSecurityClass(SecurityClass securityClass){
        securityClasses.add(securityClass);
    }


}

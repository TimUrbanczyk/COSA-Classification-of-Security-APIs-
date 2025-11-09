package data;

import java.util.ArrayList;
import java.util.List;

public class SecurityClass {

    private String name;
    private List<String> apis;
    private static List<SecurityClass> securityClasses = new ArrayList<>();

    public SecurityClass(){
    }

    public static void addClass(SecurityClass sc){
        securityClasses.add(sc);
    }

    public String getName(){
        return name;
    }

    public List<String> getApis(){
        return apis;
    }

    public static List<SecurityClass> getSecurityClasses(){
        return securityClasses;
    }


    public static String ListToString(){
        String header =
                "<html>" +
                    "<div style='max-width:500px;' font-size:20px;>" +
                        "<h1>" +
                            "Every Securityclass listed, together with children listet:" +
                        "<h1/>"+
                        "<p>";
        StringBuilder stringBuilder = new StringBuilder(header);
        for(SecurityClass securityClass : securityClasses){
            stringBuilder.append("<br><h2>").append(securityClass.name).append(":</h2><br>");
            for(int i = 0; i < securityClass.getApis().size(); i++){
                stringBuilder.append(securityClass.getApis().get(i)).append(", ");
            }
        }
        String footer = "</p></div></html>";
        stringBuilder.append(footer);
        return stringBuilder.toString();
    }

}

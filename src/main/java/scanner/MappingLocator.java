package scanner;

import SecurityClass.SecurityClass;
import SecurityClass.SecurityclassUtils;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import data.MappingNode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MappingLocator {

    public List<Integer> locateMapping(MappingNode mappingNode, PsiElement fileAsPsiElement) {
        List<Integer> lineNumbers = new ArrayList<>();
        Set<Integer> seenLines = new HashSet<>();
        PsiFile psiFile = fileAsPsiElement.getContainingFile();

        if (psiFile == null) return lineNumbers;

        Document document = PsiDocumentManager.getInstance(psiFile.getProject()).getDocument(psiFile);
        if (document == null) return lineNumbers;

        if(mappingNode.getCategories() != null && !mappingNode.getCategories().isEmpty()){
            for(String category : mappingNode.getCategories()){
                SecurityClass securityClass = new SecurityClass(category);

            }
        }
        PsiTreeUtil.processElements(psiFile, element -> {
            String text = element.getText();
            String target = mappingNode.getNamespace();
            if (text.toLowerCase().contains(target.toLowerCase())) {
                TextRange range = element.getTextRange();
                int line = document.getLineNumber(range.getStartOffset()) + 1;
                if(seenLines.contains(line)){
                    return false;
                }
                seenLines.add(line);
                if(!target.isEmpty()){
                    lineNumbers.add(line);
                    for(String category : mappingNode.getCategories()) {
                        SecurityClass securityClass = new SecurityClass(category);
                        if (SecurityclassUtils.getSecurityClasses().contains(securityClass)) {
                            continue;
                        }
                        SecurityclassUtils.addSecurityClass(securityClass);
                    }
                    System.out.println("Found '" + target + " in file " + psiFile.getName() + "at line " + line +"  " + mappingNode.getCategories() );
                }
            }
            return true;
        });
        return lineNumbers;
    }
}


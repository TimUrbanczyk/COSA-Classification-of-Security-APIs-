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
import java.util.*;
import java.util.regex.Pattern;

public class MappingLocator {

    public List<Integer> locateMapping(MappingNode mappingNode, PsiElement fileAsPsiElement) {
        List<Integer> lineNumbers = new ArrayList<>();
        Set<Integer> seenLines = new HashSet<>();

        PsiFile psiFile = fileAsPsiElement.getContainingFile();
        if (psiFile == null) return lineNumbers;

        PsiDocumentManager manager =
                PsiDocumentManager.getInstance(psiFile.getProject());
        Document document = manager.getDocument(psiFile);
        if (document == null) return lineNumbers;

        manager.commitDocument(document);

        String target = mappingNode.getNamespace();
        if (target == null || target.isEmpty()) return lineNumbers;

        Pattern pattern = Pattern.compile(
                "\\b" + Pattern.quote(target) + "\\b",
                Pattern.CASE_INSENSITIVE
        );

        PsiTreeUtil.processElements(psiFile, element -> {

            if(element.getFirstChild() != null){
                return true;
            }

            String text = element.getText();
            if(!pattern.matcher(text).find()){
                return true;
            }

            TextRange range = element.getTextRange();
            int line = document.getLineNumber(range.getStartOffset()) + 1;
            if(seenLines.contains(line)){
                return true;
            }

            seenLines.add(line);
            lineNumbers.add(line);

            for (String category : mappingNode.getCategories()) {
                Optional<SecurityClass> existingSecurityClass = SecurityclassUtils.getSecurityClasses()
                        .stream()
                        .filter(existing -> Objects.equals(existing.getName(), category))
                        .findFirst();

                if(existingSecurityClass.isPresent()){
                    // Add line to existing SecurityClass
                    existingSecurityClass.get().occurrences
                            .computeIfAbsent(psiFile.getName(), k -> new ArrayList<>())
                            .add(line);
                } else {
                    // Create new SecurityClass
                    SecurityClass securityClass =
                            new SecurityClass(category, new HashMap<>());

                    securityClass.occurrences
                            .computeIfAbsent(psiFile.getName(), k -> new ArrayList<>())
                            .add(line);

                    SecurityclassUtils.addSecurityClass(securityClass);
                }
            }

            System.out.println(
                    "Found " + target + " in file " +
                            psiFile.getName() + " at line " + line + "  " +
                            mappingNode.getCategories()
            );

            return true;
        });

        return lineNumbers;
    }
}
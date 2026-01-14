package scanner;

import SecurityClass.SecurityClass;
import SecurityClass.SecurityclassUtils;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.*;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import data.MappingNode;
import java.util.*;

public class MappingLocator {

    public List<Integer> locateMapping(MappingNode mappingNode, PsiElement fileAsPsiElement) {
        List<Integer> lineNumbers = new ArrayList<>();
        Set<Integer> seenLines = new HashSet<>();

        PsiFile psiFile = fileAsPsiElement.getContainingFile();
        if (psiFile == null || !(psiFile instanceof PsiJavaFile)) {
            return lineNumbers;
        }

        PsiDocumentManager manager = PsiDocumentManager.getInstance(psiFile.getProject());
        Document document = manager.getDocument(psiFile);

        if (document == null){
            return lineNumbers;
        }

        manager.commitDocument(document);

        String fullNamespace = buildFullNamespace(mappingNode);
        if (fullNamespace == null || fullNamespace.isEmpty()){
            return lineNumbers;
        }

        final String targetNamespace = fullNamespace.toLowerCase();
        final Set<Integer> finalSeenLines = seenLines;
        final List<Integer> finalLineNumbers = lineNumbers;
        final Document finalDocument = document;
        final String fileName = psiFile.getName();

        ((PsiJavaFile) psiFile).accept(new JavaRecursiveElementVisitor() {
            @Override
            public void visitReferenceElement(PsiJavaCodeReferenceElement reference) {
                super.visitReferenceElement(reference);
                
                PsiElement resolved = reference.resolve();
                if (resolved instanceof PsiClass) {
                    PsiClass psiClass = (PsiClass) resolved;
                    String qualifiedName = psiClass.getQualifiedName();
                    if (qualifiedName != null && matchesNamespace(qualifiedName, targetNamespace)) {
                        addLineIfNotSeen(reference, finalDocument, finalSeenLines, finalLineNumbers, fileName, mappingNode);
                    }
                }
            }

            @Override
            public void visitMethodCallExpression(PsiMethodCallExpression expression) {
                super.visitMethodCallExpression(expression);
                
                PsiMethod method = expression.resolveMethod();
                if (method != null) {
                    PsiClass containingClass = method.getContainingClass();
                    if (containingClass != null) {
                        String qualifiedName = containingClass.getQualifiedName();
                        if (qualifiedName != null && matchesNamespace(qualifiedName, targetNamespace)) {
                            addLineIfNotSeen(expression, finalDocument, finalSeenLines, finalLineNumbers, fileName, mappingNode);
                        }
                    }
                }
            }

            @Override
            public void visitImportStatement(PsiImportStatement statement) {
                super.visitImportStatement(statement);
                
                if (statement.isOnDemand()) {
                    String importQualifiedName = statement.getQualifiedName();
                    if (importQualifiedName != null && matchesNamespace(importQualifiedName, targetNamespace)) {
                        addLineIfNotSeen(statement, finalDocument, finalSeenLines, finalLineNumbers, fileName, mappingNode);
                    }
                } else {
                    String importQualifiedName = statement.getQualifiedName();
                    if (importQualifiedName != null) {
                        // For single imports, check if the class or its package matches
                        if (matchesNamespace(importQualifiedName, targetNamespace)) {
                            addLineIfNotSeen(statement, finalDocument, finalSeenLines, finalLineNumbers, fileName, mappingNode);
                        } else {
                            // Check package part
                            int lastDot = importQualifiedName.lastIndexOf('.');
                            if (lastDot > 0) {
                                String packageName = importQualifiedName.substring(0, lastDot);
                                if (matchesNamespace(packageName, targetNamespace)) {
                                    addLineIfNotSeen(statement, finalDocument, finalSeenLines, finalLineNumbers, fileName, mappingNode);
                                }
                            }
                        }
                    }
                }
            }

            // Skip comments and string literals
            @Override
            public void visitComment(PsiComment comment) {
                // Skip comments
            }

            @Override
            public void visitLiteralExpression(PsiLiteralExpression expression) {
                // Skip string literals
            }
        });

        return lineNumbers;
    }

    private String buildFullNamespace(MappingNode mappingNode) {
        if (mappingNode == null || mappingNode.getNamespace() == null || mappingNode.getNamespace().isEmpty()) {
            return null;
        }
        
        // Return the namespace as-is - it may be full (e.g., "org.springframework.security") 
        // or partial (e.g., "access"). The matching logic will handle both cases.
        return mappingNode.getNamespace();
    }

    private boolean matchesNamespace(String qualifiedName, String targetNamespace) {
        if (qualifiedName == null || targetNamespace == null) {
            return false;
        }
        
        String lowerQualifiedName = qualifiedName.toLowerCase();
        String lowerTarget = targetNamespace.toLowerCase();
        
        // Exact match
        if (lowerQualifiedName.equals(lowerTarget)) {
            return true;
        }
        
        // Match if qualified name starts with target namespace (e.g., "org.springframework.security" matches "org.springframework.security.access")
        if (lowerQualifiedName.startsWith(lowerTarget + ".")) {
            return true;
        }
        
        // Match if target namespace appears as a package segment (e.g., "access" matches "org.springframework.security.access")
        // This handles partial namespaces from child nodes
        String[] qualifiedParts = lowerQualifiedName.split("\\.");
        String[] targetParts = lowerTarget.split("\\.");
        
        // Check if target parts appear consecutively in qualified name
        for (int i = 0; i <= qualifiedParts.length - targetParts.length; i++) {
            boolean matches = true;
            for (int j = 0; j < targetParts.length; j++) {
                if (!qualifiedParts[i + j].equals(targetParts[j])) {
                    matches = false;
                    break;
                }
            }
            if (matches) {
                return true;
            }
        }
        
        return false;
    }

    private void addLineIfNotSeen(PsiElement element, Document document, Set<Integer> seenLines, 
                                   List<Integer> lineNumbers, String fileName, MappingNode mappingNode) {
        int line = document.getLineNumber(element.getTextRange().getStartOffset()) + 1;
        if (seenLines.contains(line)) {
            return;
        }

        seenLines.add(line);
        lineNumbers.add(line);

        for (String category : mappingNode.getCategories()) {
            Optional<SecurityClass> existingSecurityClass = SecurityclassUtils.getSecurityClasses()
                    .stream()
                    .filter(existing -> Objects.equals(existing.getName(), category))
                    .findFirst();

            if(existingSecurityClass.isPresent()){
                List<Integer> fileLines = existingSecurityClass.get().occurrences
                        .computeIfAbsent(fileName, k -> new ArrayList<>());
                // Only add if line doesn't already exist to prevent duplicates
                if (!fileLines.contains(line)) {
                    fileLines.add(line);
                }
            } else {
                SecurityClass securityClass =
                        new SecurityClass(category, new HashMap<>());

                securityClass.occurrences
                        .computeIfAbsent(fileName, k -> new ArrayList<>())
                        .add(line);

                SecurityclassUtils.addSecurityClass(securityClass);
            }
        }
    }
}
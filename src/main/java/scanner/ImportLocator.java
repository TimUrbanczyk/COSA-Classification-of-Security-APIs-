package scanner;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import org.yaml.snakeyaml.util.Tuple;

import java.util.*;

public class ImportLocator {

    public static HashMap<String, Map<String,List<Integer>>> locateImports(Project project){

        List<Tuple<PsiFile,PsiImportStatement[]>> fileWrapperList = ImportFetcher.fetchImports(project);

        HashMap<String, Map<String,List<Integer>>> linesMap = new HashMap<>();

        for(Tuple<PsiFile,PsiImportStatement[]> tuple : fileWrapperList) {
            PsiFile psiFile = tuple._1();
            Map<String,List<Integer>> map = new HashMap<>();

            for (PsiImportStatement psiImportStatement : tuple._2()) {
                List<Integer> lines = new ArrayList<>();

                String importQualifiedName = psiImportStatement.getQualifiedName();

                if (importQualifiedName == null) {
                    continue;
                }
                String finalImportQualifiedName = importQualifiedName;
                psiFile.accept(new JavaRecursiveElementVisitor() {
                    @Override
                    public void visitReferenceElement(PsiJavaCodeReferenceElement reference) {
                        super.visitReferenceElement(reference);
                        PsiElement resolved = reference.resolve();

                        if (resolved instanceof PsiClass psiClass) {
                            String qualifiedName = psiClass.getQualifiedName();
                            if (finalImportQualifiedName.equals(qualifiedName)) {
                                int line = getLineNumber(reference,psiFile);
                                lines.add(line+1);
                            }
                        }
                    }
                });
                linesMap.put(psiFile.getName(),map);
                map.put(importQualifiedName, lines);
            }


        }
        return linesMap;
    }

    private static int getLineNumber(PsiElement psiImportStatement, PsiFile psiFile){
        Document document = PsiDocumentManager.getInstance(psiImportStatement.getProject()).getDocument(psiFile);
        return document != null ? document.getLineNumber(psiImportStatement.getTextOffset()) : -1;
    }

}

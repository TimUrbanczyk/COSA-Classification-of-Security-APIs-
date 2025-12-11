package scanner;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import data.MappingNode;
import java.util.ArrayList;
import java.util.List;

public class MappingLocator {

    public List<Integer> locateMapping(MappingNode mappingNode, PsiElement fileAsPsiElement) {
        List<Integer> lineNumbers = new ArrayList<>();
        PsiFile psiFile = fileAsPsiElement.getContainingFile();

        if (psiFile == null) return lineNumbers;

        Document document = PsiDocumentManager.getInstance(psiFile.getProject()).getDocument(psiFile);
        if (document == null) return lineNumbers;

        PsiTreeUtil.processElements(psiFile, element -> {
            String text = element.getText();
            String target = mappingNode.getNamespace();
            if (text.contains(target)) {
                TextRange range = element.getTextRange();
                int line = document.getLineNumber(range.getStartOffset()) + 1;
                if(!target.isEmpty()){
                    System.out.println("Found '" + target + "' at line " + line +"   " + mappingNode.getCategories() );
                }
            }
            return true;
        });
        return lineNumbers;
    }
}


package service;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;

public class CommentGeneratorService  {

    public void markSecurityAPI(
            String securityApi,
            String commentString,
            Project project,
            PsiFile psiFile){

        if (psiFile == null) return;

        PsiElementFactory factory = JavaPsiFacade.getElementFactory(project);
        PsiComment comment = factory.createCommentFromText("//[" + commentString +"]", null);

        WriteCommandAction.runWriteCommandAction(project, () -> {
            PsiElement desiredElement = findChildByText(psiFile,securityApi);
            if (desiredElement == null){
                return;
            }
            psiFile.addAfter(comment, desiredElement);
        });
    }

    private PsiElement findChildByText(PsiElement parent, String text) {
        if (text.equals(parent.getText())) {
            return parent;
        }
        for (PsiElement child : parent.getChildren()) {
            PsiElement result = findChildByText(child, text);
            if (result != null) return result;
        }
        return null;
    }

}

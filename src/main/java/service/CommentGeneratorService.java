package service;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;

public class CommentGeneratorService  {

    public void markSecurityAPI(
            String securityApi,
            String commentString,
            Project project,
            PsiFile psiFile) {

        if (psiFile == null) return;

        WriteCommandAction.runWriteCommandAction(project, () -> {
            PsiElement desiredElement = findChildByText(psiFile, securityApi);
            if (desiredElement == null) {
                return;
            }

            // find complete statement (important!)
            PsiStatement statement = PsiTreeUtil.getParentOfType(desiredElement, PsiStatement.class);
            if (statement == null) {
                return;
            }

            addCommentAfter(statement, commentString, project);
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


    private void addCommentAfter(PsiElement element, String comment, Project project) {

        PsiElement parent = element.getParent();

        PsiComment commentNode = PsiElementFactory.getInstance(project)
                .createCommentFromText("//[" + comment + "]", null);

        parent.addAfter(commentNode, element);
    }
}
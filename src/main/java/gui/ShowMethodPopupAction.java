package gui;

import SecurityClass.SecurityClass;
import SecurityClass.SecurityclassUtils;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiReferenceExpression;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ShowMethodPopupAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        if(e.getProject() == null) {
            return;
        }

        String methodQualifiedName = null;
        int lineNumber = 0;
        Editor editor = e.getData(com.intellij.openapi.actionSystem.CommonDataKeys.EDITOR);
        PsiFile psiFile = e.getData(com.intellij.openapi.actionSystem.CommonDataKeys.PSI_FILE);
        if(editor != null && psiFile != null) {
            int offset = editor.getCaretModel().getOffset();
            PsiElement elementAtCaret = psiFile.findElementAt(offset);
            if(elementAtCaret != null) {
                PsiMethodCallExpression methodCall = PsiTreeUtil.getParentOfType(elementAtCaret, PsiMethodCallExpression.class);
                if(methodCall != null) {
                    PsiReferenceExpression methodExpression = methodCall.getMethodExpression();
                    methodQualifiedName = methodExpression.getText();
                    Document document = editor.getDocument();
                    lineNumber = document.getLineNumber(methodCall.getTextOffset()) + 1;
                }
            }
        }

        ClassificationPopUp dialog = new ClassificationPopUp();
        ToolWindow toolWindow = ToolWindow.getInstance();
        toolWindow.setCurrentDialog(dialog);

        if(dialog.showAndGet()){
            String textFieldValue = dialog.getTextFieldValue();
            if (textFieldValue == null || textFieldValue.trim().isEmpty()) {
                return;
            }
            
            String securityClassName = textFieldValue.trim();
            List<Integer> lineNumbers = new ArrayList<>();
            
            if (lineNumber > 0) {
                lineNumbers.add(lineNumber);
            } else if (editor != null && psiFile != null) {
                int offset = editor.getCaretModel().getOffset();
                Document document = editor.getDocument();
                lineNumbers.add(document.getLineNumber(offset) + 1);
            }

            HashMap<String, List<Integer>> occurences = new HashMap<>();
            occurences.put(psiFile.getName(), new ArrayList<>(lineNumbers));
            SecurityClass securityClass = new SecurityClass(securityClassName, occurences);
            boolean foundExisting = false;
            for(SecurityClass sc : SecurityclassUtils.getSecurityClasses()){
                if(securityClass.getName().equals(sc.getName())){
                    for(Integer line : lineNumbers){
                        if (!sc.getOccurrences().containsKey(psiFile.getName())) {
                            sc.getOccurrences().put(psiFile.getName(), new ArrayList<>());
                        }
                        if (!sc.getOccurrences().get(psiFile.getName()).contains(line)) {
                            sc.getOccurrences().get(psiFile.getName()).add(line);
                        }
                    }
                    foundExisting = true;
                    break;
                }
            }
            if (!foundExisting) {
                SecurityclassUtils.addSecurityClass(securityClass);
            }
            if (toolWindow != null) {
                toolWindow.refreshTable();
            }
        }

    }


    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(true);
    }
}
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Action that shows a popup for classifying a method call under a security class.
 */
public class ShowMethodPopupAction extends AnAction {
    /**
     * Performs the action: identifies the method at the caret and opens the classification dialog.
     *
     * @param e the action event
     */
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
            assert psiFile != null;
            occurences.put(psiFile.getName(), new ArrayList<>(lineNumbers));
            SecurityClass securityClass = new SecurityClass(securityClassName, occurences);
            for(SecurityClass sc : SecurityclassUtils.getSecurityClasses()){
                if(securityClass.name().equals(sc.name())){
                    for(Integer line : lineNumbers){
                        if (!sc.occurrences().containsKey(psiFile.getName())) {
                            sc.occurrences().put(psiFile.getName(), new ArrayList<>());
                        }
                        if (!sc.occurrences().get(psiFile.getName()).contains(line)) {
                            sc.occurrences().get(psiFile.getName()).add(line);
                        }
                    }
                    break;
                }
            }
            SecurityclassUtils.addSecurityClass(securityClass);
            for (Integer line : lineNumbers) {
                SecurityclassUtils.addMatchDetail(psiFile.getName(), line, methodQualifiedName != null ? methodQualifiedName : securityClassName);
            }

            toolWindow.refreshTable();
        }

    }


    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(true);
    }
}
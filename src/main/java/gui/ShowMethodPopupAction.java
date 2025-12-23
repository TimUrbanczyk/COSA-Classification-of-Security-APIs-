package gui;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

public class ShowMethodPopupAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        String className = Messages.showInputDialog(
                e.getProject(),
                "Enter classification",
                "Classify API - COSA",
                Messages.getQuestionIcon()
        );
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(true);
    }
}
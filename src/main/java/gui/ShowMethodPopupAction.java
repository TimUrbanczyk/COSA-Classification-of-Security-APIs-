package gui;


import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.wm.ToolWindowManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class ShowMethodPopupAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        if(e.getProject() == null) {
            return;
        }

        ClassificationPopUp dialog = new ClassificationPopUp();
        ToolWindow toolWindow = ToolWindow.getInstance();
        toolWindow.setCurrentDialog(dialog);

        if(dialog.showAndGet()){
            String text = dialog.getTextFieldValue();

        }

    }


    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(true);
    }
}
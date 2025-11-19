package gui;

import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.vfs.VirtualFile;
import data.SecurityClass;

import javax.swing.*;
import java.awt.*;
import java.nio.charset.StandardCharsets;

public class PopUp {

    public void showPopup(Editor editor) {
        SelectionModel selectionModel = editor.getSelectionModel();
        String selectedText = selectionModel.getSelectedText();
        if (selectedText == null || selectedText.isEmpty()) return;

        Project project = editor.getProject();
        if (project == null) return;

        JPanel popUpPanel = new JPanel();
        popUpPanel.setLayout(new BoxLayout(popUpPanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("Add Selected API to Class");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14f));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        popUpPanel.add(titleLabel);

        JLabel selectedLabel = new JLabel("Selected API: " + selectedText);
        selectedLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        popUpPanel.add(selectedLabel);

        final JBPopup[] popupRef = new JBPopup[1];

        for (SecurityClass securityClass : SecurityClass.getSecurityClasses()) {
            JButton btn = new JButton(securityClass.getName());
            btn.setAlignmentX(Component.LEFT_ALIGNMENT);
            btn.addActionListener(e -> {
                addToJson(project, securityClass.getName(), selectedText);
                if (popupRef[0] != null) popupRef[0].cancel();
            });
            popUpPanel.add(btn);
        }

        popUpPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JBPopup popUp = JBPopupFactory.getInstance()
                .createComponentPopupBuilder(popUpPanel, null)
                .setResizable(false)
                .setMovable(true)
                .createPopup();

        popupRef[0] = popUp;
        popUp.showInBestPositionFor(editor);
    }
    private void addToJson(Project project, String className, String api) {
        System.out.println(className+ " " + api);
    }
}
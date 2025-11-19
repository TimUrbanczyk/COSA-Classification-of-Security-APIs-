package gui;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import data.SecurityClass;

import javax.swing.*;

public class PopUp {

    public void showPopup(Editor editor){

        SelectionModel selectionModel = editor.getSelectionModel();
        String selectedText = selectionModel.getSelectedText();

        JPanel popUpPanel = new JPanel();
        JLabel label = new JLabel(selectedText);
        for(SecurityClass securityClass : SecurityClass.getSecurityClasses()){
            popUpPanel.add(new JButton(securityClass.getName()));
        }




        JBPopup classifyPopUp = JBPopupFactory.getInstance().createComponentPopupBuilder(popUpPanel,label).createPopup();
        classifyPopUp.showInBestPositionFor(editor);
    }



}
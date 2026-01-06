package gui;

import SecurityClass.SecurityClass;
import SecurityClass.SecurityclassUtils;
import com.intellij.openapi.ui.DialogWrapper;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

import java.util.List;

public class ClassificationPopUp extends DialogWrapper {
    private List<SecurityClass> securityclasses = SecurityclassUtils.getSecurityClasses();
    private JTextField textField;

    public ClassificationPopUp(){
        super(true);
        setTitle("Classify API- COSA");
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        JPanel textPanel = new JPanel(new BorderLayout(5, 5));
        JLabel label = new JLabel("Enter Lines:");
        textField = new JTextField(20);
        List<String> securityClassNames = securityclasses.stream().map(SecurityClass::getName).toList();
        AutoCompleteDecorator.decorate(textField, securityClassNames ,false);
        textPanel.add(label, BorderLayout.WEST);
        textPanel.add(textField, BorderLayout.CENTER);

        panel.add(Box.createVerticalStrut(10));
        panel.add(textPanel);

        textPanel.revalidate();
        return panel;
    }

    public String getTextFieldValue(){
        return textField.getText();
    }


    private void syncSecurityclasses(){
        securityclasses = SecurityclassUtils.getSecurityClasses();
        

    }

    public void refresh(){
        syncSecurityclasses();
    }
}

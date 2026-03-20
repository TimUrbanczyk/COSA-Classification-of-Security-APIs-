package gui;

import SecurityClass.SecurityClass;
import SecurityClass.SecurityclassUtils;
import com.intellij.openapi.ui.DialogWrapper;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

import java.util.List;

/**
 * Dialog for classifying a security API usage.
 */
public class ClassificationPopUp extends DialogWrapper {
    private static List<SecurityClass> securityclasses = SecurityclassUtils.getSecurityClasses();
    private JTextField textField;

    /**
     * Constructs a new ClassificationPopUp.
     */
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
        JLabel label = new JLabel("Enter SC:");
        textField = new JTextField(20);
        List<String> securityClassNames = securityclasses.stream().map(SecurityClass::name).toList();
        AutoCompleteDecorator.decorate(textField, securityClassNames ,false);
        textPanel.add(label, BorderLayout.WEST);
        textPanel.add(textField, BorderLayout.CENTER);

        panel.add(Box.createVerticalStrut(10));
        panel.add(textPanel);

        textPanel.revalidate();
        return panel;
    }

    /**
     * Returns the value entered in the classification text field.
     *
     * @return the text field content
     */
    public String getTextFieldValue(){
        return textField.getText();
    }


    private static void syncSecurityclasses(){
        securityclasses = SecurityclassUtils.getSecurityClasses();

    }

    /**
     * Refreshes the internal list of security classes from the global utility.
     */
    public static void refresh(){
        syncSecurityclasses();
    }
}

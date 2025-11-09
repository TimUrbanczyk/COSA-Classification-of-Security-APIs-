package gui;

import service.CommentGeneratorService;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.psi.PsiManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import data.SecurityAPILoader;
import data.SecurityClass;
import scanner.FileReader;
import javax.swing.*;
import java.io.IOException;

public class ToolWindow implements ToolWindowFactory, DumbAware {

    private final FileReader fileReader = new FileReader();
    private final SecurityAPILoader securityAPILoader = new SecurityAPILoader();
    private final CommentGeneratorService commentGeneratorService = new CommentGeneratorService();

    @Override
    public void createToolWindowContent(Project project, com.intellij.openapi.wm.ToolWindow toolwindow) {
        JPanel panel = new JPanel();
        JButton buttonRead = new JButton("Read Current File");
        JButton buttonShowSecurityClasses = new JButton("Show security classes");
        JButton buttonMarkSecurityAPIS = new JButton("Locate & Mark security apis");

        buttonMarkSecurityAPIS.addActionListener(e->{
            for(SecurityClass securityClass: SecurityClass.getSecurityClasses()){
                String sc = securityClass.getName();
                for(String securityAPIName : securityClass.getApis()){
                    commentGeneratorService.markSecurityAPI(
                            securityAPIName,
                            sc,
                            project,
                            PsiManager.getInstance(project).findFile(getCurrentFile(project)));
                }
            }

        });

        buttonShowSecurityClasses.addActionListener( e-> {
            JOptionPane.showMessageDialog(null,
                    SecurityClass.ListToString(),
                    "SecurityClassList",
                    JOptionPane.INFORMATION_MESSAGE) ;
        });


        buttonRead.addActionListener(e -> {
            try{
                String currentFileContent = fileReader.readFileAsString(getCurrentFile(project));
                JOptionPane.showMessageDialog(
                        null,
                        currentFileContent,
                        "IDK...TEST...FORNOW",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex){
                ex.printStackTrace();
            }
        });

        panel.add(buttonRead);
        panel.add(buttonShowSecurityClasses);
        panel.add(buttonMarkSecurityAPIS);
        ContentFactory contentFactory = ContentFactory.getInstance();
        Content content = contentFactory.createContent(panel, "", false);
        toolwindow.getContentManager().addContent(content);
    }

    private VirtualFile getCurrentFile(Project project){
        FileEditorManager manager = FileEditorManager.getInstance(project);
        VirtualFile[] files = manager.getSelectedFiles();
        return files[0];
    }


}
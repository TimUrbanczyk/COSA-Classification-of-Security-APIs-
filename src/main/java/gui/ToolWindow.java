package gui;

import com.d.X.C.C.d.M;
import com.intellij.openapi.editor.Editor;
import data.MappingLoader;
import data.MappingNode;
import kotlinx.html.S;
import scanner.ImportLocator;
import service.CommentGeneratorService;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import scanner.FileReader;
import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ToolWindow implements ToolWindowFactory, DumbAware {

    private final FileReader fileReader = new FileReader();
    private final CommentGeneratorService commentGeneratorService = new CommentGeneratorService();
    private final PopUp popUp = new PopUp();
    private final MappingLoader mappingLoader = new MappingLoader();
    private final List<MappingNode> allMappingNodes = mappingLoader.loadAllMappings();

    @Override
    public void createToolWindowContent(Project project, com.intellij.openapi.wm.ToolWindow toolwindow) {

        JPanel panel = new JPanel();
        JButton buttonRead = new JButton("Read Current File");
        JButton buttonMarkSecurityAPIS = new JButton("Locate & Mark security apis");
        JButton buttonClassifySecurityAPI = new JButton("Classify current selected API");

        buttonClassifySecurityAPI.addActionListener(e->{
            Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
            popUp.showPopup(editor);
        });


        buttonMarkSecurityAPIS.addActionListener(e->{

            List<List<String>> allNamespaces = new ArrayList<>();
            for(MappingNode mappingNode : allMappingNodes){
                allNamespaces.add(mappingLoader.getNamespaces(mappingNode, new ArrayList<>()));

            }

            System.out.println(allNamespaces.toString());


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
        panel.add(buttonMarkSecurityAPIS);
        panel.add(buttonClassifySecurityAPI);
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
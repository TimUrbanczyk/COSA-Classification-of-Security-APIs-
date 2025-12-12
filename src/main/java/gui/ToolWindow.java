package gui;

import SecurityClass.SecurityclassUtils;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import data.MappingLoader;
import data.MappingNode;
import psi.PsiUtils;
import scanner.MappingLocator;
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
import java.util.ArrayList;
import java.util.List;

public class ToolWindow implements ToolWindowFactory, DumbAware {

    private final FileReader fileReader = new FileReader();
    private final CommentGeneratorService commentGeneratorService = new CommentGeneratorService();
    private final PopUp popUp = new PopUp();
    private final MappingLoader mappingLoader = new MappingLoader();
    private final List<MappingNode> allParendMappingNodes = mappingLoader.loadAllParentMappings();
    private final MappingLocator mappingLocator = new MappingLocator();

    @Override
    public void createToolWindowContent(Project project, com.intellij.openapi.wm.ToolWindow toolwindow) {

        JPanel panel = new JPanel();
        JButton buttonRead = new JButton("Read Current File");
        JButton buttonMarkSecurityAPIS = new JButton("Locate & Mark security apis");
        JButton buttonClassifySecurityAPI = new JButton("Classify current selected API");


        buttonMarkSecurityAPIS.addActionListener(e->{

            List<MappingNode> allMappingNodes = new ArrayList<>();

            for(MappingNode mappingNode: allParendMappingNodes){
                System.out.println(mappingLoader.getAllChildMappings(mappingNode,new ArrayList<>()).toString());
                allMappingNodes.addAll(mappingLoader.getAllChildMappings(mappingNode, new ArrayList<>()));
            }

            PsiElement currentPsiElement = PsiUtils.getPsiFile(project,getCurrentFile(project));
            for(MappingNode mappingNode : allMappingNodes){
                List<Integer> occurences = mappingLocator.locateMapping(mappingNode,currentPsiElement);

            }

            System.out.println(SecurityclassUtils.getSecurityClasses().toString());


        });


        buttonRead.addActionListener(e -> {
            PsiUtils.getPsiFile(project, getCurrentFile(project));
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
package gui;

import SecurityClass.SecurityClass;
import SecurityClass.SecurityclassUtils;
import com.intellij.psi.PsiElement;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import data.MappingLoader;
import data.MappingNode;
import psi.PsiUtils;
import scanner.MappingLocator;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ToolWindow implements ToolWindowFactory, DumbAware {
    /*
    private final FileReader fileReader = new FileReader();
    private final CommentGeneratorService commentGeneratorService = new CommentGeneratorService();
    private final PopUp popUp = new PopUp();
     */
    private final MappingLoader mappingLoader = new MappingLoader();
    private final List<MappingNode> allParendMappingNodes = mappingLoader.loadAllParentMappings();
    private final MappingLocator mappingLocator = new MappingLocator();

    @Override
    public void createToolWindowContent(Project project, com.intellij.openapi.wm.ToolWindow toolwindow) {

        JPanel panel = new JPanel();
        JButton buttonRead = new JButton("Read Current File");
        JButton buttonMarkSecurityAPIS = new JButton("Locate & Mark security apis");
        JButton buttonClassifySecurityAPI = new JButton("Classify current selected API");
        JTable tableSecurityClasses = createTable(SecurityclassUtils.getSecurityClasses());

        buttonMarkSecurityAPIS.addActionListener(e->{
            List<MappingNode> allMappingNodes = new ArrayList<>();

            for(MappingNode mappingNode: allParendMappingNodes){
                //System.out.println(mappingLoader.getAllChildMappings(mappingNode,new ArrayList<>()).toString());
                allMappingNodes.addAll(mappingLoader.getAllChildMappings(mappingNode, new ArrayList<>()));
            }

            PsiElement currentPsiElement = PsiUtils.getPsiFile(project,getCurrentFile(project));
            for(MappingNode mappingNode : allMappingNodes){
                List<Integer> occurences = mappingLocator.locateMapping(mappingNode,currentPsiElement);
            }

            DefaultTableModel model = (DefaultTableModel) tableSecurityClasses.getModel();
            model.setRowCount(0);
            for(SecurityClass securityClass : SecurityclassUtils.getSecurityClasses()){


                model.addRow(new Object[]{
                        securityClass.getName(),
                        "COMING SOON"
                });
            }

        });

        buttonRead.addActionListener(e -> {
            PsiUtils.getPsiFile(project, getCurrentFile(project));
        });

        panel.add(buttonMarkSecurityAPIS);
        panel.add(buttonClassifySecurityAPI);
        JScrollPane tableScrollPane = new JBScrollPane(tableSecurityClasses);
        tableScrollPane.setPreferredSize(new Dimension(toolwindow.getComponent().getWidth(), toolwindow.getComponent().getHeight()));
        panel.add(tableScrollPane);
        ContentFactory contentFactory = ContentFactory.getInstance();
        System.out.println(panel.getHeight()+" "+panel.getWidth());
        Content content = contentFactory.createContent(panel, "", false);
        toolwindow.getContentManager().addContent(content);
    }
    private JTable createTable(List<SecurityClass> data){
        String[] columns = {"Securityclass","Lines"};

        DefaultTableModel model = new DefaultTableModel(columns,0){
            @Override
            public boolean isCellEditable(int row, int column){
                return false;
            }
        };

        for(SecurityClass securityClass : data){
            int lineCount = securityClass.getOccurrences()
                    .values()
                    .stream()
                    .mapToInt(List::size)
                    .sum();

            model.addRow(new Object[]{
                    securityClass.getName(),
                    lineCount
            });
        }
        JTable table = new JBTable(model);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        return table;
    }

    private VirtualFile getCurrentFile(Project project){
        FileEditorManager manager = FileEditorManager.getInstance(project);
        VirtualFile[] files = manager.getSelectedFiles();
        return files[0];
    }


}
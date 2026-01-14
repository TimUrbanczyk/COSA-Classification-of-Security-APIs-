package gui;

import SecurityClass.SecurityClass;
import SecurityClass.SecurityclassUtils;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.psi.PsiElement;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import data.MappingLoader;
import data.MappingNode;
import lombok.Getter;
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
import java.util.*;
import java.util.List;

public class ToolWindow implements ToolWindowFactory, DumbAware {

    private final MappingLoader mappingLoader = new MappingLoader();
    private final List<MappingNode> allParendMappingNodes = mappingLoader.loadAllParentMappings();
    private final MappingLocator mappingLocator = new MappingLocator();
    private ClassificationPopUp currentDialog;
    private JTable tableSecurityClasses;
    private List<VirtualFile> projectFiles = new ArrayList<>();

    @Getter
    private JPanel panel;

    @Getter
    private static ToolWindow instance;

    @Override
    public void createToolWindowContent(Project project, com.intellij.openapi.wm.ToolWindow toolwindow) {

        ToolWindow.instance = this;
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JButton buttonMarkSecurityApisSingleFile = new JButton("Mark apis in File");
        JButton buttonMarkSecurityApisProjekt= new JButton("Mark apis in Projekt");
        tableSecurityClasses = createTable(SecurityclassUtils.getSecurityClasses());
        
        // Create a horizontal panel for the buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(buttonMarkSecurityApisSingleFile);
        buttonPanel.add(buttonMarkSecurityApisProjekt);

        buttonMarkSecurityApisSingleFile.addActionListener(e->{
            List<MappingNode> allMappingNodes = new ArrayList<>();
            for(MappingNode mappingNode: allParendMappingNodes){
                allMappingNodes.addAll(mappingLoader.getAllChildMappings(mappingNode, new ArrayList<>()));
            }

            PsiElement currentPsiElement = PsiUtils.getPsiFile(project,getCurrentFile(project));
            for(MappingNode mappingNode : allMappingNodes){
                 mappingLocator.locateMapping(mappingNode,currentPsiElement);
            }

            refreshTable();
            if (currentDialog != null) {
                currentDialog.refresh();
            }


        });

        buttonMarkSecurityApisProjekt.addActionListener(e->{
            List<MappingNode> allMappingNodes = new ArrayList<>();
            for(MappingNode mappingNode: allParendMappingNodes){
                allMappingNodes.addAll(mappingLoader.getAllChildMappings(mappingNode, new ArrayList<>()));
            }

            projectFiles.clear(); // Clear previous files to prevent duplicates
            findProjectFiles(project);
            for(VirtualFile virtualFile : projectFiles) {
                PsiElement currentPsiElement = PsiUtils.getPsiFile(project, virtualFile);
                for (MappingNode mappingNode : allMappingNodes) {
                    mappingLocator.locateMapping(mappingNode, currentPsiElement);
                }
            }

            refreshTable();
            if (currentDialog != null) {
                currentDialog.refresh();
            }


        });

        panel.add(buttonPanel);
        JScrollPane tableScrollPane = new JBScrollPane(tableSecurityClasses);
        tableScrollPane.setPreferredSize(new Dimension(toolwindow.getComponent().getWidth(), toolwindow.getComponent().getHeight()));
        panel.add(tableScrollPane);
        ContentFactory contentFactory = ContentFactory.getInstance();
        Content content = contentFactory.createContent(panel, "", false);
        toolwindow.getContentManager().addContent(content);
    }

    public void refreshTable(){
        if (tableSecurityClasses == null) {
            return;
        }
        DefaultTableModel model = (DefaultTableModel) tableSecurityClasses.getModel();
        model.setRowCount(0);
        for(SecurityClass securityClass : SecurityclassUtils.getSecurityClasses()){
            // Iterate through all files and all line numbers to show all occurrences
            for(Map.Entry<String, List<Integer>> entry : securityClass.getOccurrences().entrySet()){
                String fileName = entry.getKey();
                for(Integer lineNumber : entry.getValue()){
                    model.addRow(new Object[]{
                            securityClass.getName(),
                            fileName,
                            lineNumber
                    });
                }
            }
        }
        model.fireTableDataChanged();
    }

    public void setCurrentDialog(ClassificationPopUp dialog) {
        this.currentDialog = dialog;
        if (currentDialog != null) {
            currentDialog.refresh();
        }
    }


    private JTable createTable(List<SecurityClass> data){
        String[] columns = {"Securityclass","Filename","line/row"};

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
                    securityClass.getOccurrences().keySet().stream().findFirst().get(),
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

    private void findProjectFiles(Project project){
        VirtualFile[] roots = ProjectRootManager.getInstance(project).getContentSourceRoots();
        for(VirtualFile root : roots){
            traverseFiles(root);
        }
    }

    private void traverseFiles(VirtualFile file){
        System.out.println(file);
        if(!file.isDirectory() && !projectFiles.contains(file)){
            projectFiles.add(file);
        }
        for(VirtualFile child: file.getChildren()){
            traverseFiles(child);
        }
    }




}
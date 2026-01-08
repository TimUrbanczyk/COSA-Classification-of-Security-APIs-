package gui;

import SecurityClass.SecurityClass;
import SecurityClass.SecurityclassUtils;
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
    @Getter
    private JPanel panel;

    @Getter
    private static ToolWindow instance;

    @Override
    public void createToolWindowContent(Project project, com.intellij.openapi.wm.ToolWindow toolwindow) {

        ToolWindow.instance = this;
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JButton buttonMarkSecurityAPIS = new JButton("Locate & Mark security apis");
        tableSecurityClasses = createTable(SecurityclassUtils.getSecurityClasses());

        buttonMarkSecurityAPIS.addActionListener(e->{
            VirtualFile currentFile = getCurrentFile(project);
            if (currentFile == null) {
                return;
            }

            List<MappingNode> allMappingNodes = new ArrayList<>();

            for(MappingNode mappingNode: allParendMappingNodes){
                allMappingNodes.addAll(mappingLoader.getAllChildMappings(mappingNode, new ArrayList<>()));
            }

            PsiElement currentPsiElement = PsiUtils.getPsiFile(project, currentFile);
            for(MappingNode mappingNode : allMappingNodes){
                 mappingLocator.locateMapping(mappingNode, currentPsiElement);
            }

            refreshTable();
            if (currentDialog != null) {
                currentDialog.refresh();
            }


        });


        panel.add(buttonMarkSecurityAPIS);
        JScrollPane tableScrollPane = new JBScrollPane(tableSecurityClasses);
        tableScrollPane.setPreferredSize(new Dimension(toolwindow.getComponent().getWidth(), toolwindow.getComponent().getHeight()));
        panel.add(tableScrollPane);
        ContentFactory contentFactory = ContentFactory.getInstance();
        Content content = contentFactory.createContent(panel, "", false);
        toolwindow.getContentManager().addContent(content);
    }

    public void populateTable(int with, int height){
        refreshTable();
    }

    public void refreshTable(){
        if (tableSecurityClasses == null) {
            return;
        }
        DefaultTableModel model = (DefaultTableModel) tableSecurityClasses.getModel();
        model.setRowCount(0);
        for(SecurityClass securityClass : SecurityclassUtils.getSecurityClasses()){

            model.addRow(new Object[]{
                    securityClass.getName(),
                    Arrays.toString(
                            securityClass.getOccurrences()
                                    .values()
                                    .stream()
                                    .flatMap(List::stream)
                                    .distinct()
                                    .mapToInt(Integer::intValue)
                                    .toArray())
            });

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
        String[] columns = {"Securityclass","Ocurrencess"};

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
        if (files == null || files.length == 0) {
            return null;
        }
        return files[0];
    }


}
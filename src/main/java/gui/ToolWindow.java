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
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.*;
import java.util.List;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;

public class ToolWindow implements ToolWindowFactory, DumbAware {

    private final MappingLoader mappingLoader = new MappingLoader();
    private final List<MappingNode> allParendMappingNodes = mappingLoader.loadAllParentMappings();
    private final MappingLocator mappingLocator = new MappingLocator();
    private ClassificationPopUp currentDialog;
    private JTable tableSecurityClasses;
    private List<VirtualFile> projectFiles = new ArrayList<>();
    private Integer currentSortColumn = null;

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
        JButton buttonMarkSecurityApisProjekt = new JButton("Mark apis in Projekt");
        JButton buttonAnnotateApis = new JButton("Annotate apis");
        JButton buttonSortByFile = new JButton("Sort by Filename");
        JButton buttonSortBySecurityClass = new JButton("Sort by SC name");
        JButton buttonClearTable = new JButton("Clear Table");
        tableSecurityClasses = createTable(SecurityclassUtils.getSecurityClasses());
        addTableClickListener(project);
        
        JPanel buttonPanel = new JPanel();
        List<JButton> buttons = List.of(
                buttonMarkSecurityApisSingleFile,
                buttonMarkSecurityApisProjekt,
                buttonAnnotateApis,
                buttonSortByFile,
                buttonSortBySecurityClass,
                buttonClearTable
        );
        for (JButton b : buttons) {
            buttonPanel.add(b);
        }
        updateButtonGrid(buttonPanel, buttons, toolwindow.getComponent().getWidth());
        toolwindow.getComponent().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateButtonGrid(buttonPanel, buttons, toolwindow.getComponent().getWidth());
                buttonPanel.revalidate();
                buttonPanel.repaint();
            }
        });


        buttonClearTable.addActionListener(e->{
           DefaultTableModel tableModel = (DefaultTableModel) tableSecurityClasses.getModel();
           tableModel.setRowCount(0);
        });

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

            projectFiles.clear();
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

        buttonAnnotateApis.addActionListener(e -> {
            annotateSecurityApis(project);
        });

        buttonSortByFile.addActionListener(e ->{
            sortTableByColumn(1);
        });

        buttonSortBySecurityClass.addActionListener(e ->{
            sortTableByColumn(0);
        });

        panel.add(buttonPanel);
        JScrollPane tableScrollPane = new JBScrollPane(tableSecurityClasses);
        tableScrollPane.setPreferredSize(new Dimension((int)(toolwindow.getComponent().getWidth() * 1.5), toolwindow.getComponent().getHeight()));
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
            for(Map.Entry<String, List<Integer>> entry : securityClass.getOccurrences().entrySet()){
                String fileName = entry.getKey();
                for(Integer lineNumber : entry.getValue()){
                    model.addRow(new Object[]{
                            securityClass.getName(),
                            fileName,
                            lineNumber,
                            SecurityclassUtils.getMatchDetail(fileName, lineNumber)
                    });
                }
            }
        }
        model.fireTableDataChanged();
        if (currentSortColumn != null) {
            sortTableByColumn(currentSortColumn);
        }
    }

    private void sortTableByColumn(int column){
        currentSortColumn = column;
        DefaultTableModel model = (DefaultTableModel) tableSecurityClasses.getModel();
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        tableSecurityClasses.setRowSorter(sorter);
        sorter.setSortKeys(List.of(new RowSorter.SortKey(column, SortOrder.ASCENDING)));
    }

    public void setCurrentDialog(ClassificationPopUp dialog) {
        this.currentDialog = dialog;
        if (currentDialog != null) {
            currentDialog.refresh();
        }
    }


    private JTable createTable(List<SecurityClass> data){
        String[] columns = {"Securityclass","Filename","line/row","Matched"};

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
                    lineCount,
                    ""
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
        System.out.println(Arrays.stream(roots).map(VirtualFile::toString));
        for(VirtualFile root : roots){
            System.out.println(root);
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

    private void addTableClickListener(Project project) {
        tableSecurityClasses.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = tableSecurityClasses.rowAtPoint(evt.getPoint());
                if (row >= 0) {
                    String fileName = (String) tableSecurityClasses.getValueAt(row, 1);
                    Integer lineNumber = (Integer) tableSecurityClasses.getValueAt(row, 2);

                    navigateToLine(project, fileName, lineNumber);
                }
            }
        });
    }

    private void navigateToLine(Project project, String fileName, int lineNumber) {
        VirtualFile targetFile = findFileByName(project, fileName);

        if (targetFile != null) {
            FileEditorManager editorManager = FileEditorManager.getInstance(project);
            editorManager.openFile(targetFile, true);

            com.intellij.openapi.editor.Editor editor = editorManager.getSelectedTextEditor();
            if (editor != null && lineNumber > 0) {
                int offset = editor.getDocument().getLineStartOffset(lineNumber - 1);
                editor.getCaretModel().moveToOffset(offset);
                editor.getScrollingModel().scrollToCaret(com.intellij.openapi.editor.ScrollType.CENTER);
            }
        }
    }

    private VirtualFile findFileByName(Project project, String fileName) {
        for (VirtualFile file : projectFiles) {
            if (file.getName().equals(fileName) || file.getPath().endsWith(fileName)) {
                return file;
            }
        }

        findProjectFiles(project);
        for (VirtualFile file : projectFiles) {
            if (file.getName().equals(fileName) || file.getPath().endsWith(fileName)) {
                return file;
            }
        }

        return null;
    }

    private void annotateSecurityApis(Project project) {
        Map<String, Map<Integer, Set<String>>> fileAnnotations = new HashMap<>();

        for (SecurityClass securityClass : SecurityclassUtils.getSecurityClasses()) {
            for (Map.Entry<String, List<Integer>> entry : securityClass.getOccurrences().entrySet()) {
                String fileName = entry.getKey();
                fileAnnotations.putIfAbsent(fileName, new HashMap<>());

                for (Integer lineNumber : entry.getValue()) {
                    fileAnnotations.get(fileName).putIfAbsent(lineNumber, new HashSet<>());
                    fileAnnotations.get(fileName).get(lineNumber).add(securityClass.getName());
                }
            }
        }

        for (Map.Entry<String, Map<Integer, Set<String>>> fileEntry : fileAnnotations.entrySet()) {
            String fileName = fileEntry.getKey();
            Map<Integer, Set<String>> lineAnnotations = fileEntry.getValue();

            VirtualFile virtualFile = findFileByName(project, fileName);
            if (virtualFile != null) {
                addAnnotationsToFile(project, virtualFile, lineAnnotations);
            }
        }
    }

    private void addAnnotationsToFile(Project project, VirtualFile virtualFile, Map<Integer, Set<String>> lineAnnotations) {
        WriteCommandAction.runWriteCommandAction(project, () -> {
            PsiFile psiFile = (PsiFile) PsiUtils.getPsiFile(project, virtualFile);
            if (psiFile == null) {
                return;
            }

            Document document = PsiDocumentManager.getInstance(project).getDocument(psiFile);
            if (document == null) {
                return;
            }

            List<Integer> sortedLines = new ArrayList<>(lineAnnotations.keySet());
            sortedLines.sort(Collections.reverseOrder());

            for (Integer lineNumber : sortedLines) {
                if (lineNumber > 0 && lineNumber <= document.getLineCount()) {
                    Set<String> securityClasses = lineAnnotations.get(lineNumber);
                    String annotation = String.join(", ", securityClasses);

                    int lineStartOffset = document.getLineStartOffset(lineNumber - 1);
                    String lineText = document.getText(
                            new com.intellij.openapi.util.TextRange(
                                    lineStartOffset,
                                    document.getLineEndOffset(lineNumber - 1)
                            )
                    );

                    String indentation = lineText.substring(0, lineText.length() - lineText.trim().length());

                    int lineEndOffset = document.getLineEndOffset(lineNumber - 1);
                    String comment = " //&line [" + annotation + "]";

                    document.insertString(lineEndOffset, comment);
                }
            }

            PsiDocumentManager.getInstance(project).commitDocument(document);
        });
    }

    private void updateButtonGrid(JPanel buttonPanel, List<JButton> buttons, int availableWidth) {
        int maxButtonWidth = 0;
        for (JButton b : buttons) {
            maxButtonWidth = Math.max(maxButtonWidth, b.getPreferredSize().width);
        }
        int hGap = 6;
        int vGap = 6;
        int cols = Math.max(1, (availableWidth - 20) / Math.max(1, (maxButtonWidth + hGap)));
        GridLayout layout = new GridLayout(0, cols, hGap, vGap);
        buttonPanel.setLayout(layout);
    }
}
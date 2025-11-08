import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import scanner.FileReader;
import javax.swing.*;
import java.io.IOException;


public class myToolWindowFactory implements ToolWindowFactory, DumbAware {

    @Override
    public void createToolWindowContent(Project project, ToolWindow toolwindow) {
        JPanel panel = new JPanel();
        JButton button = new JButton("Read Current File");

        button.addActionListener(e -> {
            try{
            FileReader fileReader = new FileReader();
            String currentFileContent = fileReader.readFileAsString(getCurrentFile(project));
            JOptionPane.showMessageDialog(null,currentFileContent);
            } catch (IOException ex){
                ex.printStackTrace();
            }
        });

        panel.add(button);
        ContentFactory contentFactory = ContentFactory.getInstance();
        Content content = contentFactory.createContent(panel, "", false);
        toolwindow.getContentManager().addContent(content);
    }


    private VirtualFile getCurrentFile(Project project){
        FileEditorManager manager = FileEditorManager.getInstance(project);
        VirtualFile[] files = manager.getSelectedFiles();
        return files [0];

    }
}
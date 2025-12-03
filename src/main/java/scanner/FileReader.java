package scanner;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.openapi.project.Project;

import java.io.IOException;

public class FileReader {

    public String readFileAsString(VirtualFile file) throws IOException {
        if (file == null || !file.exists()) {
            throw new IOException("File does not exist");
        }
        return new String(file.contentsToByteArray(), file.getCharset());
    }

}
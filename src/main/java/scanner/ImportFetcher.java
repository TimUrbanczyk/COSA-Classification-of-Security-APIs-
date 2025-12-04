package scanner;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiImportStatement;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import org.yaml.snakeyaml.util.Tuple;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ImportFetcher {

    public static List<Tuple<PsiFile,PsiImportStatement[]>> fetchImports(Project project) {
        PsiManager psiManager = PsiManager.getInstance(project);
        Collection<VirtualFile> javaFiles = FilenameIndex.getAllFilesByExt(project, "java", GlobalSearchScope.projectScope(project));
        List<Tuple<PsiFile,PsiImportStatement[]>> importStatements = new ArrayList<>();

        for (VirtualFile virtualFile : javaFiles) {
            PsiFile psiFile = psiManager.findFile(virtualFile);
            if (!(psiFile instanceof PsiJavaFile javaFile)) {
                return null;
            }

            PsiImportStatement[] imports = javaFile.getImportList().getImportStatements();
            Tuple<PsiFile,PsiImportStatement[]> wrapper = new Tuple<>(psiFile,imports);
            importStatements.add(wrapper);
        }
        return importStatements;
    }


}

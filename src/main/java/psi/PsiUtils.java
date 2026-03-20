package psi;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;

/**
 * Utility class for working with the IntelliJ PSI (Program Structure Interface).
 */
public class PsiUtils {

    /**
     * Retrieves the {@link PsiElement} corresponding to a given {@link VirtualFile}.
     *
     * @param project     the current project
     * @param virtualFile the virtual file to find
     * @return the root PSI element of the file
     * @throws NullPointerException if the file cannot be found or the PSI element is null
     */
    public static PsiElement getPsiFile(Project project, VirtualFile virtualFile){
        PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
        if(psiFile == null){
            throw new NullPointerException("psiFile does not exist");
        }
        final PsiElement[] psiFileAsPsiElement = {null};
        psiFile.accept(new PsiElementVisitor() {
            @Override
            public void visitElement(@NotNull PsiElement element) {
                super.visitElement(element);
                psiFileAsPsiElement[0] = element;
            }
        });
        if(psiFileAsPsiElement[0] == null){
            throw new NullPointerException("PsiElement is null");
        }
        return psiFileAsPsiElement[0];
    }
}

package com.funivan.phpstorm.refactoring.EditUsages;

import com.intellij.featureStatistics.FeatureUsageTracker;
import com.intellij.ide.scratch.ScratchFileService;
import com.intellij.ide.scratch.ScratchRootType;
import com.intellij.lang.Language;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.usageView.UsageInfo;
import com.intellij.usages.*;
import com.intellij.usages.impl.UsageNode;
import com.intellij.usages.impl.UsageViewImpl;
import com.intellij.usages.Usage;
import com.intellij.usages.impl.UsageViewManagerImpl;
import com.intellij.util.SystemProperties;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.*;
import javax.swing.tree.TreePath;
import java.util.*;
import java.util.List;

/**
 * @author Ivan Scherbak <dev@funivan.com>
 * @todo better position detector
 */
public class CreatePatchFromUsages extends AnAction {
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();

        if (project == null) {
            return;
        }
        DataContext dataContext = e.getDataContext();


        UsageView usageView = UsageView.USAGE_VIEW_KEY.getData(dataContext);

        if (!(usageView instanceof UsageViewImpl)) {
            return;
        }

        Set<Usage> usages = usageView.getUsages();

        Language language = null;
        VirtualFile baseDir = project.getBaseDir();


        StringBuilder buf = new StringBuilder();
        for (Usage usage : usages) {

            if (!(usage instanceof UsageInfo2UsageAdapter)) {
                continue;
            }


            UsageInfo2UsageAdapter usageInfo = (UsageInfo2UsageAdapter) usage;

            if (language == null) {
                language = usageInfo.getElement().getLanguage();
            }

            VirtualFile file = usageInfo.getFile();

            String path = VfsUtil.getRelativePath(file, baseDir, '/');

            buf.append("\n");
            buf.append("//file:" + path + ':' + (usageInfo.getLine() + 1) + "\n");
            buf.append(usageInfo.getPlainText() + "\n");
            buf.append("\n");
        }


        String text = buf.toString();
        if (language.getID().equals("PHP")) {
            text = "<?php\n" + text;
        }


        VirtualFile f = ScratchRootType.getInstance().createScratchFile(project, "scratch", language, text, ScratchFileService.Option.create_new_always);
        if (f != null) {
            FileEditorManager.getInstance(project).openFile(f, true);
        }

    }


}
package com.tbidea.plugin.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiJavaFile;
import com.tbidea.plugin.dialog.ChangeMVPDialog;
import com.tbidea.plugin.listener.ChangeListener;
import com.tbidea.plugin.model.MethodEntity;
import com.tbidea.plugin.util.ChangeHelper;
import com.tbidea.plugin.util.ClassHelper;
import com.tbidea.plugin.util.MsgUtil;

import java.util.Map;

/**
 * Created by jiana on 11/12/16.
 */
public class MVPChangeAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        PsiJavaFile psiJavaFile = ClassHelper.getJavaFile(e);
        boolean isContract = psiJavaFile.getName().contains("Contract");
        if (!isContract) {
            MsgUtil.showInfo("The java file is not contract!");
            return;
        }
        Map<String, Object[][]> objects = ClassHelper.getMethod(psiJavaFile);
        ChangeMVPDialog dialog = new ChangeMVPDialog(objects);
        dialog.setListener(new ChangeListener() {
            @Override
            public void add(int type, MethodEntity methodEntity) {
                ChangeHelper.methodAction(e.getProject(), psiJavaFile, type, methodEntity, false);
            }

            @Override
            public void del(int type, MethodEntity methodEntity) {
                int res = Messages.showYesNoDialog("Methods inside the implementation class will be deleted.", "Warning", Messages.getWarningIcon());
                if (res == 0)
                    ChangeHelper.methodAction(e.getProject(), psiJavaFile, type, methodEntity, true);
            }
        });
        dialog.pack();
        dialog.setVisible(true);
    }


}

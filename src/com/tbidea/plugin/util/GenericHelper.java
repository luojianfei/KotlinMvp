package com.tbidea.plugin.util;

import com.tbidea.plugin.dialog.ChangeMVPDialog;
import com.tbidea.plugin.dialog.EditorMVPDialog;
import com.tbidea.plugin.model.MethodEntity;

import javax.swing.table.DefaultTableModel;

/**
 * Created by jiana on 12/12/16.
 */
public class GenericHelper {
    /**
     * add a method
     * {@link EditorMVPDialog#addListener()}
     * {@link ChangeMVPDialog#addListener()}
     * @param model
     * @param methodEntity
     */
    public static MethodEntity addAMethod(DefaultTableModel model, MethodEntity methodEntity) {
        String reName = methodEntity.getReturnStr();
        String meName = methodEntity.getMethodStr();
        if (reName.equals("")) {
            MsgUtil.showInfo("Please input return type!");
            return null;
        }
        if (meName.equals("")) {
            MsgUtil.showInfo("Please input method name!");
            return null;
        }
        //if you do not add ()
        if (!meName.matches(".+\\(.*\\)")) {
            meName = meName + "()";
        }
        methodEntity.setReturnStr(reName);
        methodEntity.setMethodStr(meName);
        model.addRow(new Object[]{reName, meName});
        return methodEntity;
    }
}

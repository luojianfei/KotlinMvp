package com.tbidea.plugin.listener;


import com.tbidea.plugin.model.MethodEntity;

/**
 * Created by jiana on 12/12/16.
 * InputMethodDialog listener
 * {@link com.tbidea.plugin.widget.InputMethodDialog}
 */
public interface IMDListener {
    void complete(MethodEntity methodEntity);
}

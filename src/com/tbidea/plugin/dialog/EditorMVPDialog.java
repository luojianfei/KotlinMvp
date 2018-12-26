package com.tbidea.plugin.dialog;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.ui.JBColor;
import com.tbidea.plugin.Constant;
import com.tbidea.plugin.listener.EditorListener;
import com.tbidea.plugin.listener.IMDListener;
import com.tbidea.plugin.model.EditEntity;
import com.tbidea.plugin.model.InitEntity;
import com.tbidea.plugin.model.MethodEntity;
import com.tbidea.plugin.util.ClassHelper;
import com.tbidea.plugin.util.GenericHelper;
import com.tbidea.plugin.util.MsgUtil;
import com.tbidea.plugin.util.UIUtil;
import com.tbidea.plugin.widget.InputMethodDialog;
import org.apache.http.util.TextUtils;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EditorMVPDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JButton btnAddView;
    private JButton btnAddPresenter;
    private JButton btnAddModel;
    private JTable tableView;
    private JButton btnDelView;
    private JButton btnDelPresenter;
    private JButton btnDelModel;
    private JTable tablePresenter;
    private JTable tableModel;
    private JTextField viewParent;
    private JTextField contractName;
    private JTextField presenterParent;
    private JTextField modelParent;
    private JComboBox viewPackageName;
    private JTextField viewImpName;
    private JLabel labelUrl;
    private JCheckBox xmvpCheckBox;
    private JLabel labelView;
    private JLabel labelPresenter;
    private JLabel labelModel;
    private JLabel labelView2;
    private JTextField baseViewParent;
    private JLabel xmvpQuestion;
    private JRadioButton multiRadioButton;
    private JRadioButton singleRadioButton;
    private JTree packageTree;
    private JLabel refreshTree;
    private JButton[] btnAddArr = new JButton[3];
    private JButton[] btnDelArr = new JButton[3];
    private JTable[] tableArr = new JTable[3];
    private EditorListener listener;
    private InitEntity initEntity;
    private PsiDirectory mPsiDirectory;

    public void setEditorListener(EditorListener listener) {
        this.listener = listener;
    }

    public EditorMVPDialog(PsiDirectory psiDirectory, InitEntity initEntity) {
        this.initEntity = initEntity;
        this.mPsiDirectory = psiDirectory;
        setContentPane(contentPane);
        setModal(true);

        getRootPane().setDefaultButton(buttonOK);
        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        fillArr();
        initView();
        addListener();
        initData();
    }

    private void initData() {
        String bp = PropertiesComponent.getInstance().getValue(Constant.BASE_PRESENTER, Constant.DEFAULT_BASE_PRESENTER);
        baseViewParent.setText(bp);
    }

    private void changeXMVP(boolean isXMVP) {
        if (isXMVP) {
            viewParent.setText(Constant.X_VIEW);
            presenterParent.setText(Constant.X_PRESENTER);
            modelParent.setText(Constant.X_MODEL);
        } else {
            viewParent.setText(Constant.NULL);
            presenterParent.setText(Constant.NULL);
            modelParent.setText(Constant.NULL);
        }
    }

    private void initView() {
        packageTree.setVisible(false);
        labelUrl.setForeground(JBColor.BLUE);
        xmvpQuestion.setForeground(JBColor.BLUE);
        refreshTree.setForeground(JBColor.BLUE);

        boolean isMVP = PropertiesComponent.getInstance().getBoolean(Constant.IS_XMVP);
        changeXMVP(isMVP);
        xmvpCheckBox.setSelected(isMVP);

        viewImpName.setColumns(30);
        if (initEntity == null) return;
        String[] datasPackage = new String[initEntity.getPsiDirectories().length];
        Map<Object, String[]> maps = new HashMap<>();
        for (int i = 0; i < datasPackage.length; i++) {
            datasPackage[i] = initEntity.getPsiDirectories()[i].getName();
            PsiFile[] psiFiles = initEntity.getPsiDirectories()[i].getFiles();
            String[] strs = new String[psiFiles.length];
            for (int j = 0; j < psiFiles.length; j++) {
                strs[j] = psiFiles[j].getName().replace(".java", "");
            }
            maps.put(initEntity.getPsiDirectories()[i].getName(), strs);
        }
        ComboBoxModel<String> comboBoxModel = new DefaultComboBoxModel<>(datasPackage);
        viewPackageName.setModel(comboBoxModel);
        viewPackageName.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                setupAutoComplete(viewImpName, maps.get(e.getItem()));
            }
        });
    }

    private static boolean isAdjusting(JComboBox cbInput) {
        if (cbInput.getClientProperty("is_adjusting") instanceof Boolean) {
            return (Boolean) cbInput.getClientProperty("is_adjusting");
        }
        return false;
    }

    private static void setAdjusting(JComboBox cbInput, boolean adjusting) {
        cbInput.putClientProperty("is_adjusting", adjusting);
    }

    private void selectTree(Object object) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(mPsiDirectory.getName());
        if (object == singleRadioButton && singleRadioButton.isSelected()) {
            viewPackageName.setVisible(false);
            fillSinglePackageTree(root);
        } else if (object == multiRadioButton && multiRadioButton.isSelected()) {
            viewPackageName.setVisible(true);
            fillMultiPackageTree(root);
        }
        packageTree.setVisible(true);
        TreeModel treeModel = new DefaultTreeModel(root);
        packageTree.setModel(treeModel);
        UIUtil.expandTree(packageTree);
    }

    private void addListener() {
        ChangeListener radioListener = e -> selectTree(e.getSource());

        multiRadioButton.addChangeListener(radioListener);
        singleRadioButton.addChangeListener(radioListener);
        multiRadioButton.setSelected(true);

        refreshTree.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (multiRadioButton.isSelected()) {
                    selectTree(multiRadioButton);
                } else {
                    selectTree(singleRadioButton);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                refreshTree.setForeground(JBColor.RED);
            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {
                refreshTree.setForeground(JBColor.RED);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                refreshTree.setForeground(JBColor.BLUE);
            }
        });


        contractName.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                updateShow();
            }

            public void removeUpdate(DocumentEvent e) {
                updateShow();
            }

            public void insertUpdate(DocumentEvent e) {
                updateShow();
            }

            public void updateShow() {
                String value = contractName.getText() + "Contract.";
                labelView.setText(value + "View");
                labelView2.setText(value + "View extends");
                labelPresenter.setText(value + "Presenter extends");
                labelModel.setText(value + "Model extends");
            }
        });

        xmvpCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean b = xmvpCheckBox.isSelected();
                changeXMVP(b);
                PropertiesComponent.getInstance().setValue(Constant.IS_XMVP, b);
            }
        });


        labelUrl.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new java.net.URI(Constant.MVP_MANAGER_URL));
                } catch (IOException | URISyntaxException e1) {
                    e1.printStackTrace();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                labelUrl.setForeground(JBColor.RED);
            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {
                labelUrl.setForeground(JBColor.RED);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                labelUrl.setForeground(JBColor.BLUE);
            }
        });

        xmvpQuestion.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new java.net.URI(Constant.XMVP_URL));
                } catch (IOException | URISyntaxException e1) {
                    e1.printStackTrace();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                xmvpQuestion.setForeground(JBColor.RED);
            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {
                xmvpQuestion.setForeground(JBColor.RED);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                xmvpQuestion.setForeground(JBColor.BLUE);
            }
        });

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        for (int i = 0; i < btnAddArr.length; i++) {
            JButton btn = btnAddArr[i];
            int finalI = i;
            btn.addActionListener(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    InputMethodDialog.input(new IMDListener() {
                        @Override
                        public void complete(MethodEntity methodEntity) {
                            GenericHelper.addAMethod((DefaultTableModel) tableArr[finalI].getModel(), methodEntity);
                        }
                    });
                }
            });
        }

        for (int i = 0; i < btnDelArr.length; i++) {
            JButton btn = btnDelArr[i];
            int finalI = i;
            btn.addActionListener(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    DefaultTableModel model = (DefaultTableModel) tableArr[finalI].getModel();
                    int totalRow = 0;
                    for (int row : tableArr[finalI].getSelectedRows()) {
                        model.removeRow(row - totalRow);
                        totalRow++;
                    }
                }
            });
        }
    }

    private void fillSinglePackageTree(DefaultMutableTreeNode root) {
        String name = contractName.getText();
        DefaultMutableTreeNode createdFolder = new DefaultMutableTreeNode(TextUtils.isEmpty(name) ? "(Please input contract name!)" : name.toLowerCase());
        createdFolder.add(new DefaultMutableTreeNode(TextUtils.isEmpty(viewImpName.getText()) ? "(Please input Activity/Fragment!)" : viewImpName.getText() + ".kt"));
        createdFolder.add(new DefaultMutableTreeNode(name + ClassHelper.CONTRACT + ".kt"));
        createdFolder.add(new DefaultMutableTreeNode(name + ClassHelper.PRESENTER + ".kt"));
        createdFolder.add(new DefaultMutableTreeNode(name + ClassHelper.MODEL + ".kt"));
        root.add(createdFolder);
    }

    private void fillMultiPackageTree(DefaultMutableTreeNode root) {
        String name = contractName.getText();

        DefaultMutableTreeNode contract = new DefaultMutableTreeNode(ClassHelper.PACKAGE_CONTRACT);
        DefaultMutableTreeNode presenter = new DefaultMutableTreeNode(ClassHelper.PACKAGE_PRESENTER);
        DefaultMutableTreeNode model = new DefaultMutableTreeNode(ClassHelper.PACKAGE_MODEL);
        DefaultMutableTreeNode viewP = new DefaultMutableTreeNode(ClassHelper.PACKAGE_VIEW);

        if (viewPackageName.getSelectedItem() != null) {
            String viewPackage = viewPackageName.getSelectedItem().toString();
            DefaultMutableTreeNode view = new DefaultMutableTreeNode(viewPackage);
            view.add(new DefaultMutableTreeNode(TextUtils.isEmpty(viewImpName.getText()) ? "(Please input Activity/Fragment!)" : viewImpName.getText() + ".kt"));
            root.add(view);
        } else {
            viewP.add(new DefaultMutableTreeNode(TextUtils.isEmpty(viewImpName.getText()) ? "(Please input Activity/Fragment!)" : viewImpName.getText() + ".kt"));
            root.add(viewP);
//            root.add(new DefaultMutableTreeNode(TextUtils.isEmpty(viewImpName.getText()) ? "(Please input Activity/Fragment!)" : viewImpName.getText() + ".java"));
        }

        contract.add(new DefaultMutableTreeNode(name + ClassHelper.CONTRACT + ".kt"));
        presenter.add(new DefaultMutableTreeNode(name + ClassHelper.PRESENTER + ".kt"));
        model.add(new DefaultMutableTreeNode(name + ClassHelper.MODEL + ".kt"));

        root.add(contract);
        root.add(presenter);
        root.add(model);

    }

    /**
     * fill view array
     */
    private void fillArr() {
        btnAddArr[0] = btnAddView;
        btnAddArr[1] = btnAddPresenter;
        btnAddArr[2] = btnAddModel;

        btnDelArr[0] = btnDelView;
        btnDelArr[1] = btnDelPresenter;
        btnDelArr[2] = btnDelModel;

        tableArr[0] = tableView;
        tableArr[1] = tablePresenter;
        tableArr[2] = tableModel;

    }

    private void onOK() {
        if (listener == null) {
            return;
        }
        ArrayList<String> viewData = getData(tableView);
        ArrayList<String> presenterData = getData(tablePresenter);
        ArrayList<String> modelData = getData(tableModel);
        if (viewData == null || presenterData == null || modelData == null) {
            Messages.showMessageDialog("Incomplete information, please check", "Information", Messages.getInformationIcon());
            return;
        }

        String name = contractName.getText();
        if (TextUtils.isEmpty(name)) {
            MsgUtil.msgContractNameNull();
            return;
        }

        EditEntity ee = new EditEntity(viewData, presenterData, modelData);
        ee.setSinglePackage(singleRadioButton.isSelected());
        ee.setContractName(name.trim());
        ee.setBaseViewParent(baseViewParent.getText().trim());
        ee.setViewParent(viewParent.getText().trim());
        ee.setPresenterParent(presenterParent.getText().trim());
        ee.setModelParent(modelParent.getText().trim());
        String packageName = (String) viewPackageName.getModel().getSelectedItem();
        if (packageName != null && !packageName.equals("") && initEntity != null && initEntity.getPsiDirectories() != null) {
            for (int i = 0; i < initEntity.getPsiDirectories().length; i++) {
                if (initEntity.getPsiDirectories()[i].getName().equals(packageName)) {
                    ee.setViewDir(initEntity.getPsiDirectories()[i]);
                    break;
                }
            }
        }
        if ("".equals(viewImpName.getText().trim())) {
            Messages.showMessageDialog("Please input implement view!", "Information", Messages.getInformationIcon());
            return;
        }
        ee.setViewName(viewImpName.getText().trim());
        listener.editOver(ee);
        PropertiesComponent.getInstance().setValue(Constant.BASE_PRESENTER, baseViewParent.getText());
    }

    /**
     * Get data in JTable
     *
     * @param jTable
     * @return
     */
    private ArrayList<String> getData(JTable jTable) {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < jTable.getModel().getRowCount(); i++) {
            TableModel model = jTable.getModel();
            String returnStr = (String) model.getValueAt(i, 0);
            String methodStr = (String) model.getValueAt(i, 1);
            returnStr = returnStr.trim();
            methodStr = methodStr.trim();
            if (TextUtils.isEmpty(returnStr) || TextUtils.isEmpty(methodStr)) {
                return null;
            }
            list.add(returnStr + "##" + methodStr);
        }

        return list;
    }


    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public static void main(String[] args) {
        EditorMVPDialog dialog = new EditorMVPDialog(null, null);
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }

    private void createUIComponents() {
        initJTable(tableView = newTableInstance());
        initJTable(tablePresenter = newTableInstance());
        initJTable(tableModel = newTableInstance());
    }


    private void initJTable(JTable mJtable) {
        mJtable.getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (e.getType() == TableModelEvent.UPDATE) {
                    if (e.getLastRow() == -1) {
                        return;
                    }
                    String value = mJtable.getValueAt(e.getLastRow(), e.getColumn()).toString();
                    if (value.trim().equals("")) {
                        Messages.showMessageDialog("can't be empty", "Information", Messages.getInformationIcon());
                    }
                    for (int i = 0; i < mJtable.getModel().getRowCount(); i++) {
                        if (mJtable.getValueAt(i, 1).toString().equals(value) && e.getLastRow() != i) {
                            Messages.showMessageDialog("This method has been added", "Information", Messages.getInformationIcon());
                        }
                    }
                }
            }
        });

        mJtable.getTableHeader().setPreferredSize(new Dimension(tableView.getTableHeader().getWidth(), 20));
        mJtable.getColumnModel().getColumn(0).setPreferredWidth(15);
        mJtable.getColumnModel().getColumn(1).setPreferredWidth(155);
        mJtable.setRowHeight(25);
    }

    /**
     * create a JTable instance
     *
     * @return
     */
    private JTable newTableInstance() {
        String[] defaultValue = {"void", "method()"};
        DefaultTableModel mDefaultTableMoadel = new DefaultTableModel();
        Object[][] object = new Object[1][2];
        object[0][0] = "void";
        object[0][1] = "method()";
        mDefaultTableMoadel.setDataVector(object, new Object[]{"return", "method"});
        JTable mJtable = new JTable(mDefaultTableMoadel) {
            @Override
            public void tableChanged(TableModelEvent e) {
                super.tableChanged(e);
                repaint();
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        return mJtable;
    }

    public static void setupAutoComplete(final JTextField txtInput, final String[] items) {
        final DefaultComboBoxModel model = new DefaultComboBoxModel();
        final JComboBox cbInput = new JComboBox(model) {
            public Dimension getPreferredSize() {
                return new Dimension(super.getPreferredSize().width, 0);
            }
        };
        setAdjusting(cbInput, false);
        for (String item : items) {
            model.addElement(item);
        }
        cbInput.setSelectedItem(null);
        cbInput.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isAdjusting(cbInput)) {
                    if (cbInput.getSelectedItem() != null) {
                        txtInput.setText(cbInput.getSelectedItem().toString());
                    }
                }
            }
        });

        txtInput.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                setAdjusting(cbInput, true);
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    if (cbInput.isPopupVisible()) {
                        e.setKeyCode(KeyEvent.VK_ENTER);
                    }
                }
                if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN) {
                    e.setSource(cbInput);
                    cbInput.dispatchEvent(e);
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        txtInput.setText(cbInput.getSelectedItem().toString());
                        cbInput.setPopupVisible(false);
                    }
                }
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    cbInput.setPopupVisible(false);
                }
                setAdjusting(cbInput, false);
            }
        });
        txtInput.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                updateList();
            }

            public void removeUpdate(DocumentEvent e) {
                updateList();
            }

            public void changedUpdate(DocumentEvent e) {
                updateList();
            }

            private void updateList() {
                setAdjusting(cbInput, true);
                model.removeAllElements();
                String input = txtInput.getText();
                if (!input.isEmpty()) {
                    for (String item : items) {
                        if (item.toLowerCase().startsWith(input.toLowerCase())) {
                            model.addElement(item);
                        }
                    }
                }
                cbInput.setPopupVisible(model.getSize() > 0);
                setAdjusting(cbInput, false);
            }
        });
        txtInput.setLayout(new BorderLayout());
        txtInput.add(cbInput, BorderLayout.SOUTH);
    }
}

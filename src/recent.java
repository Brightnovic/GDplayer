import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Recent extends JFrame {
    private JList<String> list;
    private DefaultListModel<String> listModel;
    private JButton closeButton;

    public Recent(String[] musicFilePaths, ActionListener playSelectedListener) {
        super("Recently Played");

        setSize(800, 500);
        setLocationRelativeTo(null);
        listModel = new DefaultListModel<>();
        for (String path : musicFilePaths) {
            listModel.addElement(path);
        }

        list = new JList<>(listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setVisibleRowCount(10);
        JScrollPane listScrollPane = new JScrollPane(list);

        closeButton = new JButton("Close");
        closeButton.addActionListener(e -> setVisible(false));

        list.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedPath = list.getSelectedValue();
                playSelectedListener.actionPerformed(new ActionEvent(selectedPath, ActionEvent.ACTION_PERFORMED, "PlaySelected"));
                setVisible(false);
            }
        });

        getContentPane().add(listScrollPane, BorderLayout.CENTER);
        getContentPane().add(closeButton, BorderLayout.SOUTH);
    }
}

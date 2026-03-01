package gui;

import SecurityClass.SecurityClass;
import SecurityClass.SecurityclassUtils;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;

public class OverviewPopUp extends JDialog {

    private final JTable overviewTable;

    public OverviewPopUp(Component parent) {
        super(SwingUtilities.getWindowAncestor(parent), "Security Class Overview", ModalityType.MODELESS);
        setLayout(new BorderLayout());

        String[] columns = {"Security Class", "Features", "LoC", "Scattering", "Tangling"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        overviewTable = new JBTable(model);
        overviewTable.setFillsViewportHeight(true);
        overviewTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        populate(model);

        add(new JBScrollPane(overviewTable), BorderLayout.CENTER);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refresh());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(refreshButton);
        add(south, BorderLayout.SOUTH);

        setMinimumSize(new Dimension(650, 350));
        pack();
        setLocationRelativeTo(parent);
    }

    public void refresh() {
        DefaultTableModel model = (DefaultTableModel) overviewTable.getModel();
        model.setRowCount(0);
        populate(model);
        model.fireTableDataChanged();
    }

    private void populate(DefaultTableModel model) {
        List<SecurityClass> securityClasses = SecurityclassUtils.getSecurityClasses();

        Map<String, Set<String>> classToFiles = new LinkedHashMap<>();
        for (SecurityClass sc : securityClasses) {
            classToFiles.put(sc.getName(), sc.getOccurrences().keySet());
        }

        for (SecurityClass sc : securityClasses) {
            String name = sc.getName();
            int loc = sc.getOccurrences().values().stream().mapToInt(List::size).sum();
            int scattering = sc.getOccurrences().size();

            Set<String> ownFiles = sc.getOccurrences().keySet();
            long tangling = classToFiles.entrySet().stream()
                    .filter(e -> !e.getKey().equals(name))
                    .filter(e -> e.getValue().stream().anyMatch(ownFiles::contains))
                    .count();

            model.addRow(new Object[]{name, name, loc, scattering, tangling});
        }
    }
}

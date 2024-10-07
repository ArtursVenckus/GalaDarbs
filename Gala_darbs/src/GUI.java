import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;

public class GUI extends JFrame {

    private JPanel gridPanel; //galvenais konteineris kur likt virsu datus
    private JTextField preceField, cenaField, datumsField; //raksti blakus laukiem kas pasaka kur ko aizpildit
    private JButton enterButton, deleteButton, sortButton, sortByIdButton; //ievietot preci poga un dzesanas poga un sortesanas pogas
    private JTable dataTable; //labas puses tabula
    private DefaultTableModel tableModel;  // Tabulas Modelis
    private int selectedRow; //lauj izveleties rindu ar peli

    public GUI() {
        setTitle("Izdevumu parskats"); //uzraksts aplikacijas augspuse kreisaja sturi
        setSize(1000, 600); // Galvena panela izmers
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //aplikacija taisas ciet ja aizver

        gridPanel = new JPanel(new GridLayout(1, 2));  // Cik uz panela ir rindas un cik kollonas

        createRegisterItemsPanel(); // funkcija lai izveidotu paneli kur izvadit datus par izdevumiem
        createTablePanel(); // funkcija kas izveido paneli labaja puse kur var redzet visus ievaditos datus

        getContentPane().add(gridPanel, BorderLayout.CENTER); //kur elementi atrodas uz panela, seit vini ir pa vidu

        refreshData(); //atjauno datus lai redzetu visu no datubazes

        setVisible(true);
    }

    private void createRegisterItemsPanel() {
        JPanel panel = new JPanel(null);

        //prece uzraksts
        JLabel preceLabel = new JLabel("Prece: ");
        preceLabel.setBounds(50, 30, 80, 25);
        panel.add(preceLabel);

        //prece ievades lauks blakus prece uzrakstam
        preceField = new JTextField();
        preceField.setBounds(140, 30, 200, 25);
        panel.add(preceField);

        //cena uzraksts
        JLabel cenaLabel = new JLabel("Cena: ");
        cenaLabel.setBounds(50, 70, 80, 25);
        panel.add(cenaLabel);

        //cena ievades lauks blakus cena uzrakstam
        cenaField = new JTextField();
        cenaField.setBounds(140, 70, 200, 25);
        panel.add(cenaField);

        //datums uzraksts
        JLabel datumsLabel = new JLabel("Datums: ");
        datumsLabel.setBounds(50, 110, 80, 25);
        panel.add(datumsLabel);

        //datums ievades lauks blakus datums uzrakstam
        datumsField = new JTextField();
        datumsField.setBounds(140, 110, 200, 25);
        panel.add(datumsField);

        enterButton = new JButton("Ievadīt");
        enterButton.setBounds(140, 150, 100, 25);
        panel.add(enterButton);


        // Action listener lai ievaditu tesktu, lauj panemt ievadito vertibu un iedot konkretam mainigajam
        enterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String prece = preceField.getText();
                double cena;
                String datums = datumsField.getText();

                //parbauda vai lauks nav tukss

                if (prece.isEmpty()) {
                    JOptionPane.showMessageDialog(GUI.this, "Preces lauks nevar but tukss!");
                    return;
                }

                //parbauda vai lauka ir ievaditi cipari, ja nav tad iedod tekstu ievadiet ciparus

                try {
                    cena = Double.parseDouble(cenaField.getText());
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(GUI.this, "Cena nav skaitlis!");
                    return;
                }

                //parbauda vai datuma lauks nav tukss

                if (datums.isEmpty()) {
                    JOptionPane.showMessageDialog(GUI.this, "Datuma lauks nevar but tukss!");
                    return;
                }

                //Ja visi dati sakrit ieliek tabula datus
                insertData(prece, cena, datums);

                // Attira laukus prieks jaunas ievades
                preceField.setText("");
                cenaField.setText("");
                datumsField.setText("");
            }
        });

        gridPanel.add(panel, "Ievade"); //pievenojam galvenajam panelim ievades logu kur visu ievadit.
    }

    private void createTablePanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER)); //tabulas izvietojums uz panela labaja puse, jo panelis ir sadalits divas dalas.

        //izdevumu saraksts testa izmers, fonts
        JLabel titleLabel = new JLabel("Izdevumu saraksts");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(titleLabel);

        //katras kolonas apraksts tabula augsa
        String[] columnNames = {"ID", "Prece", "Cena", "Datums"};
        tableModel = new DefaultTableModel(columnNames, 0);  // 0 rindinas sakuma

        dataTable = new JTable(tableModel);
        dataTable.setPreferredScrollableViewportSize(new Dimension(300, 500));  // labas tabulas izmers

        JScrollPane scrollPane = new JScrollPane(dataTable); // ja tabula ir pilna lauj scroolot un apskatities vissas rindas
        panel.add(scrollPane);

        //dzest poga
        deleteButton = new JButton("Dzest");
        deleteButton.addActionListener(e -> deleteSelectedRow());
        panel.add(deleteButton);

        // sortesanas poga pec Cenas un ID
        sortButton = new JButton("Sort by Cena (Descending)");
        sortByIdButton = new JButton("Sort by ID (Ascending)");
        panel.add(sortButton);
        panel.add(sortByIdButton);

        // izveleties rindu ar peles klikski
        dataTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedRow = dataTable.getSelectedRow();
            }
        });

        sortButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Izveidots Comperator lai kartotu pec cenas
                Comparator<Object[]> comparator = (row1, row2) -> {
                    double cena1 = (double) row1[2];
                    double cena2 = (double) row2[2];
                    return (int) (cena2 - cena1); // kartot pec vertibam no mazakas uz lielako
                };

                List<Object[]> sortedDataList = new ArrayList<>();

                //Itere cauri tableModel datiem un iznem tikai rindas ar cipariskam vertibam "Cena"

                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    Object[] row = tableModel.getDataVector().get(i).toArray();
                    if (row[2] instanceof Number) { // Parbauda vai vertiba ir cipars
                        sortedDataList.add(row);
                    }
                }

                Object[][] dataToSort = sortedDataList.isEmpty() ? null : sortedDataList.toArray(new Object[0][]);


                if (dataToSort != null) {
                    Arrays.sort(dataToSort, comparator);


                    tableModel.setRowCount(0);


                    for (Object[] row : dataToSort) {
                        tableModel.addRow(row);
                    }

                }
            }
        });

        sortByIdButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Izveidots Comperator lai kartotu pec ID
                Comparator<Object[]> comparator = (row1, row2) -> {
                    Integer id1 = (Integer) row1[0]; // Assuming 'ID' is at index 0
                    Integer id2 = (Integer) row2[0];
                    return id1.compareTo(id2); // Ascending order
                };

                List<Object[]> sortedDataList = new ArrayList<>();

                //Itere cauri tableModel datiem un iznem tikai rindas ar cipariskam vertibam "ID"
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    Object[] row = tableModel.getDataVector().get(i).toArray();
                    if (row[2] instanceof Number) {  // Parbauda vai vertiba ir cipars
                        sortedDataList.add(row);
                    }
                }

                Object[][] dataToSort = sortedDataList.isEmpty() ? null : sortedDataList.toArray(new Object[0][]);


                if (dataToSort != null) {
                    Arrays.sort(dataToSort, comparator);


                    tableModel.setRowCount(0);


                    for (Object[] row : dataToSort) {
                        tableModel.addRow(row);
                    }

                }
            }
        });


        gridPanel.add(panel, "Saraksts"); // tabulas pievienosana panelim kur tiek uzglabati visi dati
    }

    private void refreshData() {
        tableModel.setRowCount(0); // Ja seit ieliek ciparus, tad no augsas paradas tuksi logi kurus nevar izdest.

        //savienojamies ar SQL lai dabutu datus
        try (Connection conn = DBlogic.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM izdevumu_sistema")) {

            while (rs.next()) {
                int id = rs.getInt("ID");
                String prece = rs.getString("PRECE");
                double cena = rs.getDouble("SUMMA");
                String datums = rs.getString("DATUMS");
                tableModel.addRow(new Object[]{id, prece, cena, datums } );
            }
            //error messege ja ievada nepareizi datus, parak daudz ciparu piemeram
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Datu ielades kluda!", "Kluda", JOptionPane.ERROR_MESSAGE);
        }
    }

    //Ievada datus SQL datubaze
    private void insertData(String prece, double cena, String datums) {
        try (Connection conn = DBlogic.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO izdevumu_sistema (PRECE, SUMMA, DATUMS) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, prece);
            pstmt.setDouble(2, cena);
            pstmt.setString(3, datums);

            pstmt.executeUpdate();

            // dabut uzgenereto ID no SQL datu bazes
            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                int newID = generatedKeys.getInt(1);
                tableModel.addRow(new Object[]{newID, prece, cena, datums});
                JOptionPane.showMessageDialog(this, "Jusu ieraksts veiksmigi pievienots!"); //pievieno ierakstu
            } else {
                JOptionPane.showMessageDialog(this, "Ieraksta kluda!", "Kluda", JOptionPane.ERROR_MESSAGE); // nevar pievienot ierakstu
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Datu ielades kluda!", "Kluda", JOptionPane.ERROR_MESSAGE); //nevar ieladet datus no datu bazes
        }
    }

    //dzest datus
    private void deleteSelectedRow() {

        int id = (int) tableModel.getValueAt(selectedRow, 0); //lauj izveleties datus bet ja uzspiez pogu dzest, tad vins piedava dzest pirmo ierakstu

        //messege kas prasa vai esat parliecinats
        int confirmation = JOptionPane.showConfirmDialog(this, "Vai tiesam velaties dzest ierakstu ar ID " + id + "?", "Dzesana", JOptionPane.YES_NO_OPTION);
        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        //izsauc funkciju kas dzes skatoties pec ID ierakstu no datubazes
        try (Connection conn = DBlogic.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM izdevumu_sistema WHERE ID = ?")) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();

            // iznemt rindu no tabulas
            tableModel.removeRow(selectedRow);

            JOptionPane.showMessageDialog(this, "Jusu ieraksts veiksmigi dzests!");

            //ja neizdodas izdzest ierakstu no datubazes
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Datu dzesanas kluda!", "Kluda", JOptionPane.ERROR_MESSAGE);
        }
    }
}
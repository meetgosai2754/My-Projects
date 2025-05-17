import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import javax.swing.*;

public class BudgetTrackerCRUD extends JFrame {
    private JTextField descField, amountField;
    private JButton addIncomeBtn, addExpenseBtn, saveBtn, loadBtn, updateBtn, deleteBtn;
    private JList<String> transactionList;
    private DefaultListModel<String> listModel;
    private double balance = 0.0;
    private ArrayList<String> transactions = new ArrayList<>();
    private int selectedIndex = -1;  // Track selected transaction

    public BudgetTrackerCRUD() {
        setTitle("Personal Budget Tracker (CRUD)");
        setSize(600, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top input panel
        JPanel inputPanel = new JPanel(new GridLayout(4, 2));

        inputPanel.add(new JLabel("Description:"));
        descField = new JTextField();
        inputPanel.add(descField);

        inputPanel.add(new JLabel("Amount:"));
        amountField = new JTextField();
        inputPanel.add(amountField);

        addIncomeBtn = new JButton("Add Income");
        addExpenseBtn = new JButton("Add Expense");
        inputPanel.add(addIncomeBtn);
        inputPanel.add(addExpenseBtn);

        updateBtn = new JButton("Update Selected");
        deleteBtn = new JButton("Delete Selected");
        inputPanel.add(updateBtn);
        inputPanel.add(deleteBtn);

        add(inputPanel, BorderLayout.NORTH);

        // Center area: JList
        listModel = new DefaultListModel<>();
        transactionList = new JList<>(listModel);
        transactionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(transactionList), BorderLayout.CENTER);

        // Bottom panel
        JPanel bottomPanel = new JPanel();
        saveBtn = new JButton("Save");
        loadBtn = new JButton("Load");
        bottomPanel.add(saveBtn);
        bottomPanel.add(loadBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        // Listeners
        addIncomeBtn.addActionListener(e -> addTransaction(true));
        addExpenseBtn.addActionListener(e -> addTransaction(false));
        saveBtn.addActionListener(e -> saveToFile());
        loadBtn.addActionListener(e -> loadFromFile());
        updateBtn.addActionListener(e -> updateTransaction());
        deleteBtn.addActionListener(e -> deleteTransaction());

        transactionList.addListSelectionListener(e -> {
            selectedIndex = transactionList.getSelectedIndex();
            if (selectedIndex >= 0 && selectedIndex < transactions.size()) {
                String selected = transactions.get(selectedIndex);
                if (selected.startsWith("Income: ")) {
                    descField.setText(selected.substring(8, selected.indexOf("|") - 1));
                    amountField.setText(selected.substring(selected.indexOf("+₹") + 2));
                } else if (selected.startsWith("Expense: ")) {
                    descField.setText(selected.substring(9, selected.indexOf("|") - 1));
                    amountField.setText(selected.substring(selected.indexOf("-₹") + 2));
                }
            }
        });

        updateDisplay();
    }

    private void addTransaction(boolean isIncome) {
        try {
            String desc = descField.getText();
            double amount = Double.parseDouble(amountField.getText());

            if (!desc.isEmpty()) {
                if (isIncome) {
                    balance += amount;
                    transactions.add("Income: " + desc + " | +₹" + amount);
                } else {
                    balance -= amount;
                    transactions.add("Expense: " + desc + " | -₹" + amount);
                }

                descField.setText("");
                amountField.setText("");
                updateDisplay();
            } else {
                JOptionPane.showMessageDialog(this, "Please enter a description.");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter a valid number.");
        }
    }

    private void updateTransaction() {
        if (selectedIndex >= 0 && selectedIndex < transactions.size()) {
            try {
                String desc = descField.getText();
                double newAmount = Double.parseDouble(amountField.getText());

                if (!desc.isEmpty()) {
                    String oldTransaction = transactions.get(selectedIndex);
                    double oldAmount = 0.0;
                    if (oldTransaction.contains("+₹")) {
                        oldAmount = Double.parseDouble(oldTransaction.split("\\+₹")[1]);
                        balance -= oldAmount;
                        balance += newAmount;
                        transactions.set(selectedIndex, "Income: " + desc + " | +₹" + newAmount);
                    } else {
                        oldAmount = Double.parseDouble(oldTransaction.split("-₹")[1]);
                        balance += oldAmount;
                        balance -= newAmount;
                        transactions.set(selectedIndex, "Expense: " + desc + " | -₹" + newAmount);
                    }

                    descField.setText("");
                    amountField.setText("");
                    updateDisplay();
                } else {
                    JOptionPane.showMessageDialog(this, "Please enter a description.");
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Enter a valid number.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Select a transaction to update.");
        }
    }

    private void deleteTransaction() {
        if (selectedIndex >= 0 && selectedIndex < transactions.size()) {
            String selected = transactions.remove(selectedIndex);
            if (selected.contains("+₹")) {
                balance -= Double.parseDouble(selected.split("\\+₹")[1]);
            } else if (selected.contains("-₹")) {
                balance += Double.parseDouble(selected.split("-₹")[1]);
            }
            descField.setText("");
            amountField.setText("");
            updateDisplay();
        } else {
            JOptionPane.showMessageDialog(this, "Select a transaction to delete.");
        }
    }

    private void updateDisplay() {
        listModel.clear();
        listModel.addElement("Current Balance: ₹" + balance);
        listModel.addElement("-------------------------------------------------");
        for (String t : transactions) {
            listModel.addElement(t);
        }
    }

    private void saveToFile() {
        try (PrintWriter writer = new PrintWriter(new File("budget_data.txt"))) {
            writer.println(balance);
            for (String t : transactions) {
                writer.println(t);
            }
            JOptionPane.showMessageDialog(this, "Data saved successfully.");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving file.");
        }
    }

    private void loadFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader("budget_data.txt"))) {
            balance = Double.parseDouble(reader.readLine());
            transactions.clear();
            String line;
            while ((line = reader.readLine()) != null) {
                transactions.add(line);
            }
            updateDisplay();
            JOptionPane.showMessageDialog(this, "Data loaded successfully.");
        } catch (IOException | NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Error loading file.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new BudgetTrackerCRUD().setVisible(true);
        });
    }
}

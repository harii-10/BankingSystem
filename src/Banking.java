import java.sql.*;
import java.util.Scanner;

class Account {
    private static int accountNumberCounter = 1000;
    private int accountNumber;
    private double balance;

    public Account(double initialBalance, int accountNumber) {
        this.accountNumber = accountNumber;
        this.balance = initialBalance;
    }

    public int getAccountNumber() {
        return accountNumber;
    }

    public double getBalance() {
        return balance;
    }

    public void deposit(double amount, Connection con) throws SQLException {
        if (amount > 0) {
            balance += amount;
            System.out.println("Deposited: $" + amount);

            String query = "UPDATE accounts SET balance = ? WHERE account_number = ?";
            try (PreparedStatement stmt = con.prepareStatement(query)) {
                stmt.setDouble(1, balance);
                stmt.setInt(2, accountNumber);
                stmt.executeUpdate();
            }
        } else {
            System.out.println("Invalid deposit amount.");
        }
    }

    public void withdraw(double amount, Connection con) throws SQLException {
        if (amount > 0 && amount <= balance) {
            balance -= amount;
            System.out.println("Withdrawn: $" + amount);

            String query = "UPDATE accounts SET balance = ? WHERE account_number = ?";
            try (PreparedStatement stmt = con.prepareStatement(query)) {
                stmt.setDouble(1, balance);
                stmt.setInt(2, accountNumber);
                stmt.executeUpdate();
            }
        } else {
            System.out.println("Invalid withdrawal amount or insufficient balance.");
        }
    }
}

class Customer {
    private String name;
    private Account account;

    public Customer(String name, double initialBalance, int accountNumber) {
        this.name = name;
        this.account = new Account(initialBalance, accountNumber);
    }

    public String getName() {
        return name;
    }

    public Account getAccount() {
        return account;
    }
}

class Bank {
    private Connection con;
    
    public Bank() throws SQLException {
        con = DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/banking", 
            "root", 
            "1111111"
        );
        System.out.println("Connection Created");
    }

    public void addCustomer(String name, double initialBalance) throws SQLException {
        String query = "INSERT INTO accounts (name, balance) VALUES (?, ?)";
        try (PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, name);
            stmt.setDouble(2, initialBalance);
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                int accountNumber = rs.getInt(1);
                System.out.println("Account created for " + name + " with Account Number: " + accountNumber);
            }
        }
    }

    public Customer findCustomer(int accountNumber) throws SQLException {
        String query = "SELECT * FROM accounts WHERE account_number = ?";
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setInt(1, accountNumber);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String name = rs.getString("name");
                double balance = rs.getDouble("balance");
                return new Customer(name, balance, accountNumber);
            }
        }
        return null;
    }

    public void deposit(int accountNumber, double amount) throws SQLException {
        Customer customer = findCustomer(accountNumber);
        if (customer != null) {
            customer.getAccount().deposit(amount, con);
        } else {
            System.out.println("Account not found.");
        }
    }

    public void withdraw(int accountNumber, double amount) throws SQLException {
        Customer customer = findCustomer(accountNumber);
        if (customer != null) {
            customer.getAccount().withdraw(amount, con);
        } else {
            System.out.println("Account not found.");
        }
    }

    public void displayAccountDetails(int accountNumber) throws SQLException {
        Customer customer = findCustomer(accountNumber);
        if (customer != null) {
            System.out.println("Customer Name: " + customer.getName());
            System.out.println("Account Number: " + customer.getAccount().getAccountNumber());
            System.out.println("Balance: $" + customer.getAccount().getBalance());
        } else {
            System.out.println("Account not found.");
        }
    }
}

public class Banking {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        try {
            Bank bank = new Bank();
            
            while (true) {
                System.out.println("\n--- Bank Management System ---");
                System.out.println("1. Add Account");
                System.out.println("2. Deposit Money");
                System.out.println("3. Withdraw Money");
                System.out.println("4. Display Account Details");
                System.out.println("5. Exit");
                System.out.print("Enter your choice: ");
                int choice = scanner.nextInt();

                // Handle remaining newline character from nextInt
                scanner.nextLine();  // Consume the newline character

                switch (choice) {
                    case 1:
                        System.out.print("Enter customer name: ");
                        String name = scanner.nextLine(); // Use nextLine() for strings
                        System.out.print("Enter initial deposit: ");
                        double initialBalance = scanner.nextDouble();
                        bank.addCustomer(name, initialBalance);
                        break;
                    case 2:
                        System.out.print("Enter account number: ");
                        int accountNumber = scanner.nextInt();
                        System.out.print("Enter deposit amount: ");
                        double depositAmount = scanner.nextDouble();
                        bank.deposit(accountNumber, depositAmount);
                        break;
                    case 3:
                        System.out.print("Enter account number: ");
                        accountNumber = scanner.nextInt();
                        System.out.print("Enter withdrawal amount: ");
                        double withdrawAmount = scanner.nextDouble();
                        bank.withdraw(accountNumber, withdrawAmount);
                        break;
                    case 4:
                        System.out.print("Enter account number: ");
                        accountNumber = scanner.nextInt();
                        bank.displayAccountDetails(accountNumber);
                        break;
                    case 5:
                        System.out.println("Exiting...");
                        scanner.close();
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

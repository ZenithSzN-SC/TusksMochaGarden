package sample.tusksmochagarden;

import java.sql.Date;

/**
 * The customersData class represents the data model for a customer transaction in the Tusk's Mocha Garden application.
 * This class includes details such as the customer transaction ID, customer ID, total amount, date of the transaction, and the employee's username who handled the transaction.
 */
public class customersData {

    // Fields representing the customer's transaction data
    private Integer id;            // Unique identifier for the customer transaction in the database
    private Integer customerID;    // Unique identifier for the customer
    private Double total;          // Total amount of the transaction
    private Date date;             // Date of the transaction
    private String emUsername;     // Username of the employee who handled the transaction

    /**
     * Constructor for creating a customersData object with full transaction details.
     *
     * @param id           Unique identifier for the customer transaction.
     * @param customerID   Unique identifier for the customer.
     * @param total        Total amount of the transaction.
     * @param date         Date of the transaction.
     * @param emUsername   Username of the employee who handled the transaction.
     */
    public customersData(Integer id, Integer customerID, Double total,
                         Date date, String emUsername) {
        this.id = id;
        this.customerID = customerID;
        this.total = total;
        this.date = date;
        this.emUsername = emUsername;
    }

    // Getter methods for accessing the customer's transaction data

    /**
     * Gets the unique identifier for the customer transaction.
     *
     * @return the transaction ID
     */
    public Integer getId() {
        return id;
    }

    /**
     * Gets the unique identifier for the customer.
     *
     * @return the customer ID
     */
    public Integer getCustomerID() {
        return customerID;
    }

    /**
     * Gets the total amount of the transaction.
     *
     * @return the transaction total
     */
    public Double getTotal() {
        return total;
    }

    /**
     * Gets the date of the transaction.
     *
     * @return the transaction date
     */
    public Date getDate() {
        return date;
    }

    /**
     * Gets the username of the employee who handled the transaction.
     *
     * @return the employee's username
     */
    public String getEmUsername() {
        return emUsername;
    }
}


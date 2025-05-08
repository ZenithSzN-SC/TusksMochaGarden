package sample.tusksmochagarden;

import java.sql.Date;

/**
 * The productData class represents the data model for a product in the Tusk's Mocha Garden application.
 * This class includes product details such as ID, name, type, stock, price, status, image, date, and quantity.
 */
public class productData {

    // Fields representing the product's data
    private Integer id;              // Unique identifier for the product in the database
    private String productId;        // Unique product ID, usually provided by the admin
    private String productName;      // Name of the product
    private String type;             // Type/category of the product (e.g., Drinks, Meals)
    private Integer stock;           // Available stock of the product
    private Double price;            // Price of the product
    private String status;           // Availability status of the product (e.g., Available, Unavailable)
    private String image;            // Path to the product's image file
    private Date date;               // Date when the product was added or last updated
    private Integer quantity;        // Quantity of the product ordered by a customer (used in the order context)

    /**
     * Constructor for creating a productData object with full product details.
     *
     * @param id          Unique identifier for the product.
     * @param productId   Unique product ID.
     * @param productName Name of the product.
     * @param type        Type/category of the product.
     * @param stock       Available stock of the product.
     * @param price       Price of the product.
     * @param status      Availability status of the product.
     * @param image       Path to the product's image file.
     * @param date        Date when the product was added or last updated.
     */
    public productData(Integer id, String productId, String productName,
                       String type, Integer stock, Double price,
                       String status, String image, Date date) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.type = type;
        this.stock = stock;
        this.price = price;
        this.status = status;
        this.image = image;
        this.date = date;
    }

    /**
     * Constructor for creating a productData object for use in order contexts.
     * This constructor omits the stock and status fields, but includes quantity.
     *
     * @param id          Unique identifier for the product.
     * @param productId   Unique product ID.
     * @param productName Name of the product.
     * @param type        Type/category of the product.
     * @param quantity    Quantity of the product ordered by a customer.
     * @param price       Price of the product.
     * @param image       Path to the product's image file.
     * @param date        Date when the product was added or last updated.
     */
    public productData(Integer id, String productId, String productName,
                       String type, Integer quantity, Double price,
                       String image, Date date) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.type = type;
        this.price = price;
        this.image = image;
        this.date = date;
        this.quantity = quantity;
    }

    // Getter methods for accessing the product's data
    public Integer getId() {
        return id;
    }

    public String getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getType() {
        return type;
    }

    public Integer getStock() {
        return stock;
    }

    public Double getPrice() {
        return price;
    }

    public String getStatus() {
        return status;
    }

    public String getImage() {
        return image;
    }

    public Date getDate() {
        return date;
    }

    public Integer getQuantity() {
        return quantity;
    }
}


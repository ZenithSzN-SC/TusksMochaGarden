package com.tusksmochagarden.controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

import com.tusksmochagarden.model.Product;

/**
 * Tap-to-add menu card for the Register (POS) view.
 * Drinks open the customization panel; everything else adds one straight to the order.
 */
public class CardProductController implements Initializable {

    @FXML
    private AnchorPane card_form;

    @FXML
    private Label prod_name;

    @FXML
    private Label prod_price;

    @FXML
    private ImageView prod_imageView;

    private Product prodData;
    private MainFormController mainForm;

    public void setData(Product prodData) {
        this.prodData = prodData;
        prod_name.setText(prodData.getProductName());
        prod_price.setText(String.format("£%.2f", prodData.getPrice()));

        if (prodData.getImage() != null && !prodData.getImage().isEmpty()) {
            Image image = new Image("file:" + prodData.getImage(), 180, 96, true, true);
            prod_imageView.setImage(image);
        }
    }

    public void setMainFormController(MainFormController MainFormController) {
        this.mainForm = MainFormController;
    }

    @FXML
    private void cardTapped() {
        if (mainForm != null && prodData != null) {
            mainForm.menuItemTapped(prodData);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }
}

package ui;

import handler.AgentTransactionHandler;
import handler.ListingTransactionHandler;
import types.AccountMode;
import types.ListingType;
import types.PropertyType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

public class ListingFrame extends JFrame implements ActionListener {
    private EstateUI parent;
    private AccountMode mode;
    private ListingTransactionHandler handler;
    private JPanel listingPanel, propertyPanel;
    private JComboBox<String> listingType, propertyType;
    private JTextField idField, priceField, addressField;
    private JRadioButton higher, lower;
    private JScrollPane leftHolder, rightHolder;
    private JButton changeMode;
    private String currkey;
    private boolean isListing;

    public ListingFrame(EstateUI parent, ListingTransactionHandler handler, AccountMode mode) {
        this.parent = parent;
        this.mode = mode;
        this.isListing = true;
        this.handler = handler;
        setupPanel();
    }

    private void setupPanel(){
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        GridBagLayout gb = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        this.setLayout(gb);


        c.gridy = 0;
        c.gridx = 1;
        c.anchor = GridBagConstraints.NORTHEAST;
        JButton mainMenu = new JButton("Menu");
        mainMenu.addActionListener(this);
        gb.setConstraints(mainMenu, c);;

        c.gridx = 0;
        c.weighty = 0.1;
        c.anchor = GridBagConstraints.NORTHWEST;
        changeMode = new JButton("Search Properties");
        changeMode.addActionListener(this);
        gb.setConstraints(changeMode, c);

        setupListingSearch();
        setupPropertySearch();
        c.fill = GridBagConstraints.NONE;
        c.gridy = 2;
        c.gridx = 0;
        c.weighty = 0.5;
        gb.setConstraints(listingPanel, c);
        gb.setConstraints(propertyPanel, c);

        c.gridy = 3;
        c.weighty = 0.1;
        JButton submit = new JButton("Submit");
        submit.addActionListener(this);
        gb.setConstraints(submit, c);

        leftHolder = new JScrollPane();
        c.gridy = 4;
        c.gridx = 0;
        gb.setConstraints(leftHolder, c);

        rightHolder = new JScrollPane();
        c.gridx = 1;
        gb.setConstraints(rightHolder, c);
        this.add(mainMenu);
        this.add(changeMode);
        this.add(listingPanel);
        this.add(propertyPanel);
        this.add(leftHolder);
        this.add(rightHolder);

        this.add(submit);
        propertyPanel.setVisible(false);

        Dimension d = this.getToolkit().getScreenSize();
        Rectangle r = this.getBounds();
        this.setLocation( (d.width - r.width)/2, (d.height - r.height)/2 );
        this.pack();
        this.setVisible(false);
    }


    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        switch(actionEvent.getActionCommand()) {
            case "Search Listings" :
                changeMode(true);
                break;
            case "Search Properties" :
                changeMode(false);
                break;
            case "Menu":
                this.setVisible(false);
                parent.switchFrame(EstateUI.states.Menu.name());
                break;
            case "Submit" :
                // TODO USE HANDLER TO PERFORM SEARCH!
                // Obtain result here
                this.remove(leftHolder);
                JTable result = new JTable(); // stub
                if (isListing) {
                    ListingType lType = ListingType.ANY;
                    switch (listingType.getSelectedItem().toString()){
                        case "SELLING":
                            lType = ListingType.SELLING;
                            break;
                        case "RENTAL":
                            lType = ListingType.RENTAL;
                            break;
                        default:
                            break;
                    }
                    result = this.handler.getListingByCondition(idField.getText(), priceField.getText(), higher.isSelected(), lType);
                } else {
                    PropertyType pType = PropertyType.ANY;
                    switch (listingType.getSelectedItem().toString()){
                        case "APARTMENT":
                            pType = PropertyType.Apartment;
                            break;
                        case "RENTAL":
                            pType = PropertyType.Office;
                            break;
                        case "HOUSE":
                            pType = PropertyType.House;
                            break;
                        default:
                            break;
                    }
                    result = this.handler.getPropertyByCondition(addressField.getText(), pType);
                }
                leftHolder = new JScrollPane(result);
                leftHolder.setBounds(10, 150, 400, 100);
                this.add(leftHolder);
                this.repaint();

                JTable finalResult = result;
                result.addMouseMotionListener(new MouseMotionListener() {
                    int hoveredRow = -1, hoveredColumn = -1;
                    @Override
                    public void mouseMoved(MouseEvent e) {
                        // Use functions in this.handler to do lookup
                        Point p = e.getPoint();
                        hoveredRow = finalResult.rowAtPoint(p);
                        hoveredColumn = finalResult.columnAtPoint(p);
                        finalResult.setRowSelectionInterval(hoveredRow, hoveredRow);
                        reverseLookup(finalResult.getValueAt(hoveredRow, 0).toString());
                        finalResult.repaint();
                    }
                    @Override
                    public void mouseDragged(MouseEvent e) {
                        hoveredRow = hoveredColumn = -1;
                        finalResult.repaint();
                    }
                });
                leftHolder.getViewport().removeAll();
                leftHolder.getViewport().add(result);
                leftHolder.repaint();
                this.pack();
                break;
        }
    }

    private void reverseLookup(String key) {
        // TODO DO REVERSE LOOKUP ON LEFTPANEL
        // If type is listing, then lookup property with the listing_id.
        // If type is property, then lookup listing with the property address.
        if (!key.equals(currkey)) {
            JTable result = new JTable(); //stub
            currkey = key;
            if (isListing) {
                // Use functions in this.handler to do lookup
                result = this.handler.getPropertyByListing(Integer.parseInt(currkey));
            } else {
                result = this.handler.getListingByProperty(currkey);

            }
            rightHolder.getViewport().removeAll();
            rightHolder.getViewport().add(result);
            rightHolder.repaint();
            this.pack();
        }
    }

    private void changeMode(boolean isListing) {
        this.isListing = isListing;
        this.changeMode.setText(isListing? "Search Properties": "Search Listings");
        listingPanel.setVisible(isListing);
        propertyPanel.setVisible(!isListing);
        this.repaint();
    }

    public void setupListingSearch(){
        listingPanel = new JPanel();
        listingPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.gridy = 0;
        c.gridx = 0;
        c.insets = new Insets(0, 0, 0, 12);
        c.anchor = GridBagConstraints.WEST;
        JLabel idPrompt = new JLabel("Listing id");
        listingPanel.add(idPrompt, c);

        c.gridx = 1;
        idField = new JTextField(10);
        listingPanel.add(idField, c);

        c.gridx = 0;
        c.gridy = 1;
        JLabel pricePrompt = new JLabel("Price");
        listingPanel.add(pricePrompt, c);


        higher = new JRadioButton("Higher than");
        lower = new JRadioButton("Lower than");
        ButtonGroup g = new ButtonGroup();
        g.add(higher);
        g.add(lower);
        g.setSelected(higher.getModel(), true);

        c.gridx = 1;
        listingPanel.add(higher, c);
        c.gridx = 2;
        listingPanel.add(lower, c);

        c.gridx = 3;
        priceField = new JTextField(10);
        listingPanel.add(priceField, c);

        c.gridy = 2;
        c.gridx = 0;
        JLabel typePrompt = new JLabel("Listing type: ");
        listingPanel.add(typePrompt, c);

        c.gridx = 1;
        c.gridwidth = GridBagConstraints.RELATIVE;
        String[] listingTypes = {"ANY", "SELLING", "RENTAL"};
        listingType = new JComboBox<>(listingTypes);
        listingType.setSelectedIndex(0);
        listingPanel.add(listingType, c);
    }

    private void setupPropertySearch(){
        propertyPanel = new JPanel();
        propertyPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.gridy = 0;
        c.gridx = 0;
        c.insets = new Insets(0, 0, 0, 12);
        c.anchor = GridBagConstraints.WEST;
        JLabel idPrompt = new JLabel("Address: ");
        propertyPanel.add(idPrompt, c);

        c.gridx = 1;
        addressField = new JTextField(10);
        propertyPanel.add(addressField, c);

        c.gridy = 1;
        c.gridx = 0;

        JLabel typePrompt = new JLabel("Property type: ");
        propertyPanel.add(typePrompt, c);

        c.gridx = 1;
        c.gridwidth = GridBagConstraints.RELATIVE;
        String[] propertyTypes = {"ANY", "HOUSE", "APARTMENT", "OFFICE"};
        propertyType = new JComboBox<>(propertyTypes);
        propertyType.setSelectedIndex(0);
        propertyPanel.add(propertyType, c);
    }
}

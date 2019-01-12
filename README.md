# Data Storage

# Book Store App

## Description: 
Design and create the structure of an Inventory App which would allow a store to keep track of its inventory.

## Requirements:
* There exists a contract class that defines name of table and constants.
* Inside the contract class, there is an inner class for each table created.
* The contract contains at minimum constants for the Product Name, Price, Quantity, Supplier Name, and Supplier Phone Number.
* There exists a subclass of SQLiteOpenHelper that overrides onCreate() and onUpgrade().
* There is a single insert method that adds:
 * Product Name
 * Price
 * Quantity
 * Supplier Name
 * Supplier Phone Number
* It is required that there are at least 2 different data types (e.g. INTEGER, STRING).
* There is a single method that uses a Cursor from the database to perform a query on the table to retrieve at least one column of data. Also the method should close the Cursor after it's done reading from it.
* The app contains activities for the user to:
 * Add Inventory
 * See Product Details
 * Edit Product Details
* See a list of all inventory from a Main Activity
* The Main Activity displaying the list of current inventory contains a ListView that populates with the current products stored in the table. Each list item displays the Product Name, Price, and Quantity.
* Each list item also contains a SaleButton that reduces the total quantity of that particular product by one (include logic so that no negative quantities are displayed).
* The Main Activity contains an Add Product Button prompts the user for product information and supplier information which are then properly stored in the table.
* The Product Detail Layout displays the Product Name, Price, Quantity, Supplier Name, and Supplier Phone Number that's stored in the database.
* The Product Detail Layout also contains buttons that increase and decrease the available quantity displayed.
* Add a check in the code to ensure that no negative quantities display (zero is the lowest amount).
* The Product Detail Layout contains a button to delete the product record entirely.
* The Product Detail Layout contains a button to order from the supplier - contact the supplier via an intent to a phone app using the Supplier Phone Number stored in the database.
* When there is no information to display in the database, the layout displays a TextView with instructions on how to populate the database (e.g. what should be entered in the field, which fields are required).
* When user inputs product information (quantity, price, name), instead of erroring out, the app includes logic to validate that no null values are accepted. If a null value is inputted, add a Toast that prompts the user to input the correct information before they can continue.

<img src="https://github.com/Limmonica/BookStoreApp/blob/stage-two/Udacity_BookStoreApp_P1.png"  width="250" height=""> <img src="https://github.com/Limmonica/BookStoreApp/blob/stage-two/Udacity_BookStoreApp_P2.png"  width="250" height=""> <img src="https://github.com/Limmonica/BookStoreApp/blob/stage-two/Udacity_BookStoreApp_P3.png"  width="250" height="">
<img src="https://github.com/Limmonica/BookStoreApp/blob/stage-two/Udacity_BookStoreApp_P4.png"  width="250" height=""> <img src="https://github.com/Limmonica/BookStoreApp/blob/stage-two/Udacity_BookStoreApp_P5.png"  width="250" height=""> <img src="https://github.com/Limmonica/BookStoreApp/blob/stage-two/Udacity_BookStoreApp_P6.png"  width="250" height="">


# --- !Ups

CREATE TABLE "users" (
    user_id INTEGER PRIMARY KEY,
    email TEXT NOT NULL UNIQUE,
	first_name TEXT NOT NULL,
	last_name TEXT NOT NULL,
	password TEXT NOT NULL,
	role TEXT CHECK( role IN ('staff', 'customer') ) NOT NULL
);

CREATE TABLE "product_types" (
    product_type_id INTEGER PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT NOT NULL
);

CREATE TABLE "products" (
    product_id INTEGER PRIMARY KEY,
    product_type_id INTEGER NOT NULL,
    name TEXT NOT NULL,
    price REAL NOT NULL,
    description TEXT NOT NULL,
    quantity INTEGER CHECK( quantity >= 0 ) NOT NULL,
    FOREIGN KEY (product_type_id)
        REFERENCES product_types (product_type_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

CREATE TABLE "orders" (
    order_id INTEGER PRIMARY KEY,
    customer_id INTEGER NOT NULL,
    status TEXT CHECK( status IN ('awaiting_payment', 'pack', 'sent', 'delivered') ) NOT NULL,
    FOREIGN KEY (customer_id)
        REFERENCES "users" (user_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

CREATE TABLE "order_items" (
    order_item_id INTEGER PRIMARY KEY,
    order_id INTEGER NOT NULL,
    product_id INTEGER NOT NULL,
    quantity INTEGER NOT NULL,
    price REAL NOT NULL,
    FOREIGN KEY (order_id)
        REFERENCES "orders" (order_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    FOREIGN KEY (product_id)
        REFERENCES "products" (product_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

CREATE TABLE "invoices" (
    invoice_id INTEGER PRIMARY KEY,
    order_id INTEGER NOT NULL,
    total_cost REAL NOT NULL,
    date TEXT NOT NULL,
    FOREIGN KEY (order_id)
        REFERENCES "orders" (order_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

CREATE TABLE "payments" (
    payment_id INTEGER PRIMARY KEY,
    invoice_id INTEGER NOT NULL,
    date TEXT NOT NULL,
    sum REAL NOT NULL,
    FOREIGN KEY (invoice_id)
        REFERENCES "invoices" (invoice_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

CREATE TABLE "shipments" (
    shipment_id INTEGER PRIMARY KEY,
    order_id INTEGER NOT NULL,
    date TEXT NOT NULL,
    tracking_code TEXT NOT NULL,
    FOREIGN KEY (order_id)
        REFERENCES "orders" (order_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

CREATE TABLE "reviews" (
    review_id INTEGER PRIMARY KEY,
    product_id INTEGER NOT NULL,
    author_id INTEGER,
    content TEXT NOT NULL,
    FOREIGN KEY (product_id)
        REFERENCES "products" (product_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    FOREIGN KEY (author_id)
        REFERENCES "users" (user_id)
        ON UPDATE CASCADE
        ON DELETE SET NULL
);

CREATE TABLE "faq_notes" (
    faq_note_id INTEGER PRIMARY KEY,
    title TEXT NOT NULL,
    message TEXT NOT NULL
);

# --- !Downs

DROP TABLE "faq_notes";
DROP TABLE "reviews";
DROP TABLE "shipments";
DROP TABLE "payments";
DROP TABLE "invoices";
DROP TABLE "order_items";
DROP TABLE "orders";
DROP TABLE "products";
DROP TABLE "product_types";
DROP TABLE "users";

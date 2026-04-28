use menshop;

-- Banner Trang chủ
CREATE TABLE banner (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255),
    image_url VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'INACTIVE'
);

-- Quyền
CREATE TABLE role (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

-- Tài khoản 
CREATE TABLE user (
    id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(150),
    username VARCHAR(150) UNIQUE,
    password VARCHAR(255),
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(20),
    avatar VARCHAR(255),
    gender ENUM('MALE', 'FEMALE', 'OTHER'),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    status ENUM('ACTIVE','LOCKED') DEFAULT 'ACTIVE'
);

-- Phân quyền
CREATE TABLE user_role (
    user_id INT,
    role_id INT,
    PRIMARY KEY(user_id, role_id),
    FOREIGN KEY(user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY(role_id) REFERENCES role(id) ON DELETE CASCADE
);

-- Địa chỉ nhận hàng
CREATE TABLE address (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    receiver_name VARCHAR(150),
    phone VARCHAR(20),
    address VARCHAR(255),
    is_default BOOLEAN DEFAULT FALSE,
    deleted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY(user_id) REFERENCES user(id)
);

-- Danh mục sản phẩm
CREATE TABLE category (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    image_url VARCHAR(255),
    status ENUM('ACTIVE','INACTIVE') DEFAULT 'ACTIVE',
	parent_id INT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY(parent_id) REFERENCES category(id)
);

-- Shop
CREATE TABLE shop (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    logo VARCHAR(255),
    -- banner VARCHAR(255),
    phone VARCHAR(255),
    email VARCHAR(255),
    address VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    status ENUM('ACTIVE', 'INACTIVE', 'PENDING', 'BANNED', 'REJECTED') DEFAULT 'PENDING',

    FOREIGN KEY(user_id) REFERENCES user(id)
);

-- Thương hiệu
CREATE TABLE brand (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150)
);

-- Sản phẩm
CREATE TABLE product (
    id INT AUTO_INCREMENT PRIMARY KEY,
    shop_id INT,
    category_id INT,
    brand_id INT,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(10,2),
    sale_price DECIMAL(10,2),
    description TEXT,
    attributes_json JSON NULL,
    view_count INT DEFAULT 0,
    sold_count INT DEFAULT 0,
    rating_avg DECIMAL(3,2) DEFAULT 0.0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    status ENUM('PENDING','ACTIVE','INACTIVE', 'OUT_OF_STOCK', 'REJECTED') DEFAULT 'PENDING',

    INDEX idx_product_status (status),
    INDEX idx_product_category (category_id),
    
    FOREIGN KEY(shop_id) REFERENCES shop(id),
    FOREIGN KEY(category_id) REFERENCES category(id),
    FOREIGN KEY(brand_id) REFERENCES brand(id)
);

-- Ảnh sản phẩm
CREATE TABLE product_image (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT,
    image_url VARCHAR(255),

    FOREIGN KEY(product_id) REFERENCES product(id)
);

-- Variant
CREATE TABLE product_variant (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT,
    -- image VARCHAR(255),
    sku VARCHAR(100) UNIQUE, 
    color VARCHAR(50), 
    size VARCHAR(50),
    stock INT DEFAULT 0,

    FOREIGN KEY(product_id) REFERENCES product(id)
);



-- Chi tiết giỏ hàng
CREATE TABLE cart_item (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    variant_id INT,
    quantity INT NOT NULL CHECK (quantity > 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE (user_id, variant_id), 

    FOREIGN KEY(user_id) REFERENCES user(id),
    FOREIGN KEY(variant_id) REFERENCES product_variant(id)
);

-- Đơn hàng
CREATE TABLE orders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(255) NOT NULL UNIQUE,
    user_id INT NOT NULL,
    shop_id INT NOT NULL,
    receiver_name VARCHAR(100) NOT NULL,
    receiver_phone VARCHAR(20) NOT NULL,
    note VARCHAR(255),
    address VARCHAR(255) NOT NULL,
    total_price DECIMAL(12,2) NOT NULL,
    shipping_fee DECIMAL(12,2) NOT NULL,
    final_total DECIMAL(12,2) NOT NULL,
    payment_method ENUM('COD','VNPAY') NOT NULL,
    payment_status ENUM('UNPAID','PAID','FAILED','REFUNDED') DEFAULT 'UNPAID',
    payment_group_id INT,

    status ENUM('PENDING','CONFIRMED','SHIPPING','DELIVERED','CANCELLED') DEFAULT 'PENDING',

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY(user_id) REFERENCES user(id),
    FOREIGN KEY(shop_id) REFERENCES shop(id),
    FOREIGN KEY(payment_group_id) REFERENCES payment_group(id),
);

-- Timeline đơn hàng
CREATE TABLE order_status_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    status ENUM('PENDING','CONFIRMED','SHIPPING','DELIVERED','CANCELLED') NOT NULL DEFAULT 'PENDING',
    note VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- Chi tiết đơn hàng
CREATE TABLE order_item (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    shop_id INT NOT NULL,
    variant_id INT NOT NULL,
    
    unit_price DECIMAL(12,2) NOT NULL,
    quantity INT NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,

    FOREIGN KEY(order_id) REFERENCES orders(id),
    FOREIGN KEY(variant_id) REFERENCES product_variant(id),
    FOREIGN KEY(shop_id) REFERENCES shop(id)
);

-- Thanh toán Chung
CREATE TABLE payment_group (
    id INT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(255) UNIQUE,
    user_id INT,
    method ENUM('COD','VNPAY') NOT NULL,
    total_amount DECIMAL(12,2) NOT NULL,
    status ENUM('PENDING','PAID','FAILED','REFUNDED') DEFAULT 'PENDING',
    transaction_code VARCHAR(255),
    paid_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY(user_id) REFERENCES user(id)
);

-- Đánh giá
CREATE TABLE review (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    product_id INT NOT NULL,
    order_id INT NOT NULL,
    rating INT CHECK (rating BETWEEN 1 AND 5),
    content TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_review_user FOREIGN KEY (user_id) REFERENCES user(id),
    CONSTRAINT fk_review_product FOREIGN KEY (product_id) REFERENCES product(id),
    CONSTRAINT fk_review_order FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT uq_review_user_product_order UNIQUE (user_id, product_id, order_id)
);

-- Ảnh đánh giá
CREATE TABLE review_image (
    id INT PRIMARY KEY AUTO_INCREMENT,
    review_id INT NOT NULL,
    image_url VARCHAR(500) NOT NULL,

    CONSTRAINT fk_review_image_review
        FOREIGN KEY (review_id) REFERENCES review(id)
        ON DELETE CASCADE
);

-- Flash Sale
CREATE TABLE flash_sale (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    is_disabled BOOLEAN DEFAULT FALSE,
    -- status ENUM('UPCOMING','ACTIVE','ENDED','DISABLED') DEFAULT 'UPCOMING',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Sản phẩm có trong Flash Sale
CREATE TABLE flash_sale_product (
    id INT AUTO_INCREMENT PRIMARY KEY,
    flash_sale_id INT,
    product_id INT,
    sale_price DECIMAL(10,2),
    -- sold_count INT DEFAULT 0,
    -- status ENUM('PENDING','APPROVED','REJECTED') DEFAULT 'PENDING',

    FOREIGN KEY(flash_sale_id) REFERENCES flash_sale(id),
    FOREIGN KEY(product_id) REFERENCES product(id)
);

-- Thông báo
CREATE TABLE notification (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    order_id INT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message VARCHAR(500) NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_notification_user
        FOREIGN KEY (user_id) REFERENCES user(id),

    CONSTRAINT fk_notification_order
        FOREIGN KEY (order_id) REFERENCES orders(id)
);

-- OTP Reset password
CREATE TABLE otp_codes (
	id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    otp VARCHAR(10) NOT NULL,
    expired_at DATETIME,
    used BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_interaction (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    product_id INT NOT NULL,
    interaction_type ENUM('VIEW', 'ADD_TO_CART', 'PURCHASE') NOT NULL,
    -- Điểm số tương ứng: VIEW=1, CART=3, PURCHASE=5
    weight_score DECIMAL(5,2) DEFAULT 1.0, 
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_user_id (user_id),
    INDEX idx_product_id (product_id),
    INDEX idx_created_at (created_at),

    FOREIGN KEY(user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY(product_id) REFERENCES product(id) ON DELETE CASCADE
);

-- Chat Room
CREATE TABLE chat_room (
    id INT PRIMARY KEY AUTO_INCREMENT,
    customer_id INT NOT NULL,
    shop_id INT NOT NULL,
    last_message TEXT NULL,
    last_message_at DATETIME NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_chat_room_customer FOREIGN KEY (customer_id) REFERENCES user(id),
    CONSTRAINT fk_chat_room_shop FOREIGN KEY (shop_id) REFERENCES shop(id),
    CONSTRAINT uq_chat_room_customer_shop UNIQUE (customer_id, shop_id)
);

-- Chat Message
CREATE TABLE chat_message (
    id INT PRIMARY KEY AUTO_INCREMENT,
    room_id INT NOT NULL,
    sender_id INT NOT NULL,
    content TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_chat_message_room FOREIGN KEY (room_id) REFERENCES chat_room(id) ON DELETE CASCADE,

    CONSTRAINT fk_chat_message_sender FOREIGN KEY (sender_id) REFERENCES user(id)
);

-- INDEX
CREATE INDEX idx_chat_room_customer ON chat_room(customer_id);
CREATE INDEX idx_chat_room_shop ON chat_room(shop_id);
CREATE INDEX idx_chat_message_room ON chat_message(room_id);
CREATE INDEX idx_chat_message_product ON chat_message(product_id);
CREATE INDEX idx_chat_message_created_at ON chat_message(created_at);
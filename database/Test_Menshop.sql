-- MySQL dump 10.13  Distrib 8.0.41, for Win64 (x86_64)
--
-- Host: localhost    Database: menshop
-- ------------------------------------------------------
-- Server version	8.0.41

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `address`
--

DROP TABLE IF EXISTS `address`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `address` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int DEFAULT NULL,
  `receiver_name` varchar(150) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `is_default` tinyint(1) DEFAULT '0',
  `deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `address_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `address`
--

LOCK TABLES `address` WRITE;
/*!40000 ALTER TABLE `address` DISABLE KEYS */;
INSERT INTO `address` VALUES (1,3,'Khách hàng 1','0382079152','xã Hải Anh, tỉnh Ninh Bình',0,0);
/*!40000 ALTER TABLE `address` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `banner`
--

DROP TABLE IF EXISTS `banner`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `banner` (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(255) DEFAULT NULL,
  `image_url` varchar(255) DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `status` enum('ACTIVE','INACTIVE') DEFAULT 'INACTIVE',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `banner`
--

LOCK TABLES `banner` WRITE;
/*!40000 ALTER TABLE `banner` DISABLE KEYS */;
INSERT INTO `banner` VALUES (1,'Chúc mừng năm mới','https://res.cloudinary.com/dcjraarbb/image/upload/v1776908460/d3c3osnryrzphahjzq6f.webp','2026-04-23 08:41:15','ACTIVE'),(2,'Men Casual','https://res.cloudinary.com/dcjraarbb/image/upload/v1776908720/sn9qldfd0nxjb0zvserg.webp','2026-04-23 08:45:35','ACTIVE'),(3,'Men Jacket','https://res.cloudinary.com/dcjraarbb/image/upload/v1776908760/qtjoe51mmebbsldbjt7n.webp','2026-04-23 08:46:14','ACTIVE'),(4,'Quý ông','https://res.cloudinary.com/dcjraarbb/image/upload/v1776908813/pex33ajjib5fgjn3acdy.webp','2026-04-23 08:47:08','ACTIVE');
/*!40000 ALTER TABLE `banner` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `brand`
--

DROP TABLE IF EXISTS `brand`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `brand` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(150) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `brand`
--

LOCK TABLES `brand` WRITE;
/*!40000 ALTER TABLE `brand` DISABLE KEYS */;
INSERT INTO `brand` VALUES (1,'VESCA');
/*!40000 ALTER TABLE `brand` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cart_item`
--

DROP TABLE IF EXISTS `cart_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cart_item` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int DEFAULT NULL,
  `variant_id` int DEFAULT NULL,
  `quantity` int NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_id` (`user_id`,`variant_id`),
  KEY `variant_id` (`variant_id`),
  CONSTRAINT `cart_item_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `cart_item_ibfk_2` FOREIGN KEY (`variant_id`) REFERENCES `product_variant` (`id`),
  CONSTRAINT `cart_item_chk_1` CHECK ((`quantity` > 0))
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cart_item`
--

LOCK TABLES `cart_item` WRITE;
/*!40000 ALTER TABLE `cart_item` DISABLE KEYS */;
INSERT INTO `cart_item` VALUES (2,3,6,1,'2026-04-24 05:53:51');
/*!40000 ALTER TABLE `cart_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `category`
--

DROP TABLE IF EXISTS `category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `category` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(150) NOT NULL,
  `image_url` varchar(255) DEFAULT NULL,
  `status` enum('ACTIVE','INACTIVE') DEFAULT 'ACTIVE',
  `parent_id` int DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `parent_id` (`parent_id`),
  CONSTRAINT `category_ibfk_1` FOREIGN KEY (`parent_id`) REFERENCES `category` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `category`
--

LOCK TABLES `category` WRITE;
/*!40000 ALTER TABLE `category` DISABLE KEYS */;
INSERT INTO `category` VALUES (1,'Áo thun ','https://res.cloudinary.com/dcjraarbb/image/upload/v1776908874/tmrfwtyxb05ihzmowvi5.jpg','ACTIVE',NULL,'2026-04-23 08:48:12'),(2,'Áo sơ mi','https://res.cloudinary.com/dcjraarbb/image/upload/v1776908913/ccgadbu25zpzb5m6vp8u.jpg','ACTIVE',NULL,'2026-04-23 08:48:51'),(3,'Áo nỉ - Áo len','https://res.cloudinary.com/dcjraarbb/image/upload/v1776908950/oquqzs7pnpmeyxnlclvv.jpg','ACTIVE',NULL,'2026-04-23 08:49:25'),(4,'Áo thun ngắn tay có cổ','https://res.cloudinary.com/dcjraarbb/image/upload/v1776909004/ao1ymfod1dfs305twbjs.jpg','ACTIVE',1,'2026-04-23 08:50:20'),(5,'Áo thun nam dài tay không cổ','https://res.cloudinary.com/dcjraarbb/image/upload/v1776909038/lw9m1cbzsc1n2zh3bps2.jpg','ACTIVE',1,'2026-04-23 08:50:55'),(6,'Áo sơ mi dài tay','https://res.cloudinary.com/dcjraarbb/image/upload/v1776909080/nk1uvnsxaecilxy6ur6h.png','ACTIVE',2,'2026-04-23 08:51:35'),(7,'Áo nỉ nam','https://res.cloudinary.com/dcjraarbb/image/upload/v1776909113/m2ks4qwopkaonmsjzg6b.png','ACTIVE',3,'2026-04-23 08:52:09');
/*!40000 ALTER TABLE `category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `flash_sale`
--

DROP TABLE IF EXISTS `flash_sale`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flash_sale` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `description` text,
  `start_time` datetime NOT NULL,
  `end_time` datetime NOT NULL,
  `is_disabled` tinyint(1) DEFAULT '0',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flash_sale`
--

LOCK TABLES `flash_sale` WRITE;
/*!40000 ALTER TABLE `flash_sale` DISABLE KEYS */;
INSERT INTO `flash_sale` VALUES (1,'Siêu Sale Cuối Tuần','Siêu Sale cuối tuần tràn ngập ưu đãi','2026-04-23 15:59:00','2026-04-23 15:59:37',0,'2026-04-23 15:59:13'),(2,'Mùa hè không nóng','Mùa hè không nóng','2026-04-23 16:13:00','2026-04-27 00:00:00',0,'2026-04-23 16:10:45');
/*!40000 ALTER TABLE `flash_sale` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `flash_sale_product`
--

DROP TABLE IF EXISTS `flash_sale_product`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flash_sale_product` (
  `id` int NOT NULL AUTO_INCREMENT,
  `flash_sale_id` int DEFAULT NULL,
  `product_id` int DEFAULT NULL,
  `sale_price` decimal(10,2) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `flash_sale_id` (`flash_sale_id`),
  KEY `product_id` (`product_id`),
  CONSTRAINT `flash_sale_product_ibfk_1` FOREIGN KEY (`flash_sale_id`) REFERENCES `flash_sale` (`id`),
  CONSTRAINT `flash_sale_product_ibfk_2` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flash_sale_product`
--

LOCK TABLES `flash_sale_product` WRITE;
/*!40000 ALTER TABLE `flash_sale_product` DISABLE KEYS */;
INSERT INTO `flash_sale_product` VALUES (1,2,1,60000.00);
/*!40000 ALTER TABLE `flash_sale_product` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notification`
--

DROP TABLE IF EXISTS `notification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notification` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `order_id` int DEFAULT NULL,
  `type` varchar(50) NOT NULL,
  `title` varchar(255) NOT NULL,
  `message` varchar(500) NOT NULL,
  `is_read` tinyint(1) DEFAULT '0',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_notification_user` (`user_id`),
  KEY `fk_notification_order` (`order_id`),
  CONSTRAINT `fk_notification_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`),
  CONSTRAINT `fk_notification_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notification`
--

LOCK TABLES `notification` WRITE;
/*!40000 ALTER TABLE `notification` DISABLE KEYS */;
INSERT INTO `notification` VALUES (1,2,1,'NEW_ORDER','Đơn hàng mới','Bạn có đơn hàng mới DH5697',0,'2026-04-23 15:07:16'),(2,2,2,'NEW_ORDER','Đơn hàng mới','Bạn có đơn hàng mới DH0019',0,'2026-04-23 15:24:50'),(3,2,3,'NEW_ORDER','Đơn hàng mới','Bạn có đơn hàng mới DH6919',0,'2026-04-23 15:47:07'),(4,1,NULL,'NEW_PRODUCT_PENDING','Có sản phẩm cần duyệt','Shop Thời trang Mạnh Kha vừa tạo sản phẩm mới',0,'2026-04-24 14:08:37'),(5,2,NULL,'PRODUCT_APPROVED','Sản phẩm đã được duyệt','Sản phẩm \"Áo Sơ Mi Form Rộng, Áo Sơ Mi Hale Oversize Vải Linen Cao Cấp\" đã được admin duyệt',0,'2026-04-24 14:20:53'),(6,1,NULL,'NEW_PRODUCT_PENDING','Có sản phẩm cần duyệt','Shop Thời trang Mạnh Kha vừa tạo sản phẩm mới',0,'2026-04-24 14:23:33'),(10,2,NULL,'PRODUCT_REJECTED','Sản phẩm bị từ chối','Sản phẩm \"Áo sơ mi O.D.I.N cộc tay Original, Áo sơmi nam ngắn tay form rộng\" chưa được duyệt',0,'2026-04-24 14:37:41'),(11,2,4,'NEW_ORDER','Đơn hàng mới','Bạn có đơn hàng mới DH5518',0,'2026-04-24 14:41:36'),(12,2,NULL,'BAD_REVIEW','Có đánh giá thấp','Sản phẩm \"Áo Sơ Mi Form Rộng, Áo Sơ Mi Hale Oversize Vải Linen Cao Cấp\" vừa nhận đánh giá 1 sao. Bạn nên kiểm tra và phản hồi.',0,'2026-04-24 14:45:17');
/*!40000 ALTER TABLE `notification` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_item`
--

DROP TABLE IF EXISTS `order_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_item` (
  `id` int NOT NULL AUTO_INCREMENT,
  `order_id` int NOT NULL,
  `shop_id` int NOT NULL,
  `variant_id` int NOT NULL,
  `unit_price` decimal(12,2) NOT NULL,
  `quantity` int NOT NULL,
  `subtotal` decimal(12,2) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `order_id` (`order_id`),
  KEY `variant_id` (`variant_id`),
  KEY `shop_id` (`shop_id`),
  CONSTRAINT `order_item_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`),
  CONSTRAINT `order_item_ibfk_2` FOREIGN KEY (`variant_id`) REFERENCES `product_variant` (`id`),
  CONSTRAINT `order_item_ibfk_3` FOREIGN KEY (`shop_id`) REFERENCES `shop` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_item`
--

LOCK TABLES `order_item` WRITE;
/*!40000 ALTER TABLE `order_item` DISABLE KEYS */;
INSERT INTO `order_item` VALUES (1,1,2,1,68000.00,1,68000.00),(2,2,2,7,119000.00,1,119000.00),(3,3,2,4,68000.00,3,204000.00),(4,4,2,8,113000.00,1,113000.00);
/*!40000 ALTER TABLE `order_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_status_history`
--

DROP TABLE IF EXISTS `order_status_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_status_history` (
  `id` int NOT NULL AUTO_INCREMENT,
  `order_id` int NOT NULL,
  `status` enum('PENDING','CONFIRMED','DELIVERING','DELIVERED','CANCELLED') NOT NULL DEFAULT 'PENDING',
  `note` varchar(255) DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `order_id` (`order_id`),
  CONSTRAINT `order_status_history_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_status_history`
--

LOCK TABLES `order_status_history` WRITE;
/*!40000 ALTER TABLE `order_status_history` DISABLE KEYS */;
INSERT INTO `order_status_history` VALUES (1,1,'PENDING','Đơn hàng đã được tạo','2026-04-23 15:07:15'),(2,1,'CONFIRMED','Người bán đã xác nhận đơn hàng','2026-04-23 15:22:22'),(3,2,'PENDING','Đơn hàng đã được tạo','2026-04-23 15:24:50'),(4,1,'DELIVERING','Đơn hàng đang được vận chuyển đến bạn','2026-04-23 15:28:00'),(5,2,'CANCELLED','Đơn hàng đã được hủy','2026-04-23 15:30:55'),(6,1,'DELIVERED','Đơn hàng đã được giao thành công','2026-04-23 15:31:46'),(7,3,'PENDING','Đơn hàng đã được tạo','2026-04-23 15:47:06'),(8,3,'CONFIRMED','Người bán đã xác nhận đơn hàng','2026-04-24 08:40:31'),(9,3,'DELIVERING','Đơn hàng đang được vận chuyển đến bạn','2026-04-24 08:42:33'),(10,3,'DELIVERED','Đơn hàng đã được giao thành công','2026-04-24 08:46:44'),(11,4,'PENDING','Đơn hàng đã được tạo','2026-04-24 14:41:35'),(12,4,'CONFIRMED','Người bán đã xác nhận đơn hàng','2026-04-24 14:42:28'),(13,4,'DELIVERING','Đơn hàng đang được vận chuyển đến bạn','2026-04-24 14:42:47'),(14,4,'DELIVERED','Đơn hàng đã được giao thành công','2026-04-24 14:42:57');
/*!40000 ALTER TABLE `order_status_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
  `id` int NOT NULL AUTO_INCREMENT,
  `code` varchar(255) NOT NULL,
  `user_id` int NOT NULL,
  `shop_id` int NOT NULL,
  `receiver_name` varchar(100) NOT NULL,
  `address` varchar(255) NOT NULL,
  `receiver_phone` varchar(20) NOT NULL,
  `note` varchar(255) DEFAULT NULL,
  `total_price` decimal(12,2) NOT NULL,
  `shipping_fee` decimal(12,2) NOT NULL,
  `final_total` decimal(12,2) NOT NULL,
  `payment_method` enum('COD','VNPAY') NOT NULL,
  `payment_status` enum('UNPAID','PAID','REFUNDED') DEFAULT 'UNPAID',
  `payment_group_id` int NOT NULL,
  `status` enum('PENDING','CONFIRMED','DELIVERING','DELIVERED','COMPLETED','CANCELLED') DEFAULT 'PENDING',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`),
  KEY `user_id` (`user_id`),
  KEY `fk_orders_shop` (`shop_id`),
  KEY `fk_orders_payment_group` (`payment_group_id`),
  CONSTRAINT `fk_orders_payment_group` FOREIGN KEY (`payment_group_id`) REFERENCES `payment_group` (`id`),
  CONSTRAINT `fk_orders_shop` FOREIGN KEY (`shop_id`) REFERENCES `shop` (`id`),
  CONSTRAINT `orders_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
INSERT INTO `orders` VALUES (1,'DH5697',3,2,'Khách hàng 1','xã Hải Anh, tỉnh Ninh Bình','0382079152','Giao giờ hành chính',68000.00,15000.00,83000.00,'VNPAY','PAID',1,'DELIVERED','2026-04-23 15:07:15','2026-04-23 15:31:46'),(2,'DH0019',3,2,'Khách hàng 1','xã Hải Anh, tỉnh Ninh Bình','0382079152','',119000.00,35000.00,154000.00,'COD','UNPAID',2,'CANCELLED','2026-04-23 15:24:50','2026-04-23 15:30:56'),(3,'DH6919',3,2,'Khách hàng 1','xã Hải Anh, tỉnh Ninh Bình','0382079152','',204000.00,35000.00,239000.00,'COD','UNPAID',3,'DELIVERED','2026-04-23 15:47:06','2026-04-24 08:46:44'),(4,'DH5518',3,2,'Khách hàng 1','xã Hải Anh, tỉnh Ninh Bình','0382079152','Giao sau 12h trưa',113000.00,15000.00,128000.00,'VNPAY','PAID',4,'DELIVERED','2026-04-24 14:41:35','2026-04-24 14:42:58');
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `otp_codes`
--

DROP TABLE IF EXISTS `otp_codes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `otp_codes` (
  `id` int NOT NULL AUTO_INCREMENT,
  `email` varchar(255) NOT NULL,
  `otp` varchar(10) NOT NULL,
  `expired_at` datetime DEFAULT NULL,
  `used` tinyint(1) DEFAULT '0',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `otp_codes`
--

LOCK TABLES `otp_codes` WRITE;
/*!40000 ALTER TABLE `otp_codes` DISABLE KEYS */;
/*!40000 ALTER TABLE `otp_codes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `payment_group`
--

DROP TABLE IF EXISTS `payment_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payment_group` (
  `id` int NOT NULL AUTO_INCREMENT,
  `code` varchar(255) DEFAULT NULL,
  `user_id` int DEFAULT NULL,
  `method` enum('COD','VNPAY') NOT NULL,
  `total_amount` decimal(12,2) NOT NULL,
  `status` enum('PENDING','PAID','FAILED','REFUNDED') DEFAULT 'PENDING',
  `transaction_code` varchar(255) DEFAULT NULL,
  `paid_at` datetime DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `payment_group_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payment_group`
--

LOCK TABLES `payment_group` WRITE;
/*!40000 ALTER TABLE `payment_group` DISABLE KEYS */;
INSERT INTO `payment_group` VALUES (1,'HD5681',3,'VNPAY',83000.00,'PAID','15510645','2026-04-23 15:08:48','2026-04-23 15:07:15'),(2,'HD0004',3,'COD',154000.00,'PENDING',NULL,NULL,'2026-04-23 15:24:50'),(3,'HD6914',3,'COD',239000.00,'PENDING',NULL,NULL,'2026-04-23 15:47:06'),(4,'HD5513',3,'VNPAY',128000.00,'PAID','15512482','2026-04-24 14:42:00','2026-04-24 14:41:35');
/*!40000 ALTER TABLE `payment_group` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product`
--

DROP TABLE IF EXISTS `product`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product` (
  `id` int NOT NULL AUTO_INCREMENT,
  `shop_id` int DEFAULT NULL,
  `category_id` int DEFAULT NULL,
  `brand_id` int DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `price` decimal(10,2) DEFAULT NULL,
  `sale_price` decimal(10,2) DEFAULT NULL,
  `description` text,
  `attributes_json` json DEFAULT NULL,
  `view_count` int DEFAULT '0',
  `sold_count` int DEFAULT '0',
  `rating_avg` decimal(3,2) DEFAULT '0.00',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `status` enum('PENDING','ACTIVE','INACTIVE','OUT_OF_STOCK','REJECTED') DEFAULT 'PENDING',
  PRIMARY KEY (`id`),
  KEY `idx_product_status` (`status`),
  KEY `idx_product_category` (`category_id`),
  KEY `shop_id` (`shop_id`),
  KEY `brand_id` (`brand_id`),
  CONSTRAINT `product_ibfk_1` FOREIGN KEY (`shop_id`) REFERENCES `shop` (`id`),
  CONSTRAINT `product_ibfk_2` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`),
  CONSTRAINT `product_ibfk_3` FOREIGN KEY (`brand_id`) REFERENCES `brand` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product`
--

LOCK TABLES `product` WRITE;
/*!40000 ALTER TABLE `product` DISABLE KEYS */;
INSERT INTO `product` VALUES (1,2,7,1,'Áo Giữ Nhiệt Nam Co Giãn 2 Chiều',80000.00,68000.00,'Thông tin sản phẩm Áo Giữ Nhiệt Nam Co Giãn 2 Chiều VESCA N\n- Sản phẩm được thiết kế theo đúng form chuẩn của nam giới Việt Nam\n- Sản phẩm chính là mẫu thiết kế mới nhất của VESCA','{\"Mẫu\": \" Khác, Trơn\", \"Xuất xứ\": \" Việt Nam\", \"Chất liệu\": \" Cotton\"}',5,4,4.00,'2026-04-23 09:15:16','2026-04-23 09:15:16','ACTIVE'),(2,2,4,NULL,'Áo Thun Thể Thao Nam Mùa Hè Cộc Tay',119000.00,NULL,'? THÔNG TIN SẢN PHẨM\n\nTên sản phẩm: Áo Thun Thể Thao Nam Mùa Hè Cộc Tay VESCA Chất Coolmax Co Giãn Mềm Mịn Vận Động Thoải Mái G12\n\nChất liệu: Polyester + Spandex với công nghệ Coolmax, giúp thấm hút mồ hôi nhanh, giữ cơ thể luôn mát mẻ.','{\"Mẫu\": \" Họa tiết, Trơn\", \"Phong cách\": \" Thể thao, Cơ bản\", \"Chiều dài tay áo\": \" Tay ngắn\"}',5,0,0.00,'2026-04-23 09:25:45','2026-04-23 09:25:45','ACTIVE'),(3,2,6,NULL,'Áo Sơ Mi Form Rộng, Áo Sơ Mi Hale Oversize Vải Linen Cao Cấp',238000.00,113000.00,'1. Sản phẩm giống mô tả và hình ảnh thật 100%\n2. Đảm bảo vải chất lượng sản phẩm 100%\n3. Cam kết được đổi trả hàng trong vòng 15 ngày.\n4. Hoàn tiền nếu sản phẩm bị lỗi\n+ Hàng phải còn mới đầy đủ tem mác và chưa qua sử dụng\n+ Sản phẩm bị lỗi do vận chuyển và do nhà sản xuất','{\"Mẫu\": \" Sọc caro\", \"Cropped Top\": \"Không\", \"Chất liệu\": \" Lanh\"}',4,1,1.00,'2026-04-24 14:08:36','2026-04-24 14:08:36','ACTIVE'),(4,2,6,NULL,'Áo sơ mi O.D.I.N cộc tay Original, Áo sơmi nam ngắn tay form rộng',99000.00,NULL,'HƯỚNG DẪN SỬ DỤNG:\n- Lần giặt đầu chỉ nên xả nước lạnh rồi phơi khô.\n- Khuyến cáo nên giặt tay, hạn chế giặt máy.\n- Chú ý lộn trái sản phẩm trước khi giặt để không ảnh hướng tới bề mặt vải.\n- Không sử dụng thuốc tẩy, không giặt chung với các sản phẩm dễ phai màu.\n- Hạn chế phơi trực tiếp dưới ánh nắng mặt trời, nên phơi khô dưới ảnh sáng tự nhiên.','{\"Mẫu\": \"Trơn\", \"Xuất xứ\": \" Trung Quốc\"}',2,0,0.00,'2026-04-24 14:23:33','2026-04-24 14:37:18','REJECTED');
/*!40000 ALTER TABLE `product` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product_image`
--

DROP TABLE IF EXISTS `product_image`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_image` (
  `id` int NOT NULL AUTO_INCREMENT,
  `product_id` int DEFAULT NULL,
  `image_url` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `product_id` (`product_id`),
  CONSTRAINT `product_image_ibfk_1` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_image`
--

LOCK TABLES `product_image` WRITE;
/*!40000 ALTER TABLE `product_image` DISABLE KEYS */;
INSERT INTO `product_image` VALUES (1,1,'https://res.cloudinary.com/dcjraarbb/image/upload/v1776910389/u06wmhtwx2opuwktqrjj.webp'),(2,1,'https://res.cloudinary.com/dcjraarbb/image/upload/v1776910404/obpnvc2fwl3ujdah0lwn.webp'),(3,2,'https://res.cloudinary.com/dcjraarbb/image/upload/v1776911040/nulqqlwhbuxf1isu0vfl.webp'),(4,3,'https://res.cloudinary.com/dcjraarbb/image/upload/v1777014338/ykpqxs7nmxdfpfe4vavr.webp'),(5,3,'https://res.cloudinary.com/dcjraarbb/image/upload/v1777014349/urpplacgnb0a1lolfmav.webp'),(6,4,'https://res.cloudinary.com/dcjraarbb/image/upload/v1777015311/hgdbdwhsx7jxjsnvvwf0.webp'),(7,4,'https://res.cloudinary.com/dcjraarbb/image/upload/v1777015326/lpoleezdddfiitifqptv.webp');
/*!40000 ALTER TABLE `product_image` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product_variant`
--

DROP TABLE IF EXISTS `product_variant`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_variant` (
  `id` int NOT NULL AUTO_INCREMENT,
  `product_id` int DEFAULT NULL,
  `sku` varchar(100) DEFAULT NULL,
  `color` varchar(50) DEFAULT NULL,
  `size` varchar(50) DEFAULT NULL,
  `stock` int DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `sku` (`sku`),
  KEY `product_id` (`product_id`),
  CONSTRAINT `product_variant_ibfk_1` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_variant`
--

LOCK TABLES `product_variant` WRITE;
/*!40000 ALTER TABLE `product_variant` DISABLE KEYS */;
INSERT INTO `product_variant` VALUES (1,1,'OGI-ENC-MXX-2D37','Đen cổ thấp','M',99),(2,1,'OGI-ENC-LXX-0E61','Đen cổ thấp','L',50),(3,1,'OGI-TRN-MXX-2BAF','Trắng cổ cao','M',25),(4,1,'OGI-TRN-LXX-7E42','Trắng cổ cao','L',97),(5,2,'OTH-G12-MXX-A8A0','G12 Đen','M',123),(6,2,'OTH-G12-XLX-0ED4','G12 Đen','XL',60),(7,2,'OTH-G12-MXX-DC0D','G12 Than','M',70),(8,3,'OSM-SMH-M45-7BAE','SM hale sọc xanh','M(45-55kg)',59),(9,3,'OSM-SMH-M45-14D0','SM hale sọc hồng','M(45-55kg)',75),(10,4,'OSM-SMI-MXX-468B','Sơ mi ODIN đen','M',45),(11,4,'OSM-SMI-LXX-B8D7','Sơ mi ODIN đen','L',30);
/*!40000 ALTER TABLE `product_variant` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `review`
--

DROP TABLE IF EXISTS `review`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `review` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `product_id` int NOT NULL,
  `order_id` int NOT NULL,
  `rating` int DEFAULT NULL,
  `content` text,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_review_user_product_order` (`user_id`,`product_id`,`order_id`),
  KEY `fk_review_product` (`product_id`),
  KEY `fk_review_order` (`order_id`),
  CONSTRAINT `fk_review_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`),
  CONSTRAINT `fk_review_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`),
  CONSTRAINT `fk_review_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `review_chk_1` CHECK ((`rating` between 1 and 5))
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `review`
--

LOCK TABLES `review` WRITE;
/*!40000 ALTER TABLE `review` DISABLE KEYS */;
INSERT INTO `review` VALUES (1,3,1,1,3,'Săn sale được giá 9k mà nhận hàng đẹp quá\nÁo khá đẹp nha mn nên mua ạ\nShip nhanh đóng gói cẩn thận lắm ạ\nCảm ơn shop nha','2026-04-23 15:43:19','2026-04-23 15:43:19'),(2,3,1,3,5,'Shop gói hàng kỹ càng và cẩn thận \nGiao hàng nhanh \nGiá rẻ','2026-04-24 08:49:32','2026-04-24 08:49:32'),(3,3,3,4,1,'Lần đầu mua mùa đồ trên mạng mà rất OK  chất liệu đẹp nỉ dầy với giả cả như này thì rất OK. Giao hàng nhạnh nên mua thử nha cbn\n dày dặn, vải mát mặc cực kì sướng luôn nhé mn ơiiii. Với mức giá này mà chất lượng vượt ngoài mong đợi\n đẹp lắm mng ơiii\nNên mua nha,giá cả hợp lí','2026-04-24 14:45:17','2026-04-24 14:45:17');
/*!40000 ALTER TABLE `review` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `review_image`
--

DROP TABLE IF EXISTS `review_image`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `review_image` (
  `id` int NOT NULL AUTO_INCREMENT,
  `review_id` int NOT NULL,
  `image_url` varchar(500) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_review_image_review` (`review_id`),
  CONSTRAINT `fk_review_image_review` FOREIGN KEY (`review_id`) REFERENCES `review` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `review_image`
--

LOCK TABLES `review_image` WRITE;
/*!40000 ALTER TABLE `review_image` DISABLE KEYS */;
INSERT INTO `review_image` VALUES (1,1,'https://res.cloudinary.com/dcjraarbb/image/upload/v1776933163/nzcxd19i9fjqtde3cgop.webp'),(2,2,'https://res.cloudinary.com/dcjraarbb/image/upload/v1776995353/kbjbnltqlcfofqjjhzct.webp'),(3,3,'https://res.cloudinary.com/dcjraarbb/image/upload/v1777016672/epfwr6sraoo9d4qi5wo4.webp');
/*!40000 ALTER TABLE `review_image` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `role`
--

DROP TABLE IF EXISTS `role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `role` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `role`
--

LOCK TABLES `role` WRITE;
/*!40000 ALTER TABLE `role` DISABLE KEYS */;
INSERT INTO `role` VALUES (1,'ADMIN'),(3,'CUSTOMER'),(2,'SHOP');
/*!40000 ALTER TABLE `role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shop`
--

DROP TABLE IF EXISTS `shop`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `shop` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `description` text,
  `logo` varchar(255) DEFAULT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `status` enum('ACTIVE','INACTIVE','PENDING','REJECTED') DEFAULT 'PENDING',
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `shop_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shop`
--

LOCK TABLES `shop` WRITE;
/*!40000 ALTER TABLE `shop` DISABLE KEYS */;
INSERT INTO `shop` VALUES (2,2,'Thời trang Mạnh Kha','Thời trang Mạnh Kha chuyên đồ nam cao cấp','https://img.favpng.com/8/20/24/computer-icons-online-shopping-png-favpng-QuiWDXbsc69EE92m3bZ2i0ybS.jpg','382079152','manhkha03020100@gmail.com','128 Nguyễn Đức Cảnh, Tương Mai, Hà Nội','2026-04-23 09:06:08','ACTIVE');
/*!40000 ALTER TABLE `shop` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` int NOT NULL AUTO_INCREMENT,
  `full_name` varchar(150) DEFAULT NULL,
  `username` varchar(150) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `avatar` varchar(255) DEFAULT NULL,
  `gender` enum('MALE','FEMALE','OTHER') DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `status` enum('ACTIVE','LOCKED') DEFAULT 'ACTIVE',
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'Mạnh Kha',NULL,'','nguyenmanhkha3225@gmail.com',NULL,'https://lh3.googleusercontent.com/a/ACg8ocIUdX0jQt7YlhYsGsYYnf6h4TR6Xrbah2AQBAsXOhg0Nq3QbQ=s96-c',NULL,'2026-04-23 08:38:01','ACTIVE'),(2,'Kha Nguyễn',NULL,'','manhkha03020100@gmail.com',NULL,'https://lh3.googleusercontent.com/a/ACg8ocLQ7Xygf_z4HPsq6_bnPEV9Ckfgc62f0L2mSjztoSD_xg4Yjg=s96-c',NULL,'2026-04-23 08:52:38','ACTIVE'),(3,'Clone Kha',NULL,'','manhkha3225@gmail.com',NULL,'https://lh3.googleusercontent.com/a/ACg8ocItq3f4c2VYKPOpnUMxkV8w4G9DRBk2WwBIXpD4mqNQ4h8UCA=s96-c',NULL,'2026-04-23 09:12:12','ACTIVE');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_role`
--

DROP TABLE IF EXISTS `user_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_role` (
  `user_id` int NOT NULL,
  `role_id` int NOT NULL,
  PRIMARY KEY (`user_id`,`role_id`),
  KEY `role_id` (`role_id`),
  CONSTRAINT `user_role_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `user_role_ibfk_2` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_role`
--

LOCK TABLES `user_role` WRITE;
/*!40000 ALTER TABLE `user_role` DISABLE KEYS */;
INSERT INTO `user_role` VALUES (1,1),(2,2),(1,3),(2,3),(3,3);
/*!40000 ALTER TABLE `user_role` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-04-24 15:14:21

-- MySQL init script for booking-service
-- This runs when the MySQL container starts for the first time

CREATE DATABASE IF NOT EXISTS `booking_db` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'booking_user'@'%' IDENTIFIED BY 'booking_pass';
GRANT ALL PRIVILEGES ON `booking_db`.* TO 'booking_user'@'%';
FLUSH PRIVILEGES;

USE `booking_db`;

-- Customer table (referenced by booking-service)
CREATE TABLE IF NOT EXISTS `customer` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(255) NOT NULL,
    `email` VARCHAR(255) NOT NULL,
    `address` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_customer_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert sample customers
INSERT INTO `customer` (`id`, `name`, `email`, `address`) VALUES
(1, 'João Silva', 'joao.silva@email.com', 'Rua A, 123 - São Paulo/SP'),
(2, 'Maria Santos', 'maria.santos@email.com', 'Av. B, 456 - Rio de Janeiro/RJ'),
(3, 'Pedro Oliveira', 'pedro.oliveira@email.com', 'Rua C, 789 - Belo Horizonte/MG')
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);
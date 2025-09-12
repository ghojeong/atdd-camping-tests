-- Create tables if not exist (so we can seed before apps start)
CREATE TABLE IF NOT EXISTS products (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  stock_quantity INT NOT NULL,
  price DECIMAL(10,2) NOT NULL,
  product_type VARCHAR(32) NOT NULL,
  UNIQUE KEY uk_products_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS campsites (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  site_number VARCHAR(32) NOT NULL,
  description VARCHAR(255),
  max_people INT NOT NULL,
  UNIQUE KEY uk_campsites_site_number (site_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS reservations (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  customer_name VARCHAR(255) NOT NULL,
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  reservation_date DATE NOT NULL,
  campsite_id BIGINT NOT NULL,
  phone_number VARCHAR(32),
  status VARCHAR(32) NOT NULL,
  confirmation_code VARCHAR(64),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_reservations_confirmation_code (confirmation_code),
  CONSTRAINT fk_reservation_campsite FOREIGN KEY (campsite_id) REFERENCES campsites(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sales_records (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  product_id BIGINT NOT NULL,
  quantity INT NOT NULL,
  total_price DECIMAL(10,2) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_sales_product FOREIGN KEY (product_id) REFERENCES products(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS rental_records (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  reservation_id BIGINT NULL,
  product_id BIGINT NOT NULL,
  quantity INT NOT NULL,
  is_returned TINYINT(1) NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_rental_reservation FOREIGN KEY (reservation_id) REFERENCES reservations(id) ON DELETE SET NULL,
  CONSTRAINT fk_rental_product FOREIGN KEY (product_id) REFERENCES products(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Products (admin-init.sql)
INSERT INTO products (name, stock_quantity, price, product_type) VALUES
  ('랜턴', 20, 30000.00, 'RENTAL'),
  ('장작팩', 50, 10000.00, 'SALE'),
  ('코펠 세트', 15, 20000.00, 'RENTAL'),
  ('의자', 25, 15000.00, 'RENTAL'),
  ('테이블', 10, 25000.00, 'RENTAL'),
  ('버너', 12, 18000.00, 'RENTAL'),
  ('취사도구 세트', 30, 12000.00, 'RENTAL'),
  ('생수(2L)', 100, 2000.00, 'SALE'),
  ('라면 세트', 80, 4000.00, 'SALE'),
  ('스낵팩', 60, 3000.00, 'SALE'),
  ('휴지', 70, 2500.00, 'SALE'),
  ('아이스팩', 90, 1500.00, 'SALE')
ON DUPLICATE KEY UPDATE name=VALUES(name), stock_quantity=VALUES(stock_quantity), price=VALUES(price), product_type=VALUES(product_type);

INSERT INTO campsites (site_number, description, max_people) VALUES 
('A-1', '대형 사이트 - 전기 있음, 화장실 인근', 6),
('A-2', '대형 사이트 - 전기 있음, 화장실 인근', 6),
('A-3', '대형 사이트 - 전기 있음, 화장실 인근', 6),
('A-4', '대형 사이트 - 전기 있음, 개수대 인근', 6),
('A-5', '대형 사이트 - 전기 있음, 개수대 인근', 6),
('A-6', '대형 사이트 - 전기 있음, 놀이터 인근', 6),
('A-7', '대형 사이트 - 전기 있음, 놀이터 인근', 6),
('A-8', '대형 사이트 - 전기 있음, 계곡 전망', 6),
('A-9', '대형 사이트 - 전기 있음, 계곡 전망', 6),
('A-10', '대형 사이트 - 전기 있음, 계곡 전망', 6),
('A-11', '대형 사이트 - 전기 있음, 산 전망', 6),
('A-12', '대형 사이트 - 전기 있음, 산 전망', 6),
('A-13', '대형 사이트 - 전기 있음, 산 전망', 6),
('A-14', '대형 사이트 - 전기 있음, 중앙 위치', 6),
('A-15', '대형 사이트 - 전기 있음, 중앙 위치', 6),
('A-16', '대형 사이트 - 전기 있음, 중앙 위치', 6),
('A-17', '대형 사이트 - 전기 있음, 조용한 위치', 6),
('A-18', '대형 사이트 - 전기 있음, 조용한 위치', 6),
('A-19', '대형 사이트 - 전기 있음, 조용한 위치', 6),
('A-20', '대형 사이트 - 전기 있음, 조용한 위치', 6),
('B-1', '소형 사이트 - 전기 있음, 매점 인근', 6),
('B-2', '소형 사이트 - 전기 있음, 매점 인근', 6),
('B-3', '소형 사이트 - 전기 있음, 매점 인근', 6),
('B-4', '소형 사이트 - 전기 있음, 주차장 인근', 6),
('B-5', '소형 사이트 - 전기 있음, 주차장 인근', 6),
('B-6', '소형 사이트 - 전기 있음, 주차장 인근', 6),
('B-7', '소형 사이트 - 전기 있음, 샤워장 인근', 6),
('B-8', '소형 사이트 - 전기 있음, 샤워장 인근', 6),
('B-9', '소형 사이트 - 전기 있음, 샤워장 인근', 6),
('B-10', '소형 사이트 - 전기 있음, 샤워장 인근', 6),
('B-11', '소형 사이트 - 전기 있음, 바비큐장 인근', 6),
('B-12', '소형 사이트 - 전기 있음, 바비큐장 인근', 6),
('B-13', '소형 사이트 - 전기 있음, 바비큐장 인근', 6),
('B-14', '소형 사이트 - 전기 있음, 운동장 인근', 6),
('B-15', '소형 사이트 - 전기 있음, 운동장 인근', 6)
ON DUPLICATE KEY UPDATE description=VALUES(description), max_people=VALUES(max_people);

-- Reservations (admin-init.sql, uses confirmation_code unique for idempotency)
INSERT INTO reservations (customer_name, start_date, end_date, campsite_id, phone_number, status, reservation_date, confirmation_code, created_at)
VALUES
  ('홍길동', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 1 DAY), 1, '010-1111-2222', 'CONFIRMED', CURDATE(), 'ABC123', CURRENT_TIMESTAMP),
  ('김철수', DATE_ADD(CURDATE(), INTERVAL 1 DAY), DATE_ADD(CURDATE(), INTERVAL 2 DAY), 2, '010-3333-4444', 'CONFIRMED', CURDATE(), 'XYZ789', CURRENT_TIMESTAMP),
  ('이영희', DATE_ADD(CURDATE(), INTERVAL -28 DAY), DATE_ADD(CURDATE(), INTERVAL -27 DAY), 1, '010-5555-6666', 'CONFIRMED', DATE_ADD(CURDATE(), INTERVAL -29 DAY), 'R00003', DATE_ADD(CURRENT_TIMESTAMP, INTERVAL -29 DAY)),
  ('박민수', DATE_ADD(CURDATE(), INTERVAL -25 DAY), DATE_ADD(CURDATE(), INTERVAL -24 DAY), 2, '010-7777-8888', 'CONFIRMED', DATE_ADD(CURDATE(), INTERVAL -26 DAY), 'R00004', DATE_ADD(CURRENT_TIMESTAMP, INTERVAL -26 DAY)),
  ('최수정', DATE_ADD(CURDATE(), INTERVAL -21 DAY), DATE_ADD(CURDATE(), INTERVAL -19 DAY), 1, '010-9999-0000', 'CONFIRMED', DATE_ADD(CURDATE(), INTERVAL -22 DAY), 'R00005', DATE_ADD(CURRENT_TIMESTAMP, INTERVAL -22 DAY)),
  ('정하늘', DATE_ADD(CURDATE(), INTERVAL -18 DAY), DATE_ADD(CURDATE(), INTERVAL -17 DAY), 2, '010-2222-3333', 'CONFIRMED', DATE_ADD(CURDATE(), INTERVAL -19 DAY), 'R00006', DATE_ADD(CURRENT_TIMESTAMP, INTERVAL -19 DAY)),
  ('오세훈', DATE_ADD(CURDATE(), INTERVAL -15 DAY), DATE_ADD(CURDATE(), INTERVAL -14 DAY), 1, '010-4444-5555', 'CONFIRMED', DATE_ADD(CURDATE(), INTERVAL -16 DAY), 'R00007', DATE_ADD(CURRENT_TIMESTAMP, INTERVAL -16 DAY)),
  ('유지민', DATE_ADD(CURDATE(), INTERVAL -12 DAY), DATE_ADD(CURDATE(), INTERVAL -11 DAY), 2, '010-6666-7777', 'CONFIRMED', DATE_ADD(CURDATE(), INTERVAL -13 DAY), 'R00008', DATE_ADD(CURRENT_TIMESTAMP, INTERVAL -13 DAY)),
  ('선우진', DATE_ADD(CURDATE(), INTERVAL -9 DAY), DATE_ADD(CURDATE(), INTERVAL -8 DAY), 1, '010-1212-3434', 'CONFIRMED', DATE_ADD(CURDATE(), INTERVAL -10 DAY), 'R00009', DATE_ADD(CURRENT_TIMESTAMP, INTERVAL -10 DAY)),
  ('배수아', DATE_ADD(CURDATE(), INTERVAL -6 DAY), DATE_ADD(CURDATE(), INTERVAL -5 DAY), 2, '010-5656-7878', 'CONFIRMED', DATE_ADD(CURDATE(), INTERVAL -7 DAY), 'R00010', DATE_ADD(CURRENT_TIMESTAMP, INTERVAL -7 DAY)),
  ('고다빈', DATE_ADD(CURDATE(), INTERVAL -3 DAY), DATE_ADD(CURDATE(), INTERVAL -2 DAY), 1, '010-9090-1010', 'CONFIRMED', DATE_ADD(CURDATE(), INTERVAL -4 DAY), 'R00011', DATE_ADD(CURRENT_TIMESTAMP, INTERVAL -4 DAY)),
  ('한도윤', DATE_ADD(CURDATE(), INTERVAL -1 DAY), CURDATE(), 2, '010-2323-4545', 'CONFIRMED', DATE_ADD(CURDATE(), INTERVAL -2 DAY), 'R00012', DATE_ADD(CURRENT_TIMESTAMP, INTERVAL -2 DAY))
ON DUPLICATE KEY UPDATE customer_name=VALUES(customer_name), start_date=VALUES(start_date), end_date=VALUES(end_date), campsite_id=VALUES(campsite_id), phone_number=VALUES(phone_number), status=VALUES(status), reservation_date=VALUES(reservation_date), created_at=VALUES(created_at);

-- Reservations (reservation-init.sql, future and past samples; de-duplicated by confirmation_code)
INSERT INTO reservations (customer_name, start_date, end_date, reservation_date, campsite_id, phone_number, status, confirmation_code, created_at) 
VALUES ('홍길동', DATE_ADD(CURDATE(), INTERVAL 7 DAY), DATE_ADD(CURDATE(), INTERVAL 9 DAY), DATE_ADD(CURDATE(), INTERVAL 7 DAY), 1, '010-1234-5678', 'CONFIRMED', 'ABC123', CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE start_date=VALUES(start_date), end_date=VALUES(end_date), reservation_date=VALUES(reservation_date), campsite_id=VALUES(campsite_id), phone_number=VALUES(phone_number), status=VALUES(status), created_at=VALUES(created_at);

INSERT INTO reservations (customer_name, start_date, end_date, reservation_date, campsite_id, phone_number, status, confirmation_code, created_at) 
VALUES ('김철수', DATE_ADD(CURDATE(), INTERVAL 14 DAY), DATE_ADD(CURDATE(), INTERVAL 15 DAY), DATE_ADD(CURDATE(), INTERVAL 14 DAY), 3, '010-2345-6789', 'CONFIRMED', 'DEF456', CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE start_date=VALUES(start_date), end_date=VALUES(end_date), reservation_date=VALUES(reservation_date), campsite_id=VALUES(campsite_id), phone_number=VALUES(phone_number), status=VALUES(status), created_at=VALUES(created_at);

INSERT INTO reservations (customer_name, start_date, end_date, reservation_date, campsite_id, phone_number, status, confirmation_code, created_at) 
VALUES ('이영희', DATE_ADD(CURDATE(), INTERVAL 21 DAY), DATE_ADD(CURDATE(), INTERVAL 23 DAY), DATE_ADD(CURDATE(), INTERVAL 21 DAY), 6, '010-3456-7890', 'CONFIRMED', 'GHI789', CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE start_date=VALUES(start_date), end_date=VALUES(end_date), reservation_date=VALUES(reservation_date), campsite_id=VALUES(campsite_id), phone_number=VALUES(phone_number), status=VALUES(status), created_at=VALUES(created_at);

INSERT INTO reservations (customer_name, start_date, end_date, reservation_date, campsite_id, phone_number, status, confirmation_code, created_at) 
VALUES ('박민수', DATE_ADD(CURDATE(), INTERVAL -7 DAY), DATE_ADD(CURDATE(), INTERVAL -5 DAY), DATE_ADD(CURDATE(), INTERVAL -7 DAY), 2, '010-4567-8901', 'CONFIRMED', 'JKL012', DATE_ADD(CURRENT_TIMESTAMP, INTERVAL -14 DAY))
ON DUPLICATE KEY UPDATE start_date=VALUES(start_date), end_date=VALUES(end_date), reservation_date=VALUES(reservation_date), campsite_id=VALUES(campsite_id), phone_number=VALUES(phone_number), status=VALUES(status), created_at=VALUES(created_at);

INSERT INTO reservations (customer_name, start_date, end_date, reservation_date, campsite_id, phone_number, status, confirmation_code, created_at) 
VALUES ('정수진', DATE_ADD(CURDATE(), INTERVAL -3 DAY), DATE_ADD(CURDATE(), INTERVAL -2 DAY), DATE_ADD(CURDATE(), INTERVAL -3 DAY), 4, '010-5678-9012', 'CONFIRMED', 'MNO345', DATE_ADD(CURRENT_TIMESTAMP, INTERVAL -10 DAY))
ON DUPLICATE KEY UPDATE start_date=VALUES(start_date), end_date=VALUES(end_date), reservation_date=VALUES(reservation_date), campsite_id=VALUES(campsite_id), phone_number=VALUES(phone_number), status=VALUES(status), created_at=VALUES(created_at);

INSERT INTO sales_records (product_id, quantity, total_price, created_at) VALUES
  (2, 3, 30000.00, DATE_ADD(CURRENT_TIMESTAMP, INTERVAL -1 DAY)),
  (2, 1, 10000.00, CURRENT_TIMESTAMP),
  (8, 5, 10000.00, DATE_ADD(CURRENT_TIMESTAMP, INTERVAL -7 DAY)),
  (9, 2, 8000.00, DATE_ADD(CURRENT_TIMESTAMP, INTERVAL -15 DAY)),
  (10, 10, 30000.00, DATE_ADD(CURRENT_TIMESTAMP, INTERVAL -25 DAY))
ON DUPLICATE KEY UPDATE product_id=VALUES(product_id), quantity=VALUES(quantity), total_price=VALUES(total_price), created_at=VALUES(created_at);

-- Rental Records (admin-init.sql; includes a walk-in without reservation)
INSERT INTO rental_records (reservation_id, product_id, quantity, is_returned, created_at) VALUES
  (3, 3, 2, 0, DATE_ADD(CURRENT_TIMESTAMP, INTERVAL -28 DAY)),
  (4, 4, 1, 1, DATE_ADD(CURRENT_TIMESTAMP, INTERVAL -25 DAY)),
  (5, 5, 3, 0, DATE_ADD(CURRENT_TIMESTAMP, INTERVAL -21 DAY)),
  (6, 6, 1, 1, DATE_ADD(CURRENT_TIMESTAMP, INTERVAL -18 DAY)),
  (7, 7, 4, 0, DATE_ADD(CURRENT_TIMESTAMP, INTERVAL -15 DAY)),
  (NULL, 3, 1, 0, DATE_ADD(CURRENT_TIMESTAMP, INTERVAL -3 DAY))
ON DUPLICATE KEY UPDATE reservation_id=VALUES(reservation_id), product_id=VALUES(product_id), quantity=VALUES(quantity), is_returned=VALUES(is_returned), created_at=VALUES(created_at);


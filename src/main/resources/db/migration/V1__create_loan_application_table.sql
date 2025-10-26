CREATE TABLE loan_application (
                                  id SERIAL PRIMARY KEY,
                                  name VARCHAR(100) NOT NULL,
                                  address VARCHAR(255) NOT NULL,
                                  email VARCHAR(100) NOT NULL,
                                  phone VARCHAR(20) NOT NULL,
                                  ssn VARCHAR(20) NOT NULL,
                                  requested_amount NUMERIC(15,2) NOT NULL,
                                  employment_status VARCHAR(20) NOT NULL,
                                  credit_lines INT,
                                  decision VARCHAR(20),
                                  reason VARCHAR(255),
                                  interest_rate NUMERIC(5,3),
                                  term_months INT,
                                  monthly_payment NUMERIC(15,2),
                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
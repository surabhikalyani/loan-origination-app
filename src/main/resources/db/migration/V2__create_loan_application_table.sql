CREATE TABLE loan_application (
                id SERIAL PRIMARY KEY,
                applicant_id INT NOT NULL REFERENCES applicant(id) ON DELETE CASCADE,
                offer_id INT NOT NULL REFERENCES loan_offer(id) ON DELETE CASCADE,
                requested_amount NUMERIC(15,2) NOT NULL,
                credit_lines INT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

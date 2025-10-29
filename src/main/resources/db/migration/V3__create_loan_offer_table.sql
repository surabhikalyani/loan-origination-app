CREATE TABLE loan_offer (
            id SERIAL PRIMARY KEY,
            application_id INT NOT NULL REFERENCES loan_application(id) ON DELETE CASCADE,
            decision VARCHAR(20) NOT NULL,
            reason VARCHAR(255),
            interest_rate NUMERIC(5,3),
            term_months INT,
            monthly_payment NUMERIC(15,2),
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_payment_cards_user_id ON payment_card(user_id);
CREATE UNIQUE INDEX idx_payment_cards_number_unique ON payment_card(number);
CREATE INDEX idx_payment_cards_active ON payment_card(active);
CREATE INDEX idx_payment_cards_expiration_date ON payment_card(expiration_date);
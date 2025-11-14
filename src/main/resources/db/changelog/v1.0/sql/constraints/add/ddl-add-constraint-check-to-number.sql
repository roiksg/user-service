ALTER TABLE payment_card
    ADD CONSTRAINT validate_card_number
        CHECK (char_length(number::text) = 16);
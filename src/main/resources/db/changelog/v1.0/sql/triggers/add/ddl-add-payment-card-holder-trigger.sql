CREATE TRIGGER trg_set_holder_before_insert
    BEFORE INSERT ON payment_card
    FOR EACH ROW
    EXECUTE FUNCTION set_payment_card_holder();
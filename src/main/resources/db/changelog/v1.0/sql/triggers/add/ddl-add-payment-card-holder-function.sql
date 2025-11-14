CREATE OR REPLACE FUNCTION set_payment_card_holder()
RETURNS TRIGGER AS $$
DECLARE
    user_name varchar(50);
    user_surname varchar(50);
BEGIN
    SELECT name, surname
    INTO user_name, user_surname
    FROM users
    WHERE id = NEW.user_id;
    NEW.holder := UPPER(user_name || ' ' || user_surname);
    RETURN NEW;
END;
$$
LANGUAGE plpgsql;
-- Function that updates `last_updated_at` timestamp
-- (used with a trigger per table)

CREATE OR REPLACE FUNCTION public.update_last_updated_column()
    RETURNS trigger
    LANGUAGE plpgsql
AS $function$
    BEGIN
        NEW.last_updated_at = CURRENT_TIMESTAMP;
        RETURN NEW;
    END;
$function$
CREATE TABLE public.week_{CURRENT_WEEK}
(
    kp_id bigint NOT NULL DEFAULT nextval('kp_seq'::regclass),
    wi_id character varying(16) COLLATE pg_catalog."default" NOT NULL,
    description text COLLATE pg_catalog."default",
    unit_size character varying(16) COLLATE pg_catalog."default",
    bonus_type character varying(16) COLLATE pg_catalog."default",
    stapelen_tot character varying(16) COLLATE pg_catalog."default",
    price_cent integer,
    bonus_price_cent integer,
    price_total_cent integer,
    bonus_percentage double precision,
    url character varying(64) COLLATE pg_catalog."default",
    initial_index integer,
    file_path character varying(128) COLLATE pg_catalog."default",
    x_voor character varying(16) COLLATE pg_catalog."default",
    voor_y character varying(16) COLLATE pg_catalog."default"
)

TABLESPACE pg_default;

ALTER TABLE public.week_{CURRENT_WEEK}
    OWNER to postgres;

CREATE UNIQUE INDEX idx_wi_id_{CURRENT_WEEK}
    ON public.week_{CURRENT_WEEK} USING btree
    (wi_id COLLATE pg_catalog."default" ASC NULLS LAST)
    TABLESPACE pg_default;

ALTER TABLE public.week_{CURRENT_WEEK}
    CLUSTER ON idx_wi_id_{CURRENT_WEEK};
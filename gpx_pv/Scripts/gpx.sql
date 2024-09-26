--
-- PostgreSQL database dump
--

-- Dumped from database version 16.3
-- Dumped by pg_dump version 16.3

-- Started on 2024-09-26 13:37:12

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 7 (class 2615 OID 16407)
-- Name: administracion; Type: SCHEMA; Schema: -; Owner: postgres
--

CREATE SCHEMA administracion;


ALTER SCHEMA administracion OWNER TO postgres;

--
-- TOC entry 5 (class 2615 OID 16406)
-- Name: inventario; Type: SCHEMA; Schema: -; Owner: postgres
--

CREATE SCHEMA inventario;


ALTER SCHEMA inventario OWNER TO postgres;

--
-- TOC entry 6 (class 2615 OID 16413)
-- Name: ventas; Type: SCHEMA; Schema: -; Owner: postgres
--

CREATE SCHEMA ventas;


ALTER SCHEMA ventas OWNER TO postgres;

--
-- TOC entry 904 (class 1247 OID 33085)
-- Name: factura; Type: TYPE; Schema: ventas; Owner: postgres
--

CREATE TYPE ventas.factura AS (
	factura bigint,
	nit bigint,
	cliente character varying,
	empleado character varying,
	producto character varying,
	cantidad bigint,
	"precio-unitario" double precision,
	precio_total numeric,
	id_producto bigint
);


ALTER TYPE ventas.factura OWNER TO postgres;

--
-- TOC entry 260 (class 1255 OID 33164)
-- Name: agregar_gastos(); Type: FUNCTION; Schema: administracion; Owner: postgres
--

CREATE FUNCTION administracion.agregar_gastos() RETURNS trigger
    LANGUAGE plpgsql
    AS $$BEGIN
    IF NEW.finalizada = TRUE AND OLD.finalizada = FALSE THEN
        UPDATE administracion.clientes
        SET gastos = gastos + NEW.total
        WHERE clientes.nit = NEW.nit_cliente;

        IF EXISTS (SELECT 1 FROM administracion.tarjetas WHERE nit_cliente = NEW.nit_cliente) THEN
            UPDATE administracion.clientes
            SET gastos_ct = gastos_ct + NEW.total
            WHERE clientes.nit = NEW.nit_cliente;
        END IF;
    END IF;

    RETURN NEW;
END;
$$;


ALTER FUNCTION administracion.agregar_gastos() OWNER TO postgres;

--
-- TOC entry 261 (class 1255 OID 33170)
-- Name: subir_tarjeta(); Type: FUNCTION; Schema: administracion; Owner: postgres
--

CREATE FUNCTION administracion.subir_tarjeta() RETURNS trigger
    LANGUAGE plpgsql
    AS $$BEGIN
    IF NEW.gastos_ct > 49999 THEN
        UPDATE administracion.tarjetas
        SET tipo = 4
        WHERE nit_cliente = NEW.nit;
    ELSIF NEW.gastos_ct > 29999 THEN
        UPDATE administracion.tarjetas
        SET tipo = 3
        WHERE nit_cliente = NEW.nit;
    ELSIF NEW.gastos_ct > 9999 THEN
        UPDATE administracion.tarjetas
        SET tipo = 2
        WHERE nit_cliente = NEW.nit;
    END IF;

    RETURN NEW;
END;
$$;


ALTER FUNCTION administracion.subir_tarjeta() OWNER TO postgres;

--
-- TOC entry 266 (class 1255 OID 33100)
-- Name: actualizar_estanteria(); Type: FUNCTION; Schema: inventario; Owner: postgres
--

CREATE FUNCTION inventario.actualizar_estanteria() RETURNS trigger
    LANGUAGE plpgsql
    AS $$DECLARE
	old_stock BIGINT := 0;

BEGIN
	SELECT COALESCE(SUM(stock), 0) INTO old_stock
    FROM inventario.detalle_estanterias
    WHERE id_producto = NEW.id_producto
    AND numero IN (
        SELECT id_e
        FROM administracion.empleados em
        JOIN inventario.estanteria es ON em.id_sucursal = es.id_sucursal
    );

	IF NEW.cantidad > old_stock THEN
        RAISE EXCEPTION 'La cantidad solicitada (%) es mayor que el stock disponible (%) para el producto %.', NEW.cantidad, old_stock, NEW.id_producto;
    END IF;
	
    UPDATE inventario.detalle_estanterias
	SET stock = stock - NEW.cantidad
	WHERE id_producto = NEW.id_producto
  		AND numero IN (
      		SELECT id_e
      			FROM administracion.empleados em
      			JOIN inventario.estanteria es ON em.id_sucursal = es.id_sucursal
  	);

    RETURN NEW;
END;$$;


ALTER FUNCTION inventario.actualizar_estanteria() OWNER TO postgres;

--
-- TOC entry 263 (class 1255 OID 33206)
-- Name: existe_producto(bigint); Type: FUNCTION; Schema: inventario; Owner: postgres
--

CREATE FUNCTION inventario.existe_producto(p_producto bigint) RETURNS boolean
    LANGUAGE plpgsql
    AS $$BEGIN
	RETURN (SELECT CASE WHEN EXISTS (
			SELECT 1 FROM inventario.productos WHERE id = p_producto) 
		THEN true 
		ELSE false 
	END);
END;$$;


ALTER FUNCTION inventario.existe_producto(p_producto bigint) OWNER TO postgres;

--
-- TOC entry 265 (class 1255 OID 33196)
-- Name: ingresar_de_bodega(); Type: FUNCTION; Schema: inventario; Owner: postgres
--

CREATE FUNCTION inventario.ingresar_de_bodega() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
    stock_actual BIGINT;
BEGIN
    SELECT stock INTO stock_actual
    FROM inventario.detalle_bodega
    WHERE id_producto = NEW.id_producto
    AND id_bodega IN (
        SELECT codigo
        FROM administracion.empleados em
        JOIN inventario.bodega b ON em.id_sucursal = b.id_sucursal
    );

    IF NEW.stock > stock_actual THEN
        RAISE EXCEPTION 'La cantidad solicitada (%) es mayor que el stodasasdasdck disponible (%) para el producto %', NEW.stock, stock_actual, NEW.id_producto;
    END IF;

    UPDATE inventario.detalle_bodega
    SET stock = stock - NEW.stock
    WHERE id_producto = NEW.id_producto
    AND id_bodega IN (
        SELECT codigo
        FROM administracion.empleados em
        JOIN inventario.bodega b ON em.id_sucursal = b.id_sucursal
    );

    RETURN NEW;

EXCEPTION
    WHEN OTHERS THEN
        RAISE EXCEPTION 'Error inesperado: %', SQLERRM;
END;
$$;


ALTER FUNCTION inventario.ingresar_de_bodega() OWNER TO postgres;

--
-- TOC entry 262 (class 1255 OID 33203)
-- Name: ingresar_producto_bodega(bigint, bigint, bigint); Type: FUNCTION; Schema: inventario; Owner: postgres
--

CREATE FUNCTION inventario.ingresar_producto_bodega(p_bodega bigint, p_producto bigint, p_stock bigint) RETURNS void
    LANGUAGE plpgsql
    AS $$
BEGIN
    INSERT INTO inventario.detalle_bodega(id_bodega, id_producto, stock)
    VALUES (p_bodega, p_producto, p_stock)
    ON CONFLICT (id_bodega, id_producto)
    DO UPDATE SET
        stock = inventario.detalle_bodega.stock + EXCLUDED.stock;
END;
$$;


ALTER FUNCTION inventario.ingresar_producto_bodega(p_bodega bigint, p_producto bigint, p_stock bigint) OWNER TO postgres;

--
-- TOC entry 264 (class 1255 OID 33225)
-- Name: insertar_estanteria(integer); Type: FUNCTION; Schema: inventario; Owner: postgres
--

CREATE FUNCTION inventario.insertar_estanteria(p_id_sucursal integer) RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
    pasillo_count INT;
    nuevo_pasillo INT;
BEGIN
    SELECT COUNT(*)
    INTO pasillo_count
    FROM inventario.estanteria
    WHERE id_sucursal = p_id_sucursal;

    nuevo_pasillo := pasillo_count + 1;

    INSERT INTO inventario.estanteria(id_sucursal, pasillo)
    VALUES (p_id_sucursal, nuevo_pasillo);
END;
$$;


ALTER FUNCTION inventario.insertar_estanteria(p_id_sucursal integer) OWNER TO postgres;

--
-- TOC entry 246 (class 1255 OID 33111)
-- Name: restaurar_estanteria(); Type: FUNCTION; Schema: inventario; Owner: postgres
--

CREATE FUNCTION inventario.restaurar_estanteria() RETURNS trigger
    LANGUAGE plpgsql
    AS $$BEGIN
    UPDATE inventario.detalle_estanterias
    SET stock = stock + OLD.cantidad
    WHERE id_producto = OLD.id_producto
  		AND numero IN (
      		SELECT id_e
      			FROM administracion.empleados em
      			JOIN inventario.estanteria es ON em.id_sucursal = es.id_sucursal
  	);

    RETURN OLD;
END;$$;


ALTER FUNCTION inventario.restaurar_estanteria() OWNER TO postgres;

--
-- TOC entry 243 (class 1255 OID 16622)
-- Name: add_total(); Type: FUNCTION; Schema: ventas; Owner: postgres
--

CREATE FUNCTION ventas.add_total() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
	update ventas.ventas
	set total = total + NEW.subtotal 
	where ventas.n_factura = NEW.num_factura;

	RETURN NEW;
END;
$$;


ALTER FUNCTION ventas.add_total() OWNER TO postgres;

--
-- TOC entry 244 (class 1255 OID 16769)
-- Name: calcular_subtotal_trigger(); Type: FUNCTION; Schema: ventas; Owner: postgres
--

CREATE FUNCTION ventas.calcular_subtotal_trigger() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    NEW.subtotal := ventas.gen_subtotal(NEW.id_producto::BIGINT, NEW.cantidad::BIGINT);
    RETURN NEW;
END;
$$;


ALTER FUNCTION ventas.calcular_subtotal_trigger() OWNER TO postgres;

--
-- TOC entry 247 (class 1255 OID 33146)
-- Name: cancelar_venta(bigint); Type: FUNCTION; Schema: ventas; Owner: postgres
--

CREATE FUNCTION ventas.cancelar_venta(id_venta_param bigint) RETURNS void
    LANGUAGE plpgsql
    AS $$
BEGIN
    DELETE FROM ventas.detalle_ventas WHERE num_factura = id_venta_param;
    DELETE FROM ventas.ventas WHERE n_factura = id_venta_param  AND finalizada = false;
END;
$$;


ALTER FUNCTION ventas.cancelar_venta(id_venta_param bigint) OWNER TO postgres;

--
-- TOC entry 245 (class 1255 OID 16771)
-- Name: gen_subtotal(bigint, bigint); Type: FUNCTION; Schema: ventas; Owner: postgres
--

CREATE FUNCTION ventas.gen_subtotal(id_producto bigint, cantidad bigint) RETURNS numeric
    LANGUAGE plpgsql
    AS $$
DECLARE
    precio DECIMAL(10, 2);
    subtotal DECIMAL(10, 2);
BEGIN
    SELECT inventario.productos.precio
    INTO precio
    FROM inventario.productos
    WHERE inventario.productos.id = id_producto;
    
    subtotal = precio * cantidad;
    
    RETURN subtotal;
END;
$$;


ALTER FUNCTION ventas.gen_subtotal(id_producto bigint, cantidad bigint) OWNER TO postgres;

--
-- TOC entry 259 (class 1255 OID 33089)
-- Name: get_factura(integer); Type: FUNCTION; Schema: ventas; Owner: postgres
--

CREATE FUNCTION ventas.get_factura(p_num_factura integer) RETURNS SETOF ventas.factura
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN QUERY
    SELECT 
        v.n_factura AS factura, 
        cli.nit AS nit,
        cli.nombre AS cliente, 
        e.nombre AS empleado, 
        pr.nombre AS producto,
		dv.cantidad as cantidad,
		pr.precio AS precio,
		dv.subtotal as subtotal,
		pr.id as id_producto
    FROM ventas.ventas v
    LEFT JOIN ventas.clientes cli ON v.nit_cliente = cli.nit
    JOIN administracion.empleados e ON v.id_empleado = e.id
    JOIN ventas.detalle_ventas dv ON dv.num_factura = v.n_factura
	JOIN inventario.productos pr ON dv.id_producto = pr.id
    WHERE v.n_factura = p_num_factura;
END;
$$;


ALTER FUNCTION ventas.get_factura(p_num_factura integer) OWNER TO postgres;

--
-- TOC entry 242 (class 1255 OID 16624)
-- Name: minus_total(); Type: FUNCTION; Schema: ventas; Owner: postgres
--

CREATE FUNCTION ventas.minus_total() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
	update ventas.ventas
	set total = total - OLD.subtotal 
	where ventas.n_factura = OLD.num_factura;

	RETURN NEW;
END;
$$;


ALTER FUNCTION ventas.minus_total() OWNER TO postgres;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 224 (class 1259 OID 16512)
-- Name: clientes; Type: TABLE; Schema: administracion; Owner: postgres
--

CREATE TABLE administracion.clientes (
    nit bigint NOT NULL,
    nombre character varying NOT NULL,
    gastos numeric DEFAULT 0 NOT NULL,
    gastos_ct numeric DEFAULT 0
);


ALTER TABLE administracion.clientes OWNER TO postgres;

--
-- TOC entry 223 (class 1259 OID 16505)
-- Name: tarjetas; Type: TABLE; Schema: administracion; Owner: postgres
--

CREATE TABLE administracion.tarjetas (
    codigo bigint NOT NULL,
    tipo smallint NOT NULL,
    nit_cliente bigint NOT NULL,
    puntos bigint DEFAULT 0 NOT NULL
);


ALTER TABLE administracion.tarjetas OWNER TO postgres;

--
-- TOC entry 225 (class 1259 OID 16524)
-- Name: ventas; Type: TABLE; Schema: ventas; Owner: postgres
--

CREATE TABLE ventas.ventas (
    id_empleado bigint NOT NULL,
    nit_cliente bigint,
    fecha date NOT NULL,
    total numeric DEFAULT 0,
    n_factura bigint NOT NULL,
    finalizada boolean DEFAULT false NOT NULL
);


ALTER TABLE ventas.ventas OWNER TO postgres;

--
-- TOC entry 233 (class 1259 OID 33148)
-- Name: all_clientes_tarjeta; Type: VIEW; Schema: administracion; Owner: postgres
--

CREATE VIEW administracion.all_clientes_tarjeta AS
 SELECT c.nombre,
    c.nit,
    count(v.n_factura) AS compras,
    t.codigo AS codigo_tarjeta,
    t.tipo AS tipo_tarjeta,
    c.gastos
   FROM ((administracion.clientes c
     LEFT JOIN administracion.tarjetas t ON ((c.nit = t.nit_cliente)))
     LEFT JOIN ventas.ventas v ON ((c.nit = v.nit_cliente)))
  GROUP BY c.nombre, c.nit, t.codigo, t.tipo, c.gastos;


ALTER VIEW administracion.all_clientes_tarjeta OWNER TO postgres;

--
-- TOC entry 222 (class 1259 OID 16493)
-- Name: empleados; Type: TABLE; Schema: administracion; Owner: postgres
--

CREATE TABLE administracion.empleados (
    id bigint NOT NULL,
    tipo smallint NOT NULL,
    nombre character varying NOT NULL,
    id_sucursal bigint NOT NULL,
    hp character varying(250) NOT NULL,
    salt character varying(250)
);


ALTER TABLE administracion.empleados OWNER TO postgres;

--
-- TOC entry 219 (class 1259 OID 16429)
-- Name: sucursal; Type: TABLE; Schema: administracion; Owner: postgres
--

CREATE TABLE administracion.sucursal (
    id bigint NOT NULL,
    nombre character varying(25) NOT NULL,
    direccion character varying(50) NOT NULL
);


ALTER TABLE administracion.sucursal OWNER TO postgres;

--
-- TOC entry 217 (class 1259 OID 16417)
-- Name: productos; Type: TABLE; Schema: inventario; Owner: postgres
--

CREATE TABLE inventario.productos (
    nombre character varying(255) NOT NULL,
    precio double precision NOT NULL,
    descripcion character varying(450),
    id bigint NOT NULL
);


ALTER TABLE inventario.productos OWNER TO postgres;

--
-- TOC entry 226 (class 1259 OID 16541)
-- Name: detalle_ventas; Type: TABLE; Schema: ventas; Owner: postgres
--

CREATE TABLE ventas.detalle_ventas (
    num_factura bigint NOT NULL,
    id_producto bigint NOT NULL,
    cantidad bigint NOT NULL,
    subtotal numeric DEFAULT 0.0,
    num_detalle bigint NOT NULL
);


ALTER TABLE ventas.detalle_ventas OWNER TO postgres;

--
-- TOC entry 240 (class 1259 OID 33248)
-- Name: top_productos; Type: VIEW; Schema: administracion; Owner: postgres
--

CREATE VIEW administracion.top_productos AS
 SELECT dv.id_producto,
    p.nombre,
    sum(dv.cantidad) AS sum
   FROM ((ventas.detalle_ventas dv
     JOIN ventas.ventas v ON ((dv.num_factura = v.n_factura)))
     JOIN inventario.productos p ON ((dv.id_producto = p.id)))
  GROUP BY dv.id_producto, dv.cantidad, p.nombre
  ORDER BY dv.cantidad DESC;


ALTER VIEW administracion.top_productos OWNER TO postgres;

--
-- TOC entry 241 (class 1259 OID 33259)
-- Name: top_sucursales; Type: VIEW; Schema: administracion; Owner: postgres
--

CREATE VIEW administracion.top_sucursales AS
 SELECT e.id_sucursal,
    s.nombre,
    sum(v.total) AS total_vendido
   FROM ((ventas.ventas v
     JOIN administracion.empleados e ON ((v.id_empleado = e.id)))
     JOIN administracion.sucursal s ON ((e.id_sucursal = s.id)))
  GROUP BY e.id_sucursal, s.nombre
  ORDER BY (sum(v.total)) DESC
 LIMIT 2;


ALTER VIEW administracion.top_sucursales OWNER TO postgres;

--
-- TOC entry 239 (class 1259 OID 33234)
-- Name: top_ventas; Type: VIEW; Schema: administracion; Owner: postgres
--

CREATE VIEW administracion.top_ventas AS
 SELECT id_empleado,
    nit_cliente,
    fecha,
    total,
    n_factura,
    finalizada
   FROM ventas.ventas
  ORDER BY total DESC
 LIMIT 10;


ALTER VIEW administracion.top_ventas OWNER TO postgres;

--
-- TOC entry 218 (class 1259 OID 16424)
-- Name: bodega; Type: TABLE; Schema: inventario; Owner: postgres
--

CREATE TABLE inventario.bodega (
    codigo bigint NOT NULL,
    id_sucursal bigint NOT NULL
);


ALTER TABLE inventario.bodega OWNER TO postgres;

--
-- TOC entry 235 (class 1259 OID 33173)
-- Name: detalle_bodega; Type: TABLE; Schema: inventario; Owner: postgres
--

CREATE TABLE inventario.detalle_bodega (
    id_detalle integer NOT NULL,
    id_bodega bigint NOT NULL,
    id_producto bigint NOT NULL,
    stock bigint DEFAULT 0 NOT NULL
);


ALTER TABLE inventario.detalle_bodega OWNER TO postgres;

--
-- TOC entry 234 (class 1259 OID 33172)
-- Name: detalle_bodega_id_detalle_seq; Type: SEQUENCE; Schema: inventario; Owner: postgres
--

CREATE SEQUENCE inventario.detalle_bodega_id_detalle_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE inventario.detalle_bodega_id_detalle_seq OWNER TO postgres;

--
-- TOC entry 5012 (class 0 OID 0)
-- Dependencies: 234
-- Name: detalle_bodega_id_detalle_seq; Type: SEQUENCE OWNED BY; Schema: inventario; Owner: postgres
--

ALTER SEQUENCE inventario.detalle_bodega_id_detalle_seq OWNED BY inventario.detalle_bodega.id_detalle;


--
-- TOC entry 221 (class 1259 OID 16478)
-- Name: detalle_estanterias; Type: TABLE; Schema: inventario; Owner: postgres
--

CREATE TABLE inventario.detalle_estanterias (
    id_producto bigint NOT NULL,
    numero bigint NOT NULL,
    stock integer DEFAULT 0 NOT NULL
);


ALTER TABLE inventario.detalle_estanterias OWNER TO postgres;

--
-- TOC entry 238 (class 1259 OID 33226)
-- Name: detalle_estanterias_d_seq; Type: SEQUENCE; Schema: inventario; Owner: postgres
--

CREATE SEQUENCE inventario.detalle_estanterias_d_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE inventario.detalle_estanterias_d_seq OWNER TO postgres;

--
-- TOC entry 5013 (class 0 OID 0)
-- Dependencies: 238
-- Name: detalle_estanterias_d_seq; Type: SEQUENCE OWNED BY; Schema: inventario; Owner: postgres
--

ALTER SEQUENCE inventario.detalle_estanterias_d_seq OWNED BY inventario.detalle_estanterias.numero;


--
-- TOC entry 220 (class 1259 OID 16468)
-- Name: estanteria; Type: TABLE; Schema: inventario; Owner: postgres
--

CREATE TABLE inventario.estanteria (
    id_e bigint NOT NULL,
    id_sucursal bigint NOT NULL,
    pasillo integer NOT NULL
);


ALTER TABLE inventario.estanteria OWNER TO postgres;

--
-- TOC entry 237 (class 1259 OID 33216)
-- Name: estanteria_d_seq; Type: SEQUENCE; Schema: inventario; Owner: postgres
--

CREATE SEQUENCE inventario.estanteria_d_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE inventario.estanteria_d_seq OWNER TO postgres;

--
-- TOC entry 5014 (class 0 OID 0)
-- Dependencies: 237
-- Name: estanteria_d_seq; Type: SEQUENCE OWNED BY; Schema: inventario; Owner: postgres
--

ALTER SEQUENCE inventario.estanteria_d_seq OWNED BY inventario.estanteria.id_e;


--
-- TOC entry 236 (class 1259 OID 33192)
-- Name: productos_disponibles(bodega); Type: VIEW; Schema: inventario; Owner: postgres
--

CREATE VIEW inventario."productos_disponibles(bodega)" AS
 SELECT p.id AS id_producto,
    p.nombre,
    d.stock,
    p.precio,
    b.id_sucursal
   FROM ((inventario.productos p
     JOIN inventario.detalle_bodega d ON ((p.id = d.id_producto)))
     JOIN inventario.bodega b ON ((d.id_bodega = b.codigo)));


ALTER VIEW inventario."productos_disponibles(bodega)" OWNER TO postgres;

--
-- TOC entry 232 (class 1259 OID 33141)
-- Name: productos_disponibles(estanteria); Type: VIEW; Schema: inventario; Owner: postgres
--

CREATE VIEW inventario."productos_disponibles(estanteria)" AS
 SELECT p.id AS id_producto,
    p.nombre,
    d.stock,
    p.precio,
    e.id_sucursal
   FROM ((inventario.productos p
     JOIN inventario.detalle_estanterias d ON ((p.id = d.id_producto)))
     JOIN inventario.estanteria e ON ((d.numero = e.id_e)));


ALTER VIEW inventario."productos_disponibles(estanteria)" OWNER TO postgres;

--
-- TOC entry 229 (class 1259 OID 16627)
-- Name: productos_id_seq; Type: SEQUENCE; Schema: inventario; Owner: postgres
--

CREATE SEQUENCE inventario.productos_id_seq
    START WITH 1000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE inventario.productos_id_seq OWNER TO postgres;

--
-- TOC entry 5015 (class 0 OID 0)
-- Dependencies: 229
-- Name: productos_id_seq; Type: SEQUENCE OWNED BY; Schema: inventario; Owner: postgres
--

ALTER SEQUENCE inventario.productos_id_seq OWNED BY inventario.productos.id;


--
-- TOC entry 231 (class 1259 OID 33133)
-- Name: detalle_venta_full; Type: VIEW; Schema: ventas; Owner: postgres
--

CREATE VIEW ventas.detalle_venta_full AS
 SELECT dv.num_factura,
    dv.id_producto,
    p.nombre AS nombre_producto,
    dv.cantidad,
    p.precio,
    dv.subtotal
   FROM (ventas.detalle_ventas dv
     JOIN inventario.productos p ON ((p.id = dv.id_producto)));


ALTER VIEW ventas.detalle_venta_full OWNER TO postgres;

--
-- TOC entry 228 (class 1259 OID 16594)
-- Name: detalle_ventas_num_detalle_seq; Type: SEQUENCE; Schema: ventas; Owner: postgres
--

CREATE SEQUENCE ventas.detalle_ventas_num_detalle_seq
    START WITH 1
    INCREMENT BY 1
    MINVALUE 0
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE ventas.detalle_ventas_num_detalle_seq OWNER TO postgres;

--
-- TOC entry 5016 (class 0 OID 0)
-- Dependencies: 228
-- Name: detalle_ventas_num_detalle_seq; Type: SEQUENCE OWNED BY; Schema: ventas; Owner: postgres
--

ALTER SEQUENCE ventas.detalle_ventas_num_detalle_seq OWNED BY ventas.detalle_ventas.num_detalle;


--
-- TOC entry 227 (class 1259 OID 16569)
-- Name: ventas_n_factura_seq; Type: SEQUENCE; Schema: ventas; Owner: postgres
--

CREATE SEQUENCE ventas.ventas_n_factura_seq
    START WITH 0
    INCREMENT BY 1
    MINVALUE 0
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE ventas.ventas_n_factura_seq OWNER TO postgres;

--
-- TOC entry 5017 (class 0 OID 0)
-- Dependencies: 227
-- Name: ventas_n_factura_seq; Type: SEQUENCE OWNED BY; Schema: ventas; Owner: postgres
--

ALTER SEQUENCE ventas.ventas_n_factura_seq OWNED BY ventas.ventas.n_factura;


--
-- TOC entry 4793 (class 2604 OID 33176)
-- Name: detalle_bodega id_detalle; Type: DEFAULT; Schema: inventario; Owner: postgres
--

ALTER TABLE ONLY inventario.detalle_bodega ALTER COLUMN id_detalle SET DEFAULT nextval('inventario.detalle_bodega_id_detalle_seq'::regclass);


--
-- TOC entry 4783 (class 2604 OID 33233)
-- Name: detalle_estanterias id_producto; Type: DEFAULT; Schema: inventario; Owner: postgres
--

ALTER TABLE ONLY inventario.detalle_estanterias ALTER COLUMN id_producto SET DEFAULT nextval('inventario.detalle_estanterias_d_seq'::regclass);


--
-- TOC entry 4782 (class 2604 OID 33223)
-- Name: estanteria id_e; Type: DEFAULT; Schema: inventario; Owner: postgres
--

ALTER TABLE ONLY inventario.estanteria ALTER COLUMN id_e SET DEFAULT nextval('inventario.estanteria_d_seq'::regclass);


--
-- TOC entry 4781 (class 2604 OID 16628)
-- Name: productos id; Type: DEFAULT; Schema: inventario; Owner: postgres
--

ALTER TABLE ONLY inventario.productos ALTER COLUMN id SET DEFAULT nextval('inventario.productos_id_seq'::regclass);


--
-- TOC entry 4792 (class 2604 OID 16595)
-- Name: detalle_ventas num_detalle; Type: DEFAULT; Schema: ventas; Owner: postgres
--

ALTER TABLE ONLY ventas.detalle_ventas ALTER COLUMN num_detalle SET DEFAULT nextval('ventas.detalle_ventas_num_detalle_seq'::regclass);


--
-- TOC entry 4789 (class 2604 OID 16570)
-- Name: ventas n_factura; Type: DEFAULT; Schema: ventas; Owner: postgres
--

ALTER TABLE ONLY ventas.ventas ALTER COLUMN n_factura SET DEFAULT nextval('ventas.ventas_n_factura_seq'::regclass);


--
-- TOC entry 4817 (class 2606 OID 33115)
-- Name: clientes clientes_nombre_key; Type: CONSTRAINT; Schema: administracion; Owner: postgres
--

ALTER TABLE ONLY administracion.clientes
    ADD CONSTRAINT clientes_nombre_key UNIQUE (nombre);


--
-- TOC entry 4819 (class 2606 OID 16518)
-- Name: clientes clientes_pk; Type: CONSTRAINT; Schema: administracion; Owner: postgres
--

ALTER TABLE ONLY administracion.clientes
    ADD CONSTRAINT clientes_pk PRIMARY KEY (nit);


--
-- TOC entry 4811 (class 2606 OID 16499)
-- Name: empleados empleados_pk; Type: CONSTRAINT; Schema: administracion; Owner: postgres
--

ALTER TABLE ONLY administracion.empleados
    ADD CONSTRAINT empleados_pk PRIMARY KEY (id);


--
-- TOC entry 4802 (class 2606 OID 16435)
-- Name: sucursal sucursal_pkey; Type: CONSTRAINT; Schema: administracion; Owner: postgres
--

ALTER TABLE ONLY administracion.sucursal
    ADD CONSTRAINT sucursal_pkey PRIMARY KEY (id);


--
-- TOC entry 4813 (class 2606 OID 16509)
-- Name: tarjetas tarjetas_pk; Type: CONSTRAINT; Schema: administracion; Owner: postgres
--

ALTER TABLE ONLY administracion.tarjetas
    ADD CONSTRAINT tarjetas_pk PRIMARY KEY (codigo);


--
-- TOC entry 4815 (class 2606 OID 16511)
-- Name: tarjetas tarjetas_unique; Type: CONSTRAINT; Schema: administracion; Owner: postgres
--

ALTER TABLE ONLY administracion.tarjetas
    ADD CONSTRAINT tarjetas_unique UNIQUE (nit_cliente);


--
-- TOC entry 4798 (class 2606 OID 16452)
-- Name: bodega bodega_id_sucursal_key; Type: CONSTRAINT; Schema: inventario; Owner: postgres
--

ALTER TABLE ONLY inventario.bodega
    ADD CONSTRAINT bodega_id_sucursal_key UNIQUE (id_sucursal);


--
-- TOC entry 4800 (class 2606 OID 16428)
-- Name: bodega bodega_pkey; Type: CONSTRAINT; Schema: inventario; Owner: postgres
--

ALTER TABLE ONLY inventario.bodega
    ADD CONSTRAINT bodega_pkey PRIMARY KEY (codigo);


--
-- TOC entry 4829 (class 2606 OID 33179)
-- Name: detalle_bodega detalle_bodega_pkey; Type: CONSTRAINT; Schema: inventario; Owner: postgres
--

ALTER TABLE ONLY inventario.detalle_bodega
    ADD CONSTRAINT detalle_bodega_pkey PRIMARY KEY (id_detalle);


--
-- TOC entry 4808 (class 2606 OID 16482)
-- Name: detalle_estanterias detalle_estanterias_pk; Type: CONSTRAINT; Schema: inventario; Owner: postgres
--

ALTER TABLE ONLY inventario.detalle_estanterias
    ADD CONSTRAINT detalle_estanterias_pk PRIMARY KEY (numero);


--
-- TOC entry 4804 (class 2606 OID 16472)
-- Name: estanteria estanteria_pk; Type: CONSTRAINT; Schema: inventario; Owner: postgres
--

ALTER TABLE ONLY inventario.estanteria
    ADD CONSTRAINT estanteria_pk PRIMARY KEY (id_e);


--
-- TOC entry 4806 (class 2606 OID 33109)
-- Name: estanteria estanteria_unica; Type: CONSTRAINT; Schema: inventario; Owner: postgres
--

ALTER TABLE ONLY inventario.estanteria
    ADD CONSTRAINT estanteria_unica UNIQUE (id_sucursal, pasillo);


--
-- TOC entry 4831 (class 2606 OID 33181)
-- Name: detalle_bodega no_repeat_producto_bodega; Type: CONSTRAINT; Schema: inventario; Owner: postgres
--

ALTER TABLE ONLY inventario.detalle_bodega
    ADD CONSTRAINT no_repeat_producto_bodega UNIQUE (id_bodega, id_producto);


--
-- TOC entry 4796 (class 2606 OID 16635)
-- Name: productos productos_pkey; Type: CONSTRAINT; Schema: inventario; Owner: postgres
--

ALTER TABLE ONLY inventario.productos
    ADD CONSTRAINT productos_pkey PRIMARY KEY (id);


--
-- TOC entry 4825 (class 2606 OID 16602)
-- Name: detalle_ventas detalle_ventas_pk; Type: CONSTRAINT; Schema: ventas; Owner: postgres
--

ALTER TABLE ONLY ventas.detalle_ventas
    ADD CONSTRAINT detalle_ventas_pk PRIMARY KEY (num_detalle);


--
-- TOC entry 4823 (class 2606 OID 16577)
-- Name: ventas ventas_pk; Type: CONSTRAINT; Schema: ventas; Owner: postgres
--

ALTER TABLE ONLY ventas.ventas
    ADD CONSTRAINT ventas_pk PRIMARY KEY (n_factura);


--
-- TOC entry 4809 (class 1259 OID 16642)
-- Name: fki_fk_de_producto; Type: INDEX; Schema: inventario; Owner: postgres
--

CREATE INDEX fki_fk_de_producto ON inventario.detalle_estanterias USING btree (id_producto);


--
-- TOC entry 4826 (class 1259 OID 16671)
-- Name: fki_fk_detalle_venta; Type: INDEX; Schema: ventas; Owner: postgres
--

CREATE INDEX fki_fk_detalle_venta ON ventas.detalle_ventas USING btree (num_factura);


--
-- TOC entry 4827 (class 1259 OID 16677)
-- Name: fki_fk_dv_producto; Type: INDEX; Schema: ventas; Owner: postgres
--

CREATE INDEX fki_fk_dv_producto ON ventas.detalle_ventas USING btree (id_producto);


--
-- TOC entry 4820 (class 1259 OID 16659)
-- Name: fki_fk_ventas_clientes; Type: INDEX; Schema: ventas; Owner: postgres
--

CREATE INDEX fki_fk_ventas_clientes ON ventas.ventas USING btree (nit_cliente);


--
-- TOC entry 4821 (class 1259 OID 16665)
-- Name: fki_fk_ventas_empleados; Type: INDEX; Schema: ventas; Owner: postgres
--

CREATE INDEX fki_fk_ventas_empleados ON ventas.ventas USING btree (id_empleado);


--
-- TOC entry 4846 (class 2620 OID 33171)
-- Name: clientes combrobar_tarjeta; Type: TRIGGER; Schema: administracion; Owner: postgres
--

CREATE TRIGGER combrobar_tarjeta AFTER UPDATE OF gastos_ct ON administracion.clientes FOR EACH ROW EXECUTE FUNCTION administracion.subir_tarjeta();


--
-- TOC entry 4844 (class 2620 OID 33208)
-- Name: detalle_estanterias colocarProducto; Type: TRIGGER; Schema: inventario; Owner: postgres
--

CREATE TRIGGER "colocarProducto" BEFORE INSERT ON inventario.detalle_estanterias FOR EACH ROW EXECUTE FUNCTION inventario.ingresar_de_bodega();


--
-- TOC entry 4845 (class 2620 OID 33197)
-- Name: detalle_estanterias colocar_producto; Type: TRIGGER; Schema: inventario; Owner: postgres
--

CREATE TRIGGER colocar_producto AFTER INSERT OR UPDATE OF stock ON inventario.detalle_estanterias FOR EACH ROW EXECUTE FUNCTION inventario.ingresar_de_bodega();

ALTER TABLE inventario.detalle_estanterias DISABLE TRIGGER colocar_producto;


--
-- TOC entry 4848 (class 2620 OID 33207)
-- Name: detalle_ventas actualizarStock; Type: TRIGGER; Schema: ventas; Owner: postgres
--

CREATE TRIGGER "actualizarStock" BEFORE INSERT ON ventas.detalle_ventas FOR EACH ROW EXECUTE FUNCTION inventario.actualizar_estanteria();


--
-- TOC entry 4849 (class 2620 OID 33110)
-- Name: detalle_ventas actualizar_stock; Type: TRIGGER; Schema: ventas; Owner: postgres
--

CREATE TRIGGER actualizar_stock AFTER INSERT ON ventas.detalle_ventas FOR EACH ROW EXECUTE FUNCTION inventario.actualizar_estanteria();

ALTER TABLE ventas.detalle_ventas DISABLE TRIGGER actualizar_stock;


--
-- TOC entry 4850 (class 2620 OID 16623)
-- Name: detalle_ventas actualizar_total; Type: TRIGGER; Schema: ventas; Owner: postgres
--

CREATE TRIGGER actualizar_total AFTER INSERT ON ventas.detalle_ventas FOR EACH ROW EXECUTE FUNCTION ventas.add_total();


--
-- TOC entry 4851 (class 2620 OID 16626)
-- Name: detalle_ventas borrar_total; Type: TRIGGER; Schema: ventas; Owner: postgres
--

CREATE TRIGGER borrar_total AFTER DELETE ON ventas.detalle_ventas FOR EACH ROW EXECUTE FUNCTION ventas.minus_total();


--
-- TOC entry 4847 (class 2620 OID 33165)
-- Name: ventas finalizar_venta; Type: TRIGGER; Schema: ventas; Owner: postgres
--

CREATE TRIGGER finalizar_venta AFTER UPDATE OF finalizada ON ventas.ventas FOR EACH ROW EXECUTE FUNCTION administracion.agregar_gastos();


--
-- TOC entry 4852 (class 2620 OID 16770)
-- Name: detalle_ventas generar_subtotal; Type: TRIGGER; Schema: ventas; Owner: postgres
--

CREATE TRIGGER generar_subtotal BEFORE INSERT ON ventas.detalle_ventas FOR EACH ROW EXECUTE FUNCTION ventas.calcular_subtotal_trigger();


--
-- TOC entry 4853 (class 2620 OID 33112)
-- Name: detalle_ventas restarurar_stock; Type: TRIGGER; Schema: ventas; Owner: postgres
--

CREATE TRIGGER restarurar_stock AFTER DELETE ON ventas.detalle_ventas FOR EACH ROW EXECUTE FUNCTION inventario.restaurar_estanteria();


--
-- TOC entry 4836 (class 2606 OID 16500)
-- Name: empleados fk_empleado_sucursal; Type: FK CONSTRAINT; Schema: administracion; Owner: postgres
--

ALTER TABLE ONLY administracion.empleados
    ADD CONSTRAINT fk_empleado_sucursal FOREIGN KEY (id_sucursal) REFERENCES administracion.sucursal(id);


--
-- TOC entry 4837 (class 2606 OID 16519)
-- Name: tarjetas fk_tarjeta_cliente; Type: FK CONSTRAINT; Schema: administracion; Owner: postgres
--

ALTER TABLE ONLY administracion.tarjetas
    ADD CONSTRAINT fk_tarjeta_cliente FOREIGN KEY (nit_cliente) REFERENCES administracion.clientes(nit);


--
-- TOC entry 4842 (class 2606 OID 33187)
-- Name: detalle_bodega fk_bodega_producto; Type: FK CONSTRAINT; Schema: inventario; Owner: postgres
--

ALTER TABLE ONLY inventario.detalle_bodega
    ADD CONSTRAINT fk_bodega_producto FOREIGN KEY (id_producto) REFERENCES inventario.productos(id);


--
-- TOC entry 4834 (class 2606 OID 16643)
-- Name: detalle_estanterias fk_de_producto; Type: FK CONSTRAINT; Schema: inventario; Owner: postgres
--

ALTER TABLE ONLY inventario.detalle_estanterias
    ADD CONSTRAINT fk_de_producto FOREIGN KEY (id_producto) REFERENCES inventario.productos(id) ON UPDATE RESTRICT ON DELETE RESTRICT NOT VALID;


--
-- TOC entry 4843 (class 2606 OID 33182)
-- Name: detalle_bodega fk_detalle_bodega; Type: FK CONSTRAINT; Schema: inventario; Owner: postgres
--

ALTER TABLE ONLY inventario.detalle_bodega
    ADD CONSTRAINT fk_detalle_bodega FOREIGN KEY (id_bodega) REFERENCES inventario.bodega(codigo);


--
-- TOC entry 4835 (class 2606 OID 33120)
-- Name: detalle_estanterias fk_detalle_estanterias; Type: FK CONSTRAINT; Schema: inventario; Owner: postgres
--

ALTER TABLE ONLY inventario.detalle_estanterias
    ADD CONSTRAINT fk_detalle_estanterias FOREIGN KEY (numero) REFERENCES inventario.estanteria(id_e) NOT VALID;


--
-- TOC entry 4833 (class 2606 OID 16473)
-- Name: estanteria fk_estanteria_sucursal; Type: FK CONSTRAINT; Schema: inventario; Owner: postgres
--

ALTER TABLE ONLY inventario.estanteria
    ADD CONSTRAINT fk_estanteria_sucursal FOREIGN KEY (id_sucursal) REFERENCES administracion.sucursal(id);


--
-- TOC entry 4832 (class 2606 OID 16446)
-- Name: bodega fk_sucursal_bodega; Type: FK CONSTRAINT; Schema: inventario; Owner: postgres
--

ALTER TABLE ONLY inventario.bodega
    ADD CONSTRAINT fk_sucursal_bodega FOREIGN KEY (id_sucursal) REFERENCES administracion.sucursal(id) NOT VALID;


--
-- TOC entry 4840 (class 2606 OID 16666)
-- Name: detalle_ventas fk_detalle_venta; Type: FK CONSTRAINT; Schema: ventas; Owner: postgres
--

ALTER TABLE ONLY ventas.detalle_ventas
    ADD CONSTRAINT fk_detalle_venta FOREIGN KEY (num_factura) REFERENCES ventas.ventas(n_factura) ON UPDATE RESTRICT ON DELETE RESTRICT NOT VALID;


--
-- TOC entry 4841 (class 2606 OID 16672)
-- Name: detalle_ventas fk_dv_producto; Type: FK CONSTRAINT; Schema: ventas; Owner: postgres
--

ALTER TABLE ONLY ventas.detalle_ventas
    ADD CONSTRAINT fk_dv_producto FOREIGN KEY (id_producto) REFERENCES inventario.productos(id) ON UPDATE RESTRICT ON DELETE RESTRICT NOT VALID;


--
-- TOC entry 4838 (class 2606 OID 16654)
-- Name: ventas fk_ventas_clientes; Type: FK CONSTRAINT; Schema: ventas; Owner: postgres
--

ALTER TABLE ONLY ventas.ventas
    ADD CONSTRAINT fk_ventas_clientes FOREIGN KEY (nit_cliente) REFERENCES administracion.clientes(nit) ON UPDATE RESTRICT ON DELETE RESTRICT NOT VALID;


--
-- TOC entry 4839 (class 2606 OID 16660)
-- Name: ventas fk_ventas_empleados; Type: FK CONSTRAINT; Schema: ventas; Owner: postgres
--

ALTER TABLE ONLY ventas.ventas
    ADD CONSTRAINT fk_ventas_empleados FOREIGN KEY (id_empleado) REFERENCES administracion.empleados(id) ON UPDATE RESTRICT ON DELETE RESTRICT NOT VALID;


--
-- TOC entry 5009 (class 0 OID 0)
-- Dependencies: 219
-- Name: TABLE sucursal; Type: ACL; Schema: administracion; Owner: postgres
--

REVOKE ALL ON TABLE administracion.sucursal FROM postgres;


--
-- TOC entry 5010 (class 0 OID 0)
-- Dependencies: 217
-- Name: TABLE productos; Type: ACL; Schema: inventario; Owner: postgres
--

REVOKE ALL ON TABLE inventario.productos FROM postgres;


--
-- TOC entry 5011 (class 0 OID 0)
-- Dependencies: 218
-- Name: TABLE bodega; Type: ACL; Schema: inventario; Owner: postgres
--

REVOKE ALL ON TABLE inventario.bodega FROM postgres;


-- Completed on 2024-09-26 13:37:13

--
-- PostgreSQL database dump complete
--


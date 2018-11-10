DROP SCHEMA arbitragem CASCADE;

CREATE SCHEMA arbitragem;

CREATE TABLE arbitragem.corretoras (
	id serial,
	nome text NOT NULL,
	url text NOT NULL,
	ativa boolean DEFAULT true,
	criado_em timestamp NOT NULL DEFAULT now(),
	PRIMARY KEY (id)
);

CREATE TABLE arbitragem.bancos (
	id serial,
	numero_banco int,
	corretora_id int REFERENCES arbitragem.corretoras,
	nome text,
	agencia int,
	agencia_digito smallint,
	conta int,
	conta_digito smallint,
	ativo boolean DEFAULT true,
	criado_em timestamp NOT NULL DEFAULT now(),
	PRIMARY KEY (id)
);
 

CREATE TABLE arbitragem.api (
	id serial,
	corretora_id int REFERENCES arbitragem.corretoras,
	descricao text,
	url text NOT NULL,
	tipo text NOT NULL, -- BTC, BRL
	ativa boolean DEFAULT true,
	criado_em timestamp NOT NULL DEFAULT now(),
	PRIMARY KEY (id)
);

CREATE TABLE arbitragem.api_campos (
	api_id int REFERENCES arbitragem.api,
	campo text,
	tipo_dado text NOT NULL,
	transacao text NOT NULL,
	criado_em timestamp NOT NULL DEFAULT now(),
	PRIMARY KEY(api_id, campo)
);

CREATE TABLE arbitragem.taxas (
	id serial,
	corretora_id int REFERENCES arbitragem.corretoras,
	moeda text DEFAULT 'BTC',
	taxa text NOT NULL,
	metrica text NOT NULL,
	valor numeric(14, 9) NOT NULL,
	ordem smallint NOT NULL DEFAULT 0,
	adicionado_em timestamp NOT NULL DEFAULT now(),
	PRIMARY KEY(id)
);

CREATE TABLE arbitragem.limites (
	id serial,
	corretora_id int REFERENCES arbitragem.corretoras,
	moeda text DEFAULT 'BTC',
	limite text NOT NULL,
	metrica text NOT NULL,
	limite_minimo numeric(14, 5) DEFAULT 0,
	limite_diario numeric(14, 5),	
	limite_mensal numeric(14, 5),
	adicionado_em timestamp NOT NULL DEFAULT now(),
	PRIMARY KEY(id)
);

CREATE TABLE arbitragem.cotacoes (
	id serial,
	moeda text DEFAULT 'BTC',
	cotado_em timestamp NOT NULL DEFAULT now(),
	PRIMARY KEY (id)
);

CREATE TABLE arbitragem.cotacoes_itens (
	id serial,
	cotacao_id int REFERENCES arbitragem.cotacoes,
	corretora_id int REFERENCES arbitragem.corretoras,
	tipo text NOT NULL,
	valor numeric(14, 9) NOT NULL,
	PRIMARY KEY (id)
);

INSERT INTO arbitragem.corretoras VALUES (1, 'NegocieCoins', 'https://broker.negociecoins.com.br');
INSERT INTO arbitragem.corretoras VALUES (2, 'MercadoBitcoin', 'https://www.mercadobitcoin.net');
INSERT INTO arbitragem.corretoras VALUES (3, 'Foxbit', 'https://api.blinktrade.com');
INSERT INTO arbitragem.corretoras VALUES (4, 'Braziliex', 'https://braziliex.com');
INSERT INTO arbitragem.corretoras VALUES (5, 'BitcoinToYou', 'https://www.bitcointoyou.com');

INSERT INTO arbitragem.api VALUES (1, 1, 'Ticker', 'https://broker.negociecoins.com.br/api/v3/BTC/ticker', 'BTC');
INSERT INTO arbitragem.api VALUES (2, 2, 'Ticker', 'https://www.mercadobitcoin.net/api/BTC/ticker/', 'BTC');
INSERT INTO arbitragem.api VALUES (3, 3, 'Ticker', 'https://api.blinktrade.com/api/v1/BRL/ticker', 'BTC');
INSERT INTO arbitragem.api VALUES (4, 4, 'Ticker', 'https://braziliex.com/api/v1/public/ticker/btc_brl', 'BTC');
INSERT INTO arbitragem.api VALUES (5, 5, 'Ticker', 'https://www.bitcointoyou.com/api/ticker.aspx', 'BTC');

INSERT INTO arbitragem.api_campos VALUES (1, '$.vol', 'bigDecimal', 'Volume');
INSERT INTO arbitragem.api_campos VALUES (1, '$.buy', 'bigDecimal', 'Comprar');
INSERT INTO arbitragem.api_campos VALUES (1, '$.sell', 'bigDecimal', 'Vender');

INSERT INTO arbitragem.api_campos VALUES (2, '$.ticker.vol', 'bigDecimal', 'Volume');
INSERT INTO arbitragem.api_campos VALUES (2, '$.ticker.buy', 'bigDecimal', 'Comprar');
INSERT INTO arbitragem.api_campos VALUES (2, '$.ticker.sell', 'bigDecimal', 'Vender');

INSERT INTO arbitragem.api_campos VALUES (3, '$.vol', 'bigDecimal', 'Volume');
INSERT INTO arbitragem.api_campos VALUES (3, '$.buy', 'bigDecimal', 'Comprar');
INSERT INTO arbitragem.api_campos VALUES (3, '$.sell', 'bigDecimal', 'Vender');

INSERT INTO arbitragem.api_campos VALUES (4, '$.baseVolume24', 'bigDecimal', 'Volume');
INSERT INTO arbitragem.api_campos VALUES (4, '$.highestBid', 'bigDecimal', 'Comprar');
INSERT INTO arbitragem.api_campos VALUES (4, '$.lowestAsk', 'bigDecimal', 'Vender');

INSERT INTO arbitragem.api_campos VALUES (5, '$.ticker.vol', 'bigDecimal', 'Volume');
INSERT INTO arbitragem.api_campos VALUES (5, '$.ticker.buy', 'bigDecimal', 'Comprar');
INSERT INTO arbitragem.api_campos VALUES (5, '$.ticker.sell', 'bigDecimal', 'Vender');

-- =========================      Taxas     =======================================
-- negocie-coins
INSERT INTO arbitragem.taxas VALUES (1, 1, 'BTC', 'DEPOSITO', 'BRL', 0.0, 1);
INSERT INTO arbitragem.taxas VALUES (2, 1, 'BTC', 'DEPOSITO', 'BTC', 0.0, 2);
INSERT INTO arbitragem.taxas VALUES (3, 1, 'BTC', 'RETIRADA', 'BRL', 8.9, 3);
INSERT INTO arbitragem.taxas VALUES (4, 1, 'BTC', 'RETIRADA', '%', 0.9, 4);
INSERT INTO arbitragem.taxas VALUES (5, 1, 'BTC', 'RETIRADA', 'BTC', 0.0003, 5);
INSERT INTO arbitragem.taxas VALUES (6, 1, 'BTC', 'COMPRA_ATIVA', 'BTC', 0.4,6);
INSERT INTO arbitragem.taxas VALUES (7, 1, 'BTC', 'COMPRA_PASSIVA', 'BTC', 0.3, 7);
INSERT INTO arbitragem.taxas VALUES (8, 1, 'BTC', 'VENDA_ATIVA', 'BTC', 0.4, 8);
INSERT INTO arbitragem.taxas VALUES (9, 1, 'BTC', 'VENDA_PASSIVA', 'BTC', 0.3, 9);

-- mercado bicoin
INSERT INTO arbitragem.taxas VALUES (10, 2, 'BTC', 'DEPOSITO', 'BRL', 0.0, 10);
INSERT INTO arbitragem.taxas VALUES (11, 2, 'BTC', 'DEPOSITO', 'BTC', 0.0, 11);
INSERT INTO arbitragem.taxas VALUES (12, 2, 'BTC', 'RETIRADA', 'BRL', 2.9, 12);
INSERT INTO arbitragem.taxas VALUES (13, 2, 'BTC', 'RETIRADA', '%', 1.99,  13);
INSERT INTO arbitragem.taxas VALUES (14, 2, 'BTC', 'COMPRA_ATIVA', 'BTC', 0.7, 14);
INSERT INTO arbitragem.taxas VALUES (15, 2, 'BTC', 'COMPRA_PASSIVA', 'BTC', 0.3, 15);
INSERT INTO arbitragem.taxas VALUES (16, 2, 'BTC', 'VENDA_ATIVA', 'BTC', 0.7, 16);
INSERT INTO arbitragem.taxas VALUES (17, 2, 'BTC', 'VENDA_PASSIVA', 'BTC', 0.3, 17);

-- =========================      Limites     =======================================
-- negocie-coins
INSERT INTO arbitragem.limites VALUES (1, 1, 'BTC', 'DEPOSITO', 'BRL', 300, 49999, 999999);
INSERT INTO arbitragem.limites VALUES (2, 1, 'BTC', 'DEPOSITO', 'BTC', 0.5, 50, 500);
INSERT INTO arbitragem.limites VALUES (3, 1, 'BTC', 'RETIRADA', 'BRL', 300, 10000, 300000);
INSERT INTO arbitragem.limites VALUES (4, 1, 'BTC', 'RETIRADA', '%', 300, 10000, 300000);
INSERT INTO arbitragem.limites VALUES (5, 1, 'BTC', 'RETIRADA', 'BTC', 0.5, 20, 200);
INSERT INTO arbitragem.limites VALUES (6, 1, 'BTC', 'COMPRA_ATIVA', 'BTC', 0.0005, 40, 999999);
INSERT INTO arbitragem.limites VALUES (7, 1, 'BTC', 'COMPRA_PASSIVA', 'BTC', 0.0005, 40, 999999);
INSERT INTO arbitragem.limites VALUES (8, 1, 'BTC', 'VENDA_ATIVA', 'BTC', 0.0005, 40, 999999);
INSERT INTO arbitragem.limites VALUES (9, 1, 'BTC', 'VENDA_PASSIVA', 'BTC', 0.0005, 40, 999999);

-- mercado bicoin
INSERT INTO arbitragem.limites VALUES (10, 2, 'BTC', 'DEPOSITO', 'BRL', 50, 5000, 5000);
INSERT INTO arbitragem.limites VALUES (11, 2, 'BTC', 'DEPOSITO', 'BTC', 0.0005, 999999, 999999);
INSERT INTO arbitragem.limites VALUES (12, 2, 'BTC', 'RETIRADA', 'BRL', 50, 500, 999999);
INSERT INTO arbitragem.limites VALUES (13, 2, 'BTC', 'RETIRADA', '%', 50, 500, 999999);
INSERT INTO arbitragem.limites VALUES (14, 2, 'BTC', 'COMPRA_ATIVA', 'BTC', 0, 999999, 999999);
INSERT INTO arbitragem.limites VALUES (15, 2, 'BTC', 'COMPRA_PASSIVA', 'BTC', 0, 999999, 999999);
INSERT INTO arbitragem.limites VALUES (16, 2, 'BTC', 'VENDA_ATIVA', 'BTC', 0, 999999, 999999);
INSERT INTO arbitragem.limites VALUES (17, 2, 'BTC', 'VENDA_PASSIVA', 'BTC', 0, 999999, 999999);


SELECT setval('arbitragem.corretoras_id_seq', 	(SELECT COALESCE(MAX(id), 1) FROM arbitragem.corretoras));
SELECT setval('arbitragem.api_id_seq', 			(SELECT COALESCE(MAX(id), 1) FROM arbitragem.api));
SELECT setval('arbitragem.taxas_id_seq',		(SELECT COALESCE(MAX(id), 1) FROM arbitragem.taxas));
SELECT setval('arbitragem.limites_id_seq',		(SELECT COALESCE(MAX(id), 1) FROM arbitragem.limites));
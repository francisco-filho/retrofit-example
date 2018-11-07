/* DROP SCHEMA arbitragem CASCADE; */

CREATE SCHEMA arbitragem;

CREATE TABLE arbitragem.corretoras (
	id serial,
	nome text NOT NULL,
	url text NOT NULL,
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
	criado_em timestamp NOT NULL DEFAULT now(),
	PRIMARY KEY (id)
);
 

CREATE TABLE arbitragem.api (
	id serial,
	corretora_id int REFERENCES arbitragem.corretoras,
	descricao text,
	url text NOT NULL,
	tipo text NOT NULL, -- BTC, BRL
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
	taxa text NOT NULL,
	metrica text NOT NULL,
	valor numeric(14, 9) NOT NULL,
	ordem smallint NOT NULL DEFAULT 0,
	adicionado_em timestamp NOT NULL DEFAULT now(),
	PRIMARY KEY(id)
);

CREATE TABLE arbitragem.cotacoes (
	id serial,
	api_id int REFERENCES arbitragem.api,
	valor numeric(14, 9) NOT NULL,
	cotado_em timestamp NOT NULL DEFAULT now(),
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

INSERT INTO arbitragem.api_campos VALUES (1, 'vol', 'bigInteger', 'Volume');
INSERT INTO arbitragem.api_campos VALUES (1, 'buy', 'bigInteger', 'Comprar');
INSERT INTO arbitragem.api_campos VALUES (1, 'sell', 'bigInteger', 'Vender');

INSERT INTO arbitragem.api_campos VALUES (2, 'ticker->vol', 'bigInteger', 'Volume');
INSERT INTO arbitragem.api_campos VALUES (2, 'ticker->buy', 'bigInteger', 'Comprar');
INSERT INTO arbitragem.api_campos VALUES (2, 'ticker->sell', 'bigInteger', 'Vender');

INSERT INTO arbitragem.api_campos VALUES (3, 'vol', 'bigInteger', 'Volume');
INSERT INTO arbitragem.api_campos VALUES (3, 'buy', 'bigInteger', 'Comprar');
INSERT INTO arbitragem.api_campos VALUES (3, 'sell', 'bigInteger', 'Vender');

INSERT INTO arbitragem.api_campos VALUES (4, 'baseVolume24', 'bigInteger', 'Volume');
INSERT INTO arbitragem.api_campos VALUES (4, 'highestBid', 'bigInteger', 'Comprar');
INSERT INTO arbitragem.api_campos VALUES (4, 'lowestAsk', 'bigInteger', 'Vender');

INSERT INTO arbitragem.api_campos VALUES (5, 'ticker->vol', 'bigInteger', 'Volume');
INSERT INTO arbitragem.api_campos VALUES (5, 'ticker->buy', 'bigInteger', 'Comprar');
INSERT INTO arbitragem.api_campos VALUES (5, 'ticker->sell', 'bigInteger', 'Vender');

SELECT setval('arbitragem.corretoras_id_seq', 	(SELECT COALESCE(MAX(id), 1) FROM arbitragem.corretoras));
SELECT setval('arbitragem.api_id_seq', 			(SELECT COALESCE(MAX(id), 1) FROM arbitragem.api));

CREATE TABLE transactions (
    transid TEXT PRIMARY KEY,
    linkid TEXT,
    request TEXT,
    response TEXT,
    created_dt TIMESTAMP,
    response_dt TIMESTAMP
);

CREATE TABLE coupling_codes (
    couplingcode TEXT,
    linkid TEXT,
    created_dt TIMESTAMP
    PRIMARY KEY (couplingcode)
);

CREATE TABLE musap_accounts (
    musapid TEXT PRIMARY KEY,
    fcmtoken TEXT,
    apnstoken TEXT,
    created_dt TIMESTAMP
);

CREATE TABLE link_ids (
    musapid TEXT,
    linkid TEXT PRIMARY KEY,
    name TEXT,
    FOREIGN KEY (musapid) REFERENCES musap_accounts(musapid)
);

CREATE TABLE transport_keys (
    musapid TEXT,
    mackey TEXT,
    enckey TEXT,
    PRIMARY KEY (musapid),
    FOREIGN KEY (musapid) REFERENCES musap_accounts(musapid)
);

CREATE TABLE key_details (
    musapid TEXT,
    keyid TEXT,
    keyname TEXT,
    publickey BYTEA,
    certificate BYTEA,
    created_dt TIMESTAMP,
    modified_dt TIMESTAMP,
    PRIMARY KEY (musapid, keyid)
);
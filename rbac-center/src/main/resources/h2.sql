CREATE TABLE "product" (
"autoId"  INT PRIMARY KEY AUTO_INCREMENT NOT NULL,
"id"  VARCHAR NOT NULL,
"name"  VARCHAR NOT NULL,
"des"  TEXT,
"lastModify"  BIGINT NOT NULL,
"seq"  INT
);
CREATE UNIQUE INDEX "product_id" ON "product" ("id" ASC);
CREATE INDEX "product_seq" ON "product" ("seq" ASC);

CREATE TABLE "user" (
"autoId"  INT PRIMARY KEY AUTO_INCREMENT NOT NULL,
"id"  VARCHAR NOT NULL,
"name"  VARCHAR NOT NULL,
"des"  TEXT,
"seq"  INT
);
CREATE UNIQUE INDEX "user_id" ON "user" ("id" ASC);
CREATE INDEX "user_seq" ON "user" ("seq" ASC);

CREATE TABLE "param" (
"autoId"  INT PRIMARY KEY AUTO_INCREMENT NOT NULL,
"proAutoId"  INT NOT NULL,
"key"  VARCHAR NOT NULL,
"name"  VARCHAR NOT NULL,
"des"  TEXT,
"seq"  INT
);
CREATE INDEX "param_proAutoId" ON "param" ("proAutoId" ASC);
CREATE UNIQUE INDEX "param_proAutoId_key" ON "param" ("proAutoId" ASC, "key" ASC);
CREATE INDEX "param_seq" ON "param" ("seq" ASC);

CREATE TABLE "access" (
"autoId"  INT PRIMARY KEY AUTO_INCREMENT NOT NULL,
"proAutoId"  INT NOT NULL,
"pattern"  VARCHAR NOT NULL,
"name"  VARCHAR NOT NULL,
"des"  TEXT,
"seq"  INT
);
CREATE INDEX "access_proAutoId" ON "access" ("proAutoId" ASC);
CREATE UNIQUE INDEX "access_proAutoId_pattern" ON "access" ("proAutoId" ASC, "pattern" ASC);
CREATE INDEX "access_seq" ON "access" ("seq" ASC);

CREATE TABLE "permission" (
"autoId"  INT PRIMARY KEY AUTO_INCREMENT NOT NULL,
"proAutoId"  INT NOT NULL,
"id"  VARCHAR NOT NULL,
"name"  VARCHAR NOT NULL,
"des"  TEXT,
"seq"  INT
);
CREATE INDEX "permission_proAutoId" ON "permission" ("proAutoId" ASC);
CREATE UNIQUE INDEX "permission_proAutoId_id" ON "permission" ("proAutoId" ASC, "id" ASC);
CREATE INDEX "permission_seq" ON "permission" ("seq" ASC);

CREATE TABLE "role" (
"autoId"  INT PRIMARY KEY AUTO_INCREMENT NOT NULL,
"proAutoId"  INT NOT NULL,
"id"  VARCHAR NOT NULL,
"name"  VARCHAR NOT NULL,
"des"  TEXT,
"seq"  INT
);
CREATE INDEX "role_proAutoId" ON "role" ("proAutoId" ASC);
CREATE UNIQUE INDEX "role_proAutoId_id" ON "role" ("proAutoId" ASC, "id" ASC);
CREATE INDEX "role_seq" ON "role" ("seq" ASC);

CREATE TABLE "role_access" (
"proAutoId"  INT NOT NULL,
"roleAutoId"  INT NOT NULL,
"accessAutoId"  INT NOT NULL,
"seq"  INT NOT NULL,
PRIMARY KEY ("proAutoId", "roleAutoId", "accessAutoId")
);
CREATE INDEX "role_access_roleAutoId" ON "role_access" ("roleAutoId" ASC);
CREATE INDEX "role_access_accessAutoId" ON "role_access" ("accessAutoId" ASC);
CREATE INDEX "role_access_seq" ON "role_access" ("seq" ASC);

CREATE TABLE "role_permission" (
"proAutoId"  INT NOT NULL,
"roleAutoId"  INT NOT NULL,
"permissionAutoId"  INT NOT NULL,
"seq"  INT NOT NULL,
PRIMARY KEY ("proAutoId" ASC, "roleAutoId" ASC, "permissionAutoId" ASC)
);
CREATE INDEX "role_permission_roleAutoId" ON "role_permission" ("roleAutoId" ASC);
CREATE INDEX "role_permission_permissionAutoId" ON "role_permission" ("permissionAutoId" ASC);
CREATE INDEX "role_permission_seq" ON "role_permission" ("seq" ASC);

CREATE TABLE "visitor_param" (
"proAutoId"  INT NOT NULL,
"paramAutoId"  INT NOT NULL,
"val"  TEXT,
PRIMARY KEY ("proAutoId", "paramAutoId")
);
CREATE INDEX "visitor_param_paramAutoId" ON "visitor_param" ("paramAutoId" ASC);

CREATE TABLE "visitor_role" (
"proAutoId"  INT NOT NULL,
"roleAutoId"  INT NOT NULL,
"seq"  INT NOT NULL,
PRIMARY KEY ("proAutoId", "roleAutoId")
);
CREATE INDEX "visitor_role_roleAutoId" ON "visitor_role" ("roleAutoId" ASC);
CREATE INDEX "visitor_role_seq" ON "visitor_role" ("seq" ASC);

CREATE TABLE "visitor_access" (
"proAutoId"  INT NOT NULL,
"accessAutoId"  INT NOT NULL,
"seq"  INT NOT NULL,
PRIMARY KEY ("proAutoId", "accessAutoId")
);
CREATE INDEX "visitor_access_accessAutoId" ON "visitor_access" ("accessAutoId" ASC);
CREATE INDEX "visitor_access_seq" ON "visitor_access" ("seq" ASC);

CREATE TABLE "visitor_permission" (
"proAutoId"  INT NOT NULL,
"permissionAutoId"  INT NOT NULL,
"seq"  INT NOT NULL,
PRIMARY KEY ("proAutoId", "permissionAutoId")
);
CREATE INDEX "visitor_permission_permissionAutoId" ON "visitor_permission" ("permissionAutoId" ASC);
CREATE INDEX "visitor_permission_seq" ON "visitor_permission" ("seq" ASC);


CREATE TABLE "user_role" (
"userAutoId"  INT NOT NULL,
"proAutoId"  INT NOT NULL,
"roleAutoId"  INT NOT NULL,
"seq"  INT NOT NULL,
PRIMARY KEY ("userAutoId" ASC, "proAutoId" ASC, "roleAutoId" ASC)
);
CREATE INDEX "user_role_proAutoId" ON "user_role" ("proAutoId" ASC);
CREATE INDEX "user_role_roleAutoId" ON "user_role" ("roleAutoId" ASC);
CREATE INDEX "user_role_seq" ON "user_role" ("seq" ASC);

CREATE TABLE "user_param" (
"userAutoId"  INT NOT NULL,
"proAutoId"  INT NOT NULL,
"paramAutoId"  INT NOT NULL,
"val"  TEXT,
PRIMARY KEY ("userAutoId", "proAutoId", "paramAutoId")
);
CREATE INDEX "user_param_proAutoId" ON "user_param" ("proAutoId" ASC);
CREATE INDEX "user_param_paramAutoId" ON "user_param" ("paramAutoId" ASC);

CREATE TABLE "user_access" (
"userAutoId"  INT NOT NULL,
"proAutoId"  INT NOT NULL,
"accessAutoId"  INT NOT NULL,
"seq"  INT NOT NULL,
PRIMARY KEY ("userAutoId", "proAutoId", "accessAutoId")
);
CREATE INDEX "user_access_proAutoId" ON "user_access" ("proAutoId" ASC);
CREATE INDEX "user_access_accessAutoId" ON "user_access" ("accessAutoId" ASC);
CREATE INDEX "user_access_seq" ON "user_access" ("seq" ASC);

CREATE TABLE "user_permission" (
"userAutoId"  INT NOT NULL,
"proAutoId"  INT NOT NULL,
"permissionAutoId"  INT NOT NULL,
"seq"  INT NOT NULL,
PRIMARY KEY ("userAutoId", "proAutoId", "permissionAutoId")
);
CREATE INDEX "user_permission_proAutoId" ON "user_permission" ("proAutoId" ASC);
CREATE INDEX "user_permission_permissionAutoId" ON "user_permission" ("permissionAutoId" ASC);
CREATE INDEX "user_permission_seq" ON "user_permission" ("seq" ASC);

CREATE TABLE "oplog" (
"operator"  VARCHAR NOT NULL,
"ip"  VARCHAR,
"action"  VARCHAR NOT NULL,
"time"  TIMESTAMP NOT NULL,
"proAutoId" INT,
"orig"  TEXT,
"now"  TEXT,
"comment"  TEXT
);
CREATE INDEX "oplog_operator" ON "oplog" ("operator" ASC);
CREATE INDEX "oplog_time" ON "oplog" ("time" DESC);
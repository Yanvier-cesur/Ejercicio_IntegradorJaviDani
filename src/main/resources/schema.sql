USE IntegradorJaviDani;

CREATE TABLE IF NOT EXISTS empresa (
                                       id INT AUTO_INCREMENT PRIMARY KEY,
                                       nombre VARCHAR(150) NOT NULL,
                                       sector VARCHAR(100),
                                       web VARCHAR(255),
                                       fecha_registro DATE
);

CREATE TABLE IF NOT EXISTS oferta (
                                      id INT AUTO_INCREMENT PRIMARY KEY,
                                      id_empresa INT NOT NULL,
                                      titulo VARCHAR(150) NOT NULL,
                                      descripcion TEXT,
                                      estado VARCHAR(30) NOT NULL,
                                      fecha_postulacion DATE NOT NULL,
                                      enlace VARCHAR(255),
                                      salario DOUBLE,
                                      notas TEXT,
                                      CONSTRAINT fk_oferta_empresa FOREIGN KEY (id_empresa) REFERENCES empresa(id)
);

CREATE TABLE IF NOT EXISTS entrevista (
                                          id INT AUTO_INCREMENT PRIMARY KEY,
                                          id_oferta INT NOT NULL,
                                          fecha DATE NOT NULL,
                                          tipo VARCHAR(50),
                                          notas TEXT,
                                          resultado VARCHAR(30),
                                          CONSTRAINT fk_entrevista_oferta FOREIGN KEY (id_oferta) REFERENCES oferta(id)
);

SHOW TABLES;
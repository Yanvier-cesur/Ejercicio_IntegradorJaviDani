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

INSERT INTO empresa (id, nombre, sector, web, fecha_registro) VALUES
                                                                  (1, 'Indra Sistemas', 'Consultoría IT', 'https://www.indracompany.com', '2026-01-15'),
                                                                  (2, 'Accenture España', 'Consultoría y Tecnología', 'https://www.accenture.com/es-es', '2026-02-03'),
                                                                  (3, 'NTT DATA Europe & LATAM', 'Servicios TI', 'https://es.nttdata.com', '2026-02-18'),
                                                                  (4, 'Capgemini España', 'Transformación Digital', 'https://www.capgemini.com/es-es', '2026-03-05'),
                                                                  (5, 'Telefónica Tech', 'Ciberseguridad y Cloud', 'https://telefonicatech.com', '2026-04-12'),
                                                                  (6, 'Sopra Steria', 'Ingeniería y Software', 'https://www.soprasteria.es', '2026-05-20'),
                                                                  (7, 'Ayesa', 'Tecnología y Consultoría', 'https://www.ayesa.com', '2026-06-08'),
                                                                  (8, 'Viewnext', 'Desarrollo de Software', 'https://www.viewnext.com', '2026-07-14'),
                                                                  (9, 'Minsait', 'Soluciones Digitales', 'https://www.minsait.com', '2026-09-02'),
                                                                  (10, 'Globant España', 'Ingeniería Digital', 'https://www.globant.com/es', '2026-10-19');


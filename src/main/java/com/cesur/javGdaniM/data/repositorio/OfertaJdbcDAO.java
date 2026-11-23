package com.cesur.javGdaniM.data.repositorio;

import com.cesur.javGdaniM.data.entidad.Empresa;
import com.cesur.javGdaniM.data.entidad.EstadoCandidatura;
import com.cesur.javGdaniM.data.entidad.Oferta;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OfertaJdbcDAO implements DAO<Oferta> {

    private static final String BASE_SELECT_WITH_EMPRESA =
            "SELECT o.id, o.id_empresa, o.titulo, o.descripcion, o.estado, " +
                    "o.fecha_postulacion, o.enlace, o.salario, o.notas, " +
                    "e.nombre AS empresa_nombre, e.sector AS empresa_sector, " +
                    "e.web AS empresa_web, e.fecha_registro AS empresa_fecha_registro " +
                    "FROM oferta o " +
                    "LEFT JOIN empresa e ON o.id_empresa = e.id";

    @Override
    public Optional<Oferta> findById(Long id) {
        String sql = BASE_SELECT_WITH_EMPRESA + " WHERE o.id = ?";
        try (Connection conn = DataSourceFactory.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowWithEmpresa(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error buscando oferta con id " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Oferta> findAll() {
        String sql = BASE_SELECT_WITH_EMPRESA + " ORDER BY o.fecha_postulacion DESC, o.id DESC";
        List<Oferta> ofertas = new ArrayList<>();
        try (Connection conn = DataSourceFactory.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ofertas.add(mapRowWithEmpresa(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error listando ofertas", e);
        }
        return ofertas;
    }

    @Override
    public Oferta save(Oferta oferta) {
        String sql = "INSERT INTO oferta (id_empresa, titulo, descripcion, estado, fecha_postulacion, enlace, salario, notas) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DataSourceFactory.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, getEmpresaId(oferta));
            ps.setString(2, oferta.getTitulo());
            ps.setString(3, oferta.getDescripcion());
            ps.setString(4, oferta.getEstadoCandidatura() != null ? oferta.getEstadoCandidatura().name() : null);
            ps.setDate(5, oferta.getFecha() != null ? Date.valueOf(oferta.getFecha()) : null);
            ps.setString(6, oferta.getEnlace());
            ps.setDouble(7, oferta.getSalario());
            ps.setString(8, oferta.getNotas());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    oferta.setId(keys.getInt(1));
                }
            }
            return oferta;
        } catch (SQLException e) {
            throw new RuntimeException("Error guardando oferta", e);
        }
    }

    @Override
    public void update(Oferta oferta) {
        String sql = "UPDATE oferta SET id_empresa = ?, titulo = ?, descripcion = ?, estado = ?, " +
                "fecha_postulacion = ?, enlace = ?, salario = ?, notas = ? WHERE id = ?";
        try (Connection conn = DataSourceFactory.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, getEmpresaId(oferta));
            ps.setString(2, oferta.getTitulo());
            ps.setString(3, oferta.getDescripcion());
            ps.setString(4, oferta.getEstadoCandidatura() != null ? oferta.getEstadoCandidatura().name() : null);
            ps.setDate(5, oferta.getFecha() != null ? Date.valueOf(oferta.getFecha()) : null);
            ps.setString(6, oferta.getEnlace());
            ps.setDouble(7, oferta.getSalario());
            ps.setString(8, oferta.getNotas());
            ps.setInt(9, oferta.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error actualizando oferta con id " + oferta.getId(), e);
        }
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM oferta WHERE id = ?";
        try (Connection conn = DataSourceFactory.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error eliminando oferta con id " + id, e);
        }
    }

    public List<Oferta> findByEmpresaId(long empresaId) {
        String sql = BASE_SELECT_WITH_EMPRESA + " WHERE o.id_empresa = ? ORDER BY o.fecha_postulacion DESC";
        List<Oferta> ofertas = new ArrayList<>();
        try (Connection conn = DataSourceFactory.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, empresaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ofertas.add(mapRowWithEmpresa(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error buscando ofertas de la empresa " + empresaId, e);
        }
        return ofertas;
    }

    public List<Oferta> findByEstado(EstadoCandidatura estado) {
        String sql = BASE_SELECT_WITH_EMPRESA + " WHERE o.estado = ? ORDER BY o.fecha_postulacion DESC";
        List<Oferta> ofertas = new ArrayList<>();
        try (Connection conn = DataSourceFactory.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, estado.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ofertas.add(mapRowWithEmpresa(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error buscando ofertas por estado " + estado, e);
        }
        return ofertas;
    }

    public void updateEstado(long ofertaId, EstadoCandidatura nuevoEstado) {
        String sql = "UPDATE oferta SET estado = ? WHERE id = ?";
        try (Connection conn = DataSourceFactory.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado.name());
            ps.setLong(2, ofertaId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error actualizando estado de oferta " + ofertaId, e);
        }
    }

    public List<Oferta> search(Long empresaId, EstadoCandidatura estado, Double salarioMin, LocalDate desdeFecha) {
        StringBuilder sql = new StringBuilder(BASE_SELECT_WITH_EMPRESA + " WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (empresaId != null) {
            sql.append(" AND o.id_empresa = ?");
            params.add(empresaId);
        }
        if (estado != null) {
            sql.append(" AND o.estado = ?");
            params.add(estado.name());
        }
        if (salarioMin != null) {
            sql.append(" AND o.salario >= ?");
            params.add(salarioMin);
        }
        if (desdeFecha != null) {
            sql.append(" AND o.fecha_postulacion >= ?");
            params.add(Date.valueOf(desdeFecha));
        }
        sql.append(" ORDER BY o.fecha_postulacion DESC, o.id DESC");

        List<Oferta> ofertas = new ArrayList<>();
        try (Connection conn = DataSourceFactory.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ofertas.add(mapRowWithEmpresa(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error filtrando ofertas", e);
        }
        return ofertas;
    }

    private long getEmpresaId(Oferta oferta) {
        if (oferta.getEmpresa() == null || oferta.getEmpresa().id <= 0) {
            throw new IllegalArgumentException("La oferta debe tener una empresa válida con id > 0");
        }
        return oferta.getEmpresa().id;
    }

    private Oferta mapRowWithEmpresa(ResultSet rs) throws SQLException {
        Oferta oferta = new Oferta();
        oferta.setId(rs.getInt("id"));
        oferta.setTitulo(rs.getString("titulo"));
        oferta.setDescripcion(rs.getString("descripcion"));

        String estadoStr = rs.getString("estado");
        if (estadoStr != null && !estadoStr.isBlank()) {
            oferta.setEstadoCandidatura(EstadoCandidatura.valueOf(estadoStr.trim().toUpperCase()));
        }

        Date fechaPost = rs.getDate("fecha_postulacion");
        oferta.setFecha(fechaPost != null ? fechaPost.toLocalDate() : null);

        oferta.setEnlace(rs.getString("enlace"));
        oferta.setSalario(rs.getDouble("salario"));
        oferta.setNotas(rs.getString("notas"));

        Empresa empresa = new Empresa();
        empresa.id = rs.getLong("id_empresa");
        empresa.nombre = rs.getString("empresa_nombre");
        empresa.sector = rs.getString("empresa_sector");
        empresa.web = rs.getString("empresa_web");
        Date fechaReg = rs.getDate("empresa_fecha_registro");
        empresa.fechaRegistro = fechaReg != null ? fechaReg.toLocalDate() : null;

        oferta.setEmpresa(empresa);
        return oferta;
    }
}
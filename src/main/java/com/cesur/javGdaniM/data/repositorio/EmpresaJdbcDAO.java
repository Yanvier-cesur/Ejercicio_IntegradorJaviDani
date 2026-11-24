package com.cesur.javGdaniM.data.repositorio;

import com.cesur.javGdaniM.data.entidad.Empresa;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EmpresaJdbcDAO implements TransactionalDAO<Empresa> {

    @Override
    public Optional<Empresa> findById(Long id) {
        try (Connection conn = DataSourceFactory.getDataSource().getConnection()) {
            return findById(conn, id);
        } catch (SQLException e) {
            throw new RuntimeException("Error buscando empresa con id " + id, e);
        }
    }

    @Override
    public Optional<Empresa> findById(Connection conn, Long id) {
        String sql = "SELECT id, nombre, sector, web, fecha_registro FROM empresa WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error buscando empresa con id " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Empresa> findAll() {
        String sql = "SELECT id, nombre, sector, web, fecha_registro FROM empresa";
        List<Empresa> empresas = new ArrayList<>();
        try (Connection conn = DataSourceFactory.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                empresas.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error listando empresas", e);
        }
        return empresas;
    }

    @Override
    public List<Empresa> findAll(Connection conn) {
        return List.of();
    }

    @Override
    public Empresa save(Empresa empresa) {
        try (Connection conn = DataSourceFactory.getDataSource().getConnection()) {
            return save(conn, empresa);
        } catch (SQLException e) {
            throw new RuntimeException("Error guardando empresa", e);
        }
    }

    @Override
    public Empresa save(Connection conn, Empresa empresa) {
        String sql = "INSERT INTO empresa (nombre, sector, web, fecha_registro) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, empresa.nombre);
            ps.setString(2, empresa.sector);
            ps.setString(3, empresa.web);
            ps.setDate(4, empresa.fechaRegistro != null ? Date.valueOf(empresa.fechaRegistro) : null);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    empresa.id = keys.getLong(1);
                }
            }
            return empresa;
        } catch (SQLException e) {
            throw new RuntimeException("Error guardando empresa", e);
        }
    }

    @Override
    public void update(Empresa empresa) {
        try (Connection conn = DataSourceFactory.getDataSource().getConnection()) {
            update(conn, empresa);
        } catch (SQLException e) {
            throw new RuntimeException("Error actualizando empresa con id " + empresa.id, e);
        }
    }

    @Override
    public void update(Connection conn, Empresa empresa) {
        String sql = "UPDATE empresa SET nombre = ?, sector = ?, web = ?, fecha_registro = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, empresa.nombre);
            ps.setString(2, empresa.sector);
            ps.setString(3, empresa.web);
            ps.setDate(4, empresa.fechaRegistro != null ? Date.valueOf(empresa.fechaRegistro) : null);
            ps.setLong(5, empresa.id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error actualizando empresa con id " + empresa.id, e);
        }
    }

    @Override
    public void deleteById(Long id) {
        try (Connection conn = DataSourceFactory.getDataSource().getConnection()) {
            deleteById(conn, id);
        } catch (SQLException e) {
            throw new RuntimeException("Error eliminando empresa con id " + id, e);
        }
    }

    @Override
    public void deleteById(Connection conn, Long id) {
        String sql = "DELETE FROM empresa WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error eliminando empresa con id " + id, e);
        }
    }

    private Empresa mapRow(ResultSet rs) throws SQLException {
        Date fechaSql = rs.getDate("fecha_registro");
        Empresa empresa = new Empresa();
        empresa.id = rs.getLong("id");
        empresa.nombre = rs.getString("nombre");
        empresa.sector = rs.getString("sector");
        empresa.web = rs.getString("web");
        empresa.fechaRegistro = fechaSql != null ? fechaSql.toLocalDate() : null;
        return empresa;
    }
}
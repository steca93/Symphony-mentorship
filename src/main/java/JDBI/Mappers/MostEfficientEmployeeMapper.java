package JDBI.Mappers;

import JDBI.Dtos.MostEfficientEmployeeDto;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MostEfficientEmployeeMapper implements RowMapper<MostEfficientEmployeeDto> {

    @Override
    public MostEfficientEmployeeDto map (ResultSet rs, StatementContext ctx) throws SQLException {
        return new MostEfficientEmployeeDto(rs.getString("product_name"), rs.getString("employee_name"), rs.getInt("ukupno_ordera"));
    }
}

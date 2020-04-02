package com.jad.boiler.remote.dao;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.FileCopyUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

@Repository
@Slf4j
public class StateHistoryDao {

    private static final int MAX_HOURS = 27;
    private final Object lock = new Object();

    @Autowired
    private JdbcTemplate template;

    @Value("classpath:db/init.sql")
    private Resource initSql;

    @PostConstruct
    public void initSchema() {
        template.execute(asString(initSql));
    }



    public void addStatus(String type, float value) {
        template.update("INSERT INTO current(name, value) VALUES (?, ?)", type, value);
    }

    public List<Point> drainCurrentDay() {
        List<Point> query = template.query("SELECT * from current", Point::map);
        template.update("TRUNCATE current");
        return query;
    }

    public List<Point> loadOldestNHours(int n) {
        return template.query("SELECT * from current WHERE created < now() - interval ? hour", Point::map, MAX_HOURS - n);
    }

    public void removeOldestNHours(int n) {
        template.update("DELETE FROM current WHERE created < now() - interval ? hour", MAX_HOURS - n);
    }



    private static String asString(Resource resource) {
        try (Reader reader = new InputStreamReader(resource.getInputStream(), UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void storeHistory(Map<String, Double> result, Timestamp time) {
        for (Map.Entry<String, Double> entry : result.entrySet()) {
            if (entry.getValue() != null) {
                template.update("INSERT INTO history(name, value, created) VALUES (?, ?, ?)",
                        entry.getKey(), entry.getValue().floatValue());
            }
        }

    }

    public List<Point> loadLastNHours(int n, String type) {
        return template.query("SELECT * FROM current WHERE name = ? AND created > now() - interval ? hour", Point::map, type, n);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Point {
        private String type;
        private Float value;
        private Timestamp time;

        public static Point map(ResultSet resultSet, int i) {
            Point point = new Point();
            try {
                point.type = resultSet.getString("name");
                point.value = resultSet.getFloat("value");
                point.time = resultSet.getTimestamp("created");
                return point;
            } catch (SQLException e) {
                throw new RuntimeException("Can not load point", e);
            }

        }
    }
}

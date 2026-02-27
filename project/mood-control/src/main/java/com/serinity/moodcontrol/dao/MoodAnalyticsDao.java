package com.serinity.moodcontrol.dao;

import com.serinity.moodcontrol.dto.ImpactRow;
import com.serinity.moodcontrol.interfaces.IMoodAnalyticsDao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MoodAnalyticsDao implements IMoodAnalyticsDao {

    @Override
    public List<ImpactRow> findInfluenceImpact(String userId,
                                               Integer lastDays,
                                               String typeFilter,
                                               int lowMoodThreshold,
                                               int minSamples,
                                               int limit) throws SQLException {

        Objects.requireNonNull(userId, "userId");

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT i.name AS label, ")
                .append("AVG(me.mood_level) AS avg_mood, ")
                .append("SUM(CASE WHEN me.mood_level <= ? THEN 1 ELSE 0 END) AS low_mood_count, ")
                .append("COUNT(*) AS total_samples ")
                .append("FROM mood_entry_influence mei ")
                .append("JOIN influence i ON i.id = mei.influence_id ")
                .append("JOIN mood_entry me ON me.id = mei.mood_entry_id ")
                .append("WHERE me.user_id = ? ");

        List<Object> params = new ArrayList<Object>();
        params.add(Integer.valueOf(lowMoodThreshold));
        params.add(userId);

        appendFilters(sql, params, lastDays, typeFilter);

        sql.append(" GROUP BY i.name ")
                .append(" HAVING total_samples >= ? ")
                .append(" ORDER BY avg_mood ASC, total_samples DESC ")
                .append(" LIMIT ? ");

        params.add(Integer.valueOf(minSamples));
        params.add(Integer.valueOf(limit));

        return runImpactQuery(sql.toString(), params);
    }

    @Override
    public List<ImpactRow> findEmotionImpact(String userId,
                                             Integer lastDays,
                                             String typeFilter,
                                             int lowMoodThreshold,
                                             int minSamples,
                                             int limit) throws SQLException {

        Objects.requireNonNull(userId, "userId");

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT e.name AS label, ")
                .append("AVG(me.mood_level) AS avg_mood, ")
                .append("SUM(CASE WHEN me.mood_level <= ? THEN 1 ELSE 0 END) AS low_mood_count, ")
                .append("COUNT(*) AS total_samples ")
                .append("FROM mood_entry_emotion mee ")
                .append("JOIN emotion e ON e.id = mee.emotion_id ")
                .append("JOIN mood_entry me ON me.id = mee.mood_entry_id ")
                .append("WHERE me.user_id = ? ");

        List<Object> params = new ArrayList<Object>();
        params.add(Integer.valueOf(lowMoodThreshold));
        params.add(userId);

        appendFilters(sql, params, lastDays, typeFilter);

        sql.append(" GROUP BY e.name ")
                .append(" HAVING total_samples >= ? ")
                .append(" ORDER BY avg_mood ASC, total_samples DESC ")
                .append(" LIMIT ? ");

        params.add(Integer.valueOf(minSamples));
        params.add(Integer.valueOf(limit));

        return runImpactQuery(sql.toString(), params);
    }

    private void appendFilters(StringBuilder sql, List<Object> params, Integer lastDays, String typeFilter) {
        if (lastDays != null) {
            sql.append(" AND me.entry_date >= (NOW() - INTERVAL ? DAY) ");
            params.add(Integer.valueOf(lastDays));
        }

        if (typeFilter != null && !"ALL".equalsIgnoreCase(typeFilter)) {
            String dbType = typeFilter.trim().toUpperCase(Locale.ROOT);
            if ("MOMENT".equals(dbType) || "DAY".equals(dbType)) {
                sql.append(" AND me.moment_type = ? ");
                params.add(dbType);
            }
        }
    }

    private List<ImpactRow> runImpactQuery(String sql, List<Object> params) throws SQLException {
        List<ImpactRow> out = new ArrayList<ImpactRow>();

        Connection cn = DbConnection.getConnection();

        try (PreparedStatement ps = cn.prepareStatement(sql)) {

            for (int i = 0; i < params.size(); i++) {
                Object p = params.get(i);
                int idx = i + 1;

                if (p instanceof Integer) {
                    ps.setInt(idx, ((Integer) p).intValue());
                } else if (p instanceof Long) {
                    ps.setLong(idx, ((Long) p).longValue());
                } else {
                    ps.setString(idx, String.valueOf(p));
                }
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new ImpactRow(
                            rs.getString("label"),
                            rs.getDouble("avg_mood"),
                            rs.getInt("low_mood_count"),
                            rs.getInt("total_samples")
                    ));
                }
            }
        }

        return out;
    }
}
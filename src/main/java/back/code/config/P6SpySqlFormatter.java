package back.code.config;

import com.p6spy.engine.spy.appender.MessageFormattingStrategy;
import org.hibernate.engine.jdbc.internal.FormatStyle;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * P6Spy를 통해 출력되는 SQL 로그를 보기 좋은 형태로 포맷팅하는 클래스.
 *
 * <p>Hibernate의 {@link FormatStyle#BASIC} 포맷터를 적용하여
 * SQL 문장을 가독성 있게 정렬하며,
 * 실행 시간과 함께 로그로 출력한다.</p>
 *
 * <p>출력 예시:</p>
 * <pre>
 * 2025-10-29 23:42:31 | OperationTime : 5ms |
 * (P6Spy sql, Hibernate format):
 * select
 *     id,
 *     name
 * from
 *     user
 * where
 *     id = ?
 * </pre>
 *
 * <p>설정 방법 (application.yml):</p>
 * <pre>
 * p6spy:
 *   config:
 *     logMessageFormat: back.code.common.config.P6SpySqlFormatter
 * </pre>
 */
public class P6SpySqlFormatter implements MessageFormattingStrategy {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * P6Spy가 SQL 로그를 출력할 때 호출되는 포맷 메서드.
     *
     * @param connectionId 연결 ID
     * @param now          현재 시간
     * @param elapsed      SQL 실행 시간 (ms)
     * @param category     SQL 카테고리 (statement, result, commit 등)
     * @param prepared     PreparedStatement (바인딩 전 SQL)
     * @param sql          실제 실행된 SQL
     * @param url          데이터베이스 URL
     * @return 포맷팅된 로그 문자열
     */
    @Override
    public String formatMessage(
            int connectionId,
            String now,
            long elapsed,
            String category,
            String prepared,
            String sql,
            String url
    ) {
        if (sql == null || sql.trim().isEmpty()) {
            return "";
        }

        sql = formatSql(category, sql);

        String nowDate = LocalDateTime.now().format(DATE_FORMATTER);
        return nowDate + " | OperationTime : " + elapsed + "ms | " + sql;
    }

    /**
     * SQL 문자열을 카테고리 및 쿼리 타입에 따라 포맷팅한다.
     *
     * @param category SQL 카테고리 (statement, commit 등)
     * @param sql      SQL 문자열
     * @return 포맷팅된 SQL 문자열
     */
    private String formatSql(String category, String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return "";
        }

        // CREATE TABLE 등의 DDL 문은 포맷팅하지 않고 그대로 출력
        if (category.contains("statement") && sql.trim().toLowerCase(Locale.ROOT).startsWith("create")) {
            return "\n(P6Spy DDL statement):\n" + sql;
        }

        // Hibernate SQL 포맷 적용
        if (category.equals("statement")) {
            String trimmedSql = sql.trim().toLowerCase(Locale.ROOT);

            if (trimmedSql.startsWith("select")
                    || trimmedSql.startsWith("insert")
                    || trimmedSql.startsWith("update")
                    || trimmedSql.startsWith("delete")) {
                sql = FormatStyle.BASIC.getFormatter().format(sql);
                return "\n(P6Spy sql, Hibernate format):\n" + sql;
            }
        }

        // 나머지 쿼리는 기본 포맷으로 출력
        return "\n(P6Spy sql):\n" + sql;
    }
}

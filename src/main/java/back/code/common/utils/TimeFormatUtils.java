package back.code.common.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 시간 관련 형식(format) 변환 유틸리티 클래스.
 *
 * <p>주요 기능:</p>
 * <ul>
 *     <li>현재 시간을 지정된 문자열 형식으로 반환</li>
 *     <li>{@link LocalDateTime} 객체를 문자열로 변환</li>
 *     <li>사용자 정의 패턴 지정 가능 (없으면 기본 패턴 사용)</li>
 * </ul>
 *
 * <p>기본 포맷: {@code yyyy-MM-dd HH:mm:ss}</p>
 *
 * <p>예시:</p>
 * <pre>
 * String now = TimeFormatUtils.getNowTime();
 * String custom = TimeFormatUtils.getTime(LocalDateTime.now(), "yyyy/MM/dd HH:mm");
 * </pre>
 */
public class TimeFormatUtils {

    /**
     * 생성자 private 선언으로 인스턴스화 방지.
     * (static 메서드만 사용하는 순수 유틸리티 클래스)
     */
    private TimeFormatUtils() {
        // 인스턴스화 방지
    }

    /** 기본 날짜 및 시간 포맷 */
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 현재 시간을 기본 포맷("yyyy-MM-dd HH:mm:ss")으로 반환.
     *
     * @return 현재 시간을 문자열로 변환한 값
     */
    public static String getNowTime() {
        // 현재 시각(LocalDateTime.now())을 기본 포맷으로 변환
        return getTime(LocalDateTime.now());
    }

    /**
     * 지정된 {@link LocalDateTime}을 주어진 패턴으로 문자열 변환.
     * <p>패턴이 null 또는 공백이면 기본 포맷을 사용함.</p>
     *
     * @param localDateTime 변환할 시간
     * @param pattern 포맷 패턴 (예: "yyyy/MM/dd HH:mm")
     * @return 변환된 시간 문자열
     */
    public static String getTime(LocalDateTime localDateTime, String pattern) {
        String usePattern = (pattern == null || pattern.isBlank()) ? DATE_FORMAT : pattern;
        return localDateTime.format(DateTimeFormatter.ofPattern(usePattern));
    }

    /**
     * 지정된 {@link LocalDateTime}을 기본 포맷("yyyy-MM-dd HH:mm:ss")으로 문자열 변환.
     *
     * @param localDateTime 변환할 시간
     * @return 변환된 시간 문자열
     */
    public static String getTime(LocalDateTime localDateTime) {
        return getTime(localDateTime, DATE_FORMAT);
    }
}

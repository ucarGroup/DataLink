package com.ucar.datalink.domain.media;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.ucar.datalink.common.errors.DatalinkException;
import com.ucar.datalink.common.errors.ValidationException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.oro.text.regex.*;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by lubiao on 2017/3/6.
 * 参考自Alibaba-Otter.
 */
public class ModeUtils {
    private static final String MODE_PATTERN = "(.*)(\\[(\\d+)\\-(\\d+)\\])(.*)"; // 匹配类似"offer[0000-0031]"的分库分表模式
    private static final String YEAR_SUFFIX = "${yyyy}";
    private static final String MONTH_SUFFIX = "${yyyyMM}";

    private static LoadingCache<String, Pattern> patterns = CacheBuilder.newBuilder().build(new CacheLoader<String, Pattern>() {
        @Override
        public Pattern load(String key) throws Exception {
            PatternCompiler pc = new Perl5Compiler();
            try {
                return pc.compile(key,
                        Perl5Compiler.CASE_INSENSITIVE_MASK
                                | Perl5Compiler.READ_ONLY_MASK);
            } catch (MalformedPatternException e) {
                throw new DatalinkException("invalid config", e);
            }
        }
    });


    /**
     * 解析MediaInfo中的namespace和name，支持类似"offer[0000-0031]"分库分表的定义
     */
    public static MediaInfo.ModeValue parseMode(String value) {
        try {
            PatternMatcher matcher = new Perl5Matcher();
            if (matcher.matches(value, patterns.get(MODE_PATTERN))) {
                MatchResult matchResult = matcher.getMatch();
                String prefix = matchResult.group(1);
                String startStr = matchResult.group(3);
                String ednStr = matchResult.group(4);
                int start = Integer.valueOf(startStr);
                int end = Integer.valueOf(ednStr);
                String postfix = matchResult.group(5);

                List<String> values = new ArrayList<>();
                for (int i = start; i <= end; i++) {
                    StringBuilder builder = new StringBuilder(value.length());
                    String str = String.valueOf(i);
                    // 处理0001类型
                    if (startStr.length() == ednStr.length() && startStr.startsWith("0")) {
                        str = StringUtils.leftPad(String.valueOf(i), startStr.length(), '0');
                    }

                    builder.append(prefix).append(str).append(postfix);
                    values.add(builder.toString());
                }
                return new MediaInfo.ModeValue(MediaInfo.Mode.MULTI, values);
            } else if (StringUtils.endsWith(value, YEAR_SUFFIX)) {
                return new MediaInfo.ModeValue(MediaInfo.Mode.YEARLY, Arrays.asList(value));
            } else if (StringUtils.endsWith(value, MONTH_SUFFIX)) {
                return new MediaInfo.ModeValue(MediaInfo.Mode.MONTHLY, Arrays.asList(value));
            } else if (isWildCard(value)) {// 通配符支持
                return new MediaInfo.ModeValue(MediaInfo.Mode.WILDCARD, Arrays.asList(value));
            } else {
                return new MediaInfo.ModeValue(MediaInfo.Mode.SINGLE, Arrays.asList(value));
            }
        } catch (Exception e) {
            throw new DatalinkException("something goes wrong when parsing mode.", e);
        }
    }

    public static boolean isWildCard(String value) {
        return StringUtils.containsAny(value, new char[]{'*', '?', '+', '|', '(', ')', '{', '}', '[', ']', '\\', '$',
                '^', '.'});
    }

    public static boolean isWildCardMatch(String matchPattern, String value) {
        PatternMatcher matcher = new Perl5Matcher();
        return matcher.matches(value, patterns.getUnchecked(matchPattern));
    }

    public static int indexIgnoreCase(List<String> datas, String value) {
        for (int i = 0; i < datas.size(); i++) {
            String data = datas.get(i);
            if (data.equalsIgnoreCase(value)) {
                return i;
            }

        }

        return -1;
    }

    public static String tryBuildYearlyPattern(String value) {
        String valueSuffix = StringUtils.substring(value, value.length() - 4);
        try {
            DateUtils.parseDate(valueSuffix, new String[]{"yyyy"});
            return StringUtils.substring(value, 0, value.length() - 4) + YEAR_SUFFIX;
        } catch (ParseException e) {
        }
        return value;
    }

    public static String tryBuildMonthlyPattern(String value) {
        try {
            String valueSuffix = StringUtils.substring(value, value.length() - 6);
            DateUtils.parseDate(valueSuffix, new String[]{"yyyyMM"});
            return StringUtils.substring(value, 0, value.length() - 6) + MONTH_SUFFIX;
        } catch (ParseException e) {
        }

        try {
            String valueSuffix = StringUtils.substring(value, value.length() - 7);
            DateUtils.parseDate(valueSuffix, new String[]{"yyyy-MM", "yyyy/MM", "yyyy.MM"});
            return StringUtils.substring(value, 0, value.length() - 7) + MONTH_SUFFIX;
        } catch (ParseException e) {
        }

        return value;
    }

    public static boolean isYearlyMatch(String pattern, String value) {
        if (!StringUtils.endsWith(pattern, YEAR_SUFFIX)) {
            throw new ValidationException("the pattern must end with " + YEAR_SUFFIX);
        }

        String valueSuffix = StringUtils.substring(value, value.length() - 4);
        String valuePrefix = StringUtils.substring(value, 0, value.length() - 4);

        if (StringUtils.substringBeforeLast(pattern, YEAR_SUFFIX).equals(valuePrefix)) {
            try {
                DateUtils.parseDate(valueSuffix, new String[]{"yyyy"});
                return true;
            } catch (ParseException e) {
            }
        }
        return false;
    }

    public static boolean isMonthlyMatch(String pattern, String value) {
        if (!StringUtils.endsWith(pattern, MONTH_SUFFIX)) {
            throw new ValidationException("the pattern must end with " + MONTH_SUFFIX);
        }
        String patternPrefix = StringUtils.substringBeforeLast(pattern, MONTH_SUFFIX);

        String valueSuffix = StringUtils.substring(value, value.length() - 6);
        String valuePrefix = StringUtils.substring(value, 0, value.length() - 6);
        if (patternPrefix.equals(valuePrefix)) {
            try {
                DateUtils.parseDate(valueSuffix, new String[]{"yyyyMM"});
                return true;
            } catch (ParseException e) {
            }
        }

        valueSuffix = StringUtils.substring(value, value.length() - 7);
        valuePrefix = StringUtils.substring(value, 0, value.length() - 7);
        if (patternPrefix.equals(valuePrefix)) {
            try {
                DateUtils.parseDate(valueSuffix, new String[]{"yyyy-MM", "yyyy/MM", "yyyy.MM"});
                return true;
            } catch (ParseException e) {
            }
        }

        return false;
    }

    public static String getYearlyPrefix(String value) {
        return StringUtils.substringBeforeLast(value, YEAR_SUFFIX);
    }

    public static String getMonthlyPrefix(String value) {
        return StringUtils.substringBeforeLast(value, MONTH_SUFFIX);
    }

    public static boolean isYearlyPattern(String value) {
        return StringUtils.endsWith(value, YEAR_SUFFIX);
    }

    public static boolean isMonthlyPattern(String value) {
        return StringUtils.endsWith(value, MONTH_SUFFIX);
    }
}

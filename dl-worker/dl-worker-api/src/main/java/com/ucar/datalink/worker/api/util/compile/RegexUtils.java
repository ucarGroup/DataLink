package com.ucar.datalink.worker.api.util.compile;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.commons.lang.StringUtils;
import org.apache.oro.text.regex.*;

import java.util.concurrent.ExecutionException;

/**
 * @author lubiao
 */
public class RegexUtils {

    private static LoadingCache<String, Pattern> patterns = null;

    static {
        patterns = CacheBuilder.newBuilder().build(new CacheLoader<String, Pattern>() {

            @Override
            public Pattern load(String key) throws Exception {
                try {
                    PatternCompiler pc = new Perl5Compiler();
                    return pc.compile(key, Perl5Compiler.CASE_INSENSITIVE_MASK | Perl5Compiler.READ_ONLY_MASK);
                } catch (MalformedPatternException e) {
                    throw new RuntimeException("Regex failed!", e);
                }
            }

        });
    }

    public static String findFirst(String originalStr, String regex) {
        if (StringUtils.isBlank(originalStr) || StringUtils.isBlank(regex)) {
            return StringUtils.EMPTY;
        }

        PatternMatcher matcher = new Perl5Matcher();
        try {
            if (matcher.contains(originalStr, patterns.get(regex))) {
                return StringUtils.trimToEmpty(matcher.getMatch().group(0));
            }
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        return StringUtils.EMPTY;
    }
}

package com.ucar.datalink.biz.utils.flinker;


import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 时间表达式引擎
 *
 * @author hanzhiwei
 * @version 1.0
 * @title DateExpressionEngine
 * @description 时间表达式引擎
 * @date 2016年10月31日
 */
public class DateExpressionEngine {

    /**
     * <pre>
     * @param needProcessString 需要处理的字符串
     *  里面可以含有任意多个时间表达式
     *  格式：dateFormat[,offset]}
     *  dateFormat: 符合java的java.text.DateFormat规范的表达式
     *  DateUnit 时间格式单位:[注意大小写]
     *      y   :   year
     *      M   :   Month
     *      d   :   day
     *      w   :   week
     *      H   :   Hour
     *      m   :   minute
     *
     *  [重点]这里的格式有两种，一种是一般日期表达式，一种是特殊表达式
     *
     *  一、一般日期表达式：
     *  offset的表达式：(-)?number+DateUnit
     *  offset的正则表达式： ^((?:-)?\\d+?)([y,M,w,d,H,m])$
     *
     *   example:
     *      ${MM} ${MMdd} ${MM/dd} ${HH} ${HHmm}
     *      ${yyyyMMdd} ${yyyy-MM-dd} ${yyyy/MM/dd}
     *      ${yyyyMMddHH} ${yyyy-MM-dd HH} ${yyyy/MM/dd HH}
     *      ${yyyyMMddHHmm} ${yyyy-MM-dd HH:mm} ${yyyy/MM/dd HH:mm}
     *      ${yyyyMMdd,-1y} ${yyyy-MM-dd,-1y} ${yyyy/MM/dd,-1y}
     *      ${yyyyMMdd,-1M} ${yyyy-MM-dd,-1M} ${yyyy/MM/dd,-1M}
     *      ${yyyyMMdd,1d} ${yyyy-MM-dd,1d} ${yyyy/MM/dd,1d}
     *      ${yyyyMMddHH,1H} ${yyyy-MM-dd HH,1H} ${yyyy/MM/dd HH,1H}
     *      ${yyyyMMdd,1w} ${yyyy-MM-dd,1w} ${yyyy/MM/dd,1w}
     *      ${yyyyMMddHHmm,10m} ${yyyy-MM-dd HH:mm,10m} ${yyyy/MM/dd HH:mm,10m}
     *
     * 二、特殊表达式
     *  用来计算：季度初/末，月初/末，周初/末（也就是周一和周日）
     *  offset的表达式：position+DateUnit
     *  offset的正则表达式：^([F,f,E,e])([M,w,W,q,Q])$
     *  ------------------
     *  F,f means: first
     *  E,e means: end
     *  ------------------
     *  M : Month
     *  w,W : Week
     *  q,Q : Quarter
     *
     * @param dateValue 时间的字符串，格式要求 yyyyMMdd,yyyyMMddHH,yyyyMMddHHmm
     * @return 经过计算之后的字符串
     * </pre>
     */
    public static String formatDateExpression(String needProcessString, String dateValue) {
        Preconditions.checkArgument(StringUtils.isNotBlank(needProcessString));
        Preconditions.checkArgument(StringUtils.isNotBlank(dateValue));
        Preconditions.checkArgument(DATE_PATTERN.matcher(dateValue).matches(), "dateValue is unexpect format:%s. "
                        + "Required min length is 8, like : 20150101. %s's length is : %s", dateValue, dateValue,
                dateValue.length());

        Matcher m = PATTERN.matcher(needProcessString);
        /**
         * 如果找到时间表达式则进行替换，如果找不到，则不进行处理
         */
        while (m.find()) {
            String expression = m.group(0);
            String varPart = m.group(1);
            // replaceMent : 20150101 201501012359
            // 得到正确的时间字符串
            String replaceMent = getCorrectDateString(varPart, dateValue);
            // 替换表达式为正确的时间字符串
            needProcessString = StringUtils.replace(needProcessString, expression, replaceMent);
            // find next
            m = PATTERN.matcher(needProcessString);
        }
        return needProcessString;
    }

    private static String getCorrectDateString(String express, String dataVersionNo) {
        // 将表达式切分开
        List<String> lst = Splitter.on(',').trimResults().omitEmptyStrings().splitToList(express);
        int size = lst.size();
        Preconditions.checkArgument(size <= 2, "unexpected expression format:%s", express);
        // 得到世界表达式的格式化部分
        String format = lst.get(0);
        // 将版本号的字符串转换为 DateTime 对象
        DateTime dateTime = parseForDataVersionNo(dataVersionNo);
        DateTime rs = dateTime;
        // 如果存在复杂的计算表达式
        if (lst.size() == 2) {
            String offsetExpression = lst.get(1);
            // 处理季度、月、周的第一天和最后一天
            Matcher sm = OFFSET_SPECIAL_PATTERN.matcher(offsetExpression);
            if (sm.matches()) {
                String str1 = sm.group(1);
                Preconditions.checkArgument(StringUtils.isNotBlank(str1), "unexpected expression format:%s", express);
                String unit = sm.group(2);
                if (QUARTR_STRING.equalsIgnoreCase(unit)) {
                    DateTime startQuarter = dateTime.plusMonths(0 - (dateTime.monthOfYear().get() - 1) % 3)
                            .dayOfMonth().withMinimumValue();
                    // 季度初
                    if (FIRST_STRING.equalsIgnoreCase(str1)) {
                        rs = startQuarter;
                    } else if (END_STRING.equalsIgnoreCase(str1)) {
                        rs = startQuarter.plusMonths(3).plusDays(-1);
                        // 季度末
                    } else {
                        throw new IllegalArgumentException(String.format("unexpected expression format:%s", express));
                    }
                } else if (MONTH_STRING.equals(unit)) {
                    if (FIRST_STRING.equalsIgnoreCase(str1)) {
                        rs = dateTime.dayOfMonth().withMinimumValue();
                    } else if (END_STRING.equalsIgnoreCase(str1)) {
                        rs = dateTime.dayOfMonth().withMaximumValue();
                    } else {
                        throw new IllegalArgumentException(String.format("unexpected expression format:%s", express));
                    }
                } else if (WEEK_STRING.equalsIgnoreCase(unit)) {
                    if (FIRST_STRING.equalsIgnoreCase(str1)) {
                        rs = dateTime.dayOfWeek().withMinimumValue();
                    } else if (END_STRING.equalsIgnoreCase(str1)) {
                        rs = dateTime.dayOfWeek().withMaximumValue();
                    } else {
                        throw new IllegalArgumentException(String.format("unexpected expression format:%s", express));
                    }
                }
                return rs.toString(format);
            }
            // 处理一般的时间表达式
            Matcher m = OFFSET_PATTERN.matcher(offsetExpression);
            Preconditions.checkArgument(m.matches(), "unexpected expression format:%s", express);
            String numString = m.group(1);
            if (StringUtils.isBlank(numString)) {
                numString = "0";
            }
            int num = Integer.valueOf(numString).intValue();
            String unit = m.group(2);
            if (YEAR_STRING.equalsIgnoreCase(unit)) {
                // IgnoreCase
                rs = dateTime.plusYears(num);
            } else if (MONTH_STRING.equals(unit)) {
                rs = dateTime.plusMonths(num);
            } else if (WEEK_STRING.equalsIgnoreCase(unit)) {
                // IgnoreCase
                rs = dateTime.plusWeeks(num);
            } else if (DAY_STRING.equalsIgnoreCase(unit)) {
                // IgnoreCase
                rs = dateTime.plusDays(num);
            } else if (HOUR_STRING.equalsIgnoreCase(unit)) {
                // IgnoreCase
                rs = dateTime.plusHours(num);
            } else if (MINUTE_STRING.equals(unit)) {
                rs = dateTime.plusMinutes(num);
            } else {
                throw new IllegalArgumentException(String.format("unexpected expression format:%s", express));
            }
        }
        return rs.toString(format);
    }

    /**
     * <pre>
     * ======================================== watcherStart ========================================
     * <!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
     * <html>
     * <head>
     * <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
     * <title>Insert title here</title>
     * </head>
     * <body>
     * <div>
     *
     * 执行命令里面可以含有任意多个时间表达式。<br>
     * 格式：${formatExpression[,offsetExpression]}
     * 其中[]方括号表示“可选”。formatExpression是符合Java的DateFormat的任意格式表达式
     * <span style="color:red;font-weight:bold;">[注意]区分大小写</span>
     * <hr>
     * 表达式中的各个字符解释如下：
     * <table border=1>
     * <thead>
     *     <td>字符</td>
     *     <td>时间单位</td>
     *     <td>原文</td>
     *     <td>出现位置</td>
     * </thead>
     * <tr>
     * <td>y</td>
     * <td>年</td>
     * <td>year</td>
     * <td>formatExpression,offsetExpression</td>
     * </tr>
     * <tr>
     * <td>M</td>
     * <td>月</td>
     * <td>Month</td>
     * <td>formatExpression,offsetExpression</td>
     * </tr>
     * <tr>
     * <td>d</td>
     * <td>天</td>
     * <td>day</td>
     * <td>formatExpression,offsetExpression</td>
     * </tr>
     * <tr>
     * <td>H</td>
     * <td>小时</td>
     * <td>Hour</td>
     * <td>formatExpression,offsetExpression</td>
     * </tr>
     * <tr>
     * <td>m</td>
     * <td>分钟</td>
     * <td>minute</td>
     * <td>formatExpression,offsetExpression</td>
     * </tr>
     * <tr>
     * <td>W</td>
     * <td>周</td>
     * <td>Week</td>
     * <td>offsetExpression</td>
     * </tr>
     * <tr>
     * <td>Q</td>
     * <td>季度</td>
     * <td>Quarter</td>
     * <td>offsetExpression</td>
     * </tr>
     * <tr>
     * <td>F</td>
     * <td>起始</td>
     * <td>First</td>
     * <td>offsetExpression</td>
     * </tr>
     * <tr>
     * <td>E</td>
     * <td>结束</td>
     * <td>End</td>
     * <td>offsetExpression</td>
     * </tr>
     * </table>
     * <hr>
     * 举例如下：
     * <table border=1>
     * <tr>
     *     <td>描述</td>
     *     <td>表达式</td>
     *     <td>结果：当前时间是2015-08-10 14:40</td>
     * </tr>
     * 2015-08-10 14:40
     *
     * <tr>
     * <td>天</td>
     * <td>${yyyyMMdd}<br>${yyyy-MM-dd}<br>${yyyy/MM/dd}<br>${dd}</td>
     * <td>20150810<br>2015-08-10<br>2015/08/10<br>10</td>
     * </tr>
     * <tr>
     * <td>小时</td>
     * <td>${yyyyMMddHH}<br>${yyyy-MM-dd HH}<br>${yyyy/MM/dd HH}<br>${HH}</td>
     * <td>2015081014<br>2015-08-10 14<br>2015/08/10 14<br>14</td>
     * </tr>
     * <tr>
     * <td>分钟</td>
     * <td>${yyyyMMddHHmm}<br>${yyyy-MM-dd HH:mm}<br>${yyyy/MM/dd HH:mm}<br>${HHmm}</td>
     * <td>201508101440<br>2015-08-10 14:40<br>2015/08/10 14:40<br>1440</td>
     * </tr>
     * <tr>
     * <td>1年前</td>
     * <td>${yyyyMMdd,-1y}<br>${yyyy-MM-dd,-1y}<br>${yyyy/MM/dd,-1y}</td>
     * <td>20140810<br>2014-08-10<br>2014/08/10</td>
     * </tr>
     * <tr>
     * <td>1个月前</td>
     * <td>${yyyyMMdd,-1M}<br>${yyyy-MM-dd,-1M}<br>${yyyy/MM/dd,-1M}</td>
     * <td>20150710<br>2015-07-10<br>2015/07/10</td>
     * </tr>
     * <tr>
     * <td>1周前</td>
     * <td>${yyyyMMdd,-1W}<br>${yyyy-MM-dd,-1W}<br>${yyyy/MM/dd,-1W}</td>
     * <td>20150803<br>2015-08-03<br>2015/08/03</td>
     * </tr>
     * <tr>
     * <td>1天前</td>
     * <td>${yyyyMMdd,-1d}<br>${yyyy-MM-dd,-1d}<br>${yyyy/MM/dd,-1d}</td>
     * <td>20150809<br>2015-08-09<br>2015/08/09</td>
     * </tr>
     * <tr>
     * <td>1小时前</td>
     * <td>${yyyyMMddHH,-1H}<br>${yyyy-MM-dd HH,-1H}<br>${yyyy/MM/dd HH,-1H}<br>${HH,-1H}</td>
     * <td>2015081015<br>2015-08-10 15<br>2015/08/10 13<br>13</td>
     * </tr>
     * <tr>
     * <td>10分钟前</td>
     * <td>${yyyyMMddHHmm,-10m}<br>${yyyy-MM-dd HH:mm,-10m}<br>${yyyy/MM/dd HH:mm,-10m}<br>${HH:mm,-10m}</td>
     * <td>201508101430<br>2015-08-10 14:30<br>2015/08/10 14:30<br>14:30</td>
     * </tr>
     * <tr>
     * <td>季度第一天</td>
     * <td>${yyyyMMdd,FQ}<br>${yyyy-MM-dd,FQ}<br>${yyyy/MM/dd,FQ}</td>
     * <td>20150701<br>2015-07-01<br>2015/07/01</td>
     * </tr>
     * <tr>
     * <td>季度最后一天</td>
     * <td>${yyyyMMdd,EQ}<br>${yyyy-MM-dd,EQ}<br>${yyyy/MM/dd,EQ}</td>
     * <td>20150930<br>2015-09-30<br>2015/09/30</td>
     * </tr>
     * <tr>
     * <td>月第一天</td>
     * <td>${yyyyMMdd,FM}<br>${yyyy-MM-dd,FM}<br>${yyyy/MM/dd,FM}</td>
     * <td>20150801<br>2015-08-01<br>2015/08/01</td>
     * </tr>
     * <tr>
     * <td>月最后一天</td>
     * <td>${yyyyMMdd,EM}<br>${yyyy-MM-dd,EM}<br>${yyyy/MM/dd,EM}</td>
     * <td>20150831<br>2015-08-31<br>2015/08/31</td>
     * </tr>
     * <tr>
     * <td>周一</td>
     * <td>${yyyyMMdd,FW}<br>${yyyy-MM-dd,FW}<br>${yyyy/MM/dd,FW}</td>
     * <td>20150810<br>2015-08-10<br>2015/08/10</td>
     * </tr>
     * <tr>
     * <td>周日</td>
     * <td>${yyyyMMdd,EW}<br>${yyyy-MM-dd,EW}<br>${yyyy/MM/dd,EW}</td>
     * <td>20150816<br>2015-08-16<br>2015/08/16</td>
     * </tr>
     *
     *
     * </table>
     * </div>
     * </body>
     * </html>
     * ======================================== end ========================================
     * </pre>
     */
    public static void main2(String[] args) {
        /**
         * <pre>
         * 用于生成说明文档
         * </pre>
         */
        Map<String, String> map = Maps.newLinkedHashMap();
        map.put("天", "${yyyyMMdd}<br>${yyyy-MM-dd}<br>${yyyy/MM/dd}<br>${dd}");
        map.put("小时", "${yyyyMMddHH}<br>${yyyy-MM-dd HH}<br>${yyyy/MM/dd HH}<br>${HH}");
        map.put("分钟", "${yyyyMMddHHmm}<br>${yyyy-MM-dd HH:mm}<br>${yyyy/MM/dd HH:mm}<br>${HHmm}");
        map.put("1年前", "${yyyyMMdd,-1y}<br>${yyyy-MM-dd,-1y}<br>${yyyy/MM/dd,-1y}");
        map.put("1个月前", "${yyyyMMdd,-1M}<br>${yyyy-MM-dd,-1M}<br>${yyyy/MM/dd,-1M}");
        map.put("1周前", "${yyyyMMdd,-1W}<br>${yyyy-MM-dd,-1W}<br>${yyyy/MM/dd,-1W}");
        map.put("1天前", "${yyyyMMdd,-1d}<br>${yyyy-MM-dd,-1d}<br>${yyyy/MM/dd,-1d}");
        map.put("1小时前", "${yyyyMMddHH,1H}<br>${yyyy-MM-dd HH,1H}<br>${yyyy/MM/dd HH,-1H}<br>${HH,-1H}");
        map.put("10分钟前",
                "${yyyyMMddHHmm,-10m}<br>${yyyy-MM-dd HH:mm,-10m}<br>${yyyy/MM/dd HH:mm,-10m}<br>${HH:mm,-10m}");
        map.put("季度第一天", "${yyyyMMdd,FQ}<br>${yyyy-MM-dd,FQ}<br>${yyyy/MM/dd,FQ}");
        map.put("季度最后一天", "${yyyyMMdd,EQ}<br>${yyyy-MM-dd,EQ}<br>${yyyy/MM/dd,EQ}");
        map.put("月第一天", "${yyyyMMdd,FM}<br>${yyyy-MM-dd,FM}<br>${yyyy/MM/dd,FM}");
        map.put("月最后一天", "${yyyyMMdd,EM}<br>${yyyy-MM-dd,EM}<br>${yyyy/MM/dd,EM}");
        map.put("周一", "${yyyyMMdd,FW}<br>${yyyy-MM-dd,FW}<br>${yyyy/MM/dd,FW}");
        map.put("周日", "${yyyyMMdd,EW}<br>${yyyy-MM-dd,EW}<br>${yyyy/MM/dd,EW}");
        String dateTime = "2015-08-10 14:40";
        System.out.println(dateTime);
        dateTime = dateTime.replace("-", "").replace(" ", "").replace(":", "");

        for (Entry<String, String> entry : map.entrySet()) {
            String detail = entry.getKey();
            String str = entry.getValue();

            System.out.println("<tr>");
            String rs = DateExpressionEngine.formatDateExpression(str, dateTime);
            System.out.println("<td>" + detail + "</td>");
            System.out.println("<td>" + str + "</td>");
            System.out.println("<td>" + rs + "</td>");
            System.out.println("</tr>");
        }

        String x = DateExpressionEngine.formatDateExpression("${time:yyyy-MM-dd-HH-mm,-10m}", dateTime);
        System.out.println(x);

        System.out.println("===========================");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
        String dateFormat = sdf.format(new Date());
        String str = "${time:yyyy-MM-dd-HH-mm,-1y}";
        long time = System.currentTimeMillis();
        String result = formatDateExpression(str,dateFormat);

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date d =  sdf.parse(result);
            String d2 = format.format(d);
            System.out.println("d2->"+d2);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        System.out.println("xxxx->"+result);

        System.out.println("==========================================");
        String s2 = formatDateExpression(str,dateFormat);


    }


    public static void main(String[] args) {
        String str = "20190301";
        String time = "${time:yyyy-MM-dd,-1d}";
        String x = formatDateExpression(time,str);
        System.out.println(x);
    }




    public static void go(String express) {
        if( StringUtils.isBlank(express) ){

        }
        express.contains(",");
        //java.time.LocalDate.
    }



    /**
     * <pre>
     * 将版本号的字符串转换为 DateTime 对象
     * @param dataVersionNo 20150101 2015010123 201501012359
     * @return 被解析之后的DateTime
     * </pre>
     */
    private static DateTime parseForDataVersionNo(String dataVersionNo) {
        int size = dataVersionNo.length();
        String tempFormat = CONST_DATE_STRING;
        if (size == CONST_DATE_STRING.length()) {
            tempFormat = CONST_DATE_STRING;
        } else if (size == CONST_HOUR_STRING.length()) {
            tempFormat = CONST_HOUR_STRING;
        } else if (size == CONST_MINUTE_STRING.length()) {
            tempFormat = CONST_MINUTE_STRING;
        } else {
            throw new IllegalArgumentException(String.format("wrong dataVersionNo:%s", dataVersionNo));
        }
        DateTimeFormatter formatter = DateTimeFormat.forPattern(tempFormat);
        DateTime dateTime = DateTime.parse(dataVersionNo, formatter);
        return dateTime;
    }

    /**
     * [^s]刨除掉sandkey的沙箱配置
     */
    private static final String REGEX = "\\$\\{time:(.*?)\\}";
    private static final Pattern PATTERN = Pattern.compile(REGEX);
    private static final Pattern DATE_PATTERN = Pattern.compile("^\\d{8,}$");
    private static final String FIRST_STRING = "F";
    private static final String END_STRING = "E";
    private static final String YEAR_STRING = "y";
    private static final String QUARTR_STRING = "Q";
    private static final String MONTH_STRING = "M";
    private static final String WEEK_STRING = "w";
    private static final String DAY_STRING = "d";
    private static final String HOUR_STRING = "H";
    private static final String MINUTE_STRING = "m";
    private static final String OFFSET_REGEX = "^(-?\\d+?)?([y,Y,M,w,W,d,D,H,h,m])$";
    private static final Pattern OFFSET_PATTERN = Pattern.compile(OFFSET_REGEX);
    private static final String OFFSET_SPECIAL_REGEX = "^([F,f,E,e])([M,w,W,q,Q])$";
    private static final Pattern OFFSET_SPECIAL_PATTERN = Pattern.compile(OFFSET_SPECIAL_REGEX);
    private static final String CONST_DATE_STRING = "yyyyMMdd";
    private static final String CONST_HOUR_STRING = "yyyyMMddHH";
    private static final String CONST_MINUTE_STRING = "yyyyMMddHHmm";

}

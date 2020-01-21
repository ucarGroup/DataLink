package com.ucar.datalink.flinker.test;

public class S {

    public static void main(String[] args) throws Exception {
        //new S().go();
        jump(new int[]{2,3,1,1,4});
    }

    public static int jump(int[] nums) {
        int end = 0;
        int maxPosition = 0;
        int steps = 0;
        for (int i = 0; i < nums.length - 1; i++) {
            //找能跳的最远的
            maxPosition = Math.max(maxPosition, nums[i] + i);
            if (i == end) { //遇到边界，就更新边界，并且步数加一
                end = maxPosition;
                steps++;
            }
        }
        return steps;
    }

    public void go()throws Exception {
        //String tmp_json = JsonConfig.json;
        String tmp_json = Json.file();
        if(tmp_json.contains("\\\\$DATAX_PRE_DATE")) {
            tmp_json = tmp_json.replace("\\\\$DATAX_PRE_DATE", "$DATAX_PRE_DATE");
        }
        if(tmp_json.contains("\\$DATAX_PRE_DATE")) {
            tmp_json = tmp_json.replace("\\$DATAX_PRE_DATE", "$DATAX_PRE_DATE");
        }

        //当前时间的标识
        if(tmp_json.contains("\\$DATAX_CURRENT_TIME")) {
            tmp_json = tmp_json.replace("\\\\$DATAX_CURRENT_TIME", "$DATAX_CURRENT_DATE");
        }

        //上一次执行成功的时间
        if(tmp_json.contains("\\$DATAX_LAST_EXECUTE_TIME")) {
            tmp_json = tmp_json.replace("\\\\$DATAX_LAST_EXECUTE_TIME", "$DATAX_LAST_EXECUTE_TIME");
        }
    }
}

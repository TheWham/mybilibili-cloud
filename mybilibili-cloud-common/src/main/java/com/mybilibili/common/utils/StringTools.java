package com.mybilibili.common.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;

public class StringTools {
    public static String generateRandomNumber(int count) {
        return RandomStringUtils.random(count, false, true);
    }

    public static boolean isEmpty(String value) {
        if (null == value || "".equals(value) || "null".equals(value) || "\u0000".equals(value))
            return true;
        else
            return "".equals(value.trim());
    }

    public static String md5Password(String password) {
        return isEmpty(password) ? null : DigestUtils.md5Hex(password);
    }

    public static String generateRandomStr(int length5) {
        return RandomStringUtils.random(length5, true, true);
    }

    public static boolean pathIsOk(String path){
        if (StringTools.isEmpty(path))
            return false;
        if (path.contains("../") || path.contains("..\\"))
            return false;
        return true;

    }

    public static String getFileSuffix(String fileName)
    {
        if (StringTools.isEmpty(fileName) || !fileName.contains("."))
            return null;

        String suffix = fileName.substring(fileName.lastIndexOf("."));
        return suffix;
    }

    public static String upperCaseFirstLetter(String str) {
        // 判空保护，许皇您在工作中一定要养成这个习惯
        if (str == null || str.isEmpty()) {
            return str;
        }

        // 如果首字母已经是大写，直接返回原字符串，减少不必要的内存分配
        if (Character.isUpperCase(str.charAt(0))) {
            return str;
        }

        return new StringBuilder(str.length())
                .append(Character.toUpperCase(str.charAt(0)))
                .append(str.substring(1))
                .toString();
    }

}
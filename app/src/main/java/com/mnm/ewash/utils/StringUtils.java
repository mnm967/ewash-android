package com.mnm.ewash.utils;

import java.util.regex.Pattern;

public class StringUtils {
    public static boolean isValidPhoneNumber(String phone){
        boolean check;
        if(!Pattern.matches("[a-zA-Z]+", phone)){
            if(phone.length() < 6 || phone.length() > 14){
                check = false;
            }else{
                check = true;
            }
        }else{
            check = false;
        }
        return check;
    }
    public static boolean isValidEmailAddress(String email){
        boolean check = false;
        if(email.contains("@")&&email.contains(".")){
            check = true;
        }
        return check;
    }
}

package com.ilife.iliferobot;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class JavaTest {

    public static void main(String[] args) throws IOException {
        String result = "Run Main";
        File file1 = new File("C:\\Users\\Administrator\\Desktop\\ilife important file\\international\\source1.txt");
        File file2 = new File("C:\\Users\\Administrator\\Desktop\\ilife important file\\international\\source2.txt");
        List<Bean> beans1= readText(file1,1);
        List<Bean> beans2=readText(file2,2);
        for (Bean bean:beans1) {
            for (Bean be:beans2) {
                if (bean.getChinese().equals(be.getChinese())){
                    bean.setEnglish(be.getEnglish());
                }
            }
        System.out.println(bean.toString());
        }
    }

    private static List<Bean> readText(File file, int type) throws IOException {
        FileInputStream fileReader=new FileInputStream(file);
        InputStreamReader ifr=new InputStreamReader(fileReader,"GBK");
        BufferedReader bufferedReader = new BufferedReader(ifr);
        String value;
        Bean bean;
        List<Bean> beans=new ArrayList<>();
        while ((value = bufferedReader.readLine()) != null) {
           String[] values=value.split("——");
//           System.out.println(value);
           bean=new Bean();
           if (type==1){
               bean.setKey(values[0]);
               bean.setChinese(values[1]);
           }else {
               bean.setChinese(values[0]);
               bean.setEnglish(values[1]);
           }
           beans.add(bean);
        }
          return  beans;
    }

    static  class Bean{
        String chinese;
        String english;
        String key;
        public Bean(){}
        public Bean(String chinese, String english, String key) {
            this.chinese = chinese;
            this.english = english;
            this.key = key;
        }

        public String getChinese() {
            return chinese;
        }

        public void setChinese(String chinese) {
            this.chinese = chinese;
        }

        public String getEnglish() {
            return english;
        }

        public void setEnglish(String english) {
            this.english = english;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        @NonNull
        @Override
        public String toString() {
            return key+"——"+chinese+"——"+english;
        }
    }
}

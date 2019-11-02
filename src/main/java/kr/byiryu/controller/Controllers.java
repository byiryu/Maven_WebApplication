package kr.byiryu.controller;

public interface Controllers {
    String baseContextPath = "contents";
    static String toJsp(String path){
        return baseContextPath + "/" + path + ".tiles";
    }

}

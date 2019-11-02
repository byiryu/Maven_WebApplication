package kr.byiryu.controller;

import kr.byiryu.retrofit.ResponseMessage;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;

public interface BaseController {

    String index(Model model);
    ResponseMessage get(@PathVariable Integer idx);
    ResponseMessage list();
    ResponseMessage list(@PathVariable String column, @PathVariable String value);
    ResponseMessage list(@PathVariable String order, @PathVariable Integer offset, @PathVariable Integer limit);
    ResponseMessage insert(@PathVariable String beanList);
    ResponseMessage update(@PathVariable String beanList);
    ResponseMessage delete(@PathVariable String beanList);
}

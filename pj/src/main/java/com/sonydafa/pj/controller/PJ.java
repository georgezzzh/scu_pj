package com.sonydafa.pj.controller;


import com.sonydafa.pj.domain.User;
import com.sonydafa.pj.domain.UserJob;
import com.sonydafa.pj.repository.Visitor;
import com.sonydafa.pj.service.Productor;
import com.sonydafa.pj.service.ScuRequest;

import com.google.gson.Gson;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
//@RequestMapping("/pj")
public class PJ {

    @Autowired
    private Productor productor;
    @Autowired
    private Visitor visitor;

    @RequestMapping(value="/test",produces = "text/json;charset=UTF-8")
    public @ResponseBody String getTest()
    {
        System.out.println("test接收到");
        return "msg:test success";
    }


    @RequestMapping(value="/submitSuccess")
    public String submitSuccess(HttpServletRequest request, Model model) throws IOException{
        Map<String,String>cookieMap=new HashMap<>();
        cookieMap.put("JSESSIONID",request.getSession().getAttribute("scuSession").toString());
        Map<String,List<Map>> list= ScuRequest.pingjiao(cookieMap);
        model.addAttribute("listMap",list);
        return "pj/submitSuccess";
    }
    @RequestMapping(value = "/submit",method = RequestMethod.POST)
    public String submit(HttpServletRequest request, Model model)
    {
        HttpSession session = request.getSession();
        if(session.isNew())
            return "pj/failure";
        if(request.getSession().getAttribute("scuSession")==null)
            return "pj/failure";

        String scuSession=request.getSession().getAttribute("scuSession").toString();
        session.setAttribute("lastSubmitTime",Long.toString(new Date().getTime()));
        Map<String,String>cookieMap=new HashMap<>();
        cookieMap.put("JSESSIONID",scuSession);
        String submitResponse="";
        try{
            Map<String,String> data=null;
            Map allData=ScuRequest.pingjiao(cookieMap);
            List<Map> not=(List) allData.get("not");
            if(not.size()>0) data=not.get(0);
            Map<String,String> formData=ScuRequest.getForm(cookieMap,data);
            submitResponse=ScuRequest.submit(cookieMap,formData);
            //设置经过刷新的数据
            Map afterSubmit=ScuRequest.pingjiao(cookieMap);
            model.addAttribute("listMap",afterSubmit);
            //加入队列
            UserJob userJob=new UserJob(scuSession,new Date().getTime(),true);
            String userJobJson=new Gson().toJson(userJob);
            productor.sendMessage("sessionId",userJobJson);
        }catch (IOException e){
            e.printStackTrace();
            return "pj/failure";
        }
        return "redirect:/submitSuccess";
    }

    @RequestMapping(value="/index",method = RequestMethod.GET,produces = "text/html;charset=UTF-8")
    public String getPic(HttpServletRequest request, Model model,
                         @RequestParam(value = "errorInfo",required = false)String errorInfo){
        //未断开连接的用户
        HttpSession session = request.getSession();
        String path=request.getServletContext().getRealPath("/statics/tmp/");
        try {
            Map tmp=ScuRequest.saveCaptcha(request,path);
            String relativePath="/pj/statics/tmp/"+tmp.get("JSESSIONID")+".jpg";
            model.addAttribute("path",relativePath);
            if(errorInfo==null)
                errorInfo="";
            model.addAttribute("errorInfo",errorInfo);
            //System.out.println("errorInfo:"+errorInfo);
            System.out.println("cookie:"+tmp);
            session.setAttribute("scuSession",tmp.get("JSESSIONID"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "pj/index";
    }
    @RequestMapping(value = "/index",method = RequestMethod.POST)
    public  String login(HttpServletRequest request, HttpServletResponse response,User user, Model model)
    {
        try {
            request.setCharacterEncoding("UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/html;charset=utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String session=(String)(request.getSession().getAttribute("scuSession"));
        Map<String,String> formdata=new HashMap<>();
        formdata.put("j_username",user.getId());
        formdata.put("j_password",user.getPassword());
        formdata.put("j_captcha",user.getcaptcha());
        //保存访客记录
        Object[]args=new Object[]{user.getId(), LocalDate.now().toString(), LocalTime.now().toString(),request.getRemoteAddr()};
        visitor.store(args);
        Map<String,String> cookieMap=new HashMap<>();
        cookieMap.put("JSESSIONID",session);
        Connection con= Jsoup.connect("http://zhjw.scu.edu.cn/j_spring_security_check");
        try {
            Connection.Response resp=con.ignoreContentType(true).method(Connection.Method.POST).
                    data(formdata).cookies(cookieMap).execute();
            Map<String,List<Map>> list= ScuRequest.pingjiao(cookieMap);
            model.addAttribute("listMap",list);
        } catch (IOException e) {
            e.printStackTrace();
            return "redirect:/pj?errorInfo="+"Incorrect school number or password or verification code";
        }
        return "pj/submit";
    }
}
